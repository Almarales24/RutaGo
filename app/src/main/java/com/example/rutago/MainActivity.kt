package com.example.rutago

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.rutago.ui.theme.RutaGoTheme
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import com.example.rutago.BuildConfig


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = packageName

        enableEdgeToEdge()
        setContent {
            RutaGoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        MapaOsm()
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun MapaOsm() {
    AndroidView(factory = { context ->
        val stadiaTileSource = XYTileSource(
            "StadiaAlidade",
            0, 20, 256,
            ".png?api_key=${BuildConfig.STADIA_API_KEY}",
            arrayOf("https://tiles.stadiamaps.com/tiles/alidade_smooth/"),
            "© Stadia Maps, © OpenMapTiles, © OpenStreetMap contributors"
        )

        MapView(context).apply {
            setTileSource(stadiaTileSource)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(4.7110, -74.0721)) // Bogotá, cámbialo si quieres
        }
    })
}