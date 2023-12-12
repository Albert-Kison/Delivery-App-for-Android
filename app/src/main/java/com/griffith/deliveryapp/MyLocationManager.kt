import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest

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

                            // default location
                            callback.invoke(37.401000, -122.117190)
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
            // show a dialog to explain why the permission is needed
            showPermissionRationaleDialog()
        } else {
            // No explanation needed, request the permission
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun showPermissionRationaleDialog() {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setTitle("Location Permission Required")
        dialogBuilder.setMessage("This app needs your location to provide relevant services.")
        dialogBuilder.setPositiveButton("Grant Permission") { _, _ ->
            // Launch the permission request when the user clicks "Grant Permission"
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        dialogBuilder.setNegativeButton("Cancel") { _, _ ->

        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }


}