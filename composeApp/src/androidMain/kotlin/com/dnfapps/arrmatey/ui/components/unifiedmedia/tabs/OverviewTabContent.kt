package com.dnfapps.arrmatey.ui.components.unifiedmedia.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.InfoArea
import com.dnfapps.arrmatey.ui.components.InfoCardData
import com.dnfapps.arrmatey.ui.components.InfoCardInstanceFooter
import com.dnfapps.arrmatey.ui.components.ItemDescriptionCard
import com.dnfapps.arrmatey.ui.components.SeerrCreditsSection
import com.dnfapps.arrmatey.ui.components.buildArrInfoItems
import com.dnfapps.arrmatey.ui.components.buildSeerrInfoItems
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun OverviewTabContent(
    state: UnifiedMediaDetailsUiState.Success,
    qualityProfiles: List<QualityProfile>,
    tags: List<Tag>,
    activeInstance: Instance?,
    activeSeerrInstance: Instance?,
    isExpanded: Boolean,
    isDualPanel: Boolean,
    onEditPath: () -> Unit,
    onPersonClick: (Long) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        state.overview?.takeIf { it.isNotBlank() }?.let { overview ->
            ItemDescriptionCard(
                overview = overview,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }

        state.seerrMedia?.credits?.let { credits ->
            SeerrCreditsSection(credits) { onPersonClick(it) }
        }

        val arrInfoItems = buildArrInfoItems(state, qualityProfiles, tags, onEditPath = onEditPath)
        val seerrInfoItems = buildSeerrInfoItems(state)
        val showBothCards = arrInfoItems.isNotEmpty() && seerrInfoItems.isNotEmpty()

        val selectedArrInstance =
            state.availableInstances.firstOrNull { it.id == state.selectedInstanceId } ?: activeInstance
        val selectedSeerrInstance = activeSeerrInstance

        if (arrInfoItems.isNotEmpty() || seerrInfoItems.isNotEmpty() || state.keywords.isNotEmpty()) {
            InfoArea(
                cards =
                    listOf(
                        InfoCardData(
                            items = arrInfoItems,
                            footer =
                                if (showBothCards && selectedArrInstance != null) {
                                    { InfoCardInstanceFooter(selectedArrInstance) }
                                } else {
                                    null
                                },
                        ),
                        InfoCardData(
                            items = seerrInfoItems,
                            content =
                                if (state.keywords.isNotEmpty()) {
                                    {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Text(
                                                text = mokoString(MR.strings.tags),
                                                fontSize = 14.sp,
                                            )
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                state.keywords.forEach { keyword ->
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                                    ) {
                                                        Text(
                                                            text = keyword.name,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                            fontWeight = FontWeight.SemiBold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    null
                                },
                            footer =
                                if (showBothCards && selectedSeerrInstance != null) {
                                    { InfoCardInstanceFooter(selectedSeerrInstance) }
                                } else {
                                    null
                                },
                        ),
                    ),
                modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                useDualColumn = isExpanded && !isDualPanel,
            )
        }
    }
}
