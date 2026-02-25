package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject

@Composable
fun NoInstanceView(
    type: InstanceType,
    modifier: Modifier = Modifier,
    navigationManager: NavigationManager = koinInject()
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.CloudQueue,
            contentDescription = null,
            modifier = Modifier.size(128.dp)
        )
        Text(
            text = mokoString(MR.strings.no_type_instances, type.name),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
        Text(text = mokoString(MR.strings.no_type_instances_message, type.name))

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                navigationManager.openNewInstanceScreen(type)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = mokoString(MR.strings.add_instance),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}