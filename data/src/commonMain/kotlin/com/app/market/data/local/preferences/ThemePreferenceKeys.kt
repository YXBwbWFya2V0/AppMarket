package com.app.market.data.local.preferences

import com.app.market.data.local.BooleanPreferenceKey
import com.app.market.data.local.StringPreferenceKey

object ThemePreferenceKeys {
    private const val NS = "theme_preferences"
    val EnableBlur = BooleanPreferenceKey(NS, "enable_blur")
    val EnableFloatingBottomBar = BooleanPreferenceKey(NS, "enable_floating_bottom_bar")
    val EnableFloatingBottomBarBlur = BooleanPreferenceKey(NS, "enable_floating_bottom_bar_blur")
    val EnableNavigationBadge = BooleanPreferenceKey(NS, "enable_navigation_badge", true)
    val FdroidEnabled = BooleanPreferenceKey(NS, "fdroid_enabled")
    val FdroidSource = StringPreferenceKey(NS, "fdroid_source")
    val NavRailExpanded = BooleanPreferenceKey(NS, "nav_rail_expanded")
    val EnablePredictiveBack = BooleanPreferenceKey(NS, "enable_predictive_back")
    val PageScale = StringPreferenceKey(NS, "page_scale")
}
