package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_add_employee
import com.devbrian.osebo.resources.iconsax_balance
import com.devbrian.osebo.resources.iconsax_calendar
import com.devbrian.osebo.resources.iconsax_clock
import com.devbrian.osebo.resources.iconsax_employees
import com.devbrian.osebo.resources.iconsax_export
import com.devbrian.osebo.resources.iconsax_filter
import com.devbrian.osebo.resources.iconsax_import
import com.devbrian.osebo.resources.iconsax_more
import com.devbrian.osebo.resources.iconsax_refresh
import com.devbrian.osebo.resources.iconsax_reports
import com.devbrian.osebo.resources.iconsax_search
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class EmployeeItemUi(
    val id: String,
    val name: String,
    val initials: String,
    val email: String,
    val phone: String,
    val role: String,
    val department: String,
    val status: String,
    val isActive: Boolean,
    val isManager: Boolean,
)

data class EmployeesUiState(
    val shopName: String = "My shop",
    val employees: List<EmployeeItemUi> = emptyList(),
    val isLoading: Boolean = false,
    val accessDenied: Boolean = false,
    val canManageRoles: Boolean = false,
)

private enum class EmployeeFilter(val label: String) {
    All("All"),
    Active("Active"),
    Managers("Managers"),
    Staff("Staff"),
}

private data class EmployeeQuickAction(
    val label: String,
    val subtitle: String,
    val icon: DrawableResource,
    val onClick: () -> Unit,
)

private val EmployeeCanvas = Color(0xFFF0F3F4)
private val EmployeeSurface = Color(0xFFFAFBFB)
private val EmployeeWhite = Color.White
private val EmployeeInk = Color(0xFF171B1F)
private val EmployeeMuted = Color(0xFF768087)
private val EmployeeBorder = Color(0xFFDDE3E5)
private val EmployeeBlue = Color(0xFF176BFF)
private val EmployeeGreen = Color(0xFF23A36D)
private val EmployeeRed = Color(0xFFE75A67)

@Composable
fun EmployeesScreen(
    state: EmployeesUiState,
    onRefreshClick: () -> Unit,
    onAddEmployeeClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onPayrollClick: () -> Unit,
    onPerformanceClick: () -> Unit,
    onViewAllClick: () -> Unit,
    onEmployeeClick: (String) -> Unit,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    onRolesClick: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(EmployeeFilter.All) }
    val managers = state.employees.count { it.isManager }
    val active = state.employees.count { it.isActive }
    val filtered = state.employees.filter { employee ->
        val matchesQuery = query.isBlank() || employee.name.contains(query, ignoreCase = true) ||
            employee.role.contains(query, ignoreCase = true) ||
            employee.department.contains(query, ignoreCase = true) ||
            employee.email.contains(query, ignoreCase = true) ||
            employee.phone.contains(query, ignoreCase = true)
        val matchesFilter = when (filter) {
            EmployeeFilter.All -> true
            EmployeeFilter.Active -> employee.isActive
            EmployeeFilter.Managers -> employee.isManager
            EmployeeFilter.Staff -> !employee.isManager
        }
        matchesQuery && matchesFilter
    }

    Scaffold(containerColor = EmployeeCanvas) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 138.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                EmployeesHeader(
                    shopName = state.shopName,
                    isLoading = state.isLoading,
                    canManageRoles = state.canManageRoles,
                    onRefreshClick = onRefreshClick,
                    onImportClick = onImportClick,
                    onExportClick = onExportClick,
                    onRolesClick = onRolesClick,
                )
            }
            item { Spacer(Modifier.height(4.dp)) }
            item {
                TeamOverview(
                    total = state.employees.size,
                    active = active,
                    managers = managers,
                    staff = state.employees.size - managers,
                )
            }
            item {
                Button(
                    onClick = onAddEmployeeClick,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(19.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmployeeInk,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.iconsax_add_employee),
                        contentDescription = null,
                        modifier = Modifier.size(21.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Add employee",
                        fontFamily = oseboFontFamily(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            item {
                EmployeeSectionTitle(
                    title = "Team tools",
                    subtitle = "Manage the working day",
                )
            }
            item {
                val actions = listOf(
                    EmployeeQuickAction("Attendance", "Clock-ins", Res.drawable.iconsax_clock, onAttendanceClick),
                    EmployeeQuickAction("Schedule", "Shifts", Res.drawable.iconsax_calendar, onScheduleClick),
                    EmployeeQuickAction("Payroll", "Payments", Res.drawable.iconsax_balance, onPayrollClick),
                    EmployeeQuickAction("Performance", "Insights", Res.drawable.iconsax_reports, onPerformanceClick),
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        EmployeeActionCard(actions[0], Modifier.weight(1f))
                        EmployeeActionCard(actions[1], Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        EmployeeActionCard(actions[2], Modifier.weight(1f))
                        EmployeeActionCard(actions[3], Modifier.weight(1f))
                    }
                }
            }
            item {
                EmployeeSearchField(query = query, onQueryChange = { query = it })
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EmployeeFilter.entries.forEach { option ->
                        EmployeeFilterChip(
                            label = option.label,
                            selected = option == filter,
                            onClick = { filter = option },
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    EmployeeSectionTitle(
                        title = "Your team",
                        subtitle = "${filtered.size} ${if (filtered.size == 1) "person" else "people"}",
                        modifier = Modifier.weight(1f),
                    )
                    if (state.employees.isNotEmpty()) {
                        TextButton(onClick = onViewAllClick) {
                            Text(
                                "View all",
                                color = EmployeeBlue,
                                fontFamily = oseboFontFamily(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
            when {
                state.isLoading && state.employees.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EmployeeBlue, strokeWidth = 2.dp)
                    }
                }
                state.accessDenied -> item {
                    EmployeeEmptyState(
                        title = "Employee access unavailable",
                        message = "Your current role does not include permission to view employees.",
                        showAdd = false,
                        onAddClick = onAddEmployeeClick,
                    )
                }
                filtered.isEmpty() -> item {
                    EmployeeEmptyState(
                        title = if (query.isNotBlank() || filter != EmployeeFilter.All) {
                            "No matching employees"
                        } else {
                            "No employees yet"
                        },
                        message = if (query.isNotBlank() || filter != EmployeeFilter.All) {
                            "Try another search or team filter."
                        } else {
                            "Add your first team member to get started."
                        },
                        showAdd = query.isBlank() && filter == EmployeeFilter.All,
                        onAddClick = onAddEmployeeClick,
                    )
                }
                else -> items(filtered, key = { it.id }) { employee ->
                    EmployeeRow(employee = employee, onClick = { onEmployeeClick(employee.id) })
                }
            }
        }
    }
}

@Composable
private fun EmployeesHeader(
    shopName: String,
    isLoading: Boolean,
    canManageRoles: Boolean,
    onRefreshClick: () -> Unit,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    onRolesClick: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Employees",
                color = EmployeeInk,
                fontFamily = poppins,
                fontSize = 27.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                shopName,
                color = EmployeeMuted,
                fontFamily = poppins,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onRefreshClick, enabled = !isLoading) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = EmployeeBlue,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_refresh),
                    contentDescription = "Refresh employees",
                    tint = EmployeeBlue,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_more),
                    contentDescription = "Employee options",
                    tint = EmployeeInk,
                    modifier = Modifier.size(22.dp),
                )
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                containerColor = EmployeeSurface,
            ) {
                EmployeeMenuItem("Import employees", Res.drawable.iconsax_import) {
                    menuOpen = false
                    onImportClick()
                }
                EmployeeMenuItem("Export employees", Res.drawable.iconsax_export) {
                    menuOpen = false
                    onExportClick()
                }
                if (canManageRoles) {
                    EmployeeMenuItem("Roles & permissions", Res.drawable.iconsax_employees) {
                        menuOpen = false
                        onRolesClick()
                    }
                }
            }
        }
    }
}

@Composable
private fun EmployeeMenuItem(label: String, icon: DrawableResource, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label, color = EmployeeInk, fontFamily = oseboFontFamily(), fontSize = 12.sp) },
        onClick = onClick,
        leadingIcon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = EmployeeBlue,
                modifier = Modifier.size(20.dp),
            )
        },
    )
}

@Composable
private fun TeamOverview(total: Int, active: Int, managers: Int, staff: Int) {
    val poppins = oseboFontFamily()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(EmployeeInk, RoundedCornerShape(28.dp))
            .padding(21.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_employees),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(27.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Team overview",
                color = Color.White.copy(alpha = 0.72f),
                fontFamily = poppins,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "$active active",
                color = Color(0xFF72E3B0),
                fontFamily = poppins,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.09f), RoundedCornerShape(50))
                    .padding(horizontal = 11.dp, vertical = 7.dp),
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            "$total ${if (total == 1) "employee" else "employees"}",
            color = Color.White,
            fontFamily = poppins,
            fontSize = 27.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            "$managers managers  •  $staff staff",
            color = Color.White.copy(alpha = 0.58f),
            fontFamily = poppins,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun EmployeeActionCard(action: EmployeeQuickAction, modifier: Modifier) {
    Row(
        modifier = modifier
            .background(EmployeeSurface, RoundedCornerShape(21.dp))
            .border(1.dp, EmployeeBorder, RoundedCornerShape(21.dp))
            .clickable(onClick = action.onClick)
            .padding(horizontal = 14.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(action.icon),
            contentDescription = null,
            tint = EmployeeBlue,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                action.label,
                color = EmployeeInk,
                fontFamily = oseboFontFamily(),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(action.subtitle, color = EmployeeMuted, fontFamily = oseboFontFamily(), fontSize = 8.sp)
        }
    }
}

@Composable
private fun EmployeeSearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text("Search employees", color = EmployeeMuted, fontFamily = oseboFontFamily(), fontSize = 12.sp)
        },
        leadingIcon = {
            Icon(
                painter = painterResource(Res.drawable.iconsax_search),
                contentDescription = null,
                tint = EmployeeMuted,
                modifier = Modifier.size(21.dp),
            )
        },
        trailingIcon = {
            Icon(
                painter = painterResource(Res.drawable.iconsax_filter),
                contentDescription = null,
                tint = EmployeeBlue,
                modifier = Modifier.size(20.dp),
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        textStyle = androidx.compose.ui.text.TextStyle(
            color = EmployeeInk,
            fontFamily = oseboFontFamily(),
            fontSize = 12.sp,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = EmployeeWhite,
            unfocusedContainerColor = EmployeeWhite,
            focusedBorderColor = EmployeeBlue,
            unfocusedBorderColor = EmployeeBorder,
            cursorColor = EmployeeBlue,
        ),
    )
}

@Composable
private fun EmployeeFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) Color.White else EmployeeMuted,
        fontFamily = oseboFontFamily(),
        fontSize = 11.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        modifier = Modifier
            .background(if (selected) EmployeeInk else EmployeeWhite, RoundedCornerShape(50))
            .border(1.dp, if (selected) EmployeeInk else EmployeeBorder, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 17.dp, vertical = 10.dp),
    )
}

@Composable
private fun EmployeeSectionTitle(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            title,
            color = EmployeeInk,
            fontFamily = oseboFontFamily(),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(subtitle, color = EmployeeMuted, fontFamily = oseboFontFamily(), fontSize = 10.sp)
    }
}

@Composable
private fun EmployeeRow(employee: EmployeeItemUi, onClick: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(EmployeeSurface, RoundedCornerShape(23.dp))
            .border(1.dp, EmployeeBorder, RoundedCornerShape(23.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8EDF0)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                employee.initials,
                color = EmployeeInk,
                fontFamily = poppins,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                employee.name,
                color = EmployeeInk,
                fontFamily = poppins,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${employee.role}  •  ${employee.department}",
                color = EmployeeMuted,
                fontFamily = poppins,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val contact = employee.phone.ifBlank { employee.email }
            if (contact.isNotBlank()) {
                Text(
                    contact,
                    color = EmployeeMuted,
                    fontFamily = poppins,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(if (employee.isActive) EmployeeGreen else EmployeeMuted, CircleShape),
            )
            Spacer(Modifier.height(5.dp))
            Text(
                employee.status,
                color = if (employee.isActive) EmployeeGreen else EmployeeMuted,
                fontFamily = poppins,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun EmployeeEmptyState(
    title: String,
    message: String,
    showAdd: Boolean,
    onAddClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(EmployeeSurface, RoundedCornerShape(24.dp))
            .border(1.dp, EmployeeBorder, RoundedCornerShape(24.dp))
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(Res.drawable.iconsax_employees),
            contentDescription = null,
            tint = if (showAdd) EmployeeBlue else EmployeeRed,
            modifier = Modifier.size(33.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(title, color = EmployeeInk, fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold)
        Text(
            message,
            color = EmployeeMuted,
            fontFamily = oseboFontFamily(),
            fontSize = 10.sp,
        )
        if (showAdd) {
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onAddClick) {
                Text("Add employee", color = EmployeeBlue, fontFamily = oseboFontFamily())
            }
        }
    }
}
