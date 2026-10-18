package com.app.market.ui.navigation

import com.app.market.domain.model.market.AppSource
import kotlinx.serialization.Serializable
import top.yukonga.miuix.kmp.nav.core.NavKey

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Main : Route

    @Serializable
    data class AppDetail(
        val appId: Long,
        val packageName: String,
        val displayName: String,
        val externalQuery: String? = null,
        val source: AppSource = AppSource.XIAOMI,
    ) : Route

    @Serializable
    data class HistoricalVersions(
        val appId: Long,
        val packageName: String,
        val displayName: String,
    ) : Route

    @Serializable
    data class TodayArticle(val rId: String) : Route

    @Serializable
    data object DeviceProfile : Route

    @Serializable
    data object IgnoredApps : Route

    @Serializable
    data object ManualUpdate : Route

    @Serializable
    data object UpdateHistory : Route

    @Serializable
    data object SavedPackages : Route

    @Serializable
    data object DownloadingApps : Route

    @Serializable
    data object InstallerSettings : Route

    @Serializable
    data object About : Route

    @Serializable
    data object ThemeSettings : Route

    @Serializable
    data object FdroidRepositories : Route
}
