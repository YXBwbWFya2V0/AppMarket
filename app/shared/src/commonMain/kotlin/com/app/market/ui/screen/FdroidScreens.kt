package com.app.market.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.market.resources.Res
import com.app.market.resources.fdroid_official
import com.app.market.resources.fdroid_official_summary
import com.app.market.resources.fdroid_enabled
import com.app.market.resources.fdroid_enabled_summary
import com.app.market.resources.fdroid_repositories
import com.app.market.resources.fdroid_tuna
import com.app.market.resources.fdroid_tuna_summary
import com.app.market.ui.component.CardSegmentContainer
import com.app.market.ui.component.MainTabScaffold
import com.app.market.ui.component.MarketScaffold
import com.app.market.ui.component.PageVerticalPadding
import com.app.market.viewmodel.ThemeSettingsViewModel
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun FdroidRepositoriesScreen(viewModel: ThemeSettingsViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val layoutDirection = LocalLayoutDirection.current
    MarketScaffold(title = stringResource(Res.string.fdroid_repositories), onBack = onBack) { padding, backdrop, behavior ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().then(backdrop).scrollEndHaptic().overScrollVertical()
                .nestedScroll(behavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                start = padding.calculateStartPadding(layoutDirection) + 12.dp,
                end = padding.calculateEndPadding(layoutDirection) + 12.dp,
                top = padding.calculateTopPadding() + PageVerticalPadding,
                bottom = padding.calculateBottomPadding() + PageVerticalPadding,
            ),
        ) {
            item {
                CardSegmentContainer(isFirst = true, isLast = false, horizontalPadding = 0.dp) {
                    SwitchPreference(
                        title = stringResource(Res.string.fdroid_enabled),
                        summary = stringResource(Res.string.fdroid_enabled_summary),
                        checked = state.fdroidEnabled,
                        onCheckedChange = viewModel::setFdroidEnabled,
                    )
                }
            }
            item {
                CardSegmentContainer(isFirst = false, isLast = true, horizontalPadding = 0.dp) {
                    val sources = listOf(
                        stringResource(Res.string.fdroid_official),
                        stringResource(Res.string.fdroid_tuna),
                    )
                    WindowDropdownPreference(
                        title = stringResource(Res.string.fdroid_source),
                        summary = sources[if (state.fdroidSource == "tuna") 1 else 0],
                        items = sources,
                        selectedIndex = if (state.fdroidSource == "tuna") 1 else 0,
                        onSelectedIndexChange = { viewModel.setFdroidSource(if (it == 1) "tuna" else "official") },
                    )
                }
            }
        }
    }
}

@Composable
fun FdroidTab(bottomPadding: Dp) {
    val settings = org.koin.compose.koinInject<ThemeSettingsViewModel>()
    val state by settings.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    MainTabScaffold(title = stringResource(Res.string.nav_fdroid)) { topPadding, backdrop, behavior ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().then(backdrop).scrollEndHaptic().overScrollVertical()
                .nestedScroll(behavior.nestedScrollConnection),
            contentPadding = PaddingValues(top = topPadding + PageVerticalPadding, bottom = bottomPadding + PageVerticalPadding),
        ) {
            item {
                CardSegmentContainer(isFirst = true, isLast = true, horizontalPadding = 12.dp) {
                    val isTuna = state.fdroidSource == "tuna"
                    ArrowPreference(
                        title = stringResource(if (isTuna) Res.string.fdroid_tuna else Res.string.fdroid_official),
                        summary = stringResource(if (isTuna) Res.string.fdroid_tuna_summary else Res.string.fdroid_official_summary),
                        onClick = {
                            val baseUrl = if (isTuna) "https://mirrors.tuna.tsinghua.edu.cn/fdroid/repo/" else "https://f-droid.org/repo/"
                            uriHandler.openUri("${baseUrl}?fingerprint=43238D512C1E5EB2D6569F4A3AFBF5523418B82E0A3ED1552770ABB9A9C9CCAB")
                        },
                    )
                }
            }
        }
    }
}
