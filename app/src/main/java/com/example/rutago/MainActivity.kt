package com.example.rutago

import android.os.Bundle
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
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import org.osmdroid.views.overlay.Marker


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = packageName

        enableEdgeToEdge()
        setContent {
            RutaGoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaMapa(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun PantallaMapa(modifier: Modifier = Modifier) {
    var textoBusqueda by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        MapaOsm(onMapReady = { mapViewRef = it })

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surface),
                    placeholder = { Text("Buscar dirección...") },
                    singleLine = true
                )
                Button(onClick = {
                    if (textoBusqueda.isNotBlank()) {
                        scope.launch {
                            try {
                                val resultados = RetrofitClient.nominatimApi.buscarDireccion(textoBusqueda)
                                if (resultados.isEmpty()) {
                                    mensajeError = "No se encontró esa dirección"
                                } else {
                                    mensajeError = null
                                    val lat = resultados[0].lat.toDouble()
                                    val lon = resultados[0].lon.toDouble()
                                    mapViewRef?.controller?.animateTo(GeoPoint(lat, lon))
                                    mapViewRef?.controller?.setZoom(16.0)
                                    mapViewRef?.let { map ->
                                        map.overlays.clear()
                                        val marcador = Marker(map)
                                        marcador.position = GeoPoint(lat, lon)
                                        marcador.title = resultados[0].display_name
                                        map.overlays.add(marcador)
                                        map.invalidate()
                                    }
                                }
                            } catch (e: Exception) {
                                mensajeError = "Error al buscar: ${e.message}"
                            }
                        }
                    }
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                }
            }
            mensajeError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MapaOsm(onMapReady: (MapView) -> Unit) {
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
            controller.setCenter(GeoPoint(4.7110, -74.0721))
            onMapReady(this)
        }
    })
}