package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.MediaItem
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import dev.icerock.moko.resources.compose.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultCardCustomizationSheet(
    searchShowBanners: Boolean,
    onToggleSearchShowBanners: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        SearchResultCardCustomizationContent(
            searchShowBanners = searchShowBanners,
            onToggleSearchShowBanners = onToggleSearchShowBanners,
        )
    }
}

@Composable
fun SearchResultCardCustomizationContent(
    searchShowBanners: Boolean,
    onToggleSearchShowBanners: () -> Unit,
) {
    val type = InstanceType.Radarr
    val mockCover = type.mockCover

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = navigationBarBottomInset() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = mokoString(MR.strings.search_result_cards),
            style = MaterialTheme.typography.titleLarge,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            MediaItem(
                aspectRatio = type.aspectRatio,
                item = MockMedia.Radarr,
                onItemClick = {},
                showBannerBackground = searchShowBanners,
                includeOverview = true,
                posterModel = mockCover?.let { painterResource(it) },
                bannerModel = mockCover?.let { painterResource(it) },
            )
        }

        LabelledSwitch(
            label = mokoString(MR.strings.search_show_banners),
            sublabel = mokoString(MR.strings.search_show_banners_description),
            checked = searchShowBanners,
            onCheckedChange = { onToggleSearchShowBanners() },
        )
    }
}

@Preview
@Composable
private fun Preview_SearchResultCardCustomizationSheet() {
    MaterialTheme {
        Surface {
            SearchResultCardCustomizationContent(
                searchShowBanners = true,
                onToggleSearchShowBanners = {},
            )
        }
    }
}
