package cn.zz.xposed.methodwatcher.ui.record

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.zz.xposed.methodwatcher.R
import cn.zz.xposed.methodwatcher.data.UiData
import cn.zz.xposed.methodwatcher.data.repository.record.MonitorMethodItemData
import cn.zz.xposed.methodwatcher.data.repository.record.TargetAppItemData
import cn.zz.xposed.methodwatcher.ui.BaseActivity
import cn.zz.xposed.methodwatcher.ui.theme.SystemCallMonitorTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MethodCallRecordsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides this
            ) {
                MethodCallRecordsScreen {
                    finish()
                }
            }
        }
    }

    companion object {

        fun startActivity(context: Context) {
            val intent = Intent(context, MethodCallRecordsActivity::class.java)
            context.startActivity(intent)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
private fun MethodCallRecordsScreen(
    recordsViewModel: MethodCallRecordsViewModel = viewModel(),
    finish: () -> Unit
) {
    SystemCallMonitorTheme {
        val controller = rememberSystemUiController()
        controller.setStatusBarColor(MaterialTheme.colors.primary)

        val scaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)
        val coroutineScope = rememberCoroutineScope()
        var startRevealed by remember {
            mutableStateOf(false)
        }
        BackHandler(enabled = scaffoldState.isRevealed) {
            coroutineScope.launch {
                scaffoldState.conceal()
            }
            startRevealed = false
        }

        val context = LocalContext.current
        when (val export = recordsViewModel.exportedFileData.collectAsState(null).value) {
            is UiData.Loading -> {
                Toast.makeText(context, "正在导出...", Toast.LENGTH_SHORT).show()
            }
            is UiData.Failure -> {
                Toast.makeText(context, export.message, Toast.LENGTH_SHORT).show()
            }
            is UiData.Success -> {
                LaunchedEffect(export) {
                    val filePath = export.data.file
                    val mime = export.data.mime
                    val authority = "${context.packageName}.commonfileprovider"
                    val uri = FileProvider.getUriForFile(context, authority, File(filePath))
                    val intent = Intent(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_STREAM, uri)
                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .setType(mime)
                    context.startActivity(Intent.createChooser(intent, "导出记录到"))
                }
            }
        }

        BackdropScaffold(
            modifier = Modifier.fillMaxWidth(),
            appBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "调用记录",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    backgroundColor = Color.Transparent,
                    elevation = 0.dp,
                    navigationIcon = {
                        IconButton(onClick = finish) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { recordsViewModel.refresh() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { recordsViewModel.exportRecords() }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                if (scaffoldState.isConcealed) {
                                    startRevealed = true
                                    coroutineScope.launch {
                                        scaffoldState.reveal()
                                    }
                                } else if (scaffoldState.isRevealed) {
                                    startRevealed = false
                                    coroutineScope.launch {
                                        scaffoldState.conceal()
                                    }
                                }
                            },
                        ) {
                            AnimatedContent(targetState = startRevealed) {
                                if (it) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                } else {
                                    Icon(
                                        painterResource(id = R.drawable.svg_icon_filter),
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                )
            },
            backLayerContent = {
                RecordsFilterOptionsScreen(recordsViewModel)
            },
            frontLayerContent = {
                RecordsListScreen(recordsViewModel)
            },
            gesturesEnabled = false,
            scaffoldState = scaffoldState
        )
    }
}

/**
 * 筛选项
 */
@Composable
private fun RecordsFilterOptionsScreen(recordsViewModel: MethodCallRecordsViewModel) {
    val targets by recordsViewModel.targetAppsData.collectAsState(emptyList())
    val methods by recordsViewModel.methodsData.collectAsState(emptyList())
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 目标应用
        item {
            Text(
                text = "目标应用",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Divider(modifier = Modifier.padding(top = 8.dp))
        }
        items(targets.size, key = {
            targets[it].packageName
        }) {
            TargetAppItem(
                modifier = Modifier.fillParentMaxWidth(),
                data = targets[it],
                recordsViewModel = recordsViewModel
            )
        }

        // 调用方法
        item {
            Text(
                text = "调用方法",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Divider(modifier = Modifier.padding(top = 8.dp))
        }
        items(methods.size, key = { methods[it].method.id }) {
            MonitorMethodItem(
                modifier = Modifier.fillParentMaxWidth(),
                data = methods[it],
                recordsViewModel = recordsViewModel
            )
        }
    }
}

@Composable
private fun TargetAppItem(
    modifier: Modifier,
    data: TargetAppItemData,
    recordsViewModel: MethodCallRecordsViewModel
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = data.selected, onCheckedChange = {
            recordsViewModel.toggleTargetAppSelected(data)
        })
        Text(
            text = data.label.ifBlank { data.packageName },
            fontSize = 16.sp,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .weight(1f)
        )
    }
}

@Composable
private fun MonitorMethodItem(
    modifier: Modifier,
    data: MonitorMethodItemData,
    recordsViewModel: MethodCallRecordsViewModel
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = data.selected,
            onCheckedChange = { recordsViewModel.toggleMonitorMethod(data) })
        Text(
            text = "${data.method.className.split('.').last()}.${data.method.methodName}",
            fontSize = 16.sp,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .weight(1f)
        )
    }
}

@Composable
private fun RecordsListScreen(recordsViewModel: MethodCallRecordsViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        val recordsCount by recordsViewModel.recordsCountData.collectAsState(0)
        val count by animateIntAsState(targetValue = recordsCount)
        Text(
            text = "调用记录(${count})",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
        Divider(Modifier.fillMaxWidth(), startIndent = 16.dp)

        val records by recordsViewModel.recordsData.collectAsState(emptyList())
        val dateFormat by remember {
            mutableStateOf(SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.getDefault()))
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
        ) {
            items(records.size, key = {
                records[it].entity.id
            }) {
                RecordItem(
                    Modifier
                        .fillParentMaxWidth()
                        .padding(vertical = 8.dp),
                    records[it],
                    dateFormat
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun RecordItem(
    modifier: Modifier,
    data: MethodCallRecordData,
    dateFormat: DateFormat
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                // 调用的方法
                Text(
                    text = "${
                        data.entity.method.className.split('.').last()
                    }.${data.entity.method.methodName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                // 调用发生的应用
                Text(
                    text = "应用: ${data.app?.label ?: data.entity.targetPackageName}",
                    fontSize = 14.sp,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
                // 调用发生的时间
                Text(
                    text = "调用时间: ${dateFormat.format(Date(data.entity.calledTime))}",
                    fontSize = 14.sp,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )

                if (expanded) {
                    Text(
                        text = "直接调用者: ${data.entity.directCaller}",
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                    Text(
                        text = "调用栈:\n${data.entity.callTrace}",
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }

            val rotate by animateFloatAsState(targetValue = if (expanded) 180f else 0f)
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 16.dp, top = 24.dp)
                    .align(Alignment.Top)
                    .rotate(rotate)
            )
        }

        Divider(startIndent = 8.dp, modifier = Modifier.padding(top = 16.dp))
    }
}