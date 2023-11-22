package com.griffith.deliveryapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.griffith.deliveryapp.ui.theme.DeliveryAppTheme

@OptIn(ExperimentalMaterial3Api::class)
class SettingsActivity : ComponentActivity() {

    private val coordinates: Coordinates = Coordinates.getInstance()

    private val currentLocation = LatLng(coordinates.getLatitude(), coordinates.getLongitude())
    private val currentLocationState = mutableStateOf(MarkerState(position = currentLocation))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val currentUsername by lazy {
            intent?.getStringExtra("username") ?: ""
        }
        val usernameText = mutableStateOf(currentUsername)


        setContent {
            val context = LocalContext.current

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(
                    currentLocation,
                    15f
                )
            }

            Column {
                Text(text = "Your name")
                TextField(value = usernameText.value,
                    onValueChange = { usernameText.value = it },
                    textStyle = TextStyle(fontSize = 24.sp),
                    singleLine = true,
                    shape = RoundedCornerShape(5),
                    placeholder = {
                        Text("Your name", fontSize = 20.sp) // Placeholder text
                    },
                    colors = TextFieldDefaults.textFieldColors( // Custom colors and removing bottom border
                        textColor = Color.Black,
                        cursorColor = Color.Black,
                        unfocusedLeadingIconColor = Color.Gray,
                        unfocusedIndicatorColor = Color.Transparent, // This removes the bottom border
                        focusedIndicatorColor = Color.Transparent // This removes the bottom border when focused as well
                    ),
                    keyboardActions = KeyboardActions.Default,
                    modifier = Modifier
                        .padding(16.dp, 0.dp, 16.dp, 0.dp)
                        .fillMaxWidth()
                )


                Text(text = "Your address")
                GoogleMap(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(200.dp),
                    cameraPositionState = cameraPositionState,
                    onMapClick = ({
                        coordinates.setLatitude(it.latitude)
                        coordinates.setLongitude(it.longitude)
                        currentLocationState.value = MarkerState(position = it)
                    })
                ) {
                    Marker(
                        state = currentLocationState.value,
                        title = "Delivery address",
                    )
                }

                Button(onClick = {
                    val intent = Intent()
                    intent.putExtra("newUsername", usernameText.value)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }) {
                    Text(text = "Save")
                }
            }

        }
    }
}
