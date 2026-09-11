package com.devbrian.osebo

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.devbrian.osebo.ui.screens.TransactionsScreenDemo
import com.devbrian.osebo.ui.theme.OseboColors
import com.devbrian.osebo.ui.theme.OseboTheme

/**
 * Root Compose Multiplatform entry point. Not yet wired into any Activity —
 * existing screens still render via the Fragment/View system in androidMain.
 * Screens migrate into this tree feature by feature.
 *
 * Currently renders TransactionsScreenDemo as a concrete preview of the
 * shared design system (see ui/theme and ui/components) ahead of the full
 * Compose UI migration.
 */
@Composable
fun App() {
    OseboTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = OseboColors.Background) {
            TransactionsScreenDemo()
        }
    }
}
