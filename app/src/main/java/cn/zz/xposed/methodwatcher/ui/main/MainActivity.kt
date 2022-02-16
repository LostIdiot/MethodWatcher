package cn.zz.xposed.methodwatcher.ui.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.zz.xposed.methodwatcher.R
import cn.zz.xposed.methodwatcher.data.UiData
import cn.zz.xposed.methodwatcher.ui.BaseActivity
import cn.zz.xposed.methodwatcher.ui.apps.AppListActivity
import cn.zz.xposed.methodwatcher.ui.record.MethodCallRecordsActivity
import cn.zz.xposed.methodwatcher.ui.theme.SystemCallMonitorTheme
import java.io.File

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides this) {
                MainScreen()
            }
        }
    }
}

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun MainScreen(mainViewModel: MainViewModel = viewModel()) {
    SystemCallMonitorTheme {
        Scaffold(topBar = {
            TopAppBar(
                backgroundColor = Color.White,
                contentPadding = PaddingValues(horizontal = 16.dp),
                elevation = 0.dp
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                MonitorState(mainViewModel)
                MonitorButtons(mainViewModel)
            }
        }
    }
}

@Composable
private fun MonitorState(mainViewModel: MainViewModel) {
    Card(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.primary,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Fit
            )
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxWidth()
            ) {
                val activeTargets by mainViewModel.activeTargetCountData.collectAsState(0)
                Text(text = "正在监控的应用: $activeTargets", fontSize = 16.sp)

                val records by mainViewModel.totalMethodCallCount.collectAsState(0)
                Text(
                    text = "共记录到${records}次方法调用",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun MonitorButtons(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    MonitorButton(
        modifier = Modifier
            .padding(top = 20.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
        text = "查看/配置监视应用",
        icon = {
            Icon(imageVector = Icons.Outlined.Settings, contentDescription = null)
        },
        onClick = {
            AppListActivity.startActivity(context)
        }
    )
    MonitorButton(
        onClick = {
            MethodCallRecordsActivity.startActivity(context)
        },
        icon = {
            Icon(imageVector = Icons.Default.List, contentDescription = null)
        },
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        text = "查看记录"
    )

    MonitorButton(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        icon = {
            Icon(imageVector = Icons.Outlined.Share, contentDescription = null)
        },
        text = "导出全部",
        onClick = {
            mainViewModel.exportAllRecords()
        }
    )
    val exportState = mainViewModel.exportData.collectAsState(initial = null)
    when (val export = exportState.value) {
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
        UiData.Empty -> {
            // do nothing
        }
    }

    MonitorButton(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        icon = {
            Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
        },
        text = "删除记录",
        onClick = {
            mainViewModel.deleteAllRecords()
        }
    )
    when (val delete = mainViewModel.deleteAllData.collectAsState(initial = null).value) {
        is UiData.Loading -> {
            Toast.makeText(context, "正在删除...", Toast.LENGTH_SHORT).show()
        }
        is UiData.Failure -> {
            Toast.makeText(context, delete.message, Toast.LENGTH_SHORT).show()
        }
        is UiData.Success -> {
            Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show()
        }
        UiData.Empty -> {
            // do nothing
        }
    }
}

@Composable
private fun MonitorButton(
    onClick: () -> Unit,
    icon: @Composable RowScope.() -> Unit,
    modifier: Modifier,
    text: String,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        elevation = null,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        icon()
        Text(
            text = text,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f)
        )
    }
}