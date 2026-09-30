package com.yelloelefant.compx551a4.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.yelloelefant.compx551a4.wear.theme.WearAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearAppTheme {
                AppScaffold {
                    val columnState = rememberTransformingLazyColumnState()
                    ScreenScaffold(
                        scrollState = columnState,
                        timeText = { TimeText() }
                    ) { contentPadding ->
                        WearApp(columnState = columnState, contentPadding = contentPadding)
                    }
                }
            }
        }
    }
}

@Composable
fun WearApp(
    viewModel: SensorViewModel = viewModel(),
    columnState: TransformingLazyColumnState = rememberTransformingLazyColumnState(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val accelData by viewModel.accelerometerData.collectAsStateWithLifecycle()
    val gyroData by viewModel.gyroscopeData.collectAsStateWithLifecycle()
    val transformationSpec = rememberTransformationSpec()

    TransformingLazyColumn(
        state = columnState,
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ListHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec)
            ) {
                Text(
                    text = "Sensor Data",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Accelerometer Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                onClick = { }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleSmall,
                        text = "Accelerometer"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            text = "X: %.2f".format(accelData.x)
                        )
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            text = "Y: %.2f".format(accelData.y)
                        )
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            text = "Z: %.2f".format(accelData.z)
                        )
                    }
                }
            }
        }

        // Gyroscope Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                onClick = { }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleSmall,
                        text = "Gyroscope"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            text = "X: %.2f".format(gyroData.x)
                        )
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            text = "Y: %.2f".format(gyroData.y)
                        )
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            text = "Z: %.2f".format(gyroData.z)
                        )
                    }
                }
            }
        }

//        // Static Test Card 1: Magnetometer (Mock)
//        item {
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .transformedHeight(this, transformationSpec),
//                transformation = SurfaceTransformation(transformationSpec),
//                onClick = { }
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text(
//                        textAlign = TextAlign.Center,
//                        color = MaterialTheme.colorScheme.primary,
//                        style = MaterialTheme.typography.titleSmall,
//                        text = "Magnetometer (Static)"
//                    )
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 4.dp),
//                        horizontalArrangement = Arrangement.SpaceEvenly,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            style = MaterialTheme.typography.bodySmall,
//                            text = "X: 24.50"
//                        )
//                        Text(
//                            style = MaterialTheme.typography.bodySmall,
//                            text = "Y: -12.10"
//                        )
//                        Text(
//                            style = MaterialTheme.typography.bodySmall,
//                            text = "Z: 45.80"
//                        )
//                    }
//                }
//            }
//        }
//
//        // Static Test Card 2: Device Info (Mock)
//        item {
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .transformedHeight(this, transformationSpec),
//                transformation = SurfaceTransformation(transformationSpec),
//                onClick = { }
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text(
//                        textAlign = TextAlign.Center,
//                        color = MaterialTheme.colorScheme.primary,
//                        style = MaterialTheme.typography.titleSmall,
//                        text = "Status (Static)"
//                    )
//                    Text(
//                        modifier = Modifier.padding(top = 4.dp),
//                        textAlign = TextAlign.Center,
//                        style = MaterialTheme.typography.bodySmall,
//                        text = "Sampling Rate: 250ms | Active"
//                    )
//                }
//            }
//        }
    }
}

@Preview(device = "id:wearos_small_round", showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearAppTheme {
        AppScaffold {
            val columnState = rememberTransformingLazyColumnState()
            ScreenScaffold(
                scrollState = columnState,
                timeText = { TimeText() }
            ) { contentPadding ->
                WearApp(columnState = columnState, contentPadding = contentPadding)
            }
        }
    }
}
