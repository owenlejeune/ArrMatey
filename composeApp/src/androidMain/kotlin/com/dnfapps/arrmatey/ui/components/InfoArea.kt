package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.Image
import com.dnfapps.arrmatey.shared.MR
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.entensions.forEachIndexed
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.model.InfoItem
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.painterResource

import androidx.compose.ui.text.font.FontWeight
import com.dnfapps.arrmatey.instances.model.Instance

data class InfoCardData(
    val items: List<InfoItem>,
    val header: (@Composable () -> Unit)? = null,
    val footer: (@Composable () -> Unit)? = null
)

@Composable
fun InfoCardInstanceFooter(
    instance: Instance,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(instance.type.icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = instance.label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InfoAreaCard(
    infoItems: List<InfoItem>,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
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

@Composable
fun InfoArea(
    infoItems: List<InfoItem>,
    modifier: Modifier = Modifier,
    title: StringResource? = MR.strings.information,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
) {
    if (infoItems.isEmpty()) return
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (title != null) {
            Text(
                text = mokoString(title),
                style = MaterialTheme.typography.titleLarge
            )
        }
        InfoAreaCard(
            infoItems = infoItems,
            header = header,
            footer = footer
        )
    }
}

@Composable
fun InfoArea(
    cards: List<InfoCardData>,
    modifier: Modifier = Modifier,
    title: StringResource? = MR.strings.information,
) {
    val validCards = cards.filter { it.items.isNotEmpty() }
    if (validCards.isEmpty()) return
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (title != null) {
            Text(
                text = mokoString(title),
                style = MaterialTheme.typography.titleLarge
            )
        }
        validCards.forEach { card ->
            InfoAreaCard(
                infoItems = card.items,
                header = card.header,
                footer = card.footer
            )
        }
    }
}

@Composable
fun InfoArea(
    arrCards: List<InfoItem>,
    seerrCards: List<InfoItem>,
    modifier: Modifier = Modifier,
    title: StringResource? = MR.strings.information,
) {
    if (arrCards.isEmpty() && seerrCards.isEmpty()) return
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (title != null) {
            Text(
                text = mokoString(title),
                style = MaterialTheme.typography.titleLarge
            )
        }
        arrCards.takeUnless { it.isEmpty() }?.let { cardItems ->
            InfoAreaCard(infoItems = cardItems)
        }
        seerrCards.takeUnless { it.isEmpty() }?.let { cardItems ->
            InfoAreaCard(infoItems = cardItems)
        }
    }
}