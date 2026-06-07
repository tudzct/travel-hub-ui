package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mobile.travelhub.viewmodels.AuthUiState

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onRegister: (String, String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onDismissError: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 28.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        IconButton(
            onClick = onNavigateToLogin,
            enabled = !uiState.isLoading
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color(0xFF111827)
            )
        }
        Spacer(modifier = Modifier.height(22.dp))
        AuthTitle(firstLine = "Tạo tài khoản")
        Spacer(modifier = Modifier.height(18.dp))
        AuthSubtitle(text = "Điền thông tin để bắt đầu")

        Spacer(modifier = Modifier.height(44.dp))
        AuthInputField(
            value = email,
            onValueChange = {
                email = it
                if (uiState.errorMessage != null) onDismissError()
            },
            placeholder = "Địa chỉ email",
            icon = authFieldIconEmail(),
            enabled = !uiState.isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthInputField(
            value = username,
            onValueChange = {
                username = it
                if (uiState.errorMessage != null) onDismissError()
            },
            placeholder = "Tên người dùng",
            icon = authFieldIconPerson(),
            enabled = !uiState.isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthInputField(
            value = name,
            onValueChange = {
                name = it
                if (uiState.errorMessage != null) onDismissError()
            },
            placeholder = "Tên hiển thị",
            icon = authFieldIconPerson(),
            enabled = !uiState.isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthInputField(
            value = password,
            onValueChange = {
                password = it
                if (uiState.errorMessage != null) onDismissError()
            },
            placeholder = "Mật khẩu",
            icon = authFieldIconLock(),
            enabled = !uiState.isLoading,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onRegister(email, username, name, password)
                }
            ),
            trailingIcon = {
                PasswordVisibilityButton(
                    visible = passwordVisible,
                    onClick = { passwordVisible = !passwordVisible }
                )
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        PasswordStrengthMeter(password = password)

        if (!uiState.errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(34.dp))
        PrimaryAuthButton(
            text = "Tạo tài khoản",
            isLoading = uiState.isLoading,
            onClick = { onRegister(email, username, name, password) }
        )
        Spacer(modifier = Modifier.height(30.dp))
        AuthFooterLink(
            normalText = "Đã có tài khoản? ",
            actionText = "Đăng nhập",
            enabled = !uiState.isLoading,
            onClick = onNavigateToLogin,
            modifier = Modifier.padding(bottom = 28.dp)
        )
    }
}
