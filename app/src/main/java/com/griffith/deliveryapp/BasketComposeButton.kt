package com.griffith.deliveryapp

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat.startActivity
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun ViewBasketButton(itemCount: Int, total: Double, startBasketActivityForResult: ActivityResultLauncher<Intent>) {
    val context = LocalContext.current // Get the current context
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .background(Color(238, 150, 75))
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, BasketActivity::class.java) // Create the intent
                startBasketActivityForResult.launch(intent)
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.CenterStart)
            ) {
                Text(
                    text = itemCount.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            Text(
                text = "View Basket",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
            ) {
                Text(
                    text = "${BigDecimal(total).setScale(2, RoundingMode.HALF_EVEN).toDouble()}€",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}