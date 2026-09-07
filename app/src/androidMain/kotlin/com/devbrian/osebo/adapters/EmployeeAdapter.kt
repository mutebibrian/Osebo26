package com.devbrian.osebo.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.models.Employee
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class EmployeeAdapter(
    private val onItemClick: (Employee) -> Unit = { _ -> },
    private val onMoreOptionsClick: (Employee, View) -> Unit = { _, _ -> },
    private val onViewProfileClick: (Employee) -> Unit = { _ -> },
    private val onViewAttendanceClick: (Employee) -> Unit = { _ -> }
) : ListAdapter<Employee, EmployeeAdapter.EmployeeViewHolder>(EmployeeDiffCallback()) {

    var showActionButtons: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isSelectionMode: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val selectedEmployees = mutableSetOf<String>()

    fun toggleSelection(employeeId: String) {
        if (selectedEmployees.contains(employeeId)) {
            selectedEmployees.remove(employeeId)
        } else {
            selectedEmployees.add(employeeId)
        }
        notifyDataSetChanged()
    }

    fun getSelectedEmployees(): List<Employee> {
        return currentList.filter { selectedEmployees.contains(it.id) }
    }

    fun clearSelection() {
        selectedEmployees.clear()
        notifyDataSetChanged()
    }

    fun selectAll() {
        selectedEmployees.clear()
        selectedEmployees.addAll(currentList.map { it.id })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_employee, parent, false)
        return EmployeeViewHolder(
            view,
            onItemClick,
            onMoreOptionsClick,
            onViewProfileClick,
            onViewAttendanceClick,
            showActionButtons,
            isSelectionMode,
            selectedEmployees
        )
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            holder.bindPartialUpdate(getItem(position), payloads)
        }
    }

    class EmployeeViewHolder(
        itemView: View,
        private val onItemClick: (Employee) -> Unit,
        private val onMoreOptionsClick: (Employee, View) -> Unit,
        private val onViewProfileClick: (Employee) -> Unit,
        private val onViewAttendanceClick: (Employee) -> Unit,
        private val showActionButtons: Boolean,
        private val isSelectionMode: Boolean,
        private val selectedEmployees: Set<String>
    ) : RecyclerView.ViewHolder(itemView) {

        private val llEmployeeAvatar: LinearLayout = itemView.findViewById(R.id.ll_employee_avatar)
        private val tvEmployeeInitial: TextView = itemView.findViewById(R.id.tv_employee_initial)
        private val tvEmployeeName: TextView = itemView.findViewById(R.id.tv_employee_name)
        private val tvEmployeeRole: TextView = itemView.findViewById(R.id.tv_employee_role)
        private val tvEmployeeDepartment: TextView = itemView.findViewById(R.id.tv_employee_department)
        private val tvEmployeeStatus: TextView = itemView.findViewById(R.id.tv_employee_status)
        private val tvEmployeeEmail: TextView = itemView.findViewById(R.id.tv_employee_email)
        private val tvEmployeePhone: TextView = itemView.findViewById(R.id.tv_employee_phone)
        private val tvHireDate: TextView = itemView.findViewById(R.id.tv_hire_date)
        private val tvEmployeeSalary: TextView = itemView.findViewById(R.id.tv_employee_salary)
        private val ivMoreOptions: ImageView = itemView.findViewById(R.id.iv_more_options)
        private val llActionButtons: LinearLayout = itemView.findViewById(R.id.ll_action_buttons)
        private val tvViewProfile: TextView = itemView.findViewById(R.id.tv_view_profile)
        private val tvViewAttendance: TextView = itemView.findViewById(R.id.tv_view_attendance)
        private val ivSelectionCheck: ImageView = itemView.findViewById(R.id.iv_selection_check)

        private var currentEmployee: Employee? = null
        private var isExpanded: Boolean = false

        init {
            
            itemView.setOnClickListener {
                currentEmployee?.let { employee ->
                    if (isSelectionMode) {
                        
                        onItemClick(employee)
                    } else {
                        
                        toggleExpansion()
                        onItemClick(employee)
                    }
                }
            }

            
            llEmployeeAvatar.setOnClickListener {
                currentEmployee?.let { employee ->
                    onViewProfileClick(employee)
                }
            }

            
            ivMoreOptions.setOnClickListener { view ->
                currentEmployee?.let { employee ->
                    onMoreOptionsClick(employee, view)
                }
            }

            
            tvViewProfile.setOnClickListener {
                currentEmployee?.let { employee ->
                    onViewProfileClick(employee)
                }
            }

            
            tvViewAttendance.setOnClickListener {
                currentEmployee?.let { employee ->
                    onViewAttendanceClick(employee)
                }
            }

            
            ivSelectionCheck?.setOnClickListener {
                currentEmployee?.let { employee ->
                    onItemClick(employee)
                }
            }

            
            llActionButtons.visibility = if (showActionButtons && isExpanded) View.VISIBLE else View.GONE
        }

        fun bind(employee: Employee) {
            currentEmployee = employee

            
            val initial = employee.name.firstOrNull()?.toString()?.uppercase() ?: "?"
            tvEmployeeInitial.text = initial

            
            llEmployeeAvatar.setBackgroundColor(getAvatarColor(employee.name))

            
            tvEmployeeName.text = employee.name
            tvEmployeeRole.text = Employee.getRoleDisplayText(employee.role)
            tvEmployeeDepartment.text = employee.department
            tvEmployeeEmail.text = employee.email
            tvEmployeePhone.text = employee.phone
            tvHireDate.text = employee.hireDate ?: "Not set"

            
            tvEmployeeSalary.text = if (employee.salary != null) {
                formatCurrency(employee.salary)
            } else {
                "Not set"
            }

            
            setEmployeeStatus(employee.status)

            
            applyRoleBasedStyling(employee.role)

            
            if (isNewEmployee(employee.hireDate)) {
                itemView.setBackgroundResource(R.drawable.bg_new_employee)
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT)
            }

            
            val isManager = employee.role.equals(Employee.ROLE_MANAGER, ignoreCase = true)
            tvEmployeeSalary.visibility = if (isManager) View.VISIBLE else View.GONE

            
            if (isSelectionMode) {
                ivSelectionCheck.visibility = View.VISIBLE
                ivMoreOptions.visibility = View.GONE
                ivSelectionCheck.setImageResource(
                    if (selectedEmployees.contains(employee.id)) {
                        R.drawable.ic_check_circle
                    } else {
                        R.drawable.ic_radio_button_unchecked
                    }
                )
            } else {
                ivSelectionCheck.visibility = View.GONE
                ivMoreOptions.visibility = View.VISIBLE
            }

            
            llActionButtons.visibility = if (showActionButtons && isExpanded) View.VISIBLE else View.GONE
        }

        fun bindPartialUpdate(employee: Employee, payloads: List<Any>) {
            currentEmployee = employee

            payloads.forEach { payload ->
                when (payload) {
                    "status" -> setEmployeeStatus(employee.status)
                    "selection" -> {
                        if (isSelectionMode) {
                            ivSelectionCheck.setImageResource(
                                if (selectedEmployees.contains(employee.id)) {
                                    R.drawable.ic_check_circle
                                } else {
                                    R.drawable.ic_radio_button_unchecked
                                }
                            )
                        }
                    }
                }
            }
        }

        private fun toggleExpansion() {
            isExpanded = !isExpanded
            llActionButtons.visibility = if (showActionButtons && isExpanded) View.VISIBLE else View.GONE

            
            if (isExpanded) {
                llActionButtons.alpha = 0f
                llActionButtons.animate().alpha(1f).setDuration(200).start()
            }
        }

        private fun setEmployeeStatus(status: String) {
            val context = itemView.context
            val statusText = Employee.getStatusDisplayText(status)
            tvEmployeeStatus.text = statusText

            val (backgroundRes, textColorRes) = when (status.uppercase()) {
                Employee.STATUS_ACTIVE -> Pair(R.drawable.bg_status_active, R.color.white)
                Employee.STATUS_INACTIVE -> Pair(R.drawable.bg_status_inactive, R.color.white)
                Employee.STATUS_ON_LEAVE -> Pair(R.drawable.bg_status_on_leave, R.color.white)
                Employee.STATUS_SUSPENDED -> Pair(R.drawable.bg_status_suspended, R.color.white)
                Employee.STATUS_TERMINATED -> Pair(R.drawable.bg_status_terminated, R.color.white)
                else -> Pair(R.drawable.bg_status_inactive, R.color.white)
            }

            tvEmployeeStatus.setBackgroundResource(backgroundRes)
            tvEmployeeStatus.setTextColor(ContextCompat.getColor(context, textColorRes))
        }

        private fun applyRoleBasedStyling(role: String) {
            val context = itemView.context
            val roleColorRes = when (role.uppercase()) {
                Employee.ROLE_MANAGER -> R.color.role_manager
                Employee.ROLE_SUPERVISOR -> R.color.role_supervisor
                Employee.ROLE_CASHIER -> R.color.role_cashier
                Employee.ROLE_SALES -> R.color.role_sales
                Employee.ROLE_INVENTORY -> R.color.role_inventory
                else -> R.color.role_staff
            }

            tvEmployeeRole.setTextColor(ContextCompat.getColor(context, roleColorRes))

            
            val avatarColor = when (role.uppercase()) {
                Employee.ROLE_MANAGER -> Color.parseColor("#9C27B0")
                Employee.ROLE_SUPERVISOR -> Color.parseColor("#2196F3")
                Employee.ROLE_CASHIER -> Color.parseColor("#FF9800")
                Employee.ROLE_SALES -> Color.parseColor("#FF5722")
                Employee.ROLE_INVENTORY -> Color.parseColor("#E91E63")
                Employee.ROLE_STAFF -> Color.parseColor("#4CAF50")
                else -> getAvatarColor(currentEmployee?.name ?: "")
            }

            llEmployeeAvatar.setBackgroundColor(avatarColor)
        }

        private fun getAvatarColor(name: String): Int {
            val colors = listOf(
                Color.parseColor("#FF6B6B"), 
                Color.parseColor("#4ECDC4"), 
                Color.parseColor("#FFD166"), 
                Color.parseColor("#06D6A0"), 
                Color.parseColor("#118AB2"), 
                Color.parseColor("#EF476F"), 
                Color.parseColor("#073B4C"), 
                Color.parseColor("#7209B7"), 
                Color.parseColor("#F72585"), 
                Color.parseColor("#3A86FF")  
            )

            val index = name.hashCode() % colors.size
            return colors[Math.abs(index)]
        }

        private fun formatCurrency(amount: Double): String {
            return when {
                amount >= 1000000 -> String.format("UGX %.1fM", amount / 1000000)
                amount >= 1000 -> String.format("UGX %.1fK", amount / 1000)
                else -> String.format("UGX %.0f", amount)
            }
        }

        private fun isNewEmployee(hireDate: String?): Boolean {
            if (hireDate == null) return false

            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val hireDateObj = dateFormat.parse(hireDate)
                val currentDate = Date()

                val diffInMillis = currentDate.time - (hireDateObj?.time ?: 0)
                val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

                return diffInDays <= 30 
            } catch (e: Exception) {
                return false
            }
        }
    }

    private class EmployeeDiffCallback : DiffUtil.ItemCallback<Employee>() {
        override fun areItemsTheSame(oldItem: Employee, newItem: Employee): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Employee, newItem: Employee): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Employee, newItem: Employee): Any? {
            val payloads = mutableListOf<String>()

            if (oldItem.status != newItem.status) payloads.add("status")
            if (oldItem.name != newItem.name) payloads.add("name")
            if (oldItem.role != newItem.role) payloads.add("role")
            if (oldItem.salary != newItem.salary) payloads.add("salary")
            if (oldItem.department != newItem.department) payloads.add("department")

            return if (payloads.isNotEmpty()) payloads else null
        }
    }

    

    fun getEmployeeAtPosition(position: Int): Employee? {
        return if (position in 0 until itemCount) {
            getItem(position)
        } else {
            null
        }
    }

    fun filterEmployees(query: String): List<Employee> {
        return if (query.isEmpty()) {
            currentList
        } else {
            currentList.filter { employee ->
                employee.name.contains(query, ignoreCase = true) ||
                        employee.email.contains(query, ignoreCase = true) ||
                        employee.phone.contains(query, ignoreCase = true) ||
                        employee.role.contains(query, ignoreCase = true) ||
                        employee.department.contains(query, ignoreCase = true)
            }
        }
    }

    fun filterByStatus(status: String): List<Employee> {
        return currentList.filter { it.status.equals(status, ignoreCase = true) }
    }

    fun filterByRole(role: String): List<Employee> {
        return currentList.filter { it.role.equals(role, ignoreCase = true) }
    }

    fun filterByDepartment(department: String): List<Employee> {
        return currentList.filter { it.department.equals(department, ignoreCase = true) }
    }

    fun sortByName(ascending: Boolean = true): List<Employee> {
        return if (ascending) {
            currentList.sortedBy { it.name }
        } else {
            currentList.sortedByDescending { it.name }
        }
    }

    fun sortByHireDate(ascending: Boolean = true): List<Employee> {
        return if (ascending) {
            currentList.sortedBy { it.hireDate }
        } else {
            currentList.sortedByDescending { it.hireDate }
        }
    }

    fun sortBySalary(ascending: Boolean = true): List<Employee> {
        return if (ascending) {
            currentList.sortedBy { it.salary ?: 0.0 }
        } else {
            currentList.sortedByDescending { it.salary ?: 0.0 }
        }
    }

    fun getActiveEmployees(): List<Employee> {
        return currentList.filter { it.status.equals(Employee.STATUS_ACTIVE, ignoreCase = true) }
    }

    fun getInactiveEmployees(): List<Employee> {
        return currentList.filter { it.status.equals(Employee.STATUS_INACTIVE, ignoreCase = true) }
    }

    fun getManagers(): List<Employee> {
        return currentList.filter { it.role.equals(Employee.ROLE_MANAGER, ignoreCase = true) }
    }

    fun getStaff(): List<Employee> {
        return currentList.filter { !it.role.equals(Employee.ROLE_MANAGER, ignoreCase = true) }
    }

    fun getNewEmployees(): List<Employee> {
        return currentList.filter { employee ->
            employee.hireDate?.let { hireDate ->
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val hireDateObj = dateFormat.parse(hireDate)
                    val currentDate = Date()

                    val diffInMillis = currentDate.time - (hireDateObj?.time ?: 0)
                    val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

                    diffInDays <= 30
                } catch (e: Exception) {
                    false
                }
            } ?: false
        }
    }

    fun getTotalSalary(): Double {
        return currentList.sumOf { it.salary ?: 0.0 }
    }

    fun getAverageSalary(): Double {
        val employeesWithSalary = currentList.filter { it.salary != null }
        return if (employeesWithSalary.isEmpty()) 0.0 else {
            employeesWithSalary.sumOf { it.salary!! } / employeesWithSalary.size
        }
    }

    fun getDepartmentCount(): Map<String, Int> {
        return currentList.groupingBy { it.department }.eachCount()
    }

    fun getRoleCount(): Map<String, Int> {
        return currentList.groupingBy { it.role }.eachCount()
    }

    fun getStatusCount(): Map<String, Int> {
        return currentList.groupingBy { it.status }.eachCount()
    }

    
    fun submitEmployeeList(employees: List<Employee>) {
        submitList(employees)
    }

    
    fun clearAll() {
        submitList(emptyList())
        clearSelection()
    }
}


