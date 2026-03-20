package space.xiaoxiao.databasemanager.features

expect class DatabaseConfigStorage {
    fun loadConfigs(): List<DatabaseConfigInfo>
    fun saveConfigs(configs: List<DatabaseConfigInfo>)
    fun addConfig(config: DatabaseConfigInfo)
    fun removeConfig(id: String)
    fun updateConfig(config: DatabaseConfigInfo)
}
