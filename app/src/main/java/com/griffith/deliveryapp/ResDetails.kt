package com.griffith.deliveryapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.compose.ui.Alignment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griffith.deliveryapp.ui.theme.DeliveryAppTheme

class ResDetails : ComponentActivity() {
    private val basket: Basket = Basket.getInstance()

    // number of items in the basket
    private var itemCount = mutableStateOf(basket.getItems().size)

    // if item from another restaurant is added
    private val showErrorDialog =  mutableStateOf(false)

    // save the itemCount if modified in another activity
    private val startBasketActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Retrieve the updated item count from the basket
            val itemCount = Basket.getInstance().getItems().size
            this.itemCount.value = itemCount
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // parse the menu data
            @Suppress("DEPRECATION")
            val menu: List<HashMap<String, Any>> = intent.getSerializableExtra("menu") as List<HashMap<String, Any>>
            val restaurantId = intent.getStringExtra("restaurantId") ?: ""

            DeliveryAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Box to overlay the content and dialog
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // column of the "Menu" text, menu items, and the "View basket" button
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn (
                                modifier = Modifier.weight(1f)
                            ) {
                                item {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        // the "Menu" text
                                        Text(
                                            text = "Menu",
                                            fontSize = 24.sp,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        // the menu items
                                        for (item in menu) {
                                            ItemCard(item=item, onClick = {
                                                val addItemResult = basket.addItem(item, restaurantId)

                                                if (addItemResult) {
                                                    // Item added successfully
                                                    itemCount.value = basket.getItems().size
                                                    val returnIntent = Intent()
                                                    setResult(Activity.RESULT_OK, returnIntent)
                                                } else {
                                                    // Show dialog indicating that the item cannot be added
                                                    showErrorDialog.value = true
                                                }
                                            })
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }
                            }

                            // if the basket exists
                            if (itemCount.value > 0) {
                                // this button has a fixed position and is always at the bottom
                                ViewBasketButton(itemCount = itemCount.value, total = basket.getTotal(), startBasketActivityForResult)
                            }
                        }

                        // add dark background and call the dialog
                        if (showErrorDialog.value) {
                            BackgroundOverlay()
                            ShowAddItemErrorDialog { showErrorDialog.value = false }
                        }
                    }
                }
            }
        }
    }

}


@Composable
fun ItemCard(item: HashMap<String, Any>, onClick: () -> Unit) {
    // menu item
    Card (
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(2f)
                    .height(100.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // the item's name
                Text(text = item["foodName"] as String ?: "Menu Item", fontWeight = FontWeight.SemiBold, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

                // the item's description
                Text(text = item["foodDescription"] as String ?: "Description", maxLines = 2, fontSize = 16.sp, overflow = TextOverflow.Ellipsis, style = TextStyle(lineHeight = 18.sp))

                // the item's price
                Text(text = (item["foodPrice"].toString() + "€") ?: "Price")
            }

            Spacer(modifier = Modifier.width(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                // the item's image
                Image(
                    painter = painterResource(item["foodImg"] as Int ?: R.drawable.default_image),
                    contentDescription = item["foodName"] as String ?: "Menu Item",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(100.dp)
                        .width(100.dp)
                        .clip(RoundedCornerShape(5.dp))
                )

                Spacer(modifier = Modifier.width(10.dp))

                // the "Add" button
                // if clicked, add the item to the basket
                FloatingActionButton(
                    onClick = onClick,
                    modifier = Modifier.height(100.dp).border(1.dp, Color.LightGray, RoundedCornerShape(5)),
                    shape = RoundedCornerShape(5),
                    containerColor = Color.Transparent, // Transparent background
                    contentColor = Color(238, 150, 75), // Color of the icon
                    elevation = FloatingActionButtonDefaults.elevation( // Set elevation to zero
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        hoveredElevation = 0.dp,
                        focusedElevation = 0.dp
                    )
                ) {
                    Icon(Icons.Filled.Add, "Add")
                }
            }

        }
    }
}


// the error dialog
// triggered when item from another restaurant is added to the basket
@Composable
fun ShowAddItemErrorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Cannot Add Item")
        },
        text = {
            Text("You can only add items from the same restaurant to the basket.", fontSize = 20.sp)
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(238, 150, 75),
                    contentColor = Color.White
                ),
            ) {
                Text("OK")
            }
        }
    )
}

// the dark background
@Composable
fun BackgroundOverlay() {
    val overlayColor = Color.Black.copy(alpha = 0.5f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(overlayColor)
    )
}
