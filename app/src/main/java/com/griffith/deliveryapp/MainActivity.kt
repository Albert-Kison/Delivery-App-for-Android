package com.griffith.deliveryapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.griffith.deliveryapp.ui.theme.DeliveryAppTheme
import java.io.Serializable


// text used to search for restaurants (not implemented yet)
var searchText = mutableStateOf("")
var latitude = mutableStateOf(0.0)
var longitude = mutableStateOf(0.0)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val basket: Basket = Basket.getInstance()

    // number of items in the basket
    private var itemCount = mutableStateOf(basket.getItems().size)

    // save the itemCount if modified in another activity
    private val startActivityForItemCountResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Retrieve the updated item count from the basket
            val itemCount = Basket.getInstance().getItems().size
            this.itemCount.value = itemCount
        }
    }

    private val MY_PERMISSIONS_REQUEST_LOCATION = 123
    private val locationPermissionCode = 2
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeLocation()

        setContent {
            val context = LocalContext.current

            DeliveryAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Column of the search text field, the restaurants cards, and the "View basket" button
                    Column(modifier = Modifier.fillMaxSize()) {
                        TextField(value = searchText.value,
                            onValueChange = { searchText.value = it},
                            textStyle = TextStyle(fontSize = 24.sp),
                            singleLine = true,
                            shape = RoundedCornerShape(5),
                            placeholder = {
                                Text("Search restaurants", fontSize = 20.sp) // Placeholder text
                            },
                            leadingIcon = { // Inserting an icon
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = "Visibility Icon",
                                    modifier = Modifier.size(24.dp),
//                                    tint = Color(244, 211, 94)
                                )
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
                                .padding(16.dp, 16.dp, 16.dp, 0.dp)
                                .fillMaxWidth()
                        )
                        Text(text = latitude.value.toString())
                        Text(text = longitude.value.toString())

                        // scrollable column of the restaurants' cards
                        LazyColumn (
                            modifier = Modifier.weight(1f)
                        ) {
                            item {
                                Column (modifier = Modifier.padding(16.dp)) {
                                    for (res in data) {
                                        ResCard(res=res, onClick = {
                                            // when clicked, go to the restaurant's menu (ResDetails)
                                            val intent = Intent(context, ResDetails::class.java)
                                            // put the restaurant's menu data
                                            intent.putExtra("menu", res["menu"] as? Serializable)
                                            startActivityForItemCountResult.launch(intent)
                                        })
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }

                        // if the basket exists
                        if (itemCount.value > 0) {
                            // this button has a fixed position and is always at the bottom
                            ViewBasketButton(itemCount = itemCount.value, total = basket.getTotal(), startActivityForItemCountResult)
                        }
                    }
                    
                }
            }
        }
    }

    private fun initializeLocation() {
        // Your code to initialize location-related functionality goes here
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations, this can be null.
                if (location != null) {
                    // Handle location
                    latitude.value = location.latitude
                    longitude.value = location.longitude
                    // Do something with the latitude and longitude
                } else {
                    // Handle the case where the last known location is null
                }
            }
            .addOnFailureListener { e ->
                // Handle failure to get location
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with GPS-related code
                    initializeLocation()
                } else {
                    // Permission denied, handle accordingly (e.g., show a message to the user)
                }
            }
        }
    }
}


@Composable
fun ResCard(res: Map<String, Any>, onClick: () -> Unit) {
    // the card has elevation and is clickable
    Card (
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.clickable { onClick() }
    ) {
        Column {
            // image of the restaurant
            Image(
                painter = painterResource(res["img"] as Int ?: R.drawable.default_image),
                contentDescription = res["name"] as String ?: "Restaurant", // decorative
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
            )

            Column (modifier = Modifier.padding(16.dp, 10.dp, 16.dp, 10.dp)) {

                // name of the restaurant
                Text(
                    text = res["name"] as String ?: "Restaurant",
                    fontSize =  24.sp,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                // rating of the restaurant
                Row (verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(id = R.drawable.star), contentDescription = "rating")
                    var rating = ""
                    if (res["rating"] as Double <= 2.5) {
                        rating = "Bad"
                    } else if (res["rating"] as Double <= 3.5) {
                        rating = "Satisfied"
                    } else if (res["rating"] as Double <= 4.5) {
                        rating = "Good"
                    } else rating = "Very good"
                    Text(text = rating + " (" + res["reviewsNumber"].toString() + "+)", color = Color.Gray)
                }

                // delivery time of the restaurant (so far just a placeholder)
                Text(text = "Delivery time 30-45 minutes", color = Color.Gray)

                // distance away of the restaurant (so far just a placeholder)
                Text(text = "2.3 km", color = Color.Gray)
            }
        }
    }
}
