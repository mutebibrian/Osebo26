package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.R
import com.devbrian.osebo.ui.screens.TransfersListScreen
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.TransfersListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransfersListFragment : Fragment() {

    private val viewModel: TransfersListViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                OseboTheme {
                    TransfersListScreen(
                        state = viewModel.uiState.value,
                        onSearchQueryChange = viewModel::onSearchQueryChange,
                        onBulkTransferClick = {
                            findNavController().navigate(R.id.action_transfersList_to_createTransfer)
                        },
                        onTransferClick = {},
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTransfers()
    }
}
