package org.gradle.intellij.plugin.gradlewrappervalidator.services

import com.intellij.testFramework.fixtures.BasePlatformTestCase


class WrapperFileChangeListenerTest : BasePlatformTestCase() {
    fun testFileChange() {
        val wrapperFile = myFixture.tempDirFixture.createFile("gradle/wrapper/gradle-wrapper.jar")
        wrapperFile.setBinaryContent("foo".toByteArray())
        wrapperFile.setBinaryContent("bar".toByteArray())
        //val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(wrapperFile)
    }
}
