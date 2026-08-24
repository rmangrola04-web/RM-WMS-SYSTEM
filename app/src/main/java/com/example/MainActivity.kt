package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.Warehouse
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleConstants
import com.example.ui.FormEvent
import com.example.ui.WarehouseViewModel
import com.example.ui.components.AuthDialog
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.DownloadReportDialog
import com.example.ui.screens.ComplianceDashboardScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DockBaysScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.VehicleFormScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WarehouseNavy
import com.example.ui.theme.WarehouseSteelBlue
import kotlinx.coroutines.flow.collectLatest

enum class AppScreen(val titleEnglish: String) {
    DASHBOARD("Live Yard"),
    FORM("Gate Entry"),
    DOCKS("Dock Bays"),
    COMPLIANCE("Compliance"),
    REPORTS("Reports")
}

class MainActivity : ComponentActivity() {
    private val viewModel: WarehouseViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
                val snackbarHostState = remember { SnackbarHostState() }
                var showGlobalScanner by remember { mutableStateOf(false) }
                var showAuthDialog by remember { mutableStateOf(false) }
                var showDownloadDialog by remember { mutableStateOf(false) }
                val currentUser by viewModel.currentUser.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.eventFlow.collectLatest { event ->
                        when (event) {
                            is FormEvent.Success -> snackbarHostState.showSnackbar(event.message)
                            is FormEvent.Error -> snackbarHostState.showSnackbar(event.message)
                        }
                    }
                }

                if (currentUser == null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = {
                                currentScreen = AppScreen.DASHBOARD
                            }
                        )
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        topBar = {
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.White.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.LocalShipping,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Warehouse Vehicle System",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.White
                                            )
                                            val user = currentUser
                                            Text(
                                                text = if (user != null) {
                                                    "👤 ${user.fullName} (${user.category})" + (if (user.mobileNumber.isNotBlank()) " | 📱 ${user.mobileNumber}" else "")
                                                } else {
                                                    "Guest Operator"
                                                },
                                                fontSize = 10.5.sp,
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = WarehouseNavy,
                                    titleContentColor = Color.White
                                ),
                                actions = {
                                    IconButton(
                                        onClick = { showAuthDialog = true },
                                        modifier = Modifier.testTag("topbar_auth_button")
                                    ) {
                                        Icon(
                                            if (currentUser != null) Icons.Default.AccountCircle else Icons.Default.Person,
                                            contentDescription = "User Auth Profile",
                                            tint = if (currentUser != null) Color(0xFF6EE7B7) else Color.White
                                        )
                                    }

                                    IconButton(
                                        onClick = { showDownloadDialog = true },
                                        modifier = Modifier.testTag("topbar_download_button")
                                    ) {
                                        Icon(
                                            Icons.Default.FileDownload,
                                            contentDescription = "Download Report",
                                            tint = Color.White
                                        )
                                    }

                                    IconButton(
                                        onClick = { showGlobalScanner = true },
                                        modifier = Modifier.testTag("topbar_qr_scan_button")
                                    ) {
                                        Icon(
                                            Icons.Default.QrCodeScanner,
                                            contentDescription = "Scan Gate Pass QR",
                                            tint = Color.White
                                        )
                                    }

                                    if (currentScreen != AppScreen.FORM) {
                                        IconButton(
                                            onClick = {
                                                viewModel.startNewEntry()
                                                currentScreen = AppScreen.FORM
                                            },
                                            modifier = Modifier.testTag("topbar_add_button")
                                        ) {
                                            Icon(
                                                Icons.Default.AddBox,
                                                contentDescription = "New Entry",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 6.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.DASHBOARD,
                                    onClick = { currentScreen = AppScreen.DASHBOARD },
                                    icon = {
                                        Icon(
                                            if (currentScreen == AppScreen.DASHBOARD) Icons.Filled.DirectionsCar else Icons.Outlined.DirectionsCar,
                                            contentDescription = "Live Yard"
                                        )
                                    },
                                    label = { Text("Live Yard", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = WarehouseNavy,
                                        indicatorColor = WarehouseNavy.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_dashboard")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.FORM,
                                    onClick = {
                                        if (currentScreen != AppScreen.FORM) {
                                            viewModel.startNewEntry()
                                        }
                                        currentScreen = AppScreen.FORM
                                    },
                                    icon = {
                                        Icon(
                                            if (currentScreen == AppScreen.FORM) Icons.Filled.AddBox else Icons.Outlined.AddBox,
                                            contentDescription = "Gate Entry"
                                        )
                                    },
                                    label = { Text("Gate Entry", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = WarehouseNavy,
                                        indicatorColor = WarehouseNavy.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_form")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.DOCKS,
                                    onClick = { currentScreen = AppScreen.DOCKS },
                                    icon = {
                                        Icon(
                                            if (currentScreen == AppScreen.DOCKS) Icons.Filled.Warehouse else Icons.Outlined.Warehouse,
                                            contentDescription = "Dock Bays"
                                        )
                                    },
                                    label = { Text("Dock Bays", fontSize = 10.5.sp, fontWeight = FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = WarehouseNavy,
                                        indicatorColor = WarehouseNavy.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_docks")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.COMPLIANCE,
                                    onClick = { currentScreen = AppScreen.COMPLIANCE },
                                    icon = {
                                        Icon(
                                            if (currentScreen == AppScreen.COMPLIANCE) Icons.Filled.FactCheck else Icons.Outlined.FactCheck,
                                            contentDescription = "Compliance"
                                        )
                                    },
                                    label = { Text("Compliance", fontSize = 10.5.sp, fontWeight = FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = WarehouseNavy,
                                        indicatorColor = WarehouseNavy.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_compliance")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.REPORTS,
                                    onClick = { currentScreen = AppScreen.REPORTS },
                                    icon = {
                                        Icon(
                                            if (currentScreen == AppScreen.REPORTS) Icons.Filled.Assessment else Icons.Outlined.Assessment,
                                            contentDescription = "Reports"
                                        )
                                    },
                                    label = { Text("Reports", fontSize = 10.5.sp, fontWeight = FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = WarehouseNavy,
                                        indicatorColor = WarehouseNavy.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_reports")
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "ScreenTransition"
                            ) { screen ->
                                when (screen) {
                                    AppScreen.DASHBOARD -> DashboardScreen(
                                        viewModel = viewModel,
                                        onNavigateToNewEntry = { currentScreen = AppScreen.FORM },
                                        onEditEntry = { currentScreen = AppScreen.FORM }
                                    )
                                    AppScreen.FORM -> VehicleFormScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { currentScreen = AppScreen.DASHBOARD }
                                    )
                                    AppScreen.DOCKS -> DockBaysScreen(
                                        viewModel = viewModel
                                    )
                                    AppScreen.COMPLIANCE -> ComplianceDashboardScreen(
                                        viewModel = viewModel
                                    )
                                    AppScreen.REPORTS -> ReportsScreen(
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                    }

                    if (showDownloadDialog) {
                        DownloadReportDialog(
                            viewModel = viewModel,
                            onDismiss = { showDownloadDialog = false }
                        )
                    }

                    if (showAuthDialog) {
                        AuthDialog(
                            viewModel = viewModel,
                            onDismiss = { showAuthDialog = false }
                        )
                    }

                    if (showGlobalScanner) {
                        BarcodeScannerDialog(
                            onDismiss = { showGlobalScanner = false },
                            onScanned = { scanned ->
                                viewModel.startNewEntry()
                                viewModel.formVehicleNumber.value = scanned.vehicleNumber
                                scanned.origin?.let { viewModel.formFromLocation.value = it }
                                scanned.destination?.let { viewModel.formToLocation.value = it }
                                scanned.activityType?.let { viewModel.formActivityType.value = it }
                                scanned.vehicleType?.let { viewModel.formVehicleType.value = it }
                                scanned.dockBay?.let { viewModel.formDockBay.value = it }
                                scanned.driverName?.let { viewModel.formDriverName.value = it }
                                scanned.driverPhone?.let { viewModel.formDriverPhone.value = it }
                                scanned.remarks?.let { viewModel.formRemarks.value = it }
                                viewModel.formInTime.value = VehicleConstants.currentFormattedDateTime()
                                showGlobalScanner = false
                                currentScreen = AppScreen.FORM
                            }
                        )
                    }
                }
            }
        }
    }
}
