package se.soderstrom.navbitdemo.newscreen

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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

@Composable
fun SheetsInfoDetailsScreen(
    onCloseClick: () -> Unit,
    onExpandClick: () -> Unit,
    expanded: Boolean,
) {
    val configuration = LocalConfiguration.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "In multiple layers",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier
                .padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )

        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape mode, place the buttons in a Row
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                DoneButton(onCloseClick)
                Spacer(modifier = Modifier.width(32.dp))
                ExpandButton(expanded, onExpandClick)
            }
        } else {
            // In portrait mode, place the buttons in a Column
            DoneButton(onCloseClick)
            Spacer(modifier = Modifier.height(32.dp))
            ExpandButton(expanded, onExpandClick)
        }

        if (expanded) {
            Text(
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur at lorem vel sapien mattis malesuada. Integer imperdiet vulputate dignissim. Donec quis accumsan felis. Donec et accumsan est. Phasellus tristique tellus sit amet turpis sodales tempor. Donec eleifend turpis hendrerit neque scelerisque, eu feugiat mi pretium. Sed fermentum arcu sed mi consectetur, ut ultrices est fringilla. Mauris ac elementum libero. Aliquam rhoncus leo eros, eu interdum purus laoreet sit amet.",
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier
                    .padding(24.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier.height(0.dp)) // Bottom Inset placeholder
    }
}

@Composable
fun DoneButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .wrapContentSize()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Close",
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ExpandButton(expanded: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .wrapContentSize()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF018786)),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = if (expanded) "Collapse" else "Expand",
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640, name = "Portrait Preview")
@Composable
fun SheetsInfoDetailsPreview() {
    SheetsInfoDetailsScreen(
        onCloseClick = { /* Handle Close action */ },
        onExpandClick = { /* Handle Expand/Collapse action */ },
        expanded = true
    )
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360, name = "Landscape Preview")
@Composable
fun SheetsInfoDetailsScreenPreview() {
    SheetsInfoDetailsScreen(
        onCloseClick = { /* Handle Close action */ },
        onExpandClick = { /* Handle Expand/Collapse action */ },
        expanded = true
    )
}