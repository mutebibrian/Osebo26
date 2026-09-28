package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.ui.screens.CreateTransferScreen
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.CreateTransferViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateTransferFragment : Fragment() {

    private val viewModel: CreateTransferViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                OseboTheme {
                    CreateTransferScreen(
                        state = viewModel.uiState.value,
                        onBack = { findNavController().navigateUp() },
                        onTargetShopSelected = viewModel::onTargetShopSelected,
                        onMethodChange = viewModel::onMethodChange,
                        onDownloadTemplateClick = viewModel::onDownloadTemplateClick,
                        onChooseFileClick = viewModel::onChooseFileClick,
                        onSearchQueryChange = viewModel::onSearchQueryChange,
                        onItemToggle = viewModel::onItemToggle,
                        onItemQtyChange = viewModel::onItemQtyChange,
                        onSubmit = viewModel::onSubmit,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.transferComplete.observe(viewLifecycleOwner) { done ->
            if (done) {
                Toast.makeText(requireContext(), "Transfer submitted", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }
}
