package com.example.rutago

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.rutago.ui.theme.RutaGoTheme
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
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
    var puntoPendiente by remember { mutableStateOf<GeoPoint?>(null) }
    var nombreMarcador by remember { mutableStateOf("") }
    var marcadorSeleccionado by remember { mutableStateOf<Marker?>(null) }
    var mostrarEdicion by remember { mutableStateOf(false) }
    var nombreEdicion by remember { mutableStateOf("") }
    val listaMarcadores = remember { mutableStateListOf<Marker>() }
    var distanciaTexto by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        MapaOsm(
            onMapReady = { mapViewRef = it },
            onMapClick = { punto ->
                puntoPendiente = punto
                nombreMarcador = ""
            }
        )

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
                                val resultados =
                                    RetrofitClient.nominatimApi.buscarDireccion(textoBusqueda)
                                if (resultados.isEmpty()) {
                                    mensajeError = "No se encontró esa dirección"
                                } else {
                                    mensajeError = null
                                    val lat = resultados[0].lat.toDouble()
                                    val lon = resultados[0].lon.toDouble()
                                    mapViewRef?.controller?.animateTo(GeoPoint(lat, lon))
                                    mapViewRef?.controller?.setZoom(16.0)
                                    mapViewRef?.let { map ->
                                        val marcador = Marker(map)
                                        marcador.position = GeoPoint(lat, lon)
                                        marcador.title = resultados[0].display_name
                                        marcador.setOnMarkerClickListener { m, _ ->
                                            marcadorSeleccionado = m as Marker
                                            true
                                        }
                                        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        map.overlays.add(marcador)
                                        map.invalidate()

                                        listaMarcadores.add(marcador)
                                        if (listaMarcadores.size >= 2) {
                                            val ultimo = listaMarcadores[listaMarcadores.size - 1]
                                            val penultimo = listaMarcadores[listaMarcadores.size - 2]
                                            val distanciaMetros = ultimo.position.distanceToAsDouble(penultimo.position)
                                            distanciaTexto = if (distanciaMetros >= 1000) {
                                                "Distancia: %.2f km".format(distanciaMetros / 1000)
                                            } else {
                                                "Distancia: %.0f m".format(distanciaMetros)
                                            }
                                        }
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
            distanciaTexto?.let {
                Text(
                    text = it,
                    color = androidx.compose.ui.graphics.Color.Black,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(androidx.compose.ui.graphics.Color.White)
                )
            }
        }
    }

    if (puntoPendiente != null) {
        AlertDialog(
            onDismissRequest = { puntoPendiente = null },
            title = { Text("Nombrar marcador") },
            text = {
                OutlinedTextField(
                    value = nombreMarcador,
                    onValueChange = { nombreMarcador = it },
                    placeholder = { Text("Ej: Mi casa") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val punto = puntoPendiente
                    if (punto != null) {
                        mapViewRef?.let { map ->
                            val marcador = Marker(map)
                            marcador.position = punto
                            marcador.title = nombreMarcador.ifBlank { "Sin nombre" }
                            marcador.setOnMarkerClickListener { m, _ ->
                                marcadorSeleccionado = m as Marker
                                true
                            }
                            marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            map.overlays.add(marcador)
                            map.invalidate()

                            listaMarcadores.add(marcador)
                            if (listaMarcadores.size >= 2) {
                                val ultimo = listaMarcadores[listaMarcadores.size - 1]
                                val penultimo = listaMarcadores[listaMarcadores.size - 2]
                                val distanciaMetros = ultimo.position.distanceToAsDouble(penultimo.position)
                                distanciaTexto = if (distanciaMetros >= 1000) {
                                    "Distancia: %.2f km".format(distanciaMetros / 1000)
                                } else {
                                    "Distancia: %.0f m".format(distanciaMetros)
                                }
                            }
                        }
                    }
                    puntoPendiente = null
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { puntoPendiente = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (marcadorSeleccionado != null && !mostrarEdicion) {
        AlertDialog(
            onDismissRequest = { marcadorSeleccionado = null },
            title = { Text(marcadorSeleccionado?.title ?: "Marcador") },
            text = { Text("¿Qué deseas hacer?") },
            confirmButton = {
                TextButton(onClick = {
                    nombreEdicion = marcadorSeleccionado?.title ?: ""
                    mostrarEdicion = true
                }) {
                    Text("Editar")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        mapViewRef?.let { map ->
                            map.overlays.remove(marcadorSeleccionado)
                            listaMarcadores.remove(marcadorSeleccionado)
                            map.invalidate()
                        }
                        marcadorSeleccionado = null
                    }) {
                        Text("Eliminar")
                    }
                    TextButton(onClick = { marcadorSeleccionado = null }) {
                        Text("Cerrar")
                    }
                }
            }
        )
    }

    if (mostrarEdicion) {
        AlertDialog(
            onDismissRequest = { mostrarEdicion = false },
            title = { Text("Editar nombre") },
            text = {
                OutlinedTextField(
                    value = nombreEdicion,
                    onValueChange = { nombreEdicion = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    marcadorSeleccionado?.title = nombreEdicion
                    mostrarEdicion = false
                    marcadorSeleccionado = null
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarEdicion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun MapaOsm(onMapReady: (MapView) -> Unit, onMapClick: (GeoPoint) -> Unit) {
    AndroidView(factory = { context ->
        val stadiaTileSource = XYTileSource(
            "StadiaAlidade",
            0, 20, 256,
            ".png?api_key=${BuildConfig.STADIA_API_KEY}",
            arrayOf("https://tiles.stadiamaps.com/tiles/alidade_smooth/"),
            "© Stadia Maps, © OpenMapTiles, © OpenStreetMap contributors"
        )

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let { onMapClick(it) }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }

        MapView(context).apply {
            setTileSource(stadiaTileSource)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(4.7110, -74.0721))
            overlays.add(MapEventsOverlay(mapEventsReceiver))
            onMapReady(this)
        }
    })
}