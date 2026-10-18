package com.app.market.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.market.domain.repository.ThemePreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class ThemeSettingsUiState(
    val enableBlur: Boolean = false,
    val enableFloatingBottomBar: Boolean = false,
    val enableFloatingBottomBarBlur: Boolean = false,
    val enableNavigationBadge: Boolean = true,
    val fdroidEnabled: Boolean = false,
    val fdroidSource: String = "official",
    val enablePredictiveBack: Boolean = false,
    val pageScale: Float = 1f,
)

class ThemeSettingsViewModel(
    private val preferences: ThemePreferencesRepository,
) : ViewModel() {
    private val appearance = combine(preferences.enableBlur, preferences.enableFloatingBottomBar) { blur, floating ->
        blur to floating
    }
    private val fdroidSettings = combine(preferences.fdroidEnabled, preferences.fdroidSource) { enabled, source -> enabled to source }
    private val appearanceAndGlass = combine(appearance, preferences.enableFloatingBottomBarBlur) { value, glass ->
        value to glass
    }

    val uiState: StateFlow<ThemeSettingsUiState> = combine(
        appearanceAndGlass,
        preferences.enableNavigationBadge,
        fdroidSettings,
        preferences.enablePredictiveBack,
        preferences.pageScale,
    ) { appearanceGlass, badge, fdroid, predictiveBack, scale ->
        ThemeSettingsUiState(
            enableBlur = appearanceGlass.first.first,
            enableFloatingBottomBar = appearanceGlass.first.second,
            enableFloatingBottomBarBlur = appearanceGlass.second,
            enableNavigationBadge = badge,
            fdroidEnabled = fdroid.first,
            fdroidSource = fdroid.second,
            enablePredictiveBack = predictiveBack,
            pageScale = scale,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ThemeSettingsUiState())

    fun setEnableBlur(value: Boolean) = persist { preferences.setEnableBlur(value) }
    fun setEnableFloatingBottomBar(value: Boolean) = persist { preferences.setEnableFloatingBottomBar(value) }
    fun setEnableFloatingBottomBarBlur(value: Boolean) = persist { preferences.setEnableFloatingBottomBarBlur(value) }
    fun setEnableNavigationBadge(value: Boolean) = persist { preferences.setEnableNavigationBadge(value) }
    fun setFdroidEnabled(value: Boolean) = persist { preferences.setFdroidEnabled(value) }
    fun setFdroidSource(value: String) = persist { preferences.setFdroidSource(value) }
    fun setEnablePredictiveBack(value: Boolean) = persist { preferences.setEnablePredictiveBack(value) }
    fun setPageScale(value: Float) = persist { preferences.setPageScale(value) }

    private fun persist(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
