package org.adprasad

data class Page(val title: String, val path: String, val order: Int) {
    override fun toString(): String = title.capitalizeWords()
}
data class PageGroup(val title: String, val list: List<Page>, val order: Int) {
    override fun toString(): String = title.capitalizeWords()
}
data class ProfileConfig(val name: String, val list: List<PageGroup>, val defaultPage: String)
data class MasterConfig(val name: String, val profileConfigs: List<ProfileConfig>, val defaultProfile: String)
