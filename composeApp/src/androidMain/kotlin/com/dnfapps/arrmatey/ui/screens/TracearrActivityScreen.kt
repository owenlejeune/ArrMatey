package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.androidModule
import com.dnfapps.arrmatey.di.appModules
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrActivityByDoW
import com.dnfapps.arrmatey.tracearr.api.model.TracearrActivityByHoD
import com.dnfapps.arrmatey.tracearr.api.model.TracearrActivityConcurrent
import com.dnfapps.arrmatey.tracearr.api.model.TracearrActivityPlatform
import com.dnfapps.arrmatey.tracearr.api.model.TracearrActivityPlay
import com.dnfapps.arrmatey.tracearr.api.model.TracearrActivityQuality
import com.dnfapps.arrmatey.tracearr.api.model.TracearrActivityRange
import com.dnfapps.arrmatey.tracearr.api.model.TracearrActivityResponse
import com.dnfapps.arrmatey.tracearr.api.model.TracearrPeriod
import com.dnfapps.arrmatey.tracearr.state.TracearrActivityState
import com.dnfapps.arrmatey.tracearr.viewmodel.TracearrActivityViewModel
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.theme.ArrBlue
import com.dnfapps.arrmatey.ui.theme.ArrMateyTheme
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.ArrYellow
import com.dnfapps.arrmatey.ui.theme.TracearrBlue
import com.dnfapps.arrmatey.ui.theme.TracearrDarkBlue
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.MarkerCornerBasedShape
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.PieSize
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.pieModel
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import org.koin.android.ext.koin.androidContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TracearrActivityScreen(
    onNavigateBack: () -> Unit,
    isLargeScreen: Boolean = false,
    viewModel: TracearrActivityViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()

    TracearrActivityContent(
        state = state,
        isRefreshing = isRefreshing,
        selectedPeriod = selectedPeriod,
        isLargeScreen = isLargeScreen,
        onPeriodSelected = { viewModel.setPeriod(it) },
        onRefresh = { viewModel.refresh() },
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TracearrActivityContent(
    state: TracearrActivityState,
    isRefreshing: Boolean,
    selectedPeriod: TracearrPeriod,
    isLargeScreen: Boolean,
    onPeriodSelected: (TracearrPeriod) -> Unit,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDualColumn by remember(isLargeScreen) { mutableStateOf(isLargeScreen) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(mokoString(MR.strings.activity)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = mokoString(MR.strings.back),
                        )
                    }
                },
                actions = {
                    if (isLargeScreen) {
                        IconButton(onClick = { isDualColumn = !isDualColumn }) {
                            Icon(
                                imageVector =
                                    if (isDualColumn) {
                                        Icons.AutoMirrored.Filled.List
                                    } else {
                                        Icons.Default.GridView
                                    },
                                contentDescription =
                                    if (isDualColumn) {
                                        "Single column"
                                    } else {
                                        "Dual column"
                                    },
                            )
                        }
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
        ) {
            when (state) {
                is TracearrActivityState.Initial,
                is TracearrActivityState.Loading,
                -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator(modifier = Modifier.size(96.dp))
                    }
                }
                is TracearrActivityState.NoInstance -> {
                    NoInstanceView(
                        type = InstanceType.Tracearr,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .wrapContentSize(),
                    )
                }
                is TracearrActivityState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Button(onClick = onRefresh) {
                                Text(mokoString(MR.strings.retry))
                            }
                        }
                    }
                }
                is TracearrActivityState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        PeriodSelector(
                            selectedPeriod = selectedPeriod,
                            onPeriodSelected = onPeriodSelected,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        if (isDualColumn) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding =
                                    PaddingValues(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 16.dp,
                                        bottom = 16.dp + if (LocalFloatingBarBottomPadding.current > 0.dp) LocalFloatingBarBottomPadding.current else navigationBarBottomInset(),
                                    ),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                item {
                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(IntrinsicSize.Max),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    ) {
                                        PlaysOverTimeCard(
                                            plays = state.response.plays,
                                            modifier =
                                                Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                        )
                                        ConcurrentStreamsCard(
                                            concurrent = state.response.concurrent,
                                            modifier =
                                                Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                        )
                                    }
                                }
                                item {
                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(IntrinsicSize.Max),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    ) {
                                        ActivityByDayOfWeekCard(
                                            byDay = state.response.byDayOfWeek,
                                            modifier =
                                                Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                        )
                                        ActivityByHourOfDayCard(
                                            byHour = state.response.byHourOfDay,
                                            modifier =
                                                Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                        )
                                    }
                                }
                                item {
                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(IntrinsicSize.Max),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    ) {
                                        PlatformsCard(
                                            platforms = state.response.platforms,
                                            modifier =
                                                Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                        )
                                        StreamQualityCard(
                                            quality = state.response.quality,
                                            modifier =
                                                Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding =
                                    PaddingValues(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 16.dp,
                                        bottom = 16.dp + if (LocalFloatingBarBottomPadding.current > 0.dp) LocalFloatingBarBottomPadding.current else navigationBarBottomInset(),
                                    ),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                item { PlaysOverTimeCard(plays = state.response.plays) }
                                item { ConcurrentStreamsCard(concurrent = state.response.concurrent) }
                                item { ActivityByDayOfWeekCard(byDay = state.response.byDayOfWeek) }
                                item { ActivityByHourOfDayCard(byHour = state.response.byHourOfDay) }
                                item { PlatformsCard(platforms = state.response.platforms) }
                                item { StreamQualityCard(quality = state.response.quality) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: TracearrPeriod,
    onPeriodSelected: (TracearrPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val periods = listOf(TracearrPeriod.Week, TracearrPeriod.Month, TracearrPeriod.Year)
    val selectedIndex = periods.indexOf(selectedPeriod).coerceAtLeast(0)

    PrimaryTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier,
    ) {
        periods.forEachIndexed { index, period ->
            val label =
                when (period) {
                    TracearrPeriod.Week -> mokoString(MR.strings.week)
                    TracearrPeriod.Month -> mokoString(MR.strings.month)
                    TracearrPeriod.Year -> mokoString(MR.strings.year)
                }
            Tab(
                selected = index == selectedIndex,
                onClick = { onPeriodSelected(period) },
                text = {
                    Text(
                        text = label,
                        fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                    )
                },
            )
        }
    }
}

@Composable
private fun rememberMarker(): CartesianMarker {
    val labelBackground =
        rememberShapeComponent(
            fill = Fill(MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = MarkerCornerBasedShape(base = RoundedCornerShape(8.dp)),
        )
    val label =
        rememberTextComponent(
            style =
                TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            padding = Insets(8.dp, 4.dp),
            background = labelBackground,
        )
    return rememberDefaultCartesianMarker(
        label = label,
        guideline = rememberAxisGuidelineComponent(),
    )
}

private fun formatChartDateLabel(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return ""
    val cleanDate = dateStr.split(" ").firstOrNull() ?: dateStr
    val parts = cleanDate.split("-")
    if (parts.size >= 3) {
        val monthNum = parts[1].toIntOrNull() ?: return cleanDate
        val dayNum = parts[2].toIntOrNull() ?: return cleanDate
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val monthName = monthNames.getOrNull(monthNum - 1) ?: monthNum.toString()
        return "$monthName $dayNum"
    }
    return dateStr
}

@Composable
private fun PlaysOverTimeCard(
    plays: List<TracearrActivityPlay>,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    val unknownString = mokoString(MR.strings.unknown)
    val serverNames =
        remember(plays, unknownString) {
            plays.mapNotNull { it.serverId }.distinct().ifEmpty {
                if (plays.isNotEmpty()) listOf(unknownString) else emptyList()
            }
        }

    val dates =
        remember(plays) {
            plays.mapNotNull { it.date }.distinct()
        }
    val dateLabels =
        remember(dates) {
            dates.map { formatChartDateLabel(it) }
        }

    val serverSeriesData =
        remember(plays, serverNames, dates, unknownString) {
            if (plays.isEmpty()) return@remember emptyList()
            if (dates.isNotEmpty()) {
                serverNames.map { server ->
                    dates.map { d ->
                        plays.firstOrNull { (it.serverId ?: unknownString) == server && it.date == d }?.count?.toDouble() ?: 0.0
                    }
                }
            } else {
                val grouped = plays.groupBy { it.serverId ?: unknownString }
                serverNames.map { server ->
                    grouped[server]?.map { it.count.toDouble() } ?: listOf(0.0)
                }
            }
        }

    val palette =
        listOf(
            ArrOrange,
            Color(0xFF00BCD4),
            Color(0xFF4CAF50),
            Color(0xFF9C27B0),
            Color(0xFF2196F3),
            Color(0xFFE91E63),
            Color(0xFFFF5722),
            Color(0xFF009688),
        )

    val serverColors =
        remember(serverNames) {
            serverNames.mapIndexed { index, _ -> palette[index % palette.size] }
        }

    LaunchedEffect(serverSeriesData) {
        modelProducer.runTransaction {
            if (serverSeriesData.isNotEmpty()) {
                lineModel {
                    serverSeriesData.forEach { counts ->
                        series(counts)
                    }
                }
            } else {
                lineModel { series(0.0) }
            }
        }
    }

    val defaultLine =
        LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(Fill(ArrOrange)),
        )
    val lineLines =
        serverColors.map { color ->
            LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(Fill(color)),
            )
        }

    val lineProvider =
        remember(lineLines, defaultLine) {
            if (lineLines.isNotEmpty()) {
                LineCartesianLayer.LineProvider.series(lineLines)
            } else {
                LineCartesianLayer.LineProvider.series(defaultLine)
            }
        }

    ChartCardContainer(
        title = mokoString(MR.strings.plays_over_time),
        modifier = modifier,
    ) {
        if (plays.isEmpty()) {
            EmptyChartPlaceholder()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CartesianChartHost(
                    chart =
                        rememberCartesianChart(
                            rememberLineCartesianLayer(lineProvider = lineProvider),
                            startAxis = VerticalAxis.rememberStart(),
                            bottomAxis =
                                HorizontalAxis.rememberBottom(
                                    itemPlacer =
                                        remember(dateLabels.size) {
                                            HorizontalAxis.ItemPlacer.aligned(
                                                spacing = {
                                                    if (dateLabels.size > 20) {
                                                        3
                                                    } else if (dateLabels.size > 10) {
                                                        2
                                                    } else {
                                                        1
                                                    }
                                                },
                                            )
                                        },
                                    labelRotationDegrees = if (dateLabels.size > 7) 45f else 0f,
                                    valueFormatter =
                                        CartesianValueFormatter { _, x, _ ->
                                            if (dateLabels.isNotEmpty()) {
                                                val index = x.toInt().coerceIn(0, dateLabels.lastIndex)
                                                dateLabels[index]
                                            } else {
                                                (x.toInt() + 1).toString()
                                            }
                                        },
                                ),
                            marker = rememberMarker(),
                        ),
                    modelProducer = modelProducer,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                )

                if (serverNames.isNotEmpty()) {
                    LegendRow(items = serverNames.mapIndexed { index, name -> name to serverColors[index] })
                }
            }
        }
    }
}

@Composable
private fun ConcurrentStreamsCard(
    concurrent: List<TracearrActivityConcurrent>,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    val dates =
        remember(concurrent) {
            concurrent.mapNotNull { it.date }.distinct()
        }
    val dateLabels =
        remember(dates) {
            dates.map { formatChartDateLabel(it) }
        }

    LaunchedEffect(concurrent) {
        modelProducer.runTransaction {
            val directList = concurrent.map { it.direct.toDouble() }
            val directStreamList = concurrent.map { it.directStream.toDouble() }
            val transcodeList = concurrent.map { it.transcode.toDouble() }

            if (concurrent.isNotEmpty()) {
                lineModel {
                    series(directList)
                    series(directStreamList)
                    series(transcodeList)
                }
            } else {
                lineModel { series(0.0) }
            }
        }
    }

    val lineLines =
        listOf(
            LineCartesianLayer.rememberLine(fill = LineCartesianLayer.LineFill.single(Fill(TracearrBlue))),
            LineCartesianLayer.rememberLine(fill = LineCartesianLayer.LineFill.single(Fill(TracearrDarkBlue))),
            LineCartesianLayer.rememberLine(fill = LineCartesianLayer.LineFill.single(Fill(ArrOrange))),
        )
    val lineProvider =
        remember(lineLines) {
            LineCartesianLayer.LineProvider.series(lineLines)
        }

    ChartCardContainer(
        title = mokoString(MR.strings.concurrent_streams),
        modifier = modifier,
    ) {
        if (concurrent.isEmpty()) {
            EmptyChartPlaceholder()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CartesianChartHost(
                    chart =
                        rememberCartesianChart(
                            rememberLineCartesianLayer(lineProvider = lineProvider),
                            startAxis = VerticalAxis.rememberStart(),
                            bottomAxis =
                                HorizontalAxis.rememberBottom(
                                    itemPlacer =
                                        remember(dateLabels.size) {
                                            HorizontalAxis.ItemPlacer.aligned(
                                                spacing = {
                                                    if (dateLabels.size > 20) {
                                                        3
                                                    } else if (dateLabels.size > 10) {
                                                        2
                                                    } else {
                                                        1
                                                    }
                                                },
                                            )
                                        },
                                    labelRotationDegrees = if (dateLabels.size > 7) 45f else 0f,
                                    valueFormatter =
                                        CartesianValueFormatter { _, x, _ ->
                                            if (dateLabels.isNotEmpty()) {
                                                val index = x.toInt().coerceIn(0, dateLabels.lastIndex)
                                                dateLabels[index]
                                            } else {
                                                (x.toInt() + 1).toString()
                                            }
                                        },
                                ),
                            marker = rememberMarker(),
                        ),
                    modelProducer = modelProducer,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                )

                LegendRow(
                    items =
                        listOf(
                            mokoString(MR.strings.direct_play) to TracearrBlue,
                            mokoString(MR.strings.direct_stream) to TracearrDarkBlue,
                            mokoString(MR.strings.transcode) to ArrOrange,
                        ),
                )
            }
        }
    }
}

@Composable
private fun ActivityByDayOfWeekCard(
    byDay: List<TracearrActivityByDoW>,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    val daysOrder = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val dayCounts =
        remember(byDay) {
            val map = byDay.associate { (it.name ?: "") to it.count }
            daysOrder.map { (map[it] ?: 0).toDouble() }
        }

    LaunchedEffect(dayCounts) {
        modelProducer.runTransaction {
            columnModel { series(dayCounts) }
        }
    }

    ChartCardContainer(
        title = mokoString(MR.strings.activity_by_day_of_week),
        modifier = modifier,
    ) {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        columnProvider =
                            ColumnCartesianLayer.ColumnProvider.series(
                                rememberLineComponent(fill = Fill(ArrBlue), 16.dp),
                            ),
                    ),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis =
                        HorizontalAxis.rememberBottom(
                            valueFormatter =
                                CartesianValueFormatter { _, x, _ ->
                                    val index = x.toInt().coerceIn(0, daysOrder.lastIndex)
                                    daysOrder[index]
                                },
                        ),
                    marker = rememberMarker(),
                ),
            modelProducer = modelProducer,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(200.dp),
        )
    }
}

@Composable
private fun ActivityByHourOfDayCard(
    byHour: List<TracearrActivityByHoD>,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    val hourCounts =
        remember(byHour) {
            val map = byHour.associate { it.hour to it.count }
            (0..23).map { (map[it] ?: 0).toDouble() }
        }

    LaunchedEffect(hourCounts) {
        modelProducer.runTransaction {
            columnModel { series(hourCounts) }
        }
    }

    ChartCardContainer(
        title = mokoString(MR.strings.activity_by_hour_of_day),
        modifier = modifier,
    ) {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        columnProvider =
                            ColumnCartesianLayer.ColumnProvider.series(
                                rememberLineComponent(fill = Fill(ArrBlue), 16.dp),
                            ),
                    ),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis =
                        HorizontalAxis.rememberBottom(
                            itemPlacer = remember { HorizontalAxis.ItemPlacer.aligned(spacing = { 2 }) },
                            labelRotationDegrees = 45f,
                            valueFormatter =
                                CartesianValueFormatter { _, x, _ ->
                                    val hr = (x.toInt() % 24 + 24) % 24
                                    when (hr) {
                                        0 -> "12am"
                                        12 -> "12pm"
                                        in 1..11 -> "${hr}am"
                                        else -> "${hr - 12}pm"
                                    }
                                },
                        ),
                    marker = rememberMarker(),
                ),
            modelProducer = modelProducer,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(200.dp),
        )
    }
}

@Composable
private fun PlatformsCard(
    platforms: List<TracearrActivityPlatform>,
    modifier: Modifier = Modifier,
) {
    val palette =
        listOf(
            Color(0xFF00BCD4),
            Color(0xFF4CAF50),
            Color(0xFF9C27B0),
            Color(0xFFFF9800),
            Color(0xFFE91E63),
            Color(0xFF2196F3),
            Color(0xFFFF5722),
            Color(0xFF009688),
            Color(0xFF3F51B5),
            Color(0xFFFFC107),
        )

    val unknownString = mokoString(MR.strings.unknown)
    val items =
        remember(platforms, unknownString) {
            platforms.filter { it.count > 0 }.mapIndexed { index, p ->
                (p.platform ?: unknownString) to (p.count.toFloat() to palette[index % palette.size])
            }
        }

    val modelProducer = remember { PieChartModelProducer() }

    LaunchedEffect(items) {
        modelProducer.runTransaction {
            if (items.isNotEmpty()) {
                pieModel {
                    series(items.map { it.second.first })
                }
            }
        }
    }

    val sliceProvider =
        remember(items) {
            val slices =
                items.map { PieChart.Slice(fill = Fill(it.second.second)) }.ifEmpty {
                    listOf(PieChart.Slice(fill = Fill(Color.Transparent)))
                }
            PieChart.SliceProvider.series(slices)
        }

    val chart =
        rememberPieChart(
            sliceProvider = sliceProvider,
            innerSize = PieSize.Inner.fixed(65.dp),
            outerSize = PieSize.Outer.fixed(115.dp),
        )

    ChartCardContainer(
        title = mokoString(MR.strings.platforms),
        modifier = modifier,
    ) {
        if (items.isEmpty()) {
            EmptyChartPlaceholder()
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    PieChartHost(
                        chart = chart,
                        modelProducer = modelProducer,
                        modifier = Modifier.size(240.dp),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LegendRow(items = items.map { it.first to it.second.second })
            }
        }
    }
}

@Composable
private fun StreamQualityCard(
    quality: TracearrActivityQuality,
    modifier: Modifier = Modifier,
) {
    val directPlayLabel = mokoString(MR.strings.direct_play)
    val directStreamLabel = mokoString(MR.strings.direct_stream)
    val transcodeLabel = mokoString(MR.strings.transcode)

    val items =
        remember(quality, directPlayLabel, directStreamLabel, transcodeLabel) {
            listOfNotNull(
                if (quality.directPlay > 0) directPlayLabel to (quality.directPlay.toFloat() to Color(0xFF4CAF50)) else null,
                if (quality.directStream > 0) directStreamLabel to (quality.directStream.toFloat() to Color(0xFF2196F3)) else null,
                if (quality.transcode > 0) transcodeLabel to (quality.transcode.toFloat() to ArrYellow) else null,
            )
        }

    val modelProducer = remember { PieChartModelProducer() }

    LaunchedEffect(items) {
        modelProducer.runTransaction {
            if (items.isNotEmpty()) {
                pieModel {
                    series(items.map { it.second.first })
                }
            }
        }
    }

    val sliceProvider =
        remember(items) {
            val slices =
                items.map { PieChart.Slice(fill = Fill(it.second.second)) }.ifEmpty {
                    listOf(PieChart.Slice(fill = Fill(Color.Transparent)))
                }
            PieChart.SliceProvider.series(slices)
        }

    val chart =
        rememberPieChart(
            sliceProvider = sliceProvider,
            innerSize = PieSize.Inner.fixed(65.dp),
            outerSize = PieSize.Outer.fixed(115.dp),
        )

    ChartCardContainer(
        title = mokoString(MR.strings.stream_quality),
        modifier = modifier,
    ) {
        if (items.isEmpty()) {
            EmptyChartPlaceholder()
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    PieChartHost(
                        chart = chart,
                        modelProducer = modelProducer,
                        modifier = Modifier.size(240.dp),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LegendRow(items = items.map { it.first to it.second.second })
            }
        }
    }
}

@Composable
private fun ChartCardContainer(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            content()
        }
    }
}

@Composable
private fun EmptyChartPlaceholder() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(160.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = mokoString(MR.strings.no_results_found),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LegendRow(
    items: List<Pair<String, Color>>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { (label, color) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
fun TracearrActivityScreenPreview() {
    val mockPlays =
        listOf(
            TracearrActivityPlay(count = 5, serverId = "openmediavault", date = "2026-09-01"),
            TracearrActivityPlay(count = 8, serverId = "openmediavault", date = "2026-09-02"),
            TracearrActivityPlay(count = 12, serverId = "openmediavault", date = "2026-09-03"),
            TracearrActivityPlay(count = 18, serverId = "openmediavault", date = "2026-09-04"),
            TracearrActivityPlay(count = 10, serverId = "openmediavault", date = "2026-09-05"),
            TracearrActivityPlay(count = 15, serverId = "openmediavault", date = "2026-09-06"),
            TracearrActivityPlay(count = 3, serverId = "openmediavault", date = "2026-09-07"),
            TracearrActivityPlay(count = 2, serverId = "synology", date = "2026-09-01"),
            TracearrActivityPlay(count = 4, serverId = "synology", date = "2026-09-02"),
            TracearrActivityPlay(count = 7, serverId = "synology", date = "2026-09-03"),
            TracearrActivityPlay(count = 10, serverId = "synology", date = "2026-09-04"),
            TracearrActivityPlay(count = 6, serverId = "synology", date = "2026-09-05"),
            TracearrActivityPlay(count = 9, serverId = "synology", date = "2026-09-06"),
            TracearrActivityPlay(count = 1, serverId = "synology", date = "2026-09-07"),
        )

    val mockConcurrent =
        listOf(
            TracearrActivityConcurrent(direct = 1, directStream = 0, transcode = 1, total = 2),
            TracearrActivityConcurrent(direct = 3, directStream = 0, transcode = 2, total = 5),
            TracearrActivityConcurrent(direct = 2, directStream = 0, transcode = 0, total = 2),
            TracearrActivityConcurrent(direct = 2, directStream = 1, transcode = 1, total = 4),
            TracearrActivityConcurrent(direct = 1, directStream = 0, transcode = 0, total = 1),
        )

    val mockByDay =
        listOf(
            TracearrActivityByDoW(day = 0, name = "Sun", count = 37),
            TracearrActivityByDoW(day = 1, name = "Mon", count = 35),
            TracearrActivityByDoW(day = 2, name = "Tue", count = 43),
            TracearrActivityByDoW(day = 3, name = "Wed", count = 29),
            TracearrActivityByDoW(day = 4, name = "Thu", count = 22),
            TracearrActivityByDoW(day = 5, name = "Fri", count = 28),
            TracearrActivityByDoW(day = 6, name = "Sat", count = 26),
        )

    val mockByHour =
        (0..23).map { hr ->
            TracearrActivityByHoD(
                hour = hr,
                count =
                    when (hr) {
                        1 -> 33
                        2 -> 15
                        12 -> 13
                        13 -> 15
                        14 -> 14
                        20 -> 18
                        21 -> 25
                        22 -> 21
                        else -> 3
                    },
            )
        }

    val mockPlatforms =
        listOf(
            TracearrActivityPlatform(platform = "Android TV", count = 50),
            TracearrActivityPlatform(platform = "Web", count = 15),
            TracearrActivityPlatform(platform = "Fire TV", count = 12),
            TracearrActivityPlatform(platform = "iOS", count = 8),
            TracearrActivityPlatform(platform = "Roku", count = 5),
        )

    val mockQuality =
        TracearrActivityQuality(
            directPlay = 55,
            directStream = 5,
            transcode = 40,
            total = 100,
            directPlayPercent = 55,
            directStreamPercent = 5,
            transcodePercent = 40,
        )

    val context = LocalContext.current
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            androidContext(context)
            modules(appModules() + listOf(androidModule))
        }
    }

    ArrMateyTheme {
        TracearrActivityContent(
            state =
                TracearrActivityState.Success(
                    response =
                        TracearrActivityResponse(
                            period = TracearrPeriod.Month,
                            range =
                                TracearrActivityRange(
                                    start = "2026-08-10 00:00:00",
                                    end = "2026-09-09 00:00:00",
                                ),
                            plays = mockPlays,
                            concurrent = mockConcurrent,
                            byDayOfWeek = mockByDay,
                            byHourOfDay = mockByHour,
                            platforms = mockPlatforms,
                            quality = mockQuality,
                        ),
                ),
            isRefreshing = false,
            selectedPeriod = TracearrPeriod.Month,
            isLargeScreen = false,
            onPeriodSelected = {},
            onRefresh = {},
            onNavigateBack = {},
        )
    }
}
