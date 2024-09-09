package se.soderstrom.navbitdemo.newscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.soderstrom.navbitdemo.parts.ActionButton

@Composable
fun PopupClearScreen(onYesClick: () -> Unit, onNoClick: () -> Unit) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .padding(vertical = 32.dp, horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Clear the counter?",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier
                .padding(vertical = 32.dp, horizontal = 48.dp),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            ActionButton(
                text = "Yes",
                backgroundColor = Color(0xFF018786),
                onClick = onYesClick
            )
            Spacer(modifier = Modifier.width(32.dp))
            ActionButton(
                text = "No",
                backgroundColor = Color.Black,
                onClick = onNoClick
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360, name = "Landscape Preview")
@Composable
fun PopupPreview() {
    PopupClearScreen({},{})
}