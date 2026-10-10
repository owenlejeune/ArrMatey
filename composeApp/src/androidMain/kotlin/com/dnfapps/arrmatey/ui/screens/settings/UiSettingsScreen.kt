package com.dnfapps.arrmatey.ui.screens.settings

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Shortcut
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.MoreScreenViewModel
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.model.AppColor
import com.dnfapps.arrmatey.model.AppTheme
import com.dnfapps.arrmatey.model.IconSource
import com.dnfapps.arrmatey.model.SettingItem
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.SettingsGroup
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.ui.icons.Hard_drive
import com.dnfapps.arrmatey.ui.sheets.ActivityCardCustomizationSheet
import com.dnfapps.arrmatey.ui.sheets.ArrViewCustomizationSheet
import com.dnfapps.arrmatey.ui.sheets.CalendarCardCustomizationSheet
import com.dnfapps.arrmatey.ui.sheets.DiscoverSectionCustomizationSheet
import com.dnfapps.arrmatey.ui.sheets.SearchResultCardCustomizationSheet
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UiSettingsScreen(
    windowSizeClass: WindowSizeClass,
    onNavigateToTabPreferences: () -> Unit,
    onNavigateToShortcutsPreferences: () -> Unit,
    onBack: () -> Unit,
    viewModel: MoreScreenViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val useServiceNavLogos by viewModel.useServiceNavLogos.collectAsStateWithLifecycle()
    val useColoredActivityCards by viewModel.useColoredActivityCards.collectAsStateWithLifecycle()
    val useColoredCalendarCards by viewModel.useColoredCalendarCards.collectAsStateWithLifecycle()
    val startOfWeekMonday by viewModel.startOfWeekMonday.collectAsStateWithLifecycle()
    val hideInstanceSwitcher by viewModel.hideInstanceSwitcher.collectAsStateWithLifecycle()
    val overlayTabBackOpensDrawer by viewModel.overlayTabBackOpensDrawer.collectAsStateWithLifecycle()
    val dualPanelSupport by viewModel.dualPanelSupport.collectAsStateWithLifecycle()
    val searchShowBanners by viewModel.searchShowBanners.collectAsStateWithLifecycle()
    val unifiedLibrarySearchAllInstances by viewModel.unifiedLibrarySearchAllInstances.collectAsStateWithLifecycle()
    val discoverSectionPreferences by viewModel.discoverSectionPreferences.collectAsStateWithLifecycle()

    var showLibraryCustomizationSheet by remember { mutableStateOf(false) }
    var showDiscoverCustomizationSheet by remember { mutableStateOf(false) }
    var showActivityCustomizationSheet by remember { mutableStateOf(false) }
    var showCalendarCustomizationSheet by remember { mutableStateOf(false) }
    var showSearchResultCustomizationSheet by remember { mutableStateOf(false) }

    val isLargeScreenSupported =
        remember(windowSizeClass, configuration) {
            windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact ||
                configuration.smallestScreenWidthDp >= 600 ||
                (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                        context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_HINGE_ANGLE)
                    )
        }

    val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val appColor by viewModel.appColor.collectAsStateWithLifecycle()

    var showThemeDropdown by remember { mutableStateOf(false) }
    var showColorDropdown by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = mokoString(MR.strings.user_interface)) },
                navigationIcon = { BackButton(onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        Column(
            modifier =
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = navigationBarBottomInset() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsGroup(
                title = mokoString(MR.strings.appearance),
                items =
                listOf(
                    SettingItem(
                        icon = IconSource.Vector(Icons.Default.Contrast),
                        title = mokoString(MR.strings.theme),
                        subtitle = mokoString(appTheme.resource),
                        onClick = { showThemeDropdown = true },
                        trailingContent = {
                            Box {
                                DropdownMenu(
                                    expanded = showThemeDropdown,
                                    onDismissRequest = { showThemeDropdown = false },
                                ) {
                                    AppTheme.entries.forEach { theme ->
                                        DropdownMenuItem(
                                            text = { Text(mokoString(theme.resource)) },
                                            onClick = {
                                                viewModel.setAppTheme(theme)
                                                showThemeDropdown = false
                                            },
                                        )
                                    }
                                }
                            }
                        },
                    ),
                    SettingItem(
                        icon = IconSource.Vector(Icons.Default.Palette),
                        title = mokoString(MR.strings.color),
                        subtitle = mokoString(appColor.resource),
                        onClick = { showColorDropdown = true },
                        trailingContent = {
                            Box {
                                DropdownMenu(
                                    expanded = showColorDropdown,
                                    onDismissRequest = { showColorDropdown = false },
                                ) {
                                    AppColor.entries
                                        .filter { it != AppColor.Dynamic || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }
                                        .forEach { color ->
                                            DropdownMenuItem(
                                                text = { Text(mokoString(color.resource)) },
                                                onClick = {
                                                    viewModel.setAppColor(color)
                                                    showColorDropdown = false
                                                },
                                            )
                                        }
                                }
                            }
                        },
                    ),
                    SettingItem(
                        icon = IconSource.Vector(Icons.Default.MiscellaneousServices),
                        title = mokoString(MR.strings.service_icons_title),
                        subtitle = mokoString(MR.strings.service_icons_description),
                        trailingContent = {
                            Switch(
                                checked = useServiceNavLogos,
                                onCheckedChange = { viewModel.toggleUseServiceNavLogos() },
                            )
                        },
                        onClick = { viewModel.toggleUseServiceNavLogos() },
                    ),
                ),
            )

            SettingsGroup(
                title = mokoString(MR.strings.navigation),
                items =
                listOf(
                    SettingItem(
                        icon = IconSource.Vector(Icons.Default.Navigation),
                        title = mokoString(MR.strings.navigation_bar_configuration),
                        onClick = {
                            onNavigateToTabPreferences()
                        },
                    ),
                    SettingItem(
                        icon = IconSource.Vector(Icons.AutoMirrored.Default.Shortcut),
                        title = mokoString(MR.strings.shortcuts_configuration),
                        onClick = {
                            onNavigateToShortcutsPreferences()
                        },
                    ),
                    SettingItem(
                        icon = IconSource.Vector(Hard_drive),
                        title = mokoString(MR.strings.instance_switcher_toggle_title),
                        subtitle = mokoString(MR.strings.instance_switcher_toggle_description),
                        trailingContent = {
                            Switch(
                                checked = hideInstanceSwitcher,
                                onCheckedChange = { viewModel.toggleInstanceSwitcher() },
                            )
                        },
                        onClick = { viewModel.toggleInstanceSwitcher() },
                    ),
                    SettingItem(
                        icon = IconSource.Vector(Icons.AutoMirrored.Default.ArrowBack),
                        title = mokoString(MR.strings.overlay_tab_back_opens_drawer_title),
                        subtitle = mokoString(MR.strings.overlay_tab_back_opens_drawer_description),
                        trailingContent = {
                            Switch(
                                checked = overlayTabBackOpensDrawer,
                                onCheckedChange = { viewModel.toggleOverlayTabBackOpensDrawer() },
                            )
                        },
                        onClick = { viewModel.toggleOverlayTabBackOpensDrawer() },
                    ),
                ),
            )

            SettingsGroup(
                title = mokoString(MR.strings.view_customization),
                items =
                listOf(
                    SettingItem(
                        icon = IconSource.Vector(Icons.Default.Tune),
                        title = mokoString(MR.strings.discover_sections),
                        subtitle = mokoString(MR.strings.reorganize_hide_sections),
                        onClick = { showDiscoverCustomizationSheet = true },
                    ),
                    SettingItem(
                        icon = IconSource.Vector(Icons.Default.VideoLibrary),
                        title = mokoString(MR.strings.library_view_customization),
                        subtitle = mokoString(MR.strings.library_view_customization_description),
                        onClick = { showLibraryCustomizationSheet = true },
                    ),
                    SettingItem(
                        icon = IconSource.Vector(Icons.Default.ColorLens),
                        title = mokoString(MR.strings.activity_cards),
                        subtitle = mokoString(MR.strings.activity_cards_description),
                        onClick = { showActivityCustomizationSheet = true },
                    ),
                    SettingItem(
                        icon = IconSource.Vector(Icons.Default.CalendarMonth),
                        title = mokoString(MR.strings.calendar_cards),
                        subtitle = mokoString(MR.strings.calendar_cards_description),
                        onClick = { showCalendarCustomizationSheet = true },
                    ),
                    SettingItem(
                        icon = IconSource.Vector(Icons.Default.CalendarViewWeek),
                        title = mokoString(MR.strings.start_week_on_monday),
                        subtitle = mokoString(MR.strings.start_week_on_monday_desc),
                        trailingContent = {
                            Switch(
                                checked = startOfWeekMonday,
                                onCheckedChange = { viewModel.toggleStartOfWeekMonday() },
                            )
                        },
                        onClick = { viewModel.toggleStartOfWeekMonday() },
                    ),
                    SettingItem(
                        icon = IconSource.Vector(Icons.Default.Image),
                        title = mokoString(MR.strings.search_result_cards),
                        subtitle = mokoString(MR.strings.search_result_cards_description),
                        onClick = { showSearchResultCustomizationSheet = true },
                    ),
                ),
            )

            if (isLargeScreenSupported) {
                SettingsGroup(
                    title = mokoString(MR.strings.large_screen_settings_title),
                    items =
                    listOf(
                        SettingItem(
                            icon = IconSource.Vector(Icons.Default.Splitscreen, rotation = 90f),
                            title = mokoString(MR.strings.dual_panel_support_title),
                            subtitle = mokoString(MR.strings.dual_panel_support_description),
                            trailingContent = {
                                Switch(
                                    checked = dualPanelSupport,
                                    onCheckedChange = { viewModel.toggleDualPanelSupport() },
                                )
                            },
                            onClick = { viewModel.toggleDualPanelSupport() },
                        ),
                    ),
                )
            }

            SettingsGroup(
                title = mokoString(MR.strings.search_results),
                items =
                listOf(
                    SettingItem(
                        icon = IconSource.Vector(Icons.Default.Search),
                        title = mokoString(MR.strings.unified_library_search_all_instances_title),
                        subtitle = mokoString(MR.strings.unified_library_search_all_instances_description),
                        trailingContent = {
                            Switch(
                                checked = unifiedLibrarySearchAllInstances,
                                onCheckedChange = { viewModel.toggleUnifiedLibrarySearchAllInstances() },
                            )
                        },
                        onClick = { viewModel.toggleUnifiedLibrarySearchAllInstances() },
                    ),
                ),
            )
        }
    }

    if (showDiscoverCustomizationSheet) {
        DiscoverSectionCustomizationSheet(
            preferences = discoverSectionPreferences,
            onUpdatePreferences = { viewModel.updateDiscoverSectionPreferences(it) },
            onResetPreferences = { viewModel.resetDiscoverSectionPreferences() },
            onDismissRequest = { showDiscoverCustomizationSheet = false },
        )
    }

    if (showActivityCustomizationSheet) {
        ActivityCardCustomizationSheet(
            useColoredActivityCards = useColoredActivityCards,
            onToggleUseColoredActivityCards = { viewModel.toggleUseColoredActivityCards() },
            onDismissRequest = { showActivityCustomizationSheet = false },
        )
    }

    if (showCalendarCustomizationSheet) {
        CalendarCardCustomizationSheet(
            useColoredCalendarCards = useColoredCalendarCards,
            onToggleUseColoredCalendarCards = { viewModel.toggleUseColoredCalendarCards() },
            onDismissRequest = { showCalendarCustomizationSheet = false },
        )
    }

    if (showSearchResultCustomizationSheet) {
        SearchResultCardCustomizationSheet(
            searchShowBanners = searchShowBanners,
            onToggleSearchShowBanners = { viewModel.toggleSearchShowBanners() },
            onDismissRequest = { showSearchResultCustomizationSheet = false },
        )
    }

    if (showLibraryCustomizationSheet) {
        val selectedInstance by viewModel.selectedCustomizationInstance.collectAsStateWithLifecycle()
        val customizationPreferences by viewModel.selectedCustomizationPreferences.collectAsStateWithLifecycle()
        val arrInstances by viewModel.arrInstances.collectAsStateWithLifecycle()

        ArrViewCustomizationSheet(
            onDismissRequest = { showLibraryCustomizationSheet = false },
            type = selectedInstance?.type ?: InstanceType.Sonarr,
            preferences = customizationPreferences,
            showInstancePicker = true,
            instances = arrInstances,
            selectedInstance = selectedInstance,
            onInstanceSelected = { viewModel.setSelectedCustomizationInstanceId(it.id) },
            onViewTypeChanged = { viewModel.updateCustomizationViewType(it) },
            onShowFullDetailsChanged = { viewModel.updateCustomizationShowFullDetails(it) },
            onShowOverlayChanged = { viewModel.updateCustomizationShowOverlay(it) },
            onShowBannerBackgroundChanged = { viewModel.updateCustomizationShowBannerBackground(it) },
            onIncludeOverviewChanged = { viewModel.updateCustomizationIncludeOverview(it) },
            onBannerBlurChanged = { viewModel.updateCustomizationBannerBlur(it) },
            onGridDensityChanged = { viewModel.updateCustomizationGridDensity(it) },
            onGridSpacingChanged = { viewModel.updateCustomizationGridSpacing(it) },
            onPosterElevationChanged = { viewModel.updateCustomizationPosterElevation(it) },
            onPosterRadiusChanged = { viewModel.updateCustomizationPosterRadius(it) },
            onApplyGloballyChanged = { viewModel.updateCustomizationApplyGlobally(it) },
        )
    }
}
