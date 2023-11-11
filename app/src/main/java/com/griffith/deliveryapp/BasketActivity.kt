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
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griffith.deliveryapp.ui.theme.DeliveryAppTheme
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Collections.addAll

class BasketActivity : ComponentActivity() {
    private val basket: Basket = Basket.getInstance()
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
                    if (itemCount.value == 0) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = "The basket is empty", fontSize = 24.sp)
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.weight(1f)
                            ) {
                                item {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = "Basket",
                                            fontSize = 24.sp,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        BasketItems(basket, onItemRemoved = {
                                            itemCount.value = basket.getItems().size
                                            // Set the result here
                                            val returnIntent = Intent().apply {
                                                putExtra("itemCount", basket.getItems().size)
                                            }
                                            setResult(Activity.RESULT_OK, returnIntent)
                                        })
                                    }
                                }
                            }

                            if (itemCount.value > 0) {
                                Box(
                                    contentAlignment = Alignment.BottomCenter,
                                    modifier = Modifier
                                        .background(Color(238, 150, 75))
                                        .fillMaxWidth()
                                        .clickable {
                                            basket.clearItems()
                                            val intent = Intent(context, MainActivity::class.java) // Create the intent
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
    val items = remember { mutableStateListOf<HashMap<String, Any>>().apply { addAll(basket.getItems()) } }
    for (item in items) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(("1x " + item["foodName"] as String) ?: "Menu Item", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text((item["foodPrice"].toString() + "€") ?: "Menu Item", fontSize = 20.sp)
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

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Total", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text("${BigDecimal(basket.getTotal()).setScale(2, RoundingMode.HALF_EVEN).toDouble()}€", fontSize = 20.sp)
    }
}