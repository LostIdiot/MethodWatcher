package cn.zz.xposed.methodwatcher.db

import androidx.room.*
import cn.zz.xposed.methodwatcher.data.MonitorMethod
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitorRecordDao {

    /**
     * 获取总记录数
     */
    @Query("select count(id) from monitor_record")
    fun observeRecordCount(): Flow<Int>

    /**
     * 插入一条记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(entity: MonitorRecordEntity)

    @Query("select distinct target_package_name from monitor_record")
    suspend fun loadAllPackageNames(): List<String>?

    @Query("select * from monitor_record where target_package_name in (:targetPackageNames) and method in (:methods)")
    suspend fun loadRecords(
        targetPackageNames: List<String>,
        @TypeConverters(MonitorMethodConverters::class)
        methods: List<MonitorMethod>
    ): List<MonitorRecordEntity>?

    /**
     * 获取全部调用记录
     */
    @Query("select * from monitor_record")
    suspend fun loadAllRecords(): List<MonitorRecordEntity>?

    /**
     * 删除全部调用记录
     */
    @Query("delete from monitor_record")
    suspend fun deleteAllRecords()

    /**
     * 监听[packageName]应用的方法调用记录数
     */
    @Query("select count(id) from monitor_record where target_package_name = :packageName")
    fun observeRecordCountByPackageName(packageName: String): Flow<Int>

    /**
     * 删除指定应用[packageName]的调用记录
     */
    @Query("delete from monitor_record where target_package_name = :packageName")
    suspend fun deleteRecords(packageName: String)
}