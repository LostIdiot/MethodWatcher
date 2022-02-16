package cn.zz.xposed.methodwatcher.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitorConfigDao {

    /**
     * load [MonitorConfigEntity] for app
     * return null if not exist
     */
    @Query("select * from monitor_config where package_name = :packageName")
    suspend fun loadConfig(packageName: String): MonitorConfigEntity?

    @Query("select * from monitor_config where package_name = :packageName")
    fun observeConfig(packageName: String): Flow<MonitorConfigEntity?>

    /**
     * 监听全部配置变化
     */
    @Query("select * from monitor_config")
    fun observeAllConfig(): Flow<List<MonitorConfigEntity>?>

    @Insert
    suspend fun insertConfig(config: MonitorConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateConfig(config: MonitorConfigEntity)
}