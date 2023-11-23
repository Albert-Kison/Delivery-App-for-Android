import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MyLocationManager(
    private val context: Context,
    private val locationPermissionLauncher: ActivityResultLauncher<String>
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var skipLocationUpdates = false

    fun getCoordinates(callback: (Double, Double) -> Unit) {
        if (checkLocationPermission()) {
            if (!skipLocationUpdates) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            callback.invoke(location.latitude, location.longitude)
                        } else {
                            // Handle the case where the last known location is null
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle failure to get location
                    }
            }
        } else {
            requestLocationPermission()
        }
    }

    fun skipLocationUpdates() {
        skipLocationUpdates = true
    }

    fun resetSkipLocationUpdates() {
        skipLocationUpdates = false
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                context as ComponentActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Explain why you need the permission
            // You can show a dialog or a snackbar here
        } else {
            // No explanation needed, request the permission
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}
