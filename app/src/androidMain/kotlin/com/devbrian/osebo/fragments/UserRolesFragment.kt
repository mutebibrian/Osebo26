package com.devbrian.osebo.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.PermissionCategoryAdapter
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.models.UserRole
import com.devbrian.osebo.databinding.DialogEditPermissionsBinding
import com.devbrian.osebo.databinding.FragmentUserRolesBinding
import com.devbrian.osebo.databinding.ItemRoleBinding
import com.devbrian.osebo.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import org.koin.android.ext.android.inject

class UserRolesFragment : Fragment() {

    private var _binding: FragmentUserRolesBinding? = null
    private val binding get() = _binding!!

    private val preferenceManager: PreferenceManager by inject()

    private val apiService: ApiService by inject()

    private lateinit var rolesAdapter: RolesAdapter
    private var rolesList = mutableListOf<UserRole>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserRolesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        loadRoles()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val shopName = preferenceManager.getCurrentShopName()
        binding.tvShopName.text = if (shopName.isNotEmpty()) {
            "$shopName · User Roles"
        } else {
            "Shops · User Roles"
        }
    }

    private fun setupRecyclerView() {
        rolesAdapter = RolesAdapter(
            onEditClick = { role -> showEditPermissionsDialog(role) }
        )
        binding.rvRoles.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rolesAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadRoles() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val token = preferenceManager.getAuthToken()
                val shopUuid = preferenceManager.getCurrentShopUuid()

                if (token.isEmpty() || !isValidUUID(shopUuid)) {
                    showError("Please login again")
                    showLoading(false)
                    return@launch
                }

                
                
                val sampleRoles = createSampleRoles()
                handleRolesResponse(sampleRoles)

            } catch (e: Exception) {
                e.printStackTrace()
                showError("Failed to load roles")
                showEmptyState("Failed to load roles")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun createSampleRoles(): List<UserRole> {
        return listOf(
            UserRole(
                id = "1",
                name = "Manager",
                description = "Manager with operational privileges",
                permissions = listOf("customers_view", "sales_view", "stock_view"),
                shopId = preferenceManager.getCurrentShopUuid()
            ),
            UserRole(
                id = "2",
                name = "Staff",
                description = "Staff member with basic privileges",
                permissions = listOf("sales_view", "stock_view"),
                shopId = preferenceManager.getCurrentShopUuid()
            )
        )
    }

    private fun handleRolesResponse(roles: List<UserRole>) {
        if (roles.isNotEmpty()) {
            rolesList.clear()
            rolesList.addAll(roles)
            rolesAdapter.submitList(rolesList.toList())
            binding.rvRoles.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
        } else {
            showEmptyState("No roles found")
        }
    }

    private fun showEditPermissionsDialog(role: UserRole) {
        val dialogBinding = DialogEditPermissionsBinding.inflate(LayoutInflater.from(requireContext()))

        
        dialogBinding.tvDialogTitle.text = "Edit Permissions - ${role.name}"

        
        val permissionAdapter = PermissionCategoryAdapter { permission, isChecked ->
            
            println("${permission.displayName} is now $isChecked")
        }

        dialogBinding.rvPermissions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = permissionAdapter
        }

        
        loadPermissions(role, permissionAdapter)

        
        dialogBinding.etSearchPermissions.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                permissionAdapter.filter(s?.toString() ?: "")
            }
        })

        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        
        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnCloseDialog.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSaveChanges.setOnClickListener {
            savePermissions(role, permissionAdapter.getSelectedPermissions(), dialog)
        }

        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun loadPermissions(role: UserRole, adapter: PermissionCategoryAdapter) {
        
        val permissionCategories = listOf(
            PermissionCategory(
                name = "Customers",
                permissions = listOf(
                    PermissionItem("cust_create", "customers_create", "Customers Create", "Customers", role.permissions.contains("customers_create")),
                    PermissionItem("cust_delete", "customers_delete", "Customers Delete", "Customers", role.permissions.contains("customers_delete")),
                    PermissionItem("cust_edit", "customers_edit", "Customers Edit", "Customers", role.permissions.contains("customers_edit")),
                    PermissionItem("cust_view", "customers_view", "Customers View", "Customers", role.permissions.contains("customers_view"))
                )
            ),
            PermissionCategory(
                name = "Financial Statement",
                permissions = listOf(
                    PermissionItem("fs_create", "financial_statement_create", "Financial Statement Create", "Financial Statement", role.permissions.contains("financial_statement_create")),
                    PermissionItem("fs_delete", "financial_statement_delete", "Financial Statement Delete", "Financial Statement", role.permissions.contains("financial_statement_delete")),
                    PermissionItem("fs_edit", "financial_statement_edit", "Financial Statement Edit", "Financial Statement", role.permissions.contains("financial_statement_edit")),
                    PermissionItem("fs_view", "financial_statement_view", "Financial Statement View", "Financial Statement", role.permissions.contains("financial_statement_view"))
                )
            ),
            PermissionCategory(
                name = "Stock",
                permissions = listOf(
                    PermissionItem("stock_create", "stock_create", "Stock Create", "Stock", role.permissions.contains("stock_create")),
                    PermissionItem("stock_delete", "stock_delete", "Stock Delete", "Stock", role.permissions.contains("stock_delete")),
                    PermissionItem("stock_edit", "stock_edit", "Stock Edit", "Stock", role.permissions.contains("stock_edit")),
                    PermissionItem("stock_view", "stock_view", "Stock View", "Stock", role.permissions.contains("stock_view"))
                )
            ),
            PermissionCategory(
                name = "Expense Categories",
                permissions = listOf(
                    PermissionItem("exp_cat_create", "expense_categories_create", "Expense Categories Create", "Expense Categories", role.permissions.contains("expense_categories_create")),
                    PermissionItem("exp_cat_delete", "expense_categories_delete", "Expense Categories Delete", "Expense Categories", role.permissions.contains("expense_categories_delete")),
                    PermissionItem("exp_cat_edit", "expense_categories_edit", "Expense Categories Edit", "Expense Categories", role.permissions.contains("expense_categories_edit")),
                    PermissionItem("exp_cat_view", "expense_categories_view", "Expense Categories View", "Expense Categories", role.permissions.contains("expense_categories_view"))
                )
            ),
            PermissionCategory(
                name = "Procurement",
                permissions = listOf(
                    PermissionItem("proc_create", "procurement_create", "Procurement Create", "Procurement", role.permissions.contains("procurement_create")),
                    PermissionItem("proc_delete", "procurement_delete", "Procurement Delete", "Procurement", role.permissions.contains("procurement_delete")),
                    PermissionItem("proc_edit", "procurement_edit", "Procurement Edit", "Procurement", role.permissions.contains("procurement_edit")),
                    PermissionItem("proc_view", "procurement_view", "Procurement View", "Procurement", role.permissions.contains("procurement_view"))
                )
            ),
            PermissionCategory(
                name = "Sales",
                permissions = listOf(
                    PermissionItem("sales_create", "sales_create", "Sales Create", "Sales", role.permissions.contains("sales_create")),
                    PermissionItem("sales_delete", "sales_delete", "Sales Delete", "Sales", role.permissions.contains("sales_delete")),
                    PermissionItem("sales_edit", "sales_edit", "Sales Edit", "Sales", role.permissions.contains("sales_edit")),
                    PermissionItem("sales_view", "sales_view", "Sales View", "Sales", role.permissions.contains("sales_view"))
                )
            ),
            PermissionCategory(
                name = "Stock Categories",
                permissions = listOf(
                    PermissionItem("stock_cat_create", "stock_categories_create", "Stock Categories Create", "Stock Categories", role.permissions.contains("stock_categories_create")),
                    PermissionItem("stock_cat_delete", "stock_categories_delete", "Stock Categories Delete", "Stock Categories", role.permissions.contains("stock_categories_delete")),
                    PermissionItem("stock_cat_edit", "stock_categories_edit", "Stock Categories Edit", "Stock Categories", role.permissions.contains("stock_categories_edit")),
                    PermissionItem("stock_cat_view", "stock_categories_view", "Stock Categories View", "Stock Categories", role.permissions.contains("stock_categories_view"))
                )
            ),
            PermissionCategory(
                name = "Suppliers",
                permissions = listOf(
                    PermissionItem("supp_create", "suppliers_create", "Suppliers Create", "Suppliers", role.permissions.contains("suppliers_create")),
                    PermissionItem("supp_delete", "suppliers_delete", "Suppliers Delete", "Suppliers", role.permissions.contains("suppliers_delete")),
                    PermissionItem("supp_edit", "suppliers_edit", "Suppliers Edit", "Suppliers", role.permissions.contains("suppliers_edit")),
                    PermissionItem("supp_view", "suppliers_view", "Suppliers View", "Suppliers", role.permissions.contains("suppliers_view"))
                )
            )
        )

        adapter.submitList(permissionCategories)
    }

    private fun savePermissions(role: UserRole, selectedPermissions: List<PermissionItem>, dialog: AlertDialog) {
        lifecycleScope.launch {
            try {
                dialog.findViewById<View>(R.id.progressBar)?.visibility = View.VISIBLE
                dialog.findViewById<View>(R.id.btnSaveChanges)?.isEnabled = false

                
                delay(1000)

                val permissionNames = selectedPermissions.map { it.name }
                Toast.makeText(requireContext(), "Permissions updated successfully", Toast.LENGTH_SHORT).show()
                dialog.dismiss()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to save permissions", Toast.LENGTH_SHORT).show()
            } finally {
                dialog.findViewById<View>(R.id.progressBar)?.visibility = View.GONE
                dialog.findViewById<View>(R.id.btnSaveChanges)?.isEnabled = true
            }
        }
    }

    private fun showEmptyState(message: String) {
        binding.tvEmpty.text = message
        binding.emptyState.visibility = View.VISIBLE
        binding.rvRoles.visibility = View.GONE
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.rvRoles.visibility = View.GONE
            binding.emptyState.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    private fun isValidUUID(uuid: String): Boolean {
        return try {
            UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class RolesAdapter(
        private val onEditClick: (UserRole) -> Unit
    ) : RecyclerView.Adapter<RolesAdapter.RoleViewHolder>() {

        private var roles = listOf<UserRole>()

        fun submitList(newList: List<UserRole>) {
            roles = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoleViewHolder {
            val binding = ItemRoleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return RoleViewHolder(binding)
        }

        override fun onBindViewHolder(holder: RoleViewHolder, position: Int) {
            holder.bind(roles[position])
        }

        override fun getItemCount(): Int = roles.size

        inner class RoleViewHolder(
            private val binding: ItemRoleBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind(role: UserRole) {
                binding.tvRoleName.text = role.name
                binding.tvRoleDescription.text = role.description ?: "No description"
                binding.btnEditPermissions.setOnClickListener { onEditClick(role) }
            }
        }
    }
}
