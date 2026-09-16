package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.devbrian.osebo.ui.screens.CustomersScreen
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.CustomerViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CustomersFragment : Fragment() {

    private val viewModel: CustomerViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                OseboTheme {
                    CustomersScreen(
                        state = viewModel.uiState.value,
                        onSearchQueryChange = viewModel::onSearchQueryChange,
                        onAddClick = viewModel::onAddClick,
                        onViewDetails = viewModel::onViewDetails,
                        onEditClick = viewModel::onEditClick,
                        onDeleteClick = viewModel::onDeleteClick,
                        onDismissDetails = viewModel::onDismissDetails,
                        onDismissDelete = viewModel::onDismissDelete,
                        onConfirmDelete = viewModel::onConfirmDelete,
                        onDismissAddDialog = viewModel::onDismissAddDialog,
                        onAddFormChange = viewModel::onAddFormChange,
                        onSubmitAdd = viewModel::onSubmitAdd,
                        onDismissEditDialog = viewModel::onDismissEditDialog,
                        onEditFormChange = viewModel::onEditFormChange,
                        onSubmitEdit = viewModel::onSubmitEdit,
                        onMessageShown = viewModel::clearMessages,
                    )
                }
            }
        }
    }
}
