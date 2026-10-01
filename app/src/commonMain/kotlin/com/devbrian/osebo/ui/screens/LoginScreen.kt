package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.ui.theme.OseboColors
import com.devbrian.osebo.ui.theme.OseboShapes
import com.devbrian.osebo.ui.theme.oseboFontFamily

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@Composable
fun LoginScreen(
    state: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSignInClick: () -> Unit,
) {
    val poppins = oseboFontFamily()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Osebo",
            fontFamily = poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = OseboColors.Primary,
        )
        Text(
            text = "Sign in to continue",
            fontFamily = poppins,
            fontSize = 15.sp,
            color = OseboColors.OnSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp, bottom = 32.dp),
        )

        OutlinedTextField(
            value = state.username,
            onValueChange = onUsernameChange,
            label = { Text("Email or phone", fontFamily = poppins) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = OseboShapes.CardSmall,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OseboColors.Primary,
                unfocusedBorderColor = OseboColors.OnSurfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = { Text("Password", fontFamily = poppins) },
            singleLine = true,
            visualTransformation = if (state.isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (state.isPasswordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (state.isPasswordVisible) "Hide password" else "Show password",
                    )
                }
            },
            shape = OseboShapes.CardSmall,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OseboColors.Primary,
                unfocusedBorderColor = OseboColors.OnSurfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )

        state.errorMessage?.let { message ->
            Text(
                text = message,
                fontFamily = poppins,
                fontSize = 13.sp,
                color = OseboColors.Error,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Button(
            onClick = onSignInClick,
            enabled = !state.isLoading && state.username.isNotBlank() && state.password.isNotBlank(),
            shape = OseboShapes.Pill,
            colors = ButtonDefaults.buttonColors(
                containerColor = OseboColors.Primary,
                contentColor = OseboColors.OnPrimary,
            ),
            modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 28.dp),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = OseboColors.OnPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Text(
                    text = "Sign In",
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}
