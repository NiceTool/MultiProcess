package com.analysys.plugin


import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class CostTimePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create("costtime", CostTimeConfig)
        def android = project.extensions.android
        boolean  islib = true
        if (android instanceof AppExtension) {
            islib=false
        }

        android.registerTransform(new CostTransform(project,islib))
        android.registerTransform(new StringFogTransform(project,islib))
    }


}