package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.signin_icon
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.painterResource

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

private val LoginBlue = Color(0xFF0074BA)
private val LoginInk = Color(0xFF14293A)
private val LoginSecondary = Color(0xFF617586)

@Composable
fun LoginScreen(
    state: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val poppins = oseboFontFamily()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                0f to Color(0xFF5AB5E3),
                0.22f to Color(0xFF9BD4EF),
                0.46f to Color(0xFFE4F6FE),
                0.68f to Color.White,
                1f to Color.White,
            ),
        ),
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.padding(8.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = LoginInk,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(start = 28.dp, end = 28.dp, top = 64.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(Res.drawable.signin_icon),
                contentDescription = null,
                modifier = Modifier.size(112.dp),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Welcome back",
                color = LoginInk,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                lineHeight = 34.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Sign in to manage your business",
                color = LoginSecondary,
                fontFamily = poppins,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))

            FieldLabel("Email or phone", poppins, LoginInk)
            PillField(
                value = state.username,
                onValueChange = onUsernameChange,
                placeholder = "Enter your email or phone",
                enabled = !state.isLoading,
                fontFamily = poppins,
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Email,
            )

            Spacer(Modifier.height(14.dp))
            FieldLabel("Password", poppins, LoginInk)
            PillField(
                value = state.password,
                onValueChange = onPasswordChange,
                placeholder = "Enter your password",
                enabled = !state.isLoading,
                fontFamily = poppins,
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
            )

            state.errorMessage?.let { message -> FieldError(message, poppins) }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onSignInClick,
                enabled = !state.isLoading && state.username.isNotBlank() && state.password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LoginBlue),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
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
}

@Composable
private fun FieldLabel(text: String, fontFamily: FontFamily, color: Color) {
    Text(
        text = text,
        color = color,
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 7.dp),
    )
}

@Composable
private fun FieldError(text: String, fontFamily: FontFamily) {
    Text(
        text = text,
        color = Color(0xFFCE4257),
        fontFamily = fontFamily,
        fontSize = 12.sp,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp),
    )
}

@Composable
private fun PillField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    fontFamily: FontFamily,
    modifier: Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
            keyboardController?.hide()
        }),
        visualTransformation = visualTransformation,
        cursorBrush = SolidColor(LoginBlue),
        textStyle = TextStyle(color = LoginInk, fontFamily = fontFamily, fontSize = 15.sp),
        modifier = modifier
            .heightIn(min = 54.dp)
            .background(Color.White, RoundedCornerShape(27.dp))
            .border(1.dp, Color(0xFFD7E7F1), RoundedCornerShape(27.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(placeholder, color = Color(0xFF8195A5), fontFamily = fontFamily, fontSize = 15.sp)
                }
                innerTextField()
            }
        },
    )
}
