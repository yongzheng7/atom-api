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

package com.atom.plugin.utils

/**
 * 扫描注册配置
 *
 * @author xuexiang
 * @since 2018/5/17 上午12:39
 */
class ScanSetting {
    static final String PLUGIN_NAME = "com.atom.api"
    /**
     * 路由表的注册代码将生成插入到该类AbstractApiImplContext（路由中心）中
     */
    static final String GENERATE_TO_CLASS_NAME = 'com/atom/core/AbstractApiImplContext'
    /**
     * 路由表的注册代码将生成插入的类文件名
     */
    static final String GENERATE_TO_CLASS_FILE_NAME = GENERATE_TO_CLASS_NAME + '.class'
    /**
     * 注册代码将动态生成到loadRouterMap方法中
     */
    static final String GENERATE_TO_METHOD_NAME = 'loadProxyClass'
    /**
     * annotationProcessor自动生成路由代码的包名
     */
    static final String ROUTER_CLASS_PACKAGE_NAME = 'com/atom/apt'

    /**
     * register method name in class: {@link #GENERATE_TO_CLASS_NAME}
     */
    static final String REGISTER_METHOD_NAME = 'registerClass'
    /**
     * 需要扫描的接口名
     */
    String interfaceName = ''

    /**
     * 需要扫描的继承名
     */
    String superName = ''

    /**
     * 包含LogisticsCenter类的jar包文件 {@link #GENERATE_TO_CLASS_NAME}
     */
    File fileContainsInitClass
    /**
     * 扫描结果 {@link #interfaceName}
     * @return 返回类名的集合
     */
    ArrayList<String> classList = new ArrayList<>()

    /**
     * 自动扫描注册的配置构造器
     * @param interfaceName 需要扫描的接口名
     */
    ScanSetting(String name , boolean isInterface){
        if(isInterface){
            this.interfaceName = name
        }else{
            this.superName = name
        }
    }

}