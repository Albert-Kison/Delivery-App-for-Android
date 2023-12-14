package com.griffith.deliveryapp

import MyLocationManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import com.google.gson.Gson
import com.griffith.deliveryapp.ui.theme.DeliveryAppTheme
import java.io.Serializable
import java.lang.StringBuilder
import kotlin.math.roundToInt


// text used to search for restaurants (not implemented yet)
var searchText = mutableStateOf("")

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private var basket = mutableStateOf(Basket.getInstance())

    private var isLocationInitialized = mutableStateOf(false)

    // to filter restaurants
    private val restaurantList = mutableStateOf(data)

    // request the location
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // if granted, initialize the location
            if (isGranted) {
                initializeLocation()
            } else {
                // Handle the case where the user denied the location permission

            }
        }

    private lateinit var myLocationManager: MyLocationManager

    // store the temperature from the sensor
    private var temperature = mutableStateOf(0f)

    private lateinit var sensorManager: SensorManager
    private var temperatureSensor: Sensor? = null
    // handle the temperature change
    private val sensorEventListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                temperature.value = event.values[0]
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            print(accuracy)
        }
    }

    // username with default value Guest
    private val username = mutableStateOf("Guest")
    // user's coordinates
    private val userLatitude = mutableStateOf(0.0f)
    private val userLongitude = mutableStateOf(0.0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create sharedPreferences and get access to local storage
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // get username from local storage
        username.value = sharedPreferences.getString("username", "Guest") ?: "Guest"
        // get user's coordinates from local storage
        userLatitude.value = sharedPreferences.getFloat("userLatitude", 0.0f) ?: 0.0f
        userLongitude.value = sharedPreferences.getFloat("userLongitude", 0.0f) ?: 0.0f
        // get user's basket from local storage
        basket.value = Gson().fromJson(sharedPreferences.getString("basket", Gson().toJson(basket.value)) ?: Gson().toJson(basket.value), Basket::class.java)

        // location manager to get the last known location
        myLocationManager = MyLocationManager(this, locationPermissionLauncher)

        // Check if the intent contains the flag to skip location initialization
        val skipLocationInitialization =
            intent?.getBooleanExtra("skipLocationInitialization", false) ?: false

        // if location not initialised, do so
        if (!isLocationInitialized.value && !skipLocationInitialization) {
            initializeLocation()
        }

        // filter the restaurants
        filterRestaurantsBasedOnCoordinates(userLatitude.value, userLongitude.value)

        setContent {
            // get local context
            val context = LocalContext.current

            // get the temperature sensor and register the listener
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
            temperatureSensor?.let {
                sensorManager.registerListener(
                    sensorEventListener,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }


            DeliveryAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Column of the search text field, the restaurants cards, and the "View basket" button
                    Column(modifier = Modifier.fillMaxSize()) {

                        // greeting and the settings button
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            // greet the user
                            Text(
                                text = "Hello, ${username.value.take(20).ifEmpty { "Guest" }}${if (username.value.length > 20) "..." else ""}",
                                modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp),
                                fontSize = 20.sp
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // the settings button
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
                                    // go to settings activity when clicked
                                    val intent = Intent(context, SettingsActivity::class.java)
                                    startActivity(intent)
                                },
                            ) {
                                Icon(Icons.Filled.Settings, "Add", modifier = Modifier.size(30.dp))
                            }
                        }


                        // the search text field
                        TextField(
                            value = searchText.value,
                            onValueChange = {
                                searchText.value = it
                                searchAndFilterRestaurants(
                                    it,
                                    userLatitude.value.toDouble(),
                                    userLongitude.value.toDouble()
                                )
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

                        // if the location is loading
                        if (!isLocationInitialized.value && !skipLocationInitialization) {

                            // show loading... if location is initialising
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Loading...",
                                        fontSize = 24.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else if (restaurantList.value.size == 0) {

                            // show if there are no restaurants in the area
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "There no restaurants in your area",
                                        fontSize = 24.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
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
                                                intent.putExtra(
                                                    "restaurantId",
                                                    res["id"] as? String
                                                )

                                                // start the restaurant activity (ResDetails)
                                                startActivity(intent)
                                            })
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // if the basket exists
                        if (basket.value.getItems().size > 0) {
                            // this button has a fixed position and is always at the bottom
                            ViewBasketButton(
                                itemCount = basket.value.getItems().size,
                                total = basket.value.getTotal(),
                                onClick = {
                                    // go to basket activity when clicked
                                    val intent = Intent(context, BasketActivity::class.java) // Create the intent
                                    startActivity(intent)
                                }
                            )
                        }
                    }

                }
            }
        }

    }

    // update the restaurants list
    private fun updateLocationAndFilterRestaurants() {
        myLocationManager.resetSkipLocationUpdates()

        filterRestaurantsBasedOnCoordinates(userLatitude.value, userLongitude.value)
    }

    override fun onStart() {
        super.onStart()

        // If the activity is started from another activity, skip location updates
        val skipLocationInitialization =
            intent?.getBooleanExtra("skipLocationInitialization", false) ?: false
        if (skipLocationInitialization) {
            myLocationManager.skipLocationUpdates()
        } else {
            // If not skipping, update location and filter restaurants
            updateLocationAndFilterRestaurants()
        }
    }

    override fun onResume() {
        super.onResume()

        // get access to local storage
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        // get username from local storage
        username.value = sharedPreferences.getString("username", "Guest") ?: "Guest"
        // get user's coordinates from local storage
        userLatitude.value = sharedPreferences.getFloat("userLatitude", 0.0f) ?: 0.0f
        userLongitude.value = sharedPreferences.getFloat("userLongitude", 0.0f) ?: 0.0f
        // get user's basket from local storage
        basket.value = Gson().fromJson(sharedPreferences.getString("basket", Gson().toJson(basket.value)) ?: Gson().toJson(basket.value), Basket::class.java)

        // register listener on the temperature sensor
        temperatureSensor?.let {
            sensorManager.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        // If not skipping location updates, update location and filter restaurants
        val skipLocationInitialization =
            intent?.getBooleanExtra("skipLocationInitialization", false) ?: false
        if (!skipLocationInitialization) {
            updateLocationAndFilterRestaurants()
        }
    }

    override fun onPause() {
        super.onPause()

        // unregister listener on the temperature sensor
        temperatureSensor?.let {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    // search restaurants by name and based on the delivery address
    private fun searchAndFilterRestaurants(query: String, latitude: Double, longitude: Double) {
        if (query.isBlank()) {
            // If the query is blank, show the original list
            filterRestaurantsBasedOnCoordinates(userLatitude.value, userLongitude.value)
        } else {
            // Filter the list based on both search query and distance
            restaurantList.value = data.filter { res ->
                ((res["name"] as? String)?.contains(query, ignoreCase = true) == true) &&
                        isWithinDistance(res, latitude, longitude)
            }
        }
    }

    private fun isWithinDistance(
        res: Map<String, Any>,
        latitude: Double,
        longitude: Double
    ): Boolean {
        val restaurantLatitude = res["latitude"] as Double
        val restaurantLongitude = res["longitude"] as Double
        val distance =
            calculateDistance(latitude, longitude, restaurantLatitude, restaurantLongitude)
        // Filter restaurants within a 30 km radius
        return distance <= 30000.0
    }


    private fun calculateDistance(
        userLatitude: Double,
        userLongitude: Double,
        restaurantLatitude: Double,
        restaurantLongitude: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            userLatitude,
            userLongitude,
            restaurantLatitude,
            restaurantLongitude,
            results
        )
        return results[0]
    }


    // filter restaurants within a 30 km radius
    private fun filterRestaurantsBasedOnCoordinates(latitude: Float, longitude: Float) {
        // Filter restaurants based on distance
        restaurantList.value = data.filter { res ->
            val restaurantLatitude = res["latitude"] as Double
            val restaurantLongitude = res["longitude"] as Double
            val distance = calculateDistance(
                latitude.toDouble(),
                longitude.toDouble(),
                restaurantLatitude,
                restaurantLongitude
            )
            // Filter restaurants within a 30 km radius
            distance <= 30000.0
        }
    }


    private fun initializeLocation() {
        // get access to local storage
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // get coordinates from MyLocationManager
        myLocationManager.getCoordinates { latitude, longitude ->
            isLocationInitialized.value = true

            userLatitude.value = latitude.toFloat()
            userLongitude.value = longitude.toFloat()

            // save the coordinates to local storage
            editor.putFloat("userLatitude", userLatitude.value)
            editor.putFloat("userLongitude", userLongitude.value)

            editor.apply()

            // Filter restaurants based on distance
            filterRestaurantsBasedOnCoordinates(userLatitude.value, userLongitude.value)
        }
    }

    // delivery time calculation
    private fun calculateDeliveryTime(
        userLatitude: Double,
        userLongitude: Double,
        restaurantLatitude: Double,
        restaurantLongitude: Double
    ): Int {
        // minimum delivery time
        val minDeliveryTime = 20.0
        var totalDeliveryTime = minDeliveryTime

        // 1km - 1 minute
        totalDeliveryTime += calculateDistance(userLatitude, userLongitude, restaurantLatitude, restaurantLongitude) / 1000

        // if temperature less than 0, add extra 5 minutes
        if (temperature.value < 0) {
            totalDeliveryTime += 5.0
        }

        // round to nearest 5
        return (totalDeliveryTime / 5.0).roundToInt() * 5
    }


    // card to display a restaurant
    @Composable
    fun ResCard(res: Map<String, Any>, onClick: () -> Unit) {
        // the card has elevation and is clickable
        Card(
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

                Column(modifier = Modifier.padding(16.dp, 10.dp, 16.dp, 10.dp)) {

                    // name of the restaurant
                    Text(
                        text = res["name"] as String ?: "Restaurant",
                        fontSize = 24.sp,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    // rating of the restaurant
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.star),
                            contentDescription = "rating"
                        )
                        var rating = ""
                        if (res["rating"] as Double <= 2.5) {
                            rating = "Bad"
                        } else if (res["rating"] as Double <= 3.5) {
                            rating = "Satisfied"
                        } else if (res["rating"] as Double <= 4.5) {
                            rating = "Good"
                        } else rating = "Very good"
                        Text(
                            text = rating + " (" + res["reviewsNumber"].toString() + "+)",
                            color = Color.Gray
                        )
                    }

                    // calculate delivery time of the restaurant
                    val deliveryTime = calculateDeliveryTime(userLatitude.value.toDouble(), userLongitude.value.toDouble(), res["latitude"] as Double, res["longitude"] as Double)
                    Text(text = "Delivery time $deliveryTime minutes", color = Color.Gray)

                    // calculate distance away of the restaurant
                    var distanceAway = calculateDistance(
                        userLatitude.value.toDouble(),
                        userLongitude.value.toDouble(),
                        res["latitude"] as Double,
                        res["longitude"] as Double
                    ) / 1000
                    distanceAway = "%.1f".format(distanceAway).toFloat()
                    Text(text = "$distanceAway km", color = Color.Gray)
                }
            }
        }
    }
}
