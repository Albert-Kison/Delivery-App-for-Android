package com.griffith.deliveryapp

import MyLocationManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
    val userLatitude = mutableStateOf(0.0f)
    val userLongitude = mutableStateOf(0.0f)

    private lateinit var myLocationManager: MyLocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        userLatitude.value = sharedPreferences.getFloat("userLatitude", 0.0f) ?: 0.0f
        userLongitude.value = sharedPreferences.getFloat("userLongitude", 0.0f) ?: 0.0f

        // get current location to display on the map
        val currentLocation = LatLng(userLatitude.value.toDouble(), userLongitude.value.toDouble())
        // position marker
        val currentLocationState = mutableStateOf(MarkerState(position = currentLocation))

        // get current username to put it into the text field
//        val currentUsername by lazy {
//            intent?.getStringExtra("username") ?: ""
//        }
        val usernameText = mutableStateOf(sharedPreferences.getString("username", "Guest") ?: "Guest")

        setContent {
            val context = LocalContext.current

            // position the camera on the current delivery address
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(
                    currentLocation,
                    15f
                )
            }

            DeliveryAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            // username text field
                            Text(text = "Your name", fontSize = 20.sp)
                            TextField(
                                value = usernameText.value,
                                onValueChange = { usernameText.value = it },
                                textStyle = TextStyle(fontSize = 20.sp),
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
                                    .fillMaxWidth()
                            )


                            // display google maps
                            Text(text = "Your address", fontSize = 20.sp, modifier = Modifier.padding(0.dp, 16.dp, 0.dp, 0.dp))
                            GoogleMap(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                cameraPositionState = cameraPositionState,
                                onMapClick = ({
                                    // update the coordinates
                                    userLatitude.value = it.latitude.toFloat()
                                    userLongitude.value = it.longitude.toFloat()

                                    editor.putFloat("userLatitude", userLatitude.value)
                                    editor.putFloat("userLongitude", userLongitude.value)

                                    editor.apply()

                                    // position the marker on the new location
                                    currentLocationState.value = MarkerState(position = it)
                                })
                            ) {
                                Marker(
                                    state = currentLocationState.value,
                                    title = "Delivery address",
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // the save button
                        Box(
                            contentAlignment = Alignment.BottomCenter,
                            modifier = Modifier
                                .background(Color(238, 150, 75))
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent()

                                    // put updated username
                                    intent.putExtra("newUsername", usernameText.value)
                                    editor.putString("username", usernameText.value)

                                    // put updated coordinates
                                    intent.putExtra(
                                        "newLatitude",
                                        currentLocationState.value.position.latitude
                                    )
                                    editor.putFloat("userLatitude", currentLocationState.value.position.latitude.toFloat())

                                    intent.putExtra(
                                        "newLongitude",
                                        currentLocationState.value.position.longitude
                                    )
                                    editor.putFloat("userLongitude", currentLocationState.value.position.longitude.toFloat())

                                    intent.putExtra("skipLocationInitialization", true)

                                    editor.apply()
                                    setResult(Activity.RESULT_OK, intent)

                                    finish()
                                }
                        ) {
                            Text(
                                text = "Save",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(16.dp),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}
