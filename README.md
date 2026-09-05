# RutaGo

Aplicación Android de mapas interactivos que utiliza OpenStreetMap, osmdroid y Stadia Maps. Permite buscar direcciones, crear y administrar marcadores personalizados y consultar la distancia entre los dos últimos puntos agregados.

## Características

- Visualización de mapas de OpenStreetMap mediante `osmdroid` y tiles de Stadia Maps.
- Búsqueda de direcciones con geocodificación gratuita de Nominatim.
- Creación de marcadores al tocar el mapa, con nombre personalizado.
- Menú de gestión para cada marcador: editar nombre, eliminar o cerrar.
- Cálculo y visualización de la distancia entre los dos últimos marcadores creados.
- Ícono de ubicación personalizado para los marcadores (`location on`).
- Protección de la clave de Stadia Maps en `local.properties`, expuesta a la app mediante `BuildConfig`.

## Tecnologías usadas

| Tecnología | Uso |
| --- | --- |
| Kotlin | Lenguaje principal de la aplicación. |
| Jetpack Compose | Construcción declarativa de la interfaz. |
| osmdroid 6.1.20 | Renderizado del mapa y gestión de marcadores. |
| Stadia Maps | Proveedor de tiles para el mapa. |
| Retrofit 2.11.0 | Cliente HTTP para la consulta a Nominatim. |
| Gson | Conversión de las respuestas JSON de Nominatim. |
| OpenStreetMap / Nominatim | Datos cartográficos y geocodificación. |

## Requisitos previos

- Android Studio con soporte para proyectos Kotlin y Jetpack Compose.
- JDK 11.
- Una API key válida de [Stadia Maps](https://stadiamaps.com/).
- Dispositivo o emulador con Android API 26 o superior.

## Instalación y configuración

1. Clone el repositorio:

   ```bash
   git clone https://github.com/Almarales24/RutaGo.git
   cd RutaGo
   ```

2. Cree un archivo `local.properties` en la raíz del proyecto e incluya su clave de Stadia Maps:

   ```properties
   STADIA_API_KEY=tu_api_key_de_stadia_maps
   ```

   El archivo está excluido por Git. El módulo `app` lee esta propiedad e inyecta su valor como `BuildConfig.STADIA_API_KEY` durante la compilación.

3. Abra el proyecto en Android Studio y ejecute la sincronización de Gradle.

4. Seleccione un dispositivo o emulador y ejecute la aplicación.

## Estructura técnica

```text
RutaGo/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/rutago/
│       │   ├── MainActivity.kt
│       │   ├── NominatimApiService.kt
│       │   ├── NominatimResult.kt
│       │   ├── RetrofitClient.kt
│       │   └── ui/theme/
│       │       ├── Color.kt
│       │       ├── Theme.kt
│       │       └── Type.kt
│       └── res/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── gradle/
```

El proyecto no contiene directorios `Models`, `Controllers` o `Views` separados: las responsabilidades de presentación e interacción se implementan con composables de Jetpack Compose. Tampoco contiene archivos CSS o JavaScript.

### Archivos principales

- `app/src/main/java/com/example/rutago/MainActivity.kt`: contiene `MainActivity`, que inicializa osmdroid y el tema `RutaGoTheme`; `PantallaMapa`, composable que coordina la búsqueda, el estado de marcadores, los diálogos de creación/edición y la distancia; y `MapaOsm`, composable que configura el `MapView`, los tiles de Stadia Maps y los eventos de toque.
- `app/src/main/java/com/example/rutago/NominatimApiService.kt`: declara la interfaz Retrofit `NominatimApiService` y el método suspendido `buscarDireccion(texto, format, limit)`, que consulta el endpoint `search` de Nominatim.
- `app/src/main/java/com/example/rutago/NominatimResult.kt`: define el modelo `NominatimResult` con las propiedades `display_name`, `lat` y `lon` recibidas desde Nominatim.
- `app/src/main/java/com/example/rutago/RetrofitClient.kt`: implementa el singleton `RetrofitClient`; su propiedad perezosa `nominatimApi` configura Retrofit con la URL base de Nominatim y `GsonConverterFactory`.
- `app/src/main/java/com/example/rutago/ui/theme/Color.kt`: declara la paleta de colores del tema Compose (`Purple80`, `PurpleGrey80`, `Pink80`, `Purple40`, `PurpleGrey40` y `Pink40`).
- `app/src/main/java/com/example/rutago/ui/theme/Theme.kt`: define el composable `RutaGoTheme`, responsable de aplicar el esquema Material de la aplicación.
- `app/src/main/java/com/example/rutago/ui/theme/Type.kt`: define la configuración tipográfica del tema Compose.
- `app/src/main/AndroidManifest.xml`: declara los permisos de red, la aplicación y `MainActivity` como actividad de inicio.
- `app/build.gradle.kts`: configura el módulo Android, Compose, `BuildConfig.STADIA_API_KEY`, el mínimo de API 26 y las dependencias de osmdroid, Retrofit y Gson.
- `build.gradle.kts`: declara los plugins de Android y Kotlin Compose compartidos por el proyecto.
- `gradle.properties`: contiene propiedades de Gradle para la compilación del proyecto.
- `settings.gradle.kts`: configura el nombre y los módulos incluidos en la compilación.

## Capturas de pantalla

### Pantalla del mapa

<p align="center">
  <img width="360" alt="Pantalla del mapa de RutaGo" src="https://github.com/user-attachments/assets/d9cb9a02-cd35-480e-9dad-3d7e6711bf14" />
</p>

### Búsqueda de direcciones

<p align="center">
  <img width="360" alt="Búsqueda de direcciones en RutaGo" src="https://github.com/user-attachments/assets/6e7da641-2ce5-4f49-918b-309df24c87d1" />
</p>

### Gestión de marcadores

<p align="center">
  <img width="360" alt="Gestión de marcadores en RutaGo" src="https://github.com/user-attachments/assets/3db79796-28bf-4e07-bf81-849fbbfe2a6a" />
</p>


## Autor

Desarrollado por [Almarales24](https://github.com/Almarales24).
