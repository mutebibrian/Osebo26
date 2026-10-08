package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_close
import com.devbrian.osebo.resources.iconsax_lock
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.painterResource

data class RoleFormUiState(
    val name: String = "",
    val description: String = "",
    val permissions: Set<String> = emptySet(),
    val nameError: String? = null,
    val descriptionError: String? = null,
    val permissionError: String? = null,
    val submitError: String? = null,
    val isLoading: Boolean = false,
)

data class RolePermissionOption(val id: String, val label: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoleFormScreen(
    state: RoleFormUiState,
    permissions: List<RolePermissionOption>,
    onStateChange: (RoleFormUiState) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp), color = Color.White) {
        Column(
            modifier = Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(RoleBlueSoft), contentAlignment = Alignment.Center) {
                    Icon(painterResource(Res.drawable.iconsax_lock), null, tint = RoleBlue, modifier = Modifier.size(21.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 11.dp)) {
                    Text("Add role", color = RoleInk, fontFamily = oseboFontFamily(), fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text("Choose what this role can access", color = RoleMuted, fontFamily = oseboFontFamily(), fontSize = 8.sp)
                }
                IconButton(onClick = onDismiss, enabled = !state.isLoading, modifier = Modifier.size(40.dp)) {
                    Icon(painterResource(Res.drawable.iconsax_close), "Close", tint = RoleInk, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFDDEBFF), Color(0xFFF5F8FF))))
                    .border(1.dp, Color.White.copy(alpha = 0.92f), RoundedCornerShape(25.dp)).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Text("Role details", color = RoleInk, fontFamily = oseboFontFamily(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                RoleField("Role name", state.name, state.nameError, true, !state.isLoading) {
                    onStateChange(state.copy(name = it, nameError = null, submitError = null))
                }
                RoleField("Description", state.description, state.descriptionError, false, !state.isLoading) {
                    onStateChange(state.copy(description = it, descriptionError = null, submitError = null))
                }
            }
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE0F7F7), Color(0xFFF4FBFB))))
                    .border(1.dp, Color.White.copy(alpha = 0.92f), RoundedCornerShape(25.dp)).padding(14.dp),
            ) {
                Text("Permissions", color = RoleInk, fontFamily = oseboFontFamily(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("Select at least one", color = if (state.permissionError != null) RoleRed else RoleMuted, fontFamily = oseboFontFamily(), fontSize = 8.sp)
                Spacer(Modifier.height(9.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    permissions.forEach { option ->
                        val selected = option.id in state.permissions
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(50)).background(if (selected) RoleInk else Color.White.copy(alpha = 0.82f))
                                .border(1.dp, if (selected) RoleInk else RoleBorder, RoundedCornerShape(50))
                                .clickable(enabled = !state.isLoading) {
                                    val updated = if (selected) state.permissions - option.id else state.permissions + option.id
                                    onStateChange(state.copy(permissions = updated, permissionError = null, submitError = null))
                                }.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(if (selected) RoleBlue else RoleBorder))
                            Text(option.label, modifier = Modifier.padding(start = 7.dp), color = if (selected) Color.White else RoleInk, fontFamily = oseboFontFamily(), fontSize = 8.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    }
                }
            }
            state.submitError?.let {
                Spacer(Modifier.height(9.dp))
                Text(it, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Color(0xFFFFEEF0)).padding(11.dp), color = RoleRed, fontFamily = oseboFontFamily(), fontSize = 8.sp)
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Button(
                    onClick = onDismiss,
                    enabled = !state.isLoading,
                    modifier = Modifier.weight(0.8f).height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = RoleCanvas, contentColor = RoleInk),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                ) { Text("Cancel", fontFamily = oseboFontFamily(), fontSize = 10.sp) }
                Button(
                    onClick = onSave,
                    enabled = !state.isLoading,
                    modifier = Modifier.weight(1.2f).height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = RoleInk, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                ) {
                    if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Save role", fontFamily = oseboFontFamily(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun RoleField(label: String, value: String, error: String?, singleLine: Boolean, enabled: Boolean, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        placeholder = { Text(label, color = RoleMuted, fontFamily = oseboFontFamily(), fontSize = 9.sp) },
        supportingText = error?.let { { Text(it, color = RoleRed, fontFamily = oseboFontFamily(), fontSize = 8.sp) } },
        isError = error != null,
        textStyle = TextStyle(color = RoleInk, fontFamily = oseboFontFamily(), fontSize = 10.sp),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.82f), unfocusedContainerColor = Color.White.copy(alpha = 0.82f),
            focusedBorderColor = RoleBlue, unfocusedBorderColor = RoleBorder, errorBorderColor = RoleRed, cursorColor = RoleBlue,
        ),
    )
}

private val RoleInk = Color(0xFF171B1E)
private val RoleMuted = Color(0xFF7B8794)
private val RoleBlue = Color(0xFF0789CF)
private val RoleBlueSoft = Color(0xFFE2F3FC)
private val RoleCanvas = Color(0xFFF3F6F7)
private val RoleBorder = Color(0xFFDDE3E6)
private val RoleRed = Color(0xFFE54C65)
