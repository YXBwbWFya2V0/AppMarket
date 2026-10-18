package com.app.market.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.LayoutDirection
import com.app.market.domain.model.market.AppSource
import com.app.market.domain.model.update.IgnoredUpdate
import com.app.market.ui.screen.AboutScreen
import com.app.market.ui.screen.AppDetailScreen
import com.app.market.ui.screen.DeviceProfileScreen
import com.app.market.ui.screen.DownloadingAppsScreen
import com.app.market.ui.screen.HistoricalVersionsScreen
import com.app.market.ui.screen.IgnoredAppsScreen
import com.app.market.ui.screen.InstallerSettingsScreen
import com.app.market.ui.screen.ManualUpdateScreen
import com.app.market.ui.screen.SavedPackagesScreen
import com.app.market.ui.screen.ThemeSettingsScreen
import com.app.market.ui.screen.FdroidRepositoriesScreen
import com.app.market.ui.screen.TodayArticleScreen
import com.app.market.ui.screen.UpdateHistoryScreen
import com.app.market.viewmodel.AppDetailViewModel
import com.app.market.viewmodel.DeviceProfileViewModel
import com.app.market.viewmodel.DownloadingAppsViewModel
import com.app.market.viewmodel.HistoricalVersionsViewModel
import com.app.market.viewmodel.IgnoredAppsViewModel
import com.app.market.viewmodel.InstallerSettingsViewModel
import com.app.market.viewmodel.ManualUpdateViewModel
import com.app.market.viewmodel.SavedPackagesViewModel
import com.app.market.viewmodel.SearchViewModel
import com.app.market.viewmodel.ThemeSettingsViewModel
import com.app.market.viewmodel.TodayViewModel
import com.app.market.viewmodel.UpdateHistoryViewModel
import com.app.market.viewmodel.UpdatesViewModel
import org.koin.compose.viewmodel.koinViewModel
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.rememberNavBackStack
import top.yukonga.miuix.kmp.nav.core.rememberNavSystemCornerRadius
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection

@Composable
fun AppNavigation(
    externalDetailPackageName: String? = null,
    externalDetailQuery: String? = null,
    onExternalDetailConsumed: (String) -> Unit = {},
    externalSearchKeyword: String? = null,
    onExternalSearchConsumed: (String) -> Unit = {},
    externalOpenDownloads: Boolean = false,
    onExternalDownloadsConsumed: () -> Unit = {},
) {
    val backStack = rememberNavBackStack<Route>(Route.Main)
    val navigator = remember { Navigator(backStack) }
    var pendingSearchKeyword by remember { mutableStateOf<String?>(null) }

    val updatesViewModel = koinViewModel<UpdatesViewModel>()
    val searchViewModel = koinViewModel<SearchViewModel>()
    val installerSettingsViewModel = koinViewModel<InstallerSettingsViewModel>()
    val todayViewModel = koinViewModel<TodayViewModel>()
    val swipeBackDirection = if (LocalLayoutDirection.current == LayoutDirection.Rtl) {
        NavSwipeDirection.RightToLeft
    } else {
        NavSwipeDirection.LeftToRight
    }

    LaunchedEffect(externalDetailPackageName, externalDetailQuery) {
        val packageName =
            externalDetailPackageName?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        val target = Route.AppDetail(
            appId = 0L,
            packageName = packageName,
            displayName = packageName,
            externalQuery = externalDetailQuery ?: "id=$packageName",
        )
        val current = navigator.current() as? Route.AppDetail
        if (current?.packageName != packageName || current.externalQuery != target.externalQuery) {
            navigator.push(target)
        }
        onExternalDetailConsumed(packageName)
    }

    // 外部搜索链接：先回主页面，再交由主页面切页执行
    LaunchedEffect(externalSearchKeyword) {
        val keyword = externalSearchKeyword ?: return@LaunchedEffect
        navigator.popUntil { it is Route.Main }
        pendingSearchKeyword = keyword
        onExternalSearchConsumed(keyword)
    }

    LaunchedEffect(externalOpenDownloads) {
        if (!externalOpenDownloads) return@LaunchedEffect
        if (navigator.current() != Route.DownloadingApps) {
            navigator.push(Route.DownloadingApps)
        }
        onExternalDownloadsConsumed()
    }

    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavDisplay(
            backStack = backStack,
            onBack = { navigator.pop() },
            effects = NavDisplayEffects(
                enableCornerClip = true,
                cornerClipRadius = rememberNavSystemCornerRadius(),
                dimAmount = 0.5f,
            ),
        ) {
            entry<Route.Main> {
                MainPage(
                    navigator = navigator,
                    updatesViewModel = updatesViewModel,
                    searchViewModel = searchViewModel,
                    installerSettingsViewModel = installerSettingsViewModel,
                    todayViewModel = todayViewModel,
                    pendingSearchKeyword = pendingSearchKeyword,
                    onPendingSearchConsumed = { pendingSearchKeyword = null },
                )
            }
            entry<Route.AppDetail>(swipeDismiss = swipeBackDirection) { route ->
                val vm = koinViewModel<AppDetailViewModel>()
                AppDetailScreen(
                    viewModel = vm,
                    appId = route.appId,
                    packageName = route.packageName,
                    externalQuery = route.externalQuery,
                    source = route.source,
                    onOpenDetail = {
                        navigator.push(
                            Route.AppDetail(
                                it.appId,
                                it.packageName,
                                it.displayName,
                                source = it.source,
                            )
                        )
                    },
                    onOpenHistory = { app ->
                        navigator.push(
                            Route.HistoricalVersions(
                                appId = app.appId,
                                packageName = app.packageName,
                                displayName = app.displayName,
                            )
                        )
                    },
                    onBack = { navigator.pop() },
                )
            }
            entry<Route.HistoricalVersions>(swipeDismiss = swipeBackDirection) { route ->
                val vm = koinViewModel<HistoricalVersionsViewModel>()
                HistoricalVersionsScreen(
                    viewModel = vm,
                    appId = route.appId,
                    packageName = route.packageName,
                    displayName = route.displayName,
                    onBack = { navigator.pop() },
                )
            }
            entry<Route.TodayArticle>(swipeDismiss = swipeBackDirection) { route ->
                TodayArticleScreen(
                    rId = route.rId,
                    viewModel = todayViewModel,
                    onOpenApp = {
                        navigator.push(
                            Route.AppDetail(
                                it.appId,
                                it.packageName,
                                it.displayName,
                                source = it.source,
                            )
                        )
                    },
                    onBack = { navigator.pop() },
                )
            }
            entry<Route.DeviceProfile>(swipeDismiss = swipeBackDirection) {
                val vm = koinViewModel<DeviceProfileViewModel>()
                DeviceProfileScreen(vm, onBack = { navigator.pop() })
            }
            entry<Route.IgnoredApps>(swipeDismiss = swipeBackDirection) {
                val vm = koinViewModel<IgnoredAppsViewModel>()
                IgnoredAppsScreen(
                    viewModel = vm,
                    onOpenDetail = {
                        val activeSource = updatesViewModel.updateSource.value
                        navigator.push(ignoredDetailRoute(it, activeSource))
                    },
                    onBack = { navigator.pop() },
                )
            }
            entry<Route.ManualUpdate>(swipeDismiss = swipeBackDirection) {
                val vm = koinViewModel<ManualUpdateViewModel>()
                ManualUpdateScreen(
                    viewModel = vm,
                    onOpenDetail = {
                        navigator.push(
                            Route.AppDetail(
                                it.appId,
                                it.packageName,
                                it.displayName,
                                source = it.source,
                            )
                        )
                    },
                    onBack = { navigator.pop() },
                )
            }
            entry<Route.UpdateHistory>(swipeDismiss = swipeBackDirection) {
                val vm = koinViewModel<UpdateHistoryViewModel>()
                UpdateHistoryScreen(
                    viewModel = vm,
                    onOpenDetail = {
                        navigator.push(
                            Route.AppDetail(
                                it.appId,
                                it.packageName,
                                it.displayName,
                                source = it.source
                                    ?: updatesViewModel.updateSource.value,
                            )
                        )
                    },
                    onBack = { navigator.pop() },
                )
            }
            entry<Route.SavedPackages>(swipeDismiss = swipeBackDirection) {
                val vm = koinViewModel<SavedPackagesViewModel>()
                SavedPackagesScreen(
                    viewModel = vm,
                    onBack = { navigator.pop() },
                )
            }
            entry<Route.DownloadingApps>(swipeDismiss = swipeBackDirection) {
                val vm = koinViewModel<DownloadingAppsViewModel>()
                DownloadingAppsScreen(
                    viewModel = vm,
                    onOpenDetail = {
                        val source = it.source
                            ?: updatesViewModel.searchSources.value.firstOrNull()
                            ?: AppSource.Default.first()
                        navigator.push(
                            Route.AppDetail(
                                it.appId,
                                it.packageName,
                                it.displayName,
                                source = source,
                            )
                        )
                    },
                    onBack = { navigator.pop() },
                )
            }
            entry<Route.InstallerSettings>(swipeDismiss = swipeBackDirection) {
                InstallerSettingsScreen(installerSettingsViewModel, onBack = { navigator.pop() })
            }
            entry<Route.About>(swipeDismiss = swipeBackDirection) {
                val uriHandler = LocalUriHandler.current
                AboutScreen(onBack = { navigator.pop() }, onOpenUrl = { uriHandler.openUri(it) })
            }
            entry<Route.ThemeSettings>(swipeDismiss = swipeBackDirection) {
                val vm = koinViewModel<ThemeSettingsViewModel>()
                ThemeSettingsScreen(vm, onBack = { navigator.pop() })
            }
            entry<Route.FdroidRepositories>(swipeDismiss = swipeBackDirection) {
                val vm = koinViewModel<ThemeSettingsViewModel>()
                FdroidRepositoriesScreen(vm, onBack = { navigator.pop() })
            }
        }
    }
}

internal fun ignoredDetailRoute(entry: IgnoredUpdate, activeSource: AppSource): Route.AppDetail {
    val sameSource = entry.source == activeSource
    return Route.AppDetail(
        appId = if (sameSource) entry.appId else 0L,
        packageName = entry.packageName,
        displayName = entry.displayName,
        externalQuery = if (sameSource) null else "id=${entry.packageName}",
        source = activeSource,
    )
}
