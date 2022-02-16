package cn.zz.xposed.methodwatcher.data.source

import android.app.Application
import android.os.Environment
import cn.zz.xposed.methodwatcher.data.DataResult
import cn.zz.xposed.methodwatcher.db.MonitorRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.util.WorkbookUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

/**
 * 导出方法调用记录导出数据源
 */
class MethodCallRecordsExportSource(private val application: Application) {

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.getDefault())

    /**
     * 获取导出文件所在目录
     */
    private val exportDir: File
        get() {
            val dir = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                application.externalCacheDir ?: application.cacheDir
            } else {
                application.cacheDir
            }
            return File(dir, "records_export")
        }

    /**
     * 导出记录
     * @param records 从数据库中获取的调用记录
     * @return 导出的文件路径
     */
    suspend fun exportRecords(records: List<MonitorRecordEntity>): DataResult<MethodCallRecordsExportResult> {
        val file = File(exportDir, "records-${System.currentTimeMillis()}.xlsx")

        val exportData = records.groupBy {
            it.targetPackageName
        }

        return withContext(Dispatchers.IO) {
            try {
                val book = XSSFWorkbook()
                val maxWidth = mutableMapOf<Int, Int>()
                for ((packageName, list) in exportData) {
                    maxWidth.clear()
                    val sheet = book.createSheet(WorkbookUtil.createSafeSheetName(packageName))
                    val headRow = sheet.createRow(0)
                    repeat(6) {
                        val cell = headRow.createCell(it)
                        cell.setCellValue(
                            when (it) {
                                0 -> "应用"
                                1 -> "进程名"
                                2 -> "调用方法"
                                3 -> "直接调用者"
                                4 -> "调用时间"
                                5 -> "调用堆栈"
                                else -> ""
                            }
                        )
                        cell.cellStyle = book.createCellStyle().apply {
                            setAlignment(HorizontalAlignment.CENTER)
                        }
                        maxWidth[it] = cell.stringCellValue.toByteArray().size * 256 + 200
                    }
                    list.forEachIndexed { index, data ->
                        val row = sheet.createRow(index + 1)
                        repeat(6) {
                            val cell = row.createCell(it)
                            cell.setCellValue(
                                when (it) {
                                    0 -> data.targetPackageName
                                    1 -> data.targetProcessName
                                    2 -> "${
                                        data.method.className.split('.').last()
                                    }.${data.method.methodName}"
                                    3 -> data.directCaller
                                    4 -> dateFormat.format(data.calledTime)
                                    5 -> data.callTrace
                                    else -> ""
                                }
                            )
                            cell.cellStyle = book.createCellStyle().apply {
                                setVerticalAlignment(VerticalAlignment.TOP)
                                wrapText = true
                            }
                            val length =
                                (cell.stringCellValue.toByteArray().size * 256 + 200).coerceAtMost(
                                    65280
                                )
                            maxWidth[it] = max(length, maxWidth[it] ?: 0)
                        }
                    }
                    repeat(6) {
                        sheet.setColumnWidth(it, maxWidth[it] ?: sheet.defaultColumnWidth)
                    }
                }

                if (file.parentFile?.exists() == false) {
                    file.parentFile?.mkdirs()
                }
                if (!file.exists()) {
                    file.createNewFile()
                }

                file.outputStream().use {
                    book.write(it)
                }

                DataResult.Success(
                    MethodCallRecordsExportResult(
                        file.absolutePath,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
                )
            } catch (e: Exception) {
                DataResult.Error(e)
            }
        }
    }
}

/**
 * 导出调用记录的结果数据类
 */
data class MethodCallRecordsExportResult(
    /**
     * 导出的文件路径
     */
    val file: String,
    val mime: String
)