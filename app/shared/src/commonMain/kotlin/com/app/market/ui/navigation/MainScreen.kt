package com.app.market.ui.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.app.market.domain.model.market.MarketAppInfo
import com.app.market.domain.model.preference.HomePage
import com.app.market.domain.repository.ThemePreferencesRepository
import com.app.market.platform.UiPlatform
import com.app.market.resources.Res
import com.app.market.resources.nav_search
import com.app.market.resources.nav_settings
import com.app.market.resources.nav_today
import com.app.market.resources.nav_updates
import com.app.market.ui.component.FloatingBottomBar
import com.app.market.ui.component.FloatingBottomBarItem
import com.app.market.ui.component.blur.BlurredBar
import com.app.market.ui.component.blur.rememberBlurBackdrop
import com.app.market.ui.screen.SearchTab
import com.app.market.ui.screen.FdroidTab
import com.app.market.ui.screen.SettingsTab
import com.app.market.ui.screen.TodayTab
import com.app.market.ui.screen.UpdatesTab
import com.app.market.ui.theme.LocalEnableFloatingBottomBar
import com.app.market.ui.theme.LocalEnableFloatingBottomBarBlur
import com.app.market.ui.theme.LocalEnableNavigationBadge
import com.app.market.ui.util.rememberIsWideScreen
import com.app.market.viewmodel.InstallerSettingsViewModel
import com.app.market.viewmodel.SearchViewModel
import com.app.market.viewmodel.TodayViewModel
import com.app.market.viewmodel.UpdatesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.BadgedBox
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.basic.NavigationRailValue
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.rememberNavigationRailState
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Create
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Update
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.abs

// 主页签集合按平台能力裁剪：桌面端（无法扫描已装应用 / 安装）不含「更新」。
private enum class MainTab { Today, Updates, Search, Fdroid, Settings }

@Composable
fun MainPage(
    navigator: Navigator,
    updatesViewModel: UpdatesViewModel,
    searchViewModel: SearchViewModel,
    installerSettingsViewModel: InstallerSettingsViewModel,
    todayViewModel: TodayViewModel,
    pendingSearchKeyword: String? = null,
    onPendingSearchConsumed: () -> Unit = {},
) {
    val uiPlatform = koinInject<UiPlatform>()
    val themePreferences = koinInject<ThemePreferencesRepository>()
    val preferencesInitialized by themePreferences.initialized.collectAsStateWithLifecycle()
    val enableFloatingBottomBar = LocalEnableFloatingBottomBar.current
    val enableFloatingBottomBarBlur = LocalEnableFloatingBottomBarBlur.current
    val updatesState by updatesViewModel.uiState.collectAsStateWithLifecycle()
    val fdroidEnabled by themePreferences.fdroidEnabled.collectAsStateWithLifecycle()
    val appManagementSupported = uiPlatform.packageInstallationSupported
    val tabs = remember(appManagementSupported, fdroidEnabled) {
        buildList {
            add(MainTab.Today)
            if (appManagementSupported) add(MainTab.Updates)
            add(MainTab.Search)
            add(MainTab.Settings)
            if (fdroidEnabled) add(MainTab.Fdroid)
        }
    }
    val searchPage = tabs.indexOf(MainTab.Search)
    fun homeIndexOf(page: HomePage): Int =
        tabs.indexOf(page.toTab()).takeIf { it >= 0 } ?: 0

    // Initial page reads the seeded value once (no launch jump); reactive for the Back target.
    val homePage by updatesViewModel.homePage.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = homeIndexOf(updatesViewModel.homePage.value),
        pageCount = { tabs.size },
    )
    val mainPagerState = rememberMainPagerState(pagerState)
    val selectedPage = mainPagerState.selectedPage
    val isWideScreen = rememberIsWideScreen()
    var notificationPermissionRequested by remember { mutableStateOf(false) }
    var searchFocusRequestId by remember { mutableIntStateOf(0) }
    val onSearchTabClick = {
        if (selectedPage == searchPage && !mainPagerState.isNavigating) {
            searchFocusRequestId += 1
        } else {
            mainPagerState.animateToPage(searchPage)
        }
    }
    val onTabClick: (Int, MainTab) -> Unit = { index, tab ->
        if (tab == MainTab.Search) onSearchTabClick() else mainPagerState.animateToPage(index)
    }
    val startInitialCheckAfterAppListPermission = {
        val launched = uiPlatform.requestInstalledAppsPermission {
            updatesViewModel.startInitialCheck()
        }
        if (!launched) updatesViewModel.startInitialCheck()
    }

    LaunchedEffect(pendingSearchKeyword) {
        val keyword = pendingSearchKeyword ?: return@LaunchedEffect
        mainPagerState.animateToPage(searchPage)
        if (keyword.isNotEmpty()) searchViewModel.searchWith(keyword) else searchFocusRequestId += 1
        onPendingSearchConsumed()
    }

    // Keep selectedPage (nav-bar highlight + per-tab gating) in sync when the user swipes the pager,
    // not just when a tab is tapped (animateToPage).
    LaunchedEffect(mainPagerState.pagerState.currentPage) {
        mainPagerState.syncPage()
    }

    // Update checking (and its permission dance) only applies where app management is supported.
    LaunchedEffect(Unit) {
        if (appManagementSupported && !notificationPermissionRequested) {
            notificationPermissionRequested = true
            val launched = uiPlatform.requestPostNotificationsPermission {
                startInitialCheckAfterAppListPermission()
            }
            if (!launched) startInitialCheckAfterAppListPermission()
        }
    }

    MainScreenBackHandler(mainPagerState, navigator, homeIndexOf(homePage))

    val openDetail: (MarketAppInfo) -> Unit = { app ->
        // 搜索页可能给出 vivo 条目，详情要打对应源的接口
        navigator.push(Route.AppDetail(app.appId, app.packageName, app.displayName, source = app.source))
    }

    val pagerContent: @Composable (Modifier, Dp) -> Unit = { pagerModifier, bottomPadding ->
        HorizontalPager(
            modifier = pagerModifier,
            state = mainPagerState.pagerState,
            overscrollEffect = null,
            verticalAlignment = Alignment.Top,
        ) { page ->
            when (tabs[page]) {
                MainTab.Today -> TodayTab(
                    viewModel = todayViewModel,
                    updatesViewModel = updatesViewModel.takeIf { appManagementSupported },
                    bottomPadding = bottomPadding,
                    onClickViewUpdates = { tabs.indexOf(MainTab.Updates).takeIf { it >= 0 }?.let(mainPagerState::animateToPage) },
                    onClickArticle = { article ->
                        navigator.push(Route.TodayArticle(article.rId))
                    },
                )

                MainTab.Updates -> UpdatesTab(
                    viewModel = updatesViewModel,
                    bottomPadding = bottomPadding,
                    onOpenDetail = openDetail,
                )

                MainTab.Search -> SearchTab(
                    viewModel = searchViewModel,
                    bottomPadding = bottomPadding,
                    onOpenDetail = openDetail,
                    isCurrentPage = selectedPage == searchPage,
                    focusRequestId = searchFocusRequestId,
                )

                MainTab.Fdroid -> FdroidTab(bottomPadding = bottomPadding)

                MainTab.Settings -> SettingsTab(
                    updatesViewModel = updatesViewModel,
                    installerSettingsViewModel = installerSettingsViewModel,
                    bottomPadding = bottomPadding,
                    appManagementSupported = appManagementSupported,
                    onNavigateDeviceProfile = { navigator.push(Route.DeviceProfile) },
                    onNavigateInstaller = { navigator.push(Route.InstallerSettings) },
                    onNavigateIgnored = { navigator.push(Route.IgnoredApps) },
                    onNavigateManualUpdate = { navigator.push(Route.ManualUpdate) },
                    onNavigateUpdateHistory = { navigator.push(Route.UpdateHistory) },
                    onNavigateSavedPackages = { navigator.push(Route.SavedPackages) },
                    onNavigateDownloadingApps = { navigator.push(Route.DownloadingApps) },
                    onNavigateAbout = { navigator.push(Route.About) },
                    onNavigateTheme = { navigator.push(Route.ThemeSettings) },
                    onNavigateFdroid = { navigator.push(Route.FdroidRepositories) },
                )
            }
        }
    }

    if (isWideScreen) {
        Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
            Row(Modifier.fillMaxSize()) {
                if (preferencesInitialized) {
                    val railState = rememberNavigationRailState(
                        initialValue = if (themePreferences.navRailExpanded.value) {
                            NavigationRailValue.Expanded
                        } else {
                            NavigationRailValue.Collapsed
                        },
                    )
                    LaunchedEffect(railState.currentValue) {
                        themePreferences.setNavRailExpanded(railState.isExpanded)
                    }
                    NavigationRail(state = railState) {
                        tabs.forEachIndexed { index, tab ->
                            NavigationRailItem(
                                selected = selectedPage == index,
                                onClick = { onTabClick(index, tab) },
                                icon = tab.icon,
                                label = stringResource(tab.labelRes),
                            )
                        }
                    }
                }
                pagerContent(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        // The rail already absorbed the start-side cutout/nav-bar insets; the end
                        // side has no rail, so pad it here (and mark it consumed for descendants).
                        .consumeWindowInsets(
                            WindowInsets.displayCutout.union(WindowInsets.navigationBars)
                                .only(WindowInsetsSides.Start),
                        )
                        .windowInsetsPadding(
                            WindowInsets.systemBars.union(WindowInsets.displayCutout)
                                .only(WindowInsetsSides.End),
                        ),
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                )
            }
        }
    } else {
        val blurBackdrop = rememberBlurBackdrop()
        val blurActive = blurBackdrop != null
        val barColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surface
        val surfaceColor = MiuixTheme.colorScheme.surface
        val glassBackdrop = rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    if (!enableFloatingBottomBar) {
                        BlurredBar(backdrop = blurBackdrop, blurActive = blurActive) {
                            NavigationBar(color = barColor) {
                                tabs.forEachIndexed { index, tab ->
                                    NavigationBarItem(
                                        modifier = Modifier.weight(1f),
                                        selected = selectedPage == index,
                                        onClick = { onTabClick(index, tab) },
                                        icon = tab.icon,
                                        label = stringResource(tab.labelRes),
                                        badge = navigationBadge(tab, updatesState.updates.size),
                                    )
                                }
                            }
                        }
                    } else {
                        FloatingBottomBar(
                            modifier = Modifier
                                .padding(
                                    bottom = 12.dp + WindowInsets.navigationBars.asPaddingValues()
                                        .calculateBottomPadding(),
                                ),
                            selectedIndex = mainPagerState.selectedPage,
                            onSelected = { index -> onTabClick(index, tabs[index]) },
                            backdrop = glassBackdrop,
                            tabsCount = tabs.size,
                            isBlurEnabled = enableFloatingBottomBarBlur,
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                val label = stringResource(tab.labelRes)
                                FloatingBottomBarItem(
                                    modifier = Modifier.defaultMinSize(minWidth = 76.dp),
                                    selected = mainPagerState.selectedPage == index,
                                    onClick = { onTabClick(index, tab) },
                                ) {
                                    val badge = navigationBadge(tab, updatesState.updates.size)
                                    val icon: @Composable () -> Unit = {
                                        Icon(
                                            modifier = Modifier.size(22.dp),
                                            imageVector = tab.icon,
                                            contentDescription = null,
                                        )
                                    }
                                    if (badge != null) {
                                        BadgedBox(badge = { badge() }) { icon() }
                                    } else {
                                        icon()
                                    }
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (blurBackdrop != null) Modifier.layerBackdrop(blurBackdrop) else Modifier),
            ) {
                pagerContent(
                    Modifier
                        .fillMaxSize()
                        .then(
                            if (enableFloatingBottomBar && enableFloatingBottomBarBlur) {
                                Modifier.layerBackdrop(glassBackdrop)
                            } else {
                                Modifier
                            },
                        ),
                    padding.calculateBottomPadding(),
                )
            }
        }
    }
}

private fun HomePage.toTab(): MainTab = when (this) {
    HomePage.TODAY -> MainTab.Today
    HomePage.UPDATES -> MainTab.Updates
    HomePage.SEARCH -> MainTab.Search
}

private val MainTab.icon
    get() = when (this) {
        MainTab.Today -> MiuixIcons.Create
        MainTab.Updates -> MiuixIcons.Update
        MainTab.Search -> MiuixIcons.Search
        MainTab.Fdroid -> MiuixIcons.More
        MainTab.Settings -> MiuixIcons.Settings
    }

private val MainTab.labelRes
    get() = when (this) {
        MainTab.Today -> Res.string.nav_today
        MainTab.Updates -> Res.string.nav_updates
        MainTab.Search -> Res.string.nav_search
        MainTab.Fdroid -> Res.string.nav_fdroid
        MainTab.Settings -> Res.string.nav_settings
    }

@Composable
private fun navigationBadge(
    tab: MainTab,
    updateCount: Int,
): (@Composable () -> Unit)? {
    if (!LocalEnableNavigationBadge.current || tab != MainTab.Updates || updateCount <= 0) return null
    return {
        Badge {
            Text(updateCount.toString())
        }
    }
}

@Stable
class MainPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope,
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    var isNavigating by mutableStateOf(false)
        private set

    private var navJob: Job? = null

    fun animateToPage(targetIndex: Int) {
        if (targetIndex == selectedPage) return
        navJob?.cancel()
        selectedPage = targetIndex
        isNavigating = true
        navJob = coroutineScope.launch {
            val myJob = coroutineContext.job
            try {
                pagerState.springAnimateToPage(targetIndex)
            } finally {
                if (navJob == myJob) {
                    isNavigating = false
                    if (pagerState.currentPage != targetIndex) selectedPage = pagerState.currentPage
                }
            }
        }
    }

    /** Sync [selectedPage] to the pager after a manual swipe (when not animating a tab tap). */
    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage) selectedPage =
            pagerState.currentPage
    }
}

private suspend fun PagerState.springAnimateToPage(target: Int) {
    if (target !in 0 until pageCount) return
    var shouldSnapToTarget = false
    scroll(MutatePriority.UserInput) {
        val pageSize = layoutInfo.pageSize + layoutInfo.pageSpacing
        val distance = target - currentPage - currentPageOffsetFraction
        val scrollPixels = distance * pageSize
        if (abs(scrollPixels) <= 0.5f) return@scroll

        var consumedScroll = 0f
        var skipScroll = false
        Animatable(0f).animateTo(
            targetValue = scrollPixels,
            animationSpec = PagerNavigationSpringSpec,
        ) {
            if (skipScroll) return@animateTo

            val delta = value - consumedScroll
            if (abs(delta) > 0.5f) {
                val consumed = scrollBy(delta)
                consumedScroll += consumed
                if (abs(delta - consumed) > 0.1f) {
                    shouldSnapToTarget = true
                    skipScroll = true
                }
            } else {
                consumedScroll = value
            }

            if (abs(velocity) < 0.1f && abs(scrollPixels - consumedScroll) < 1.0f) {
                skipScroll = true
            }
        }

        val remaining = scrollPixels - consumedScroll
        if (abs(remaining) > 0.5f) {
            scrollBy(remaining)
        }
    }

    if (shouldSnapToTarget || currentPage != target) {
        scrollToPage(target)
    }
}

@Composable
fun rememberMainPagerState(
    pagerState: PagerState,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): MainPagerState =
    remember(pagerState, coroutineScope) { MainPagerState(pagerState, coroutineScope) }

@Composable
private fun MainScreenBackHandler(
    mainState: MainPagerState,
    navigator: Navigator,
    homeIndex: Int,
) {
    val isPagerBackHandlerEnabled by remember(homeIndex) {
        derivedStateOf {
            navigator.current() is Route.Main &&
                    navigator.backStackSize() == 1 &&
                    mainState.selectedPage != homeIndex
        }
    }
    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = isPagerBackHandlerEnabled,
        onBackCompleted = { mainState.animateToPage(homeIndex) },
    )
}





