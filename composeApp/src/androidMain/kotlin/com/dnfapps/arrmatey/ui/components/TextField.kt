package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AMOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    label: String? = null,
    required: Boolean = false,
    description: String? = null,
    placeholder: String? = null,
    errorMessage: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        label?.let {
            val labelText = buildAnnotatedString {
                if (required) {
                    withStyle(SpanStyle(color = Color.Red)) {
                        append("* ")
                    }
                }
                append(label)
            }
            Text(
                text = labelText,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.5f),
                fontSize = 14.sp,
                maxLines = 1
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder?.let { {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            } },
            singleLine = singleLine,
            isError = isError,
            supportingText = if (isError && errorMessage != null) {
                { Text(text = errorMessage) }
            } else null,
            enabled = enabled,
            keyboardOptions = keyboardOptions,
            shape = MaterialTheme.shapes.large,
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon
        )
        description?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                lineHeight = 14.sp
            )
        }
    }
}