package org.gradle.intellij.plugin.gradlewrappervalidator.services

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.application


class WrapperFileChangeListenerTest : BasePlatformTestCase() {
    fun testFileChange() {
        // Creates two change events
        val wrapperFile = myFixture.tempDirFixture.createFile("gradle/wrapper/gradle-wrapper.jar", "foo")

        // Another event
        application.runWriteAction {
            wrapperFile.setBinaryContent("bar".toByteArray())
        }
    }
}
