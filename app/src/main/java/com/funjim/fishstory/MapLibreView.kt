package com.funjim.fishstory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.MarkerOptions

@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    initialLatLng: LatLng,
    onMapClick: (LatLng) -> Unit,
    markerPosition: LatLng? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context).apply {
            getMapAsync { map ->
                map.setStyle(Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty"))
                
                map.cameraPosition = CameraPosition.Builder()
                    .target(initialLatLng)
                    .zoom(10.0)
                    .build()

                map.addOnMapClickListener { point ->
                    onMapClick(LatLng(point.latitude, point.longitude))
                    true
                }

                markerPosition?.let {
                    map.addMarker(MarkerOptions().position(it))
                }
            }
        }
    }

    // Lifecycle management for MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            view.getMapAsync { map ->
                // Update marker if needed
                map.clear()
                markerPosition?.let {
                    map.addMarker(MarkerOptions().position(it))
                }
            }
        }
    )
}
