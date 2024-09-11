package se.soderstrom.navbitdemo.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.quidbit.navbit.types.InteractionReceiver
import se.soderstrom.navbitdemo.navbit.Interaction
import se.soderstrom.navbitdemo.parts.ActionButton

@Composable
fun SheetsInfoScreen(i : InteractionReceiver<Interaction>) {
    val configuration = LocalConfiguration.current

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFFB3E5FC))
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "It can open sheets",
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 72.dp else 24.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal
            )

            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    ActionButton(text = "Details", backgroundColor = Color(0xFF018786), onClick = {i.send(Interaction.ViewSheetsInfoDetails) })
                    Spacer(modifier = Modifier.width(32.dp))
                    ActionButton(text = "Close", backgroundColor = Color.Black, onClick = { i.sendBack()})
                }
            } else {
                ActionButton(text = "Details", backgroundColor = Color(0xFF018786), onClick = {i.send(Interaction.ViewSheetsInfoDetails) })
                Spacer(modifier = Modifier.height(32.dp))
                ActionButton(text = "Close", backgroundColor = Color.Black, onClick = { i.sendBack()})
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640, name = "Portrait Preview")
@Composable
fun PortraitPreview() {
    SheetsInfoScreen(InteractionReceiver())
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360, name = "Landscape Preview")
@Composable
fun LandscapePreview() {
    SheetsInfoScreen(InteractionReceiver())
}