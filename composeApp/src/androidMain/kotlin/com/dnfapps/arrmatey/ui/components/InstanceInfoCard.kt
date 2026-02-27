package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.entensions.openLink
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun InstanceInfoCard(
    instanceType: InstanceType,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(instanceType.icon),
                    contentDescription = instanceType.toString(),
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = instanceType.toString(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = mokoString(MR.strings.close)
                    )
                }
            }
            Text(
                text = mokoString(instanceType.resource),
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        context.openLink(instanceType.github)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = mokoString(MR.strings.github))
                }
                Button(
                    onClick = {
                        context.openLink(instanceType.website)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = mokoString(MR.strings.website))
                }
            }
        }
    }
}