package com.griffith.deliveryapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griffith.deliveryapp.ui.theme.DeliveryAppTheme
import java.math.BigDecimal
import java.math.RoundingMode

class BasketActivity : ComponentActivity() {
    private val basket: Basket = Basket.getInstance()

    // number of items in the basket
    private var itemCount = mutableStateOf(basket.getItems().size)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            DeliveryAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    // if no items, display the message in the center
                    if (itemCount.value == 0) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = "The basket is empty", fontSize = 24.sp, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        // column of the "Basket" text, items and the "Order now" button
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.weight(1f)
                            ) {
                                item {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {

                                        // "Basket" text
                                        Text(
                                            text = "Basket",
                                            fontSize = 24.sp,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        // the items
                                        BasketItems(basket, onItemRemoved = {
                                            itemCount.value = basket.getItems().size
                                            // save the result when an item is deleted
                                            val returnIntent = Intent().apply {
                                                putExtra("itemCount", basket.getItems().size)
                                            }
                                            setResult(Activity.RESULT_OK, returnIntent)
                                        })
                                    }
                                }
                            }

                            // if the basket exists
                            if (itemCount.value > 0) {
                                // the "Order now" button
                                // if clicked, clear the basket and go to the starting screen
                                // this button has a fixed position and is always at the bottom
                                Box(
                                    contentAlignment = Alignment.BottomCenter,
                                    modifier = Modifier
                                        .background(Color(238, 150, 75))
                                        .fillMaxWidth()
                                        .clickable {
                                            basket.clearBasket()
                                            val intent = Intent(context, MainActivity::class.java) // Create the intent
                                            intent.putExtra("skipLocationInitialization", true)
                                            startActivity(intent)
                                        }
                                ) {
                                    Text(
                                        text = "Order now",
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
    }
}

@Composable
fun BasketItems(basket: Basket = Basket.getInstance(), onItemRemoved: () -> Unit) {

    // to update the ui if an item is deleted
    val items = remember { mutableStateListOf<HashMap<String, Any>>().apply { addAll(basket.getItems()) } }

    for (item in items) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            // the item's name
            Text(("1x " + item["foodName"] as String) ?: "Menu Item", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {

                // the item's price
                Text((item["foodPrice"].toString() + "€") ?: "Menu Item", fontSize = 20.sp)

                // the delete button
                // if clicked, delete the item and update the ui
                FloatingActionButton(
                    modifier = Modifier.size(50.dp),
                    containerColor = Color.Transparent, // Transparent background
                    contentColor = Color(238, 150, 75), // Color of the icon
                    elevation = FloatingActionButtonDefaults.elevation( // Set elevation to zero
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        hoveredElevation = 0.dp,
                        focusedElevation = 0.dp
                    ),
                    onClick = {
                        // Remove the item from the basket
                        basket.removeItem(item)
                        items.remove(item) // This will trigger recomposition
                        onItemRemoved()
                    }
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                }
            }
        }
    }
    
    Divider()
    Spacer(modifier = Modifier.height(20.dp))

    // the total
    Row(verticalAlignment = Alignment.CenterVertically) {

        // the "Total" text
        Text("Total", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        Spacer(modifier = Modifier.weight(1f))
        // the total amount
        Text("${BigDecimal(basket.getTotal()).setScale(2, RoundingMode.HALF_EVEN).toDouble()}€", fontSize = 20.sp)
    }
}