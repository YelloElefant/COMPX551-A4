package com.yelloelefant.compx551a4.data

/**
 * Application-wide configuration flags for the Polar H10 Data Streamer.
 */
object AppConfig {
    /**
     * Set to true to test UI/flow on macOS/Wi-Fi or emulator without physical Polar H10.
     * Set to false when connecting to the physical Polar H10 chest strap sensor.
     */
    const val USE_MOCK_DATA: Boolean = true
}
