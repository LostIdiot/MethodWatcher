package cn.zz.xposed.methodwatcher.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Custom [Scaffold] with specified title text style, white background and no elevation by default
 *
 * 自定义[Scaffold], 默认的白色背景以及无阴影
 */
@Composable
fun MonitorScaffold(
    modifier: Modifier = Modifier,
    title: String,
    topBarElevation: Dp = 0.dp,
    topBarActions: @Composable RowScope.() -> Unit = {},
    showNavigationIcon: Boolean = true,
    onBackPressed: () -> Unit = {},
    backgroundColor: Color = Color.White,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
                backgroundColor = backgroundColor,
                elevation = topBarElevation,
                actions = topBarActions,
                navigationIcon = {
                    if (showNavigationIcon) {
                        IconButton(onClick = onBackPressed) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                        }
                    }
                })
        },
        content = content
    )
}