package com.dnfapps.arrmatey.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.compose.DashboardCardService
import com.dnfapps.arrmatey.compose.DashboardCards
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDashboardCardSheet(
    availableCards: List<DashboardCards>,
    onAddCard: (DashboardCards) -> Unit,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    isCompact: Boolean = true,
    onNavigateToArrDashboard: (Long) -> Unit = {},
    onNavigateToMediaDetails: (Long, InstanceType) -> Unit = { _, _ -> },
    onNavigateToSeerrMediaDetails: (Long, RequestType) -> Unit = { _, _ -> },
    onNavigateToDiscoverTab: () -> Unit = {},
) {
    val groupedCards = remember(availableCards) {
        DashboardCardService.entries.map { service ->
            service to availableCards.filter { it.service == service }
        }
    }

    var expandedServices by remember {
        mutableStateOf(emptySet<DashboardCardService>())
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = mokoString(MR.strings.add_dashboard_cards),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = groupedCards,
                    key = { (service, _) -> service.name },
                ) { (service, cards) ->
                    val isExpanded = service in expandedServices
                    ServiceCollapsibleSection(
                        service = service,
                        cards = cards,
                        isExpanded = isExpanded,
                        isCompact = isCompact,
                        onToggleExpand = {
                            expandedServices = if (isExpanded) {
                                expandedServices - service
                            } else {
                                expandedServices + service
                            }
                        },
                        onAddCard = onAddCard,
                        onNavigateToArrDashboard = onNavigateToArrDashboard,
                        onNavigateToMediaDetails = onNavigateToMediaDetails,
                        onNavigateToSeerrMediaDetails = onNavigateToSeerrMediaDetails,
                        onNavigateToDiscoverTab = onNavigateToDiscoverTab,
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceCollapsibleSection(
    service: DashboardCardService,
    cards: List<DashboardCards>,
    isExpanded: Boolean,
    isCompact: Boolean,
    onToggleExpand: () -> Unit,
    onAddCard: (DashboardCards) -> Unit,
    onNavigateToArrDashboard: (Long) -> Unit,
    onNavigateToMediaDetails: (Long, InstanceType) -> Unit,
    onNavigateToSeerrMediaDetails: (Long, RequestType) -> Unit,
    onNavigateToDiscoverTab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = onToggleExpand)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ServiceIcon(service)

            Text(
                text = mokoString(service.title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = cards.size.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            val rotation by animateFloatAsState(
                targetValue = if (isExpanded) 180f else 0f,
                label = "ServiceSectionChevron",
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.rotate(rotation),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                )

                if (cards.isEmpty()) {
                    Text(
                        text = mokoString(MR.strings.all_cards_added),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        textAlign = TextAlign.Center,
                    )
                } else if (isCompact) {
                    cards.forEach { card ->
                        DashboardCardAddPreview(
                            card = card,
                            onAddCard = onAddCard,
                            onNavigateToArrDashboard = onNavigateToArrDashboard,
                            onNavigateToMediaDetails = onNavigateToMediaDetails,
                            onNavigateToSeerrMediaDetails = onNavigateToSeerrMediaDetails,
                            onNavigateToDiscoverTab = onNavigateToDiscoverTab,
                        )
                    }
                } else {
                    cards.chunked(2).forEach { rowCards ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            rowCards.forEach { card ->
                                Box(modifier = Modifier.weight(1f)) {
                                    DashboardCardAddPreview(
                                        card = card,
                                        onAddCard = onAddCard,
                                        onNavigateToArrDashboard = onNavigateToArrDashboard,
                                        onNavigateToMediaDetails = onNavigateToMediaDetails,
                                        onNavigateToSeerrMediaDetails = onNavigateToSeerrMediaDetails,
                                        onNavigateToDiscoverTab = onNavigateToDiscoverTab,
                                    )
                                }
                            }
                            if (rowCards.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceIcon(
    service: DashboardCardService,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        when (service) {
            DashboardCardService.Arr ->
                Icon(
                    painter = painterResource(MR.images.sonarr_tab),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            DashboardCardService.Seerr ->
                Icon(
                    painter = painterResource(MR.images.seerr_tab),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            DashboardCardService.Prowlarr ->
                Icon(
                    painter = painterResource(MR.images.prowlarr_tab),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            DashboardCardService.Bazarr ->
                Icon(
                    painter = painterResource(MR.images.bazarr_tab),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            DashboardCardService.Playback ->
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            DashboardCardService.DownloadClients ->
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            DashboardCardService.Network ->
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            DashboardCardService.WakeOnLan ->
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
        }
    }
}

@Composable
private fun DashboardCardAddPreview(
    card: DashboardCards,
    onAddCard: (DashboardCards) -> Unit,
    onNavigateToArrDashboard: (Long) -> Unit,
    onNavigateToMediaDetails: (Long, InstanceType) -> Unit,
    onNavigateToSeerrMediaDetails: (Long, RequestType) -> Unit,
    onNavigateToDiscoverTab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { onAddCard(card) },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = mokoString(card.title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                FilledTonalIconButton(
                    onClick = { onAddCard(card) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = mokoString(MR.strings.add),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            DashboardCardContent(
                enabled = false,
                cardType = card,
                currentState = CombinedDashboardState.Mock,
                isEditing = false,
                onNavigateToArrDashboard = onNavigateToArrDashboard,
                onNavigateToMediaDetails = onNavigateToMediaDetails,
                onNavigateToSeerrMediaDetails = onNavigateToSeerrMediaDetails,
                onNavigateToDiscoverTab = onNavigateToDiscoverTab,
                onSendWol = {},
            )
        }
    }
}
