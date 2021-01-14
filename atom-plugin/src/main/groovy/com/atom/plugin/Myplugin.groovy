package com.atom.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

public class Myplugin implements Plugin<Project> {
    @Override
    void apply(Project project) {

        project.task('showTime') {
            println "Current time is "+System.currentTimeMillis()
        }

    }
}
