package com.mobile.travelhub.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SimpleFormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = RoundedCornerShape(14.dp),
    focusedContainerColor: Color? = null,
    unfocusedContainerColor: Color? = null,
    disabledContainerColor: Color? = null,
    focusedIndicatorColor: Color = Color.Transparent,
    unfocusedIndicatorColor: Color = Color.Transparent,
    disabledIndicatorColor: Color = Color.Transparent,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    leadingIcon: (@Composable (() -> Unit))? = null,
    trailingIcon: (@Composable (() -> Unit))? = null
) {
    SimpleFormTextFieldContent(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        shape = shape,
        focusedContainerColor = focusedContainerColor,
        unfocusedContainerColor = unfocusedContainerColor,
        disabledContainerColor = disabledContainerColor,
        focusedIndicatorColor = focusedIndicatorColor,
        unfocusedIndicatorColor = unfocusedIndicatorColor,
        disabledIndicatorColor = disabledIndicatorColor,
        textStyle = textStyle,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon
    )
}

@Composable
fun SimpleFormTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = RoundedCornerShape(14.dp),
    focusedContainerColor: Color? = null,
    unfocusedContainerColor: Color? = null,
    disabledContainerColor: Color? = null,
    focusedIndicatorColor: Color = Color.Transparent,
    unfocusedIndicatorColor: Color = Color.Transparent,
    disabledIndicatorColor: Color = Color.Transparent,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    leadingIcon: (@Composable (() -> Unit))? = null,
    trailingIcon: (@Composable (() -> Unit))? = null
) {
    SimpleFormTextFieldContent(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        shape = shape,
        focusedContainerColor = focusedContainerColor,
        unfocusedContainerColor = unfocusedContainerColor,
        disabledContainerColor = disabledContainerColor,
        focusedIndicatorColor = focusedIndicatorColor,
        unfocusedIndicatorColor = unfocusedIndicatorColor,
        disabledIndicatorColor = disabledIndicatorColor,
        textStyle = textStyle,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon
    )
}

@Composable
private fun SimpleFormTextFieldContent(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier,
    enabled: Boolean,
    readOnly: Boolean,
    singleLine: Boolean,
    minLines: Int,
    maxLines: Int,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    visualTransformation: VisualTransformation,
    shape: Shape,
    focusedContainerColor: Color?,
    unfocusedContainerColor: Color?,
    disabledContainerColor: Color?,
    focusedIndicatorColor: Color,
    unfocusedIndicatorColor: Color,
    disabledIndicatorColor: Color,
    textStyle: TextStyle,
    leadingIcon: (@Composable (() -> Unit))?,
    trailingIcon: (@Composable (() -> Unit))?
) {
    val defaultFocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val defaultUnfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)

    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        placeholder = { Text(placeholder) },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = simpleFormTextFieldColors(
            focusedContainerColor = focusedContainerColor ?: defaultFocusedContainerColor,
            unfocusedContainerColor = unfocusedContainerColor ?: defaultUnfocusedContainerColor,
            disabledContainerColor = disabledContainerColor ?: defaultUnfocusedContainerColor,
            focusedIndicatorColor = focusedIndicatorColor,
            unfocusedIndicatorColor = unfocusedIndicatorColor,
            disabledIndicatorColor = disabledIndicatorColor
        ),
        textStyle = textStyle,
        shape = shape,
        modifier = modifier
    )
}

@Composable
private fun SimpleFormTextFieldContent(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    modifier: Modifier,
    enabled: Boolean,
    readOnly: Boolean,
    singleLine: Boolean,
    minLines: Int,
    maxLines: Int,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    visualTransformation: VisualTransformation,
    shape: Shape,
    focusedContainerColor: Color?,
    unfocusedContainerColor: Color?,
    disabledContainerColor: Color?,
    focusedIndicatorColor: Color,
    unfocusedIndicatorColor: Color,
    disabledIndicatorColor: Color,
    textStyle: TextStyle,
    leadingIcon: (@Composable (() -> Unit))?,
    trailingIcon: (@Composable (() -> Unit))?
) {
    val defaultFocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val defaultUnfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)

    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        placeholder = { Text(placeholder) },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = simpleFormTextFieldColors(
            focusedContainerColor = focusedContainerColor ?: defaultFocusedContainerColor,
            unfocusedContainerColor = unfocusedContainerColor ?: defaultUnfocusedContainerColor,
            disabledContainerColor = disabledContainerColor ?: defaultUnfocusedContainerColor,
            focusedIndicatorColor = focusedIndicatorColor,
            unfocusedIndicatorColor = unfocusedIndicatorColor,
            disabledIndicatorColor = disabledIndicatorColor
        ),
        textStyle = textStyle,
        shape = shape,
        modifier = modifier
    )
}

@Composable
private fun simpleFormTextFieldColors(
    focusedContainerColor: Color,
    unfocusedContainerColor: Color,
    disabledContainerColor: Color,
    focusedIndicatorColor: Color,
    unfocusedIndicatorColor: Color,
    disabledIndicatorColor: Color
) = TextFieldDefaults.colors(
    focusedContainerColor = focusedContainerColor,
    unfocusedContainerColor = unfocusedContainerColor,
    disabledContainerColor = disabledContainerColor,
    focusedIndicatorColor = focusedIndicatorColor,
    unfocusedIndicatorColor = unfocusedIndicatorColor,
    disabledIndicatorColor = disabledIndicatorColor,
    errorIndicatorColor = Color.Transparent,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurface,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
)
