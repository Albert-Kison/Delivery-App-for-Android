package com.griffith.deliveryapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griffith.deliveryapp.ui.theme.DeliveryAppTheme
import java.io.Serializable

var enteredText = mutableStateOf("")
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val basket: Basket = Basket.getInstance()
    private var itemCount = mutableStateOf(basket.getItems().size)
    private val someActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            itemCount.value = basket.getItems().size
        }
    }
    private val startBasketActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Retrieve the updated item count from the basket
            val itemCount = Basket.getInstance().getItems().size
            this.itemCount.value = itemCount
            // Update your UI here if necessary
        }
    }

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
                    Column(modifier = Modifier.fillMaxSize()) {
                        TextField(value = enteredText.value,
                            onValueChange = { enteredText.value = it},
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
//                                backgroundColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent, // This removes the bottom border
                                focusedIndicatorColor = Color.Transparent // This removes the bottom border when focused as well
                            ),
                            keyboardActions = KeyboardActions.Default,
                            modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp).fillMaxWidth()
                        )
                        LazyColumn (
                            modifier = Modifier.weight(1f)
                        ) {
                            item {
                                Column (modifier = Modifier.padding(16.dp)) {
                                    for (res in data) {
                                        ResCard(res=res, onClick = {
                                            val intent = Intent(context, ResDetails::class.java)
                                            intent.putExtra("menu", res["menu"] as? Serializable)
                                            someActivityResultLauncher.launch(intent)
                                        })
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }
//                        Spacer(modifier = Modifier.height(16.dp))
                        if (itemCount.value > 0) {
                            ViewBasketButton(itemCount = itemCount.value, total = basket.getTotal(), startBasketActivityForResult)
                        }
                    }
                    
                }
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun ResCard(res: Map<String, Any>, onClick: () -> Unit) {
    Card (
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.clickable { onClick() }
    ) {
        Column {
            Image(
                painter = painterResource(res["img"] as Int ?: R.drawable.default_image),
                contentDescription = res["name"] as String ?: "Restaurant", // decorative
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
            )
//                                    Spacer(modifier = Modifier.height(4.dp))

            Column (modifier = Modifier.padding(16.dp, 10.dp, 16.dp, 10.dp)) {
                Text(
                    text = res["name"] as String ?: "Restaurant",
                    fontSize =  24.sp,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
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
                Text(text = "Delivery time 30-45 minutes", color = Color.Gray)
                Text(text = "2.3 km", color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DeliveryAppTheme {
        Greeting("Android")
    }
}