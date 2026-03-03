package com.dnfapps.arrmatey.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Try
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.arr.viewmodel.InstancesViewModel
import com.dnfapps.arrmatey.entensions.collectAsLazyPagingItems
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.SeerrScreen
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.MediaStatus
import com.dnfapps.arrmatey.seerr.api.model.RequestStatus
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.seerr.api.model.UserPermission
import com.dnfapps.arrmatey.seerr.state.RequestOperationsState
import com.dnfapps.arrmatey.seerr.viewmodel.RequestsViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.BannerView
import com.dnfapps.arrmatey.ui.components.ConfirmableButton
import com.dnfapps.arrmatey.ui.components.MediaRequestTypeChip
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.theme.TranslucentBlack
import com.dnfapps.arrmatey.ui.theme.inverseOnSurfaceLight
import com.dnfapps.arrmatey.ui.theme.inverseSurfaceLight
import com.dnfapps.arrmatey.ui.theme.onPrimaryDark
import com.dnfapps.arrmatey.ui.theme.primaryDark
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RequestsScreen(
    viewModel: RequestsViewModel,
    instancesViewModel: InstancesViewModel = koinInjectParams(InstanceType.Seerr),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<SeerrScreen> = navigationManager.requests()
) {
    val instancesState by instancesViewModel.instancesState.collectAsStateWithLifecycle()
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val requestsPagingState = viewModel.requestsState.collectAsLazyPagingItems(
        onLoadMore = { viewModel.loadNextPage() },
        onRefresh = { viewModel.refresh() }
    )
    val requestOperationsState by viewModel.operationsState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Requests") },
                navigationIcon = { NavigationDrawerButton() }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = requestsPagingState.isLoading,
            onRefresh = { requestsPagingState.refresh() },
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (instancesState.selectedInstance == null) {
                NoInstanceView(InstanceType.Seerr)
            } else {
                when {
                    requestsPagingState.isLoading && requestsPagingState.itemCount == 0 -> {
                        LoadingIndicator(
                            modifier = Modifier.size(96.dp)
                        )
                    }

                    requestsPagingState.isEmpty -> {
                        EmptyState(
                            message = mokoString(MR.strings.no_requests_found)
                        )
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                count = requestsPagingState.itemCount,
                                key = { index -> requestsPagingState.peek(index)?.request?.id ?: index }
                            ) { index ->
                                requestsPagingState[index]?.let { rPackage ->
                                    RequestCard(
                                        mediaPackage = rPackage,
                                        user = userState,
                                        requestOperationsState = requestOperationsState,
                                        onApproveClicked = { viewModel.approveRequest(rPackage.request.id) },
                                        onDeclineClicked = { viewModel.declineRequest(rPackage.request.id) },
                                        onEditClicked = { },
                                        onDeleteClicked = { viewModel.cancelRequest(rPackage.request.id) },
                                        onRemoveFromServiceClicked = { viewModel.deleteMediaFile(rPackage.request) },
                                        onClick = {
                                            navigation.navigateTo(SeerrScreen.Details(
                                                tmdbId = rPackage.request.media.tmdbId,
                                                requestType = rPackage.request.type
                                            ))
                                        }
                                    )
                                }
                            }

                            if (requestsPagingState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }

                requestsPagingState.error?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = error,
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                TextButton(onClick = { requestsPagingState.retry() }) {
                                    Text(mokoString(MR.strings.retry))
                                }
                                IconButton(onClick = { viewModel.clearError() }) {
                                    Icon(Icons.Default.Close, "Dismiss")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun RequestCard(
    mediaPackage: MediaRequestPackage,
    user: SeerrUser?,
    requestOperationsState: RequestOperationsState,
    onApproveClicked: () -> Unit,
    onDeclineClicked: () -> Unit,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onRemoveFromServiceClicked: () -> Unit,
    onClick: () -> Unit,
) {
    val request = mediaPackage.request
    val details = mediaPackage.details

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = inverseSurfaceLight,
            contentColor = inverseOnSurfaceLight
        ),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            BannerView(
                bannerUrl = details?.fullBackdropPath,
                modifier = Modifier.matchParentSize()
            )
            Box(modifier = Modifier.matchParentSize().background(TranslucentBlack))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(12.dp).fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    val posterUrl = details?.fullPosterPath
                    AsyncImage(
                        model = rememberRemoteImageData(posterUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .height(100.dp)
                            .aspectRatio(AspectRatio.Poster.ratio, true)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )

                    Column(modifier = Modifier.defaultMinSize(minHeight = 100.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = details?.displayDate?.year?.toString() ?: "",
                                style = MaterialTheme.typography.labelMedium
                            )
                            MediaRequestTypeChip(text = request.type.name, request.type)
                        }
                        Text(
                            text = details?.displayTitle ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatusChip(request)

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = buildAnnotatedString {
                                            append("Requested by ")
                                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append(request.requestedBy.displayName)
                                            }
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    AsyncImage(
                                        model = rememberRemoteImageData(request.requestedBy.avatar),
                                        modifier = Modifier.size(18.dp).clip(CircleShape),
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Text(
                                    text = request.createdAt.format("HH:mm, MMM d, yyyy"),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                request.modifiedBy?.let { modifiedBy ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = buildAnnotatedString {
                                                append(mokoString(MR.strings.modified_by))
                                                append(" ")
                                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                                    append(modifiedBy.displayName)
                                                }
                                            },
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        AsyncImage(
                                            model = rememberRemoteImageData(modifiedBy.avatar),
                                            modifier = Modifier.size(18.dp).clip(CircleShape),
                                            contentDescription = null,
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    Text(
                                        text = request.updatedAt.format("HH:mm, MMM d, yyyy"),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                (details as? TvDetails)?.let { tvDetails ->
                    Text(
                        text = mokoPlural(MR.plurals.seasons, tvDetails.numberOfSeasons),
                        style = MaterialTheme.typography.labelSmall
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        tvDetails.seasons.forEach {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                contentColor = MaterialTheme.colorScheme.surfaceVariant
                            ) { Text(it.seasonNumber.toString()) }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                val isAdmin = user?.hasPermission(UserPermission.ADMIN) == true
                RequestButtons(
                    isAdmin = isAdmin,
                    request = request,
                    operationsState = requestOperationsState,
                    onApproveClicked = onApproveClicked,
                    onDeclineClicked = onDeclineClicked,
                    onEditClicked = onEditClicked,
                    onDeleteClicked = onDeleteClicked,
                    onRemoveFromServiceClicked = onRemoveFromServiceClicked
                )
            }
        }
    }
}

@Composable
private fun StatusChip(request: MediaRequest) {
    val mediaStatus = MediaStatus.fromValue(request.media.status)
    val requestStatus = RequestStatus.fromValue(request.status)

    // Priority Logic: If media is Processing, Available, or Deleted, show that.
    // Otherwise, show the Request status (Pending/Approved/Declined).
    val (label, container, content) = when {
        mediaStatus == MediaStatus.Deleted ->
            Triple(mediaStatus.resource, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)

        mediaStatus == MediaStatus.Available ->
            Triple(mediaStatus.resource, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)

        mediaStatus == MediaStatus.PartiallyAvailable ->
            Triple(mediaStatus.resource, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)

        mediaStatus == MediaStatus.Processing ->
            Triple(mediaStatus.resource, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)

        requestStatus == RequestStatus.Declined ->
            Triple(requestStatus.resource, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)

        requestStatus == RequestStatus.Approved ->
            Triple(requestStatus.resource, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)

        else -> // Default to Pending
            Triple(requestStatus.resource, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
    }

    AssistChip(
        onClick = { },
        label = { Text(mokoString(label)) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = container,
            labelColor = content
        ),
        border = null
    )
}

@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inbox,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RequestButtons(
    isAdmin: Boolean,
    request: MediaRequest,
    operationsState: RequestOperationsState,
    onApproveClicked: () -> Unit,
    onDeclineClicked: () -> Unit,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onRemoveFromServiceClicked: () -> Unit
) {
    var showDeclineConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRemoveConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(showDeclineConfirm) {
        if (showDeclineConfirm) {
            delay(3000)
            showDeclineConfirm = false
        }
    }

    LaunchedEffect(showDeleteConfirm) {
        if (showDeleteConfirm) {
            delay(3000)
            showDeleteConfirm = false
        }
    }

    LaunchedEffect(showRemoveConfirm) {
        if (showRemoveConfirm) {
            delay(3000)
            showRemoveConfirm = false
        }
    }

    val approveColours = ButtonDefaults.buttonColors(
        containerColor = primaryDark,
        contentColor = onPrimaryDark
    )
    val declineColours = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
    )
    val editColours = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary
    )

    val isApproved = RequestStatus.fromValue(request.status) == RequestStatus.Approved
    val isDeclined = RequestStatus.fromValue(request.status) == RequestStatus.Declined

    Column {
        if (!isApproved && !isDeclined) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (isAdmin) {
                    Button(
                        onClick = onApproveClicked,
                        modifier = Modifier.weight(1f),
                        colors = approveColours,
                        enabled = operationsState.approvalStates.none { it.key == request.id }
                    ) {
                        if (operationsState.approvalStates.any { it.key == request.id }) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Check, null)
                            Text(mokoString(MR.strings.approve))
                        }
                    }
                }
                ConfirmableButton(
                    isConfirming = showDeclineConfirm,
                    onClick = {
                        if (showDeclineConfirm) {
                            onDeclineClicked()
                            showDeclineConfirm = false
                        } else {
                            showDeclineConfirm = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = declineColours,
                    enabled = operationsState.cancelStates.none { it.key == request.id },
                    content = {
                        if (operationsState.cancelStates.any { it.key == request.id }) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onError
                            )
                        } else {
                            Icon(Icons.Default.Close, null)
                            Spacer(Modifier.width(4.dp))
                            Text(if (isAdmin) mokoString(MR.strings.decline) else mokoString(MR.strings.cancel_request))
                        }
                    }
                )
            }

            if (isAdmin || request.type == RequestType.Tv) {
                Button(
                    onClick = onEditClicked,
                    modifier = Modifier.fillMaxWidth(),
                    colors = editColours
                ) {
                    Icon(Icons.Default.Edit, null)
                    Text("Edit")
                }
            }
        }

        if (isAdmin && (isApproved || isDeclined)) {
            ConfirmableButton(
                isConfirming = showDeleteConfirm,
                onClick = {
                    if (showDeleteConfirm) {
                        onDeleteClicked()
                        showDeleteConfirm = false
                    } else {
                        showDeleteConfirm = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = declineColours,
                content = {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.width(4.dp))
                    Text(mokoString(MR.strings.delete_request))
                }
            )

            if (isApproved) {
                ConfirmableButton(
                    isConfirming = showRemoveConfirm,
                    onClick = {
                        if (showRemoveConfirm) {
                            onRemoveFromServiceClicked()
                            showRemoveConfirm = false
                        } else {
                            showRemoveConfirm = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = declineColours,
                    content = {
                        Icon(Icons.Default.Delete, null)
                        Spacer(Modifier.width(4.dp))
                        Text(mokoString(MR.strings.remove_from_service, "[SERVICE NAME]"))
                    }
                )
            }
        }
    }
}
