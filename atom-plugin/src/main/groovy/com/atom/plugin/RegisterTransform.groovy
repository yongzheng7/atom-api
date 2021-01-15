/*
 * Copyright (C) 2018 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atom.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.atom.plugin.utils.*
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project


class RegisterTransform extends Transform {

    Project project
    /**
     * 扫描接口的集合
     */
    static ArrayList<ScanSetting> registerList
    /**
     * 包含 AbstractApiImplContext 类的jar文件
     */
    static File fileContainsInitClass

    RegisterTransform(Project project) {
        this.project = project
    }

    /**
     * name of this transform
     * @return
     */
    @Override
    String getName() {
        return ScanSetting.PLUGIN_NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 插件扫描的作用域【项目中所有的classes文件】
     * @return
     */
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return true
    }


    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider,
                   boolean isIncremental) throws IOException, TransformException, InterruptedException {

        Logger.i('Start scan register info in jar file.')

        long startTime = System.currentTimeMillis()
        boolean leftSlash = File.separator == '/'

        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        inputs.each { TransformInput input ->
            // 扫描所有的jar包，因为jar包中可能包含其他module的注册的路由
            input.jarInputs.each { JarInput jarInput ->
                String destName = jarInput.name
                // 重新命名
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
                if (destName.endsWith(".jar")) {
                    // 如果是.jar结尾，截取之前的字符串
                    destName = destName.substring(0, destName.length() - 4)
                }
                // 获取输入的源文件
                File src = jarInput.file
                // 创建输出的源文件
                File dest = outputProvider.getContentLocation(
                        destName + "_" + hexName, // 预防名字重复
                        jarInput.contentTypes,
                        jarInput.scopes,
                        Format.JAR)

                //进行检查排除不必要的jar包
                if (ScanUtils.shouldProcessPreDexJar(src.absolutePath)) {
                    // 进行遍历jar包中的class
                    ScanUtils.scanJar(src, dest)
                }
                FileUtils.copyFile(src, dest)
            }

            // 扫描所有的目录中的class文件
            input.directoryInputs.each { DirectoryInput directoryInput ->
                File dest = outputProvider.getContentLocation(
                        directoryInput.name,
                        directoryInput.contentTypes,
                        directoryInput.scopes,
                        Format.DIRECTORY)

                String root = directoryInput.file.absolutePath
                if (!root.endsWith(File.separator)) {
                    root += File.separator
                }
                directoryInput.file.eachFileRecurse { File file ->
                    def path = file.absolutePath.replace(root, '')
                    if (!leftSlash) {
                        path = path.replaceAll("\\\\", "/")
                    }
                    if (file.isFile() && ScanUtils.shouldProcessClass(path)) {
                        ScanUtils.scanClass(file)
                    }
                }
                // copy to dest
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }

        Logger.i('Scan finish, current cost time ' + (System.currentTimeMillis() - startTime) + "ms")

        //扫描结束后，将扫描到的路由注册结果自动注入到 LogisticsCenter.class 的loadRouterMap方法中
        if (fileContainsInitClass) {
            registerList.each { ext ->
                Logger.i('Insert register code to file ' + fileContainsInitClass.absolutePath)

                if (ext.classList.isEmpty()) {
                    Logger.e("No class implements found for interface:" + ext.interfaceName)
                } else {
                    ext.classList.each {
                        Logger.i(it)
                    }
                    RegisterCodeGenerator.insertInitCodeTo(ext)
                }
            }
        }

        Logger.i("Generate code finish, current cost time: " + (System.currentTimeMillis() - startTime) + "ms")
    }
}
