package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.entensions.rememberHtml

@Composable
fun ItemDescriptionCard(
    overview: String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val parsed = overview.rememberHtml()
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .animateContentSize()
                .clickable {
                    expanded = !expanded
                },
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 18.dp),
        ) {
            Text(
                text = parsed,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 5,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { result ->
                    if (!result.didOverflowHeight) {
                        expanded = true
                    }
                },
            )
        }
    }
}
