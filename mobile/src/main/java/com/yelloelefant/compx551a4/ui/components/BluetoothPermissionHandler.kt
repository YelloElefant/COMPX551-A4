package com.yelloelefant.compx551a4.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun BluetoothPermissionHandler(
    onPermissionsGranted: () -> Unit,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermissions by remember { mutableStateOf(checkPermissions(context)) }
    var isLocationServicesEnabled by remember { mutableStateOf(checkLocationServicesEnabled(context)) }

    // Re-check permissions and system GPS whenever app resumes from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermissions = checkPermissions(context)
                isLocationServicesEnabled = checkLocationServicesEnabled(context)
                if (hasPermissions && isLocationServicesEnabled) {
                    onPermissionsGranted()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermissions = results.values.all { it } || checkPermissions(context)
        isLocationServicesEnabled = checkLocationServicesEnabled(context)
        if (hasPermissions && isLocationServicesEnabled) {
            onPermissionsGranted()
        }
    }

    LaunchedEffect(Unit) {
        if (hasPermissions && isLocationServicesEnabled) {
            onPermissionsGranted()
        }
    }

    when {
        !hasPermissions -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bluetooth & Location Permissions Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To scan and connect to your Polar H10 heart rate sensor, please grant Bluetooth and Location access.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { launcher.launch(permissionsToRequest) }
                    ) {
                        Text("Grant Permissions")
                    }
                }
            }
        }

        !isLocationServicesEnabled -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚠️ Phone Location Services (GPS) is OFF",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Android OS requires phone Location Services (GPS) to be turned ON to discover nearby Bluetooth Low Energy devices like your Polar H10.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }
                    ) {
                        Text("Open Phone Location Settings")
                    }
                }
            }
        }

        else -> {
            content()
        }
    }
}

private fun checkPermissions(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val scan = androidx.core.content.ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_SCAN
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val connect = androidx.core.content.ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_CONNECT
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        return scan && connect
    } else {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}

private fun checkLocationServicesEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return true
    val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    return gpsEnabled || networkEnabled
}
