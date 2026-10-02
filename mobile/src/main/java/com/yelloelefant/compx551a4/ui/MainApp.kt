package com.yelloelefant.compx551a4.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yelloelefant.compx551a4.ui.screens.DashboardScreen
import com.yelloelefant.compx551a4.ui.screens.DeviceSettingsScreen
import com.yelloelefant.compx551a4.ui.screens.SessionHistoryScreen
import com.yelloelefant.compx551a4.ui.screens.TrendAnalyticsScreen
import com.yelloelefant.compx551a4.viewmodel.MainViewModel

enum class NavigationTab(
    val title: String,
    val icon: ImageVector,
) {
    DASHBOARD("Dashboard", Icons.Default.MonitorHeart),
    HISTORY("History", Icons.Default.History),
    TRENDS("Trends", Icons.Default.Assessment),
    SETTINGS("Device", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: MainViewModel = viewModel(),
) {
    var selectedTabItem by remember { mutableIntStateOf(0) }
    val currentTab = NavigationTab.entries[selectedTabItem]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Polar H10 • ${currentTab.title}",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabItem == index,
                        onClick = { selectedTabItem = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            NavigationTab.DASHBOARD -> DashboardScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )

            NavigationTab.HISTORY -> SessionHistoryScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )

            NavigationTab.TRENDS -> TrendAnalyticsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )

            NavigationTab.SETTINGS -> DeviceSettingsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
