package com.griffith.deliveryapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.GoogleApi.Settings
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.griffith.deliveryapp.ui.theme.DeliveryAppTheme
import java.io.Serializable


// text used to search for restaurants (not implemented yet)
var searchText = mutableStateOf("")

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val basket: Basket = Basket.getInstance()

    private var isLocationInitialized = false
    private val coordinates: Coordinates = Coordinates.getInstance()

    // number of items in the basket
    private val itemCount = mutableStateOf(basket.getItems().size)

    private val restaurantList = mutableStateOf(data)

    private val username = mutableStateOf("")
    private val startSettingsActivityForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Retrieve the updated username from the intent
                val newUsername =
                    result.data?.getStringExtra("newUsername")
                newUsername?.let {
                    // Update the username in MainActivity
                    username.value = it
                }
            }
        }

    // save the itemCount if modified in another activity
    private val startActivityForItemCountResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Retrieve the updated item count from the basket
            val itemCount = Basket.getInstance().getItems().size
            this.itemCount.value = itemCount
        }
    }

    private val MY_PERMISSIONS_REQUEST_LOCATION = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the intent contains the flag to skip location initialization
        val skipLocationInitialization = intent?.getBooleanExtra("skipLocationInitialization", false) ?: false

        if (!isLocationInitialized && !skipLocationInitialization) {
            initializeLocation()
        }

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
                        Row (verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Hello " + username.value, modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp))
                            Spacer(modifier = Modifier.weight(1f))
                            FloatingActionButton(
                                modifier = Modifier.height(50.dp),
//                                shape = RoundedCornerShape(5),
                                containerColor = Color.Transparent, // Transparent background
                                contentColor = Color(238, 150, 75), // Color of the icon
                                elevation = FloatingActionButtonDefaults.elevation( // Set elevation to zero
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    hoveredElevation = 0.dp,
                                    focusedElevation = 0.dp
                                ),
                                onClick = {
                                    val intent = Intent(context, SettingsActivity::class.java)
                                    intent.putExtra("username", username.value)
                                    startSettingsActivityForResult.launch(intent)
                                },
                            ) {
                                Icon(Icons.Filled.Settings, "Add")
                            }
                        }
                        
                        
                        TextField(value = searchText.value,
                            onValueChange = {
                                searchText.value = it
                                searchRestaurants(it)
                                            },
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
                                .padding(16.dp, 0.dp, 16.dp, 0.dp)
                                .fillMaxWidth()
                        )
                        Text(text = coordinates.getLatitude().toString())
                        Text(text = coordinates.getLongitude().toString())

                        if (restaurantList.value.size > 0) {
                            // scrollable column of the restaurants' cards
                            LazyColumn(
                                modifier = Modifier.weight(1f)
                            ) {
                                item {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        for (res in restaurantList.value) {
                                            ResCard(res = res, onClick = {
                                                // when clicked, go to the restaurant's menu (ResDetails)
                                                val intent = Intent(context, ResDetails::class.java)
                                                // put the restaurant's menu data
                                                intent.putExtra(
                                                    "menu",
                                                    res["menu"] as? Serializable
                                                )
                                                intent.putExtra("restaurantId", res["id"] as? String)
                                                startActivityForItemCountResult.launch(intent)
                                            })
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(text = "There no restaurants in your area", fontSize = 24.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }

                        // if the basket exists
                        if (itemCount.value > 0) {
                            // this button has a fixed position and is always at the bottom
                            ViewBasketButton(itemCount = basket.getItems().size, total = basket.getTotal(), startActivityForItemCountResult)
                        }
                    }
                    
                }
            }
        }

    }


    override fun onResume() {
        super.onResume()

        // Call your method to update the location and filter restaurants
        filterRestaurantsBasedOnCoordinates(coordinates)
    }

    private fun searchRestaurants(query: String) {
        restaurantList.value = if (query.isBlank()) {
            data // If the query is blank, show the original list
        } else {
            // Filter the list based on the search query
            data.filter { res ->
                (res["name"] as? String)?.contains(query, ignoreCase = true) == true
            }
        }
    }


    fun calculateDistance(
        userLatitude: Double,
        userLongitude: Double,
        restaurantLatitude: Double,
        restaurantLongitude: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(userLatitude, userLongitude, restaurantLatitude, restaurantLongitude, results)
        return results[0]
    }


    fun filterRestaurantsBasedOnCoordinates(coordinates: Coordinates) {
        // Filter restaurants based on distance
        restaurantList.value = data.filter { res ->
            val restaurantLatitude = res["latitude"] as Double
            val restaurantLongitude = res["longitude"] as Double
            val distance = calculateDistance(
                coordinates.getLatitude(),
                coordinates.getLongitude(),
                restaurantLatitude,
                restaurantLongitude
            )
            // Filter restaurants within a 30 km radius
            distance <= 30000.0
        }
    }



    private fun initializeLocation() {
        // api to get the location
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // request the permission, executes when permission is not granted
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

        // get the last known location
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations, this can be null.
                if (location != null) {
                    // Handle location
                    coordinates.setLatitude(location.latitude)
                    coordinates.setLongitude(location.longitude)

                    isLocationInitialized = true

                    // Filter restaurants based on distance
                    filterRestaurantsBasedOnCoordinates(coordinates)
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
