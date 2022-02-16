package cn.zz.xposed.methodwatcher.ui.target

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import cn.zz.xposed.methodwatcher.ui.BaseActivity
import cn.zz.xposed.methodwatcher.ui.methods.MonitorMethodsSelectorActivity
import cn.zz.xposed.methodwatcher.ui.theme.SystemCallMonitorTheme
import com.google.accompanist.drawablepainter.rememberDrawablePainter

class MonitorTargetActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageInfo = intent.getParcelableExtra<PackageInfo>(KEY_PACKAGE_INFO)
        if (packageInfo == null) {
            finish()
            return
        }

        setContent {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides this
            ) {
                MonitorTargetScreen(packageInfo) {
                    onBackPressed()
                }
            }
        }
    }

    companion object {

        private const val KEY_PACKAGE_INFO = "packageInfo"

        fun startActivity(context: Context, packageInfo: PackageInfo) {
            val intent = Intent(context, MonitorTargetActivity::class.java)
                .putExtra(KEY_PACKAGE_INFO, packageInfo)
            context.startActivity(intent)
        }
    }
}

@Composable
private fun MonitorTargetScreen(packageInfo: PackageInfo, onBackPressed: () -> Unit) {
    SystemCallMonitorTheme {
        Scaffold(topBar = {
            TopAppBar(title = {
                Text(
                    text = "配置",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
                backgroundColor = Color.White,
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                })
        }) {
            MonitorTargetContent(
                modifier = Modifier.fillMaxSize(),
                packageInfo,
            )
        }
    }
}

@Composable
private fun MonitorTargetContent(
    modifier: Modifier,
    packageInfo: PackageInfo,
) {
    Column(
        modifier = modifier
            .padding(vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AppInfoComponent(
            packageInfo,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        MonitorConfigsComponent(Modifier.fillMaxWidth(), packageInfo)
    }
}

@Composable
private fun AppInfoComponent(packageInfo: PackageInfo, modifier: Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current
        val appIcon = context.packageManager.getApplicationIcon(packageInfo.applicationInfo)
        Image(
            painter = rememberDrawablePainter(drawable = appIcon),
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = context.packageManager.getApplicationLabel(packageInfo.applicationInfo)
                    .toString(),
                fontSize = 16.sp
            )
            Text(
                text = packageInfo.packageName,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MonitorConfigsComponent(
    modifier: Modifier,
    packageInfo: PackageInfo,
    viewModel: MonitorTargetViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = MonitorTargetViewModelFactory(
            packageInfo.packageName
        )
    )
) {
    Column(
        modifier = modifier
    ) {
        val enabled by viewModel.enabled.collectAsState(initial = true)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(text = "启用", fontSize = 16.sp, modifier = Modifier.weight(1f))
            Switch(checked = enabled, onCheckedChange = { viewModel.enabled(it) })
        }

        Divider(
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            thickness = 1.dp,
            startIndent = 16.dp,
        )

        val contentAlpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
        CompositionLocalProvider(LocalContentAlpha provides contentAlpha) {
            val monitorMethods by viewModel.monitorMethods.collectAsState(initial = emptyList())
            val context = LocalContext.current
            val launcher = rememberLauncherForActivityResult(
                contract = MonitorMethodsSelectorActivity.SelectMonitorMethodsContract()
            ) {
                if (it != null) {
                    Toast.makeText(context, "修改后需要重启目标应用才能生效", Toast.LENGTH_SHORT).show()
                    viewModel.monitorMethods(it)
                }
            }
            Card(
                onClick = {
                    launcher.launch(monitorMethods)
                },
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 12.dp, start = 8.dp, end = 8.dp),
                RoundedCornerShape(12.dp),
                Color.Transparent,
                elevation = 0.dp,
                enabled = enabled
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "监听方法", fontSize = 16.sp, modifier = Modifier.weight(1f))
                    Text(
                        text = monitorMethods.size.toString(),
                        fontSize = 14.sp,
                        maxLines = 1,
                    )
                    Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null)
                }
            }
        }

        Divider(
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            startIndent = 16.dp
        )

        DeleteRecordsItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp), viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DeleteRecordsItem(modifier: Modifier, viewModel: MonitorTargetViewModel) {
    Card(
        onClick = { viewModel.deleteRecords() },
        modifier = modifier.then(Modifier.padding(horizontal = 8.dp)),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color.Transparent,
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "删除记录", fontSize = 16.sp, modifier = Modifier.weight(1f))

            val count by viewModel.recordsCountData.collectAsState(initial = 0)
            Text(text = "$count", fontSize = 14.sp)
        }
    }
}