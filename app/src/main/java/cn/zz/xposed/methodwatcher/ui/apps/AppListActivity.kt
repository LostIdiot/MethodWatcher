package cn.zz.xposed.methodwatcher.ui.apps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.zz.xposed.methodwatcher.data.repository.apps.AppListItemData
import cn.zz.xposed.methodwatcher.ui.BaseActivity
import cn.zz.xposed.methodwatcher.ui.target.MonitorTargetActivity
import cn.zz.xposed.methodwatcher.ui.theme.SystemCallMonitorTheme
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

class AppListActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides this) {
                AppListScreen {
                    onBackPressed()
                }
            }
        }
    }

    companion object {

        fun startActivity(context: Context) {
            val intent = Intent(context, AppListActivity::class.java)
            context.startActivity(intent)
        }
    }
}

@Composable
private fun AppListScreen(onBackPressed: () -> Unit) {
    val viewModel = viewModel(AppListViewModel::class.java)
    SystemCallMonitorTheme {
        Scaffold(topBar = {
            TopAppBar(title = {
                Text(text = "应用列表", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }, backgroundColor = Color.White, elevation = 0.dp, navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
            },
                actions = {
                    var menuExpanded by remember { mutableStateOf(false) }

                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(onClick = {
                            menuExpanded = false
                            viewModel.toggleShowSystemApps()
                        }) {
                            val showSystemApps by viewModel.showSystemAppData.observeAsState(false)
                            Text(
                                text = if (showSystemApps) "隐藏系统应用" else "显示系统应用",
                                fontSize = 14.sp
                            )
                        }
                    }
                })
        }) {
            AppList(Modifier.fillMaxSize(), viewModel)
        }
    }
}

@Composable
private fun AppList(
    modifier: Modifier,
    viewModel: AppListViewModel
) {
    val refreshing by viewModel.refreshingData.observeAsState(initial = false)
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = refreshing),
        onRefresh = { viewModel.refresh() },
        modifier = modifier
    ) {
        val appList by viewModel.appListData.observeAsState(initial = emptyList())

        LazyColumn(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(appList, key = { it.packageInfo.packageName }) {
                AppListItem(modifier = Modifier.fillMaxWidth(), data = it)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AppListItem(modifier: Modifier, data: AppListItemData) {
    val context = LocalContext.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color.White,
        elevation = 2.dp,
        onClick = {
            MonitorTargetActivity.startActivity(context, data.packageInfo)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val logoDrawable =
                context.packageManager.getApplicationIcon(data.packageInfo.applicationInfo)
            val painter = rememberDrawablePainter(drawable = logoDrawable)
            Image(painter = painter, contentDescription = null, modifier = Modifier.size(64.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),

                ) {
                Text(
                    text = context.packageManager.getApplicationLabel(data.packageInfo.applicationInfo)
                        .toString(),
                    fontSize = 16.sp,
                )
                Text(
                    text = data.packageInfo.packageName,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}