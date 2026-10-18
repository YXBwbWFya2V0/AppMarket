package com.app.market.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.market.domain.model.market.AppSource
import com.app.market.domain.model.preference.HomePage
import com.app.market.resources.Res
import com.app.market.resources.about
import com.app.market.resources.about_summary
import com.app.market.resources.app_detail
import com.app.market.resources.device_profile
import com.app.market.resources.device_profile_summary
import com.app.market.resources.downloading_apps
import com.app.market.resources.downloading_apps_summary
import com.app.market.resources.filter_quick_games
import com.app.market.resources.filter_quick_games_summary
import com.app.market.resources.filter_reservation_apps
import com.app.market.resources.filter_reservation_apps_summary
import com.app.market.resources.focus_notification_optimization
import com.app.market.resources.general
import com.app.market.resources.fdroid_repositories
import com.app.market.resources.fdroid_repositories_summary
import com.app.market.resources.home_page
import com.app.market.resources.home_page_summary
import com.app.market.resources.ignored_apps
import com.app.market.resources.ignored_apps_summary
import com.app.market.resources.installer_delta_fallback_notice
import com.app.market.resources.installer_delta_fallback_notice_summary
import com.app.market.resources.installer_delta_update
import com.app.market.resources.installer_delta_update_summary
import com.app.market.resources.installer_section
import com.app.market.resources.installer_section_summary
import com.app.market.resources.manual_update
import com.app.market.resources.manual_update_summary
import com.app.market.resources.nav_search
import com.app.market.resources.nav_settings
import com.app.market.resources.nav_today
import com.app.market.resources.nav_updates
import com.app.market.resources.remove_search_ads
import com.app.market.resources.remove_search_ads_summary
import com.app.market.resources.saved_packages
import com.app.market.resources.saved_packages_summary
import com.app.market.resources.search_sources
import com.app.market.resources.search_sources_summary
import com.app.market.resources.settings_section_download_install
import com.app.market.resources.show_app_comments
import com.app.market.resources.show_app_comments_summary
import com.app.market.resources.show_promotions
import com.app.market.resources.show_promotions_summary
import com.app.market.resources.show_same_developer
import com.app.market.resources.show_same_developer_summary
import com.app.market.resources.show_system_apps
import com.app.market.resources.show_system_apps_summary
import com.app.market.resources.strip_name_subtitle
import com.app.market.resources.strip_name_subtitle_summary
import com.app.market.resources.theme
import com.app.market.resources.theme_summary
import com.app.market.resources.today_source
import com.app.market.resources.today_source_summary
import com.app.market.resources.update_history
import com.app.market.resources.update_history_summary
import com.app.market.resources.update_source
import com.app.market.resources.update_source_summary
import com.app.market.resources.xiaomi_island_optimization
import com.app.market.resources.xiaomi_island_optimization_summary
import com.app.market.ui.component.MainTabScaffold
import com.app.market.ui.component.PageVerticalPadding
import com.app.market.ui.component.SectionTitle
import com.app.market.ui.util.appSourceLabel
import com.app.market.viewmodel.InstallerSettingsViewModel
import com.app.market.viewmodel.UpdatesViewModel
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun SettingsTab(
    updatesViewModel: UpdatesViewModel,
    installerSettingsViewModel: InstallerSettingsViewModel,
    bottomPadding: Dp,
    appManagementSupported: Boolean = true,
    onNavigateDeviceProfile: () -> Unit,
    onNavigateInstaller: () -> Unit,
    onNavigateIgnored: () -> Unit,
    onNavigateManualUpdate: () -> Unit,
    onNavigateUpdateHistory: () -> Unit,
    onNavigateSavedPackages: () -> Unit,
    onNavigateDownloadingApps: () -> Unit,
    onNavigateAbout: () -> Unit,
    onNavigateTheme: () -> Unit,
    onNavigateFdroid: () -> Unit,
) {
    val updatesState by updatesViewModel.uiState.collectAsStateWithLifecycle()
    val installerState by installerSettingsViewModel.uiState.collectAsStateWithLifecycle()
    val homePage by updatesViewModel.homePage.collectAsStateWithLifecycle()
    val searchSources by updatesViewModel.searchSources.collectAsStateWithLifecycle()
    val todaySource by updatesViewModel.todaySource.collectAsStateWithLifecycle()
    val showAppComments by updatesViewModel.showAppComments.collectAsStateWithLifecycle()
    val showSameDeveloper by updatesViewModel.showSameDeveloper.collectAsStateWithLifecycle()
    val showPromotions by updatesViewModel.showPromotions.collectAsStateWithLifecycle()
    val stripAppNameSubtitle by updatesViewModel.stripAppNameSubtitle.collectAsStateWithLifecycle()
    val selectedSource = AppSource.entries.firstOrNull { it in searchSources } ?: AppSource.Default.first()
    val selectedUpdateSource by updatesViewModel.updateSource.collectAsStateWithLifecycle()
    val searchCapabilities = selectedSource.capabilities
    val showSearchFilters = searchCapabilities.supportsSearchAdsFilter ||
            searchCapabilities.supportsQuickAppFilter ||
            searchCapabilities.supportsReservationFilter
    val showDetailOptions = searchCapabilities.supportsPromotions ||
            searchCapabilities.supportsComments ||
            searchCapabilities.supportsSameDeveloperApps

    MainTabScaffold(title = stringResource(Res.string.nav_settings)) { topPadding, backdropModifier, scrollBehavior ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().then(backdropModifier).scrollEndHaptic().overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                top = topPadding + PageVerticalPadding,
                bottom = bottomPadding + PageVerticalPadding,
            ),
        ) {
            item {
                val sourceOptions = AppSource.entries
                val updateSourceOptions = AppSource.entries.filter { it.capabilities.supportsUpdates }
                val todaySourceOptions = AppSource.entries.filter { it.capabilities.supportsTodayFeed }
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    WindowDropdownPreference(
                        title = stringResource(Res.string.today_source),
                        summary = stringResource(Res.string.today_source_summary),
                        items = todaySourceOptions.map { appSourceLabel(it) },
                        selectedIndex = todaySourceOptions.indexOf(todaySource).coerceAtLeast(0),
                        onSelectedIndexChange = { updatesViewModel.setTodaySource(todaySourceOptions[it]) },
                    )
                    if (appManagementSupported) {
                        WindowDropdownPreference(
                            title = stringResource(Res.string.update_source),
                            summary = stringResource(Res.string.update_source_summary),
                            items = updateSourceOptions.map { appSourceLabel(it) },
                            selectedIndex = updateSourceOptions.indexOf(selectedUpdateSource).coerceAtLeast(0),
                            onSelectedIndexChange = { updatesViewModel.setUpdateSource(updateSourceOptions[it]) },
                        )
                    }
                    WindowDropdownPreference(
                        title = stringResource(Res.string.search_sources),
                        summary = stringResource(Res.string.search_sources_summary),
                        items = sourceOptions.map { appSourceLabel(it) },
                        selectedIndex = sourceOptions.indexOf(selectedSource),
                        onSelectedIndexChange = { updatesViewModel.setSearchSource(sourceOptions[it]) },
                    )
                }
            }
            if (appManagementSupported) {
                item { SectionTitle(text = stringResource(Res.string.nav_updates)) }
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        SwitchPreference(
                            title = stringResource(Res.string.show_system_apps),
                            summary = stringResource(Res.string.show_system_apps_summary),
                            checked = updatesState.showSystemUpdates,
                            onCheckedChange = updatesViewModel::setShowSystemUpdates,
                        )
                        if (installerState.deltaUpdateSupported && selectedUpdateSource.capabilities.supportsDeltaUpdates) {
                            SwitchPreference(
                                title = stringResource(Res.string.installer_delta_update),
                                summary = stringResource(Res.string.installer_delta_update_summary),
                                checked = installerState.deltaUpdateEnabled,
                                onCheckedChange = installerSettingsViewModel::setDeltaUpdateEnabled,
                            )
                            if (installerState.deltaUpdateEnabled) {
                                SwitchPreference(
                                    title = stringResource(Res.string.installer_delta_fallback_notice),
                                    summary = stringResource(Res.string.installer_delta_fallback_notice_summary),
                                    checked = installerState.deltaFallbackNoticeEnabled,
                                    onCheckedChange = installerSettingsViewModel::setDeltaFallbackNoticeEnabled,
                                )
                            }
                        }
                        ArrowPreference(
                            title = stringResource(Res.string.ignored_apps),
                            summary = stringResource(Res.string.ignored_apps_summary),
                            onClick = onNavigateIgnored,
                        )
                        ArrowPreference(
                            title = stringResource(Res.string.manual_update),
                            summary = stringResource(Res.string.manual_update_summary),
                            onClick = onNavigateManualUpdate,
                        )
                        ArrowPreference(
                            title = stringResource(Res.string.update_history),
                            summary = stringResource(Res.string.update_history_summary),
                            onClick = onNavigateUpdateHistory,
                        )
                    }
                }
                if (showSearchFilters) {
                    item { SectionTitle(text = stringResource(Res.string.nav_search)) }
                }
            } else {
                if (showSearchFilters) {
                    item { SectionTitle(text = stringResource(Res.string.nav_search)) }
                }
            }
            if (showSearchFilters) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        if (searchCapabilities.supportsSearchAdsFilter) {
                            SwitchPreference(
                                title = stringResource(Res.string.remove_search_ads),
                                summary = stringResource(Res.string.remove_search_ads_summary),
                                checked = updatesState.removeSearchAds,
                                onCheckedChange = updatesViewModel::setRemoveSearchAds,
                            )
                        }
                        if (searchCapabilities.supportsQuickAppFilter) {
                            SwitchPreference(
                                title = stringResource(Res.string.filter_quick_games),
                                summary = stringResource(Res.string.filter_quick_games_summary),
                                checked = updatesState.filterQuickGames,
                                onCheckedChange = updatesViewModel::setFilterQuickGames,
                            )
                        }
                        if (searchCapabilities.supportsReservationFilter) {
                            SwitchPreference(
                                title = stringResource(Res.string.filter_reservation_apps),
                                summary = stringResource(Res.string.filter_reservation_apps_summary),
                                checked = updatesState.filterReservationApps,
                                onCheckedChange = updatesViewModel::setFilterReservationApps,
                            )
                        }
                    }
                }
            }
            if (showDetailOptions) {
                item { SectionTitle(text = stringResource(Res.string.app_detail)) }
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        if (searchCapabilities.supportsPromotions) {
                            SwitchPreference(
                                title = stringResource(Res.string.show_promotions),
                                summary = stringResource(Res.string.show_promotions_summary),
                                checked = showPromotions,
                                onCheckedChange = updatesViewModel::setShowPromotions,
                            )
                        }
                        if (searchCapabilities.supportsComments) {
                            SwitchPreference(
                                title = stringResource(Res.string.show_app_comments),
                                summary = stringResource(Res.string.show_app_comments_summary),
                                checked = showAppComments,
                                onCheckedChange = updatesViewModel::setShowAppComments,
                            )
                        }
                        if (searchCapabilities.supportsSameDeveloperApps) {
                            SwitchPreference(
                                title = stringResource(Res.string.show_same_developer),
                                summary = stringResource(Res.string.show_same_developer_summary),
                                checked = showSameDeveloper,
                                onCheckedChange = updatesViewModel::setShowSameDeveloper,
                            )
                        }
                    }
                }
            }
            item { SectionTitle(text = stringResource(Res.string.settings_section_download_install)) }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    ArrowPreference(
                        title = stringResource(Res.string.downloading_apps),
                        summary = stringResource(Res.string.downloading_apps_summary),
                        onClick = onNavigateDownloadingApps,
                    )
                    if (installerState.installationSupported) {
                        ArrowPreference(
                            title = stringResource(Res.string.saved_packages),
                            summary = stringResource(Res.string.saved_packages_summary),
                            onClick = onNavigateSavedPackages,
                        )
                        ArrowPreference(
                            title = stringResource(Res.string.installer_section),
                            summary = stringResource(Res.string.installer_section_summary),
                            onClick = onNavigateInstaller,
                        )
                        if (installerState.focusNotificationSupported) {
                            SwitchPreference(
                                // OS3 带岛胶囊称「超级岛」，OS2 仅焦点通知
                                title = stringResource(
                                    if (installerState.xiaomiIslandSupported) {
                                        Res.string.xiaomi_island_optimization
                                    } else {
                                        Res.string.focus_notification_optimization
                                    }
                                ),
                                summary = stringResource(Res.string.xiaomi_island_optimization_summary),
                                checked = installerState.xiaomiIslandOptimizationEnabled,
                                onCheckedChange = installerSettingsViewModel::setXiaomiIslandOptimizationEnabled,
                            )
                        }
                    }
                }
            }
            item { SectionTitle(text = stringResource(Res.string.general)) }
            item {
                // 桌面端无「更新」页，首页选项相应剔除
                val homePageOptions = if (appManagementSupported) {
                    listOf(HomePage.TODAY, HomePage.UPDATES, HomePage.SEARCH)
                } else {
                    listOf(HomePage.TODAY, HomePage.SEARCH)
                }
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    WindowDropdownPreference(
                        title = stringResource(Res.string.home_page),
                        summary = stringResource(Res.string.home_page_summary),
                        items = homePageOptions.map { stringResource(it.labelRes) },
                        selectedIndex = homePageOptions.indexOf(homePage).coerceAtLeast(0),
                        onSelectedIndexChange = { updatesViewModel.setHomePage(homePageOptions[it]) },
                    )
                    SwitchPreference(
                        title = stringResource(Res.string.strip_name_subtitle),
                        summary = stringResource(Res.string.strip_name_subtitle_summary),
                        checked = stripAppNameSubtitle,
                        onCheckedChange = updatesViewModel::setStripAppNameSubtitle,
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.device_profile),
                        summary = stringResource(Res.string.device_profile_summary),
                        onClick = onNavigateDeviceProfile,
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.theme),
                        summary = stringResource(Res.string.theme_summary),
                        onClick = onNavigateTheme,
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.fdroid_repositories),
                        summary = stringResource(Res.string.fdroid_repositories_summary),
                        onClick = onNavigateFdroid,
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.about),
                        summary = stringResource(Res.string.about_summary),
                        onClick = onNavigateAbout,
                    )
                }
            }
        }
    }
}

private val HomePage.labelRes
    get() = when (this) {
        HomePage.TODAY -> Res.string.nav_today
        HomePage.UPDATES -> Res.string.nav_updates
        HomePage.SEARCH -> Res.string.nav_search
    }
