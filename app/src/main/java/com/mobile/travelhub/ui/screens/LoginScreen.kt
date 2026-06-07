package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.travelhub.viewmodels.AuthUiState

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onDismissError: () -> Unit
) {
    var email by remember { mutableStateOf("") }
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
        Spacer(modifier = Modifier.height(92.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AuthLogo()
        }
        Spacer(modifier = Modifier.height(40.dp))
        AuthTitle(firstLine = "Chào mừng trở lại")
        Spacer(modifier = Modifier.height(22.dp))
        AuthSubtitle(text = "Đăng nhập để tiếp tục hành trình của bạn")

        Spacer(modifier = Modifier.height(58.dp))
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
        Spacer(modifier = Modifier.height(18.dp))
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
                    onLogin(email, password)
                }
            ),
            trailingIcon = {
                PasswordVisibilityButton(
                    visible = passwordVisible,
                    onClick = { passwordVisible = !passwordVisible }
                )
            }
        )

        Text(
            text = "Quên mật khẩu?",
            color = Color(0xFF2F7DF1),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            ),
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 14.dp)
        )

        if (!uiState.errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
        PrimaryAuthButton(
            text = "Đăng nhập",
            isLoading = uiState.isLoading,
            onClick = { onLogin(email, password) }
        )
        Spacer(modifier = Modifier.height(42.dp))
        AuthDivider()
        Spacer(modifier = Modifier.height(30.dp))
        GoogleAuthButton(text = "Tiếp tục với Google")
        Spacer(modifier = Modifier.height(32.dp))
        AuthFooterLink(
            normalText = "New to TravelHub? ",
            actionText = "Signup",
            enabled = !uiState.isLoading,
            onClick = onNavigateToRegister,
            modifier = Modifier.padding(bottom = 28.dp)
        )
    }
}
