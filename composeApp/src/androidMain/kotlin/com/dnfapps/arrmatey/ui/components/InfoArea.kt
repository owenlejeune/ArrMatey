package com.dnfapps.arrmatey.ui.components

import com.dnfapps.arrmatey.shared.MR
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.ArtistMonitorType
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.MonitorNewItems
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.entensions.forEachIndexed
import com.dnfapps.arrmatey.model.InfoItem
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.StringResource
import kotlin.time.ExperimentalTime

@Composable
fun InfoArea(
    infoItems: List<InfoItem>,
    modifier: Modifier = Modifier,
    title: StringResource = MR.strings.information,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = mokoString(title),
            style = MaterialTheme.typography.titleLarge
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column (
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                header?.invoke()
                if (header != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                infoItems.forEachIndexed { index, (key, value) ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = key, fontSize = 14.sp)
                        Text(
                            text = value,
                            color = MaterialTheme.colorScheme.primary,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            fontSize = 14.sp,
                            modifier = Modifier.widthIn(max = 200.dp)
                        )
                    }
                    if (index < infoItems.size - 1 || footer != null) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
                footer?.invoke()
            }
        }
    }
}