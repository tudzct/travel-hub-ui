package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.BorderStroke
import com.mobile.travelhub.ui.theme.isDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.travelhub.ui.components.SkeletonBlock

private val AuthBlue = Color(0xFF2F7DF1)
private val AuthBlueDark = Color(0xFF246EE7)

private val AuthText: Color
    @Composable
    get() = if (isDarkTheme) Color(0xFFEEEEEE) else Color(0xFF111827)

private val AuthMuted: Color
    @Composable
    get() = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)

private val AuthField: Color
    @Composable
    get() = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF7F8FC)

private val AuthStroke: Color
    @Composable
    get() = if (isDarkTheme) Color(0xFF444444) else Color(0xFFE1E5EE)

private val AuthOrange = Color(0xFFFF8A00)

@Composable
fun AuthLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(72.dp)
            .shadow(18.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x223B82F6))
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF5F7FC))
            .border(1.dp, if (isDarkTheme) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else Color(0xFFE9EDF5), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Navigation,
            contentDescription = null,
            tint = AuthBlue,
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
fun AuthTitle(
    firstLine: String,
    brandOnSecondLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (brandOnSecondLine) {
        Text(
            text = buildAnnotatedString {
                append(firstLine)
                append("\n")
                withStyle(SpanStyle(color = AuthBlue)) {
                    append("TravelHub")
                }
            },
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 31.sp,
                lineHeight = 39.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.sp
            ),
            textAlign = TextAlign.Center,
            color = AuthText,
            modifier = modifier.fillMaxWidth()
        )
    } else {
        Text(
            text = firstLine,
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 31.sp,
                lineHeight = 39.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.sp
            ),
            textAlign = TextAlign.Center,
            color = AuthText,
            modifier = modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AuthSubtitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.sp
        ),
        textAlign = TextAlign.Center,
        color = AuthMuted,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun AuthInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable (() -> Unit))? = null
) {
    val shape = RoundedCornerShape(16.dp)
    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        placeholder = {
            Text(
                text = placeholder,
                color = AuthMuted,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AuthMuted,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 16.sp,
            color = AuthText,
            letterSpacing = 0.sp
        ),
        shape = shape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AuthField,
            unfocusedContainerColor = AuthField,
            disabledContainerColor = AuthField,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = AuthBlue
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    )
}

@Composable
fun PasswordVisibilityButton(
    visible: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
            contentDescription = if (visible) "Ẩn mật khẩu" else "Hiện mật khẩu",
            tint = AuthMuted
        )
    }
}

@Composable
fun PrimaryAuthButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(AuthBlue, AuthBlueDark)))
            .clickable(
                enabled = !isLoading,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                )
            )
            if (isLoading) {
                Spacer(modifier = Modifier.size(10.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun AuthDivider(text: String = "hoặc") {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(AuthStroke)
        )
        Text(
            text = text,
            color = AuthMuted,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(AuthStroke)
        )
    }
}

@Composable
fun GoogleAuthButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else Color.White,
        border = BorderStroke(1.dp, AuthStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = enabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            GoogleGlyph()
            Spacer(modifier = Modifier.size(18.dp))
            Text(
                text = text,
                color = AuthText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.sp
                )
            )
        }
    }
}

@Composable
private fun GoogleGlyph() {
    Text(
        text = "G",
        color = Color(0xFF4285F4),
        style = MaterialTheme.typography.titleLarge.copy(
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.sp
        )
    )
}

@Composable
fun AuthFooterLink(
    normalText: String,
    actionText: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = Color(0xFF4B5563), fontWeight = FontWeight.Normal)) {
                append(normalText)
            }
            withStyle(SpanStyle(color = AuthBlue, fontWeight = FontWeight.Bold)) {
                append(actionText)
            }
        },
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, letterSpacing = 0.sp),
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    )
}

@Composable
fun PasswordStrengthMeter(password: String, modifier: Modifier = Modifier) {
    val score = passwordStrengthScore(password)
    val label = when {
        password.isBlank() -> "Yếu"
        score <= 2 -> "Yếu"
        score <= 4 -> "Trung bình"
        else -> "Mạnh"
    }
    val labelColor = when (label) {
        "Mạnh" -> Color(0xFF10B981)
        "Trung bình" -> AuthOrange
        else -> Color(0xFFEF4444)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (index < score) Color(0xFF7DB7F7) else Color(0xFFE6EAF1))
            )
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Mật khẩu phải có ít nhất 8 ký tự",
            color = AuthMuted,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, letterSpacing = 0.sp),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = AuthMuted)) {
                    append("Độ mạnh: ")
                }
                withStyle(SpanStyle(color = labelColor, fontWeight = FontWeight.Bold)) {
                    append(label)
                }
            },
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, letterSpacing = 0.sp)
        )
    }
}

private fun passwordStrengthScore(password: String): Int {
    if (password.isBlank()) return 0
    var score = 0
    if (password.length >= 8) score++
    if (password.length >= 12) score++
    if (password.any(Char::isDigit)) score++
    if (password.any(Char::isUpperCase) && password.any(Char::isLowerCase)) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    return score.coerceIn(1, 5)
}

fun authFieldIconEmail(): ImageVector = Icons.Outlined.Email
fun authFieldIconPerson(): ImageVector = Icons.Outlined.Person
fun authFieldIconLock(): ImageVector = Icons.Outlined.Lock
