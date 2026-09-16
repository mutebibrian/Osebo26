package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.devbrian.osebo.ui.screens.UserRolesScreen
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.UserRolesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserRolesFragment : Fragment() {

    private val viewModel: UserRolesViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                OseboTheme {
                    UserRolesScreen(
                        state = viewModel.uiState.value,
                        onBack = { parentFragmentManager.popBackStack() },
                        onEditPermissions = viewModel::onEditPermissions,
                        onDismissEdit = viewModel::onDismissEdit,
                        onEditSearchQueryChange = viewModel::onEditSearchQueryChange,
                        onPermissionToggle = viewModel::onPermissionToggle,
                        onSaveChanges = viewModel::onSaveChanges,
                    )
                }
            }
        }
    }
}
