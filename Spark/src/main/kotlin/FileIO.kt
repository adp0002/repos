package org.adprasad

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class FileIO {

    companion object {

        // Configuration directory
        private val appDirectory = System.getProperty("user.dir")
//      val appDirectory = "/Users/Prasad.Amajala/Library/CloudStorage/GoogleDrive-durgaprasadamajala@gmail.com/My Drive/My Projects/Cordyceps"
//    private val appDirectory = "/Users/Prasad.Amajala/Library/CloudStorage/GoogleDrive-prasad.amajala@priviahealth.com/My Drive/UserGuide" //System.getProperty("user.dir")

        fun getAbsoluteFilePath(relativePath: String) = "$appDirectory/$relativePath"

        fun getFileContent(relativePath: String) = String(Files.readAllBytes(Paths.get("$appDirectory/$relativePath")))

        fun configFiles(): List<File> {
            val files = listDirectoryFiles("${appDirectory}/config")
            return files.filter { it.isFile && it.path.endsWith("_config.json") }
        }

        fun listDirectoryFiles(path: String): List<File> {
            val directory = File(path)
            return if (directory.exists() && directory.isDirectory) {
                directory.listFiles()?.toList() ?: emptyList()
            } else {
                emptyList()
            }
        }

        fun isFileExists(absolutePath: String): Boolean = File(absolutePath).exists()

        // Checks existence using app relative path
        fun isExists(path: String): Boolean = File("$appDirectory/$path").exists()

    }
}