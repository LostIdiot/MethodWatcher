package cn.zz.xposed.methodwatcher.ui.methods

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import cn.zz.xposed.methodwatcher.data.MonitorMethod
import cn.zz.xposed.methodwatcher.data.MonitorMethodGroup
import cn.zz.xposed.methodwatcher.data.UiData
import cn.zz.xposed.methodwatcher.data.ifSuccess
import cn.zz.xposed.methodwatcher.data.repository.methods.MonitorMethodItemData
import cn.zz.xposed.methodwatcher.ui.BaseActivity
import cn.zz.xposed.methodwatcher.ui.components.MonitorScaffold
import cn.zz.xposed.methodwatcher.ui.theme.SystemCallMonitorTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Select methods you want to monitor
 *
 * 选择想要监听的方法页面
 */
class MonitorMethodsSelectorActivity : BaseActivity() {

    private val viewModel: MonitorMethodsSelectorViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return if (modelClass == MonitorMethodsSelectorViewModel::class.java) {
                    val methods = intent.getIntArrayExtra(KEY_MONITOR_METHODS)
                        ?.map { MonitorMethod.findById(it) }
                        ?.filter { it != MonitorMethod.Unknown } ?: emptyList()
                    MonitorMethodsSelectorViewModel(methods) as T
                } else {
                    defaultViewModelProviderFactory.create(modelClass)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViewModel()

        setContent {
            MonitorMethodsSelectorScreen(viewModel) {
                onBackPressed()
            }
        }
    }

    private fun initViewModel() {
        lifecycleScope.launch {
            viewModel.selectedMethodsData
                .collect { result ->
                    if (result !is UiData.Success) {
                        return@collect
                    }
                    val methods = IntArray(result.data.size) {
                        result.data[it].id
                    }
                    val intent = Intent()
                        .putExtra(KEY_MONITOR_METHODS, methods)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
        }
    }

    companion object {
        private const val KEY_MONITOR_METHODS = "MONITOR_METHODS"
    }

    class SelectMonitorMethodsContract :
        ActivityResultContract<List<MonitorMethod>, List<MonitorMethod>?>() {

        override fun createIntent(context: Context, input: List<MonitorMethod>): Intent {
            val methods = IntArray(input.size) {
                input[it].id
            }

            return Intent(context, MonitorMethodsSelectorActivity::class.java)
                .putExtra(KEY_MONITOR_METHODS, methods)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): List<MonitorMethod>? {
            return if (resultCode == Activity.RESULT_OK) {
                val result = intent?.getIntArrayExtra(KEY_MONITOR_METHODS) ?: intArrayOf()
                result.map {
                    MonitorMethod.findById(it)
                }.filterNot { it == MonitorMethod.Unknown }
            } else {
                null
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun MonitorMethodsSelectorScreen(
    viewModel: MonitorMethodsSelectorViewModel,
    onBackPressed: () -> Unit
) {
    SystemCallMonitorTheme {
        MonitorScaffold(
            title = "选择要监听的方法",
            topBarActions = {
                IconButton(onClick = { viewModel.reset() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "重置")
                }
                IconButton(onClick = { viewModel.select() }) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = "保存")
                }
            },
            onBackPressed = onBackPressed
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val pagerState = rememberPagerState()
                val coroutineScope = rememberCoroutineScope()
                val methodGroups by viewModel.methodGroupsData.collectAsState(UiData.Empty)

                methodGroups.ifSuccess { groups ->
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        backgroundColor = Color.White,
                        edgePadding = 0.dp,
                        indicator = {
                            TabRowDefaults.Indicator(
                                Modifier.pagerTabIndicatorOffset(pagerState, it),
                                color = MaterialTheme.colors.secondary,
                                height = 3.dp
                            )
                        }
                    ) {
                        groups.forEachIndexed { index, group ->
                            Tab(
                                text = {
                                    Text(
                                        text = group.groupName,
                                        modifier = Modifier.widthIn(max = 200.dp)
                                    )
                                },
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                })
                        }
                    }

                    HorizontalPager(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        count = groups.size,
                        state = pagerState
                    ) {
                        MonitorMethodsPage(
                            modifier = Modifier.fillMaxSize(),
                            group = groups[it],
                            viewModel = viewModel,
                            contentPadding = PaddingValues(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonitorMethodsPage(
    modifier: Modifier,
    group: MonitorMethodGroup,
    viewModel: MonitorMethodsSelectorViewModel,
    contentPadding: PaddingValues
) {
    val list by viewModel.observeMethodsOfGroup(group).collectAsState(emptyList())

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(list.size, key = {
            list[it].method.id
        }) {
            MonitorMethodItem(list[it], viewModel)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MonitorMethodItem(
    data: MonitorMethodItemData,
    viewModel: MonitorMethodsSelectorViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color.White,
        elevation = 2.dp,
        onClick = {
            viewModel.toggleMethodSelected(data)
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = data.method.className.split('.').last(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val methodText = buildString {
                    append(data.method.methodName)
                    append("(")
                    for (type in data.method.paramTypes) {
                        append(type.simpleName)
                        append(", ")
                    }
                    if (data.method.paramTypes.isNotEmpty()) {
                        deleteRange(length - 2, length)
                    }
                    append(")")
                }
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = methodText,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Checkbox(
                modifier = Modifier.padding(horizontal = 20.dp),
                checked = data.isSelected,
                onCheckedChange = null
            )
        }
    }
}