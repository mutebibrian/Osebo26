package com.devbrian.osebo.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.models.UserRole
import com.devbrian.osebo.ui.screens.EditPermissionsState
import com.devbrian.osebo.ui.screens.PermissionCategoryUi
import com.devbrian.osebo.ui.screens.PermissionItemUi
import com.devbrian.osebo.ui.screens.UserRoleUi
import com.devbrian.osebo.ui.screens.UserRolesUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

// NOTE: roles are sample/placeholder data and Save Changes doesn't persist anywhere yet —
// this mirrors the pre-Compose UserRolesFragment exactly (it never called a real backend
// endpoint either; a `by inject() ApiService` field sat there unused). Wire this up to a
// real roles/permissions API once one exists.
class UserRolesViewModel(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = mutableStateOf(UserRolesUiState())
    val uiState: State<UserRolesUiState> = _uiState

    private var roles: List<UserRole> = emptyList()
    private var allCategoriesForEdit: List<PermissionCategoryUi> = emptyList()

    init {
        loadRoles()
    }

    private fun loadRoles() {
        update { it.copy(isLoading = true, shopLabel = shopLabel()) }
        viewModelScope.launch {
            try {
                val token = preferenceManager.getAuthToken()
                val shopUuid = preferenceManager.getCurrentShopUuid()
                if (token.isEmpty() || !isValidUUID(shopUuid)) {
                    update { it.copy(isLoading = false, emptyMessage = "Please login again") }
                    return@launch
                }

                roles = sampleRoles(shopUuid)
                update {
                    it.copy(
                        isLoading = false,
                        roles = roles.map(::toUi),
                        emptyMessage = if (roles.isEmpty()) "No roles found" else null,
                    )
                }
            } catch (e: Exception) {
                update { it.copy(isLoading = false, emptyMessage = "Failed to load roles") }
            }
        }
    }

    private fun sampleRoles(shopUuid: String): List<UserRole> = listOf(
        UserRole(
            id = "1",
            name = "Manager",
            description = "Manager with operational privileges",
            permissions = listOf("customers_view", "sales_view", "stock_view"),
            shopId = shopUuid,
        ),
        UserRole(
            id = "2",
            name = "Staff",
            description = "Staff member with basic privileges",
            permissions = listOf("sales_view", "stock_view"),
            shopId = shopUuid,
        ),
    )

    private fun shopLabel(): String {
        val name = preferenceManager.getCurrentShopName()
        return name.ifEmpty { "" }
    }

    // ----- Edit permissions dialog -----

    fun onEditPermissions(role: UserRoleUi) {
        val fullRole = roles.find { it.id == role.id } ?: return
        allCategoriesForEdit = permissionCategoriesFor(fullRole)
        update { it.copy(editState = EditPermissionsState(role = role, categories = allCategoriesForEdit)) }
    }

    fun onDismissEdit() {
        update { it.copy(editState = null) }
    }

    fun onEditSearchQueryChange(query: String) {
        val edit = _uiState.value.editState ?: return
        val filtered = if (query.isBlank()) {
            allCategoriesForEdit
        } else {
            allCategoriesForEdit.mapNotNull { category ->
                val matches = category.permissions.filter {
                    it.displayName.contains(query, ignoreCase = true) || it.name.contains(query, ignoreCase = true)
                }
                if (matches.isNotEmpty()) category.copy(permissions = matches) else null
            }
        }
        update { it.copy(editState = edit.copy(searchQuery = query, categories = filtered)) }
    }

    fun onPermissionToggle(categoryName: String, permissionId: String) {
        allCategoriesForEdit = allCategoriesForEdit.map { category ->
            if (category.name != categoryName) return@map category
            category.copy(
                permissions = category.permissions.map { permission ->
                    if (permission.id == permissionId) permission.copy(isChecked = !permission.isChecked) else permission
                },
            )
        }
        // re-apply the current search filter so the toggle is reflected in the visible (possibly filtered) list too
        onEditSearchQueryChange(_uiState.value.editState?.searchQuery.orEmpty())
    }

    fun onSaveChanges() {
        val edit = _uiState.value.editState ?: return
        viewModelScope.launch {
            update { it.copy(editState = edit.copy(isSaving = true)) }
            delay(1000)
            update { it.copy(editState = null) }
        }
    }

    private fun permissionCategoriesFor(role: UserRole): List<PermissionCategoryUi> {
        fun item(id: String, name: String, displayName: String) =
            PermissionItemUi(id = id, name = name, displayName = displayName, isChecked = role.permissions.contains(name))

        return listOf(
            PermissionCategoryUi("Customers", listOf(
                item("cust_create", "customers_create", "Customers Create"),
                item("cust_delete", "customers_delete", "Customers Delete"),
                item("cust_edit", "customers_edit", "Customers Edit"),
                item("cust_view", "customers_view", "Customers View"),
            )),
            PermissionCategoryUi("Financial Statement", listOf(
                item("fs_create", "financial_statement_create", "Financial Statement Create"),
                item("fs_delete", "financial_statement_delete", "Financial Statement Delete"),
                item("fs_edit", "financial_statement_edit", "Financial Statement Edit"),
                item("fs_view", "financial_statement_view", "Financial Statement View"),
            )),
            PermissionCategoryUi("Stock", listOf(
                item("stock_create", "stock_create", "Stock Create"),
                item("stock_delete", "stock_delete", "Stock Delete"),
                item("stock_edit", "stock_edit", "Stock Edit"),
                item("stock_view", "stock_view", "Stock View"),
            )),
            PermissionCategoryUi("Expense Categories", listOf(
                item("exp_cat_create", "expense_categories_create", "Expense Categories Create"),
                item("exp_cat_delete", "expense_categories_delete", "Expense Categories Delete"),
                item("exp_cat_edit", "expense_categories_edit", "Expense Categories Edit"),
                item("exp_cat_view", "expense_categories_view", "Expense Categories View"),
            )),
            PermissionCategoryUi("Procurement", listOf(
                item("proc_create", "procurement_create", "Procurement Create"),
                item("proc_delete", "procurement_delete", "Procurement Delete"),
                item("proc_edit", "procurement_edit", "Procurement Edit"),
                item("proc_view", "procurement_view", "Procurement View"),
            )),
            PermissionCategoryUi("Sales", listOf(
                item("sales_create", "sales_create", "Sales Create"),
                item("sales_delete", "sales_delete", "Sales Delete"),
                item("sales_edit", "sales_edit", "Sales Edit"),
                item("sales_view", "sales_view", "Sales View"),
            )),
            PermissionCategoryUi("Stock Categories", listOf(
                item("stock_cat_create", "stock_categories_create", "Stock Categories Create"),
                item("stock_cat_delete", "stock_categories_delete", "Stock Categories Delete"),
                item("stock_cat_edit", "stock_categories_edit", "Stock Categories Edit"),
                item("stock_cat_view", "stock_categories_view", "Stock Categories View"),
            )),
            PermissionCategoryUi("Suppliers", listOf(
                item("supp_create", "suppliers_create", "Suppliers Create"),
                item("supp_delete", "suppliers_delete", "Suppliers Delete"),
                item("supp_edit", "suppliers_edit", "Suppliers Edit"),
                item("supp_view", "suppliers_view", "Suppliers View"),
            )),
        )
    }

    private fun toUi(role: UserRole) = UserRoleUi(id = role.id, name = role.name, description = role.description)

    private fun isValidUUID(uuid: String): Boolean = try {
        UUID.fromString(uuid)
        true
    } catch (e: IllegalArgumentException) {
        false
    }

    private fun update(transform: (UserRolesUiState) -> UserRolesUiState) {
        _uiState.value = transform(_uiState.value)
    }
}
