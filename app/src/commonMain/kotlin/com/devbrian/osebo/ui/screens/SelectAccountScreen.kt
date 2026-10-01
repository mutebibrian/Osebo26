package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.data.remote.dto.response.AccountInfo
import com.devbrian.osebo.ui.theme.oseboFontFamily

data class SelectAccountUiState(
    val accounts: List<AccountInfo> = emptyList(),
    val selectedAccountId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@Composable
fun SelectAccountScreen(
    state: SelectAccountUiState,
    onAccountSelected: (String) -> Unit,
    onContinueClick: () -> Unit,
    onBackToSignInClick: () -> Unit,
) {
    val poppins = oseboFontFamily()
    val blue = Color(0xFF0785CA)
    val ink = Color(0xFF151A1D)
    val secondary = Color(0xFF657078)

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFEFF))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 56.dp, bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(68.dp).background(Color(0xFFEAF7FD), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "O",
                    color = blue,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Choose an account",
                color = ink,
                fontFamily = poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Select the profile you want to continue with",
                color = secondary,
                fontFamily = poppins,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Spacer(Modifier.height(34.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(items = state.accounts, key = { _, account -> account.accountId }) { _, account ->
                    AccountCard(
                        account = account,
                        selected = state.selectedAccountId == account.accountId,
                        enabled = !state.isLoading,
                        onClick = { onAccountSelected(account.accountId) },
                    )
                }
            }

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFFB3261E),
                    fontFamily = poppins,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                )
            }

            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onContinueClick,
                enabled = !state.isLoading && state.selectedAccountId != null,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = blue,
                    disabledContainerColor = blue.copy(alpha = 0.55f),
                ),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(23.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(text = "Continue", fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }

            Text(
                text = "Back to sign in",
                color = blue,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clickable(enabled = !state.isLoading, onClick = onBackToSignInClick)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
            )
        }
    }
}

@Composable
private fun AccountCard(
    account: AccountInfo,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val poppins = oseboFontFamily()
    val blue = Color(0xFF0785CA)
    val ink = Color(0xFF151A1D)
    val secondary = Color(0xFF657078)
    val shape = RoundedCornerShape(22.dp)
    val name = listOf(account.ownerFirstName.trim(), account.ownerLastName.trim())
        .filter { it.isNotEmpty() }
        .joinToString(" ")
        .ifEmpty { "Osebo account" }
    val initials = listOf(account.ownerFirstName.trim(), account.ownerLastName.trim())
        .filter { it.isNotEmpty() }
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .take(2)
        .ifEmpty { "OA" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 94.dp)
            .background(if (selected) Color(0xFFE5F6FE) else Color(0xFFF4F6F8), shape)
            .border(1.dp, if (selected) Color(0xFF8BD2F2) else Color.Transparent, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(50.dp).background(Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = initials, color = blue, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(text = name, color = ink, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 21.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (account.isOwner) "Owner account" else "Team account",
                color = secondary,
                fontFamily = poppins,
                fontSize = 12.sp,
                lineHeight = 17.sp,
            )
        }

        Box(
            modifier = Modifier
                .size(25.dp)
                .background(if (selected) blue else Color.White, CircleShape)
                .border(if (selected) 0.dp else 1.5.dp, if (selected) Color.Transparent else Color(0xFFC5CDD2), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Text(text = "✓", color = Color.White, fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
