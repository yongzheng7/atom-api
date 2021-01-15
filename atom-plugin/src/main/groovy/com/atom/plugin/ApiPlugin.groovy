package com.atom.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.atom.plugin.utils.ScanSetting
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.atom.plugin.utils.Logger

public class ApiPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        def isApp = project.plugins.hasPlugin(AppPlugin)
        //only application module needs this plugin to generate register code
        if (isApp) {
            Logger.make(project)
            Logger.i('Project enable atom-api-register plugin --> start')
            def android = project.extensions.getByType(AppExtension)
            def transformImpl = new RegisterTransform(project)

            //初始化 xrouter-plugin 扫描设置
            ArrayList<ScanSetting> list = new ArrayList<>(1)
            list.add(new ScanSetting('com/atom/annotation/bean/ApiImpls' , false))   //apt
            RegisterTransform.registerList = list
            android.registerTransform(transformImpl)
        }
        Logger.i('Project enable atom-api-register plugin --> over')
        println('Project enable atom-api-register plugin --> over')
    }
}
