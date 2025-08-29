package org.adprasad

import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.JOptionPane

class ConfigManager {

    companion object {
        // Json keys
        private val keyName = "name"
        private val keyPageGroups = "groups"
        private val keyPages = "pages"
        private val keyPath = "path"
        private val keyDefaultPage = "default"

        fun getMasterConfig(): MasterConfig {
            val list = getProfileConfigList()
            return MasterConfig("", list, list.first().name)
        }

        private fun getProfileConfigList(): List<ProfileConfig> {
            val list = mutableListOf<ProfileConfig>()
            val configFiles = FileIO.configFiles()
            configFiles.forEach { file ->
                list.add(getProfileConfig(file.absolutePath))
            }
            return list
        }

        private fun getProfileConfig(path: String): ProfileConfig {
            val profileObj = JSONObject(String(Files.readAllBytes(Paths.get(path))))
            val profileName = profileObj.getString(keyName)
            val defaultPage = profileObj.getString(keyDefaultPage)

            // Iterate Groups
            val groupList = mutableListOf<PageGroup>()
            val groupsArray = profileObj.getJSONArray(keyPageGroups)
            for (i in 0 until groupsArray.length()) {
                val groupObj = groupsArray.getJSONObject(i)
                val groupName = groupObj.getString(keyName)

                // Iterate Pages
                val pageList = mutableListOf<Page>()
                val pagesArray = groupObj.getJSONArray(keyPages)
                for (j in 0 until pagesArray.length()) {
                    val pageObj = pagesArray.getJSONObject(j)
                    val pageName = pageObj.getString(keyName)
                    val pagePath = pageObj.getString(keyPath)
                    pageList.add(Page(pageName, pagePath, j))
                }
                groupList.add(PageGroup(groupName, pageList, i))
            }
            return ProfileConfig(profileName, groupList, defaultPage)
        }


    }
}