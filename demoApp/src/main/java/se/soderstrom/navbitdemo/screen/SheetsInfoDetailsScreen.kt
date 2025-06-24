package se.soderstrom.navbitdemo.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.quidbit.navbit.types.PreviewTheme
import se.soderstrom.navbitdemo.BaseActivity
import se.soderstrom.navbitdemo.R
import se.soderstrom.navbitdemo.navbit.Interaction
import se.soderstrom.navbitdemo.navbit.ScreenHandler

@Composable
fun SheetsInfoDetailsScreen(expanded: Boolean) {
    val configuration = LocalConfiguration.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.demo_white)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "In multiple layers",
            fontSize = 20.sp,
            color = colorResource(R.color.demo_black),
            modifier = Modifier.padding(vertical = 24.dp),
            textAlign = TextAlign.Center
        )

        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape mode, place the buttons in a Row
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                DoneButton { BaseActivity.instance().send(Interaction.Done) }
                ExpandButton(expanded) { BaseActivity.instance().send(Interaction.Expand) }
            }
        } else {
            // In portrait mode, place the buttons in a Column
            DoneButton { BaseActivity.instance().send(Interaction.Done) }
            ExpandButton(expanded) { BaseActivity.instance().send(Interaction.Expand)}
        }

        val scrollState = rememberScrollState()

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur at lorem vel sapien mattis malesuada. Integer imperdiet vulputate dignissim. Donec quis accumsan felis. Donec et accumsan est. Phasellus tristique tellus sit amet turpis sodales tempor. Donec eleifend turpis hendrerit neque scelerisque, eu feugiat mi pretium. Sed fermentum arcu sed mi consectetur, ut ultrices est fringilla. Mauris ac elementum libero. Aliquam rhoncus leo eros, eu interdum purus laoreet sit amet. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur at lorem vel sapien mattis malesuada. Integer imperdiet vulputate dignissim. Donec quis accumsan felis. Donec et accumsan est. Phasellus tristique tellus sit amet turpis sodales tempor. Donec eleifend turpis hendrerit neque scelerisque, eu feugiat mi pretium. Sed fermentum arcu sed mi consectetur, ut ultrices est fringilla. Mauris ac elementum libero. Aliquam rhoncus leo eros, eu interdum purus laoreet sit amet. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur at lorem vel sapien mattis malesuada. Integer imperdiet vulputate dignissim. Donec quis accumsan felis. Donec et accumsan est. Phasellus tristique tellus sit amet turpis sodales tempor. Donec eleifend turpis hendrerit neque scelerisque, eu feugiat mi pretium. Sed fermentum arcu sed mi consectetur, ut ultrices est fringilla. Mauris ac elementum libero. Aliquam rhoncus leo eros, eu interdum purus laoreet sit amet. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur at lorem vel sapien mattis malesuada. Integer imperdiet vulputate dignissim. Donec quis accumsan felis. Donec et accumsan est. Phasellus tristique tellus sit amet turpis sodales tempor. Donec eleifend turpis hendrerit neque scelerisque, eu feugiat mi pretium. Sed fermentum arcu sed mi consectetur, ut ultrices est fringilla. Mauris ac elementum libero. Aliquam rhoncus leo eros, eu interdum purus laoreet sit amet. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur at lorem vel sapien mattis malesuada. Integer imperdiet vulputate dignissim. Donec quis accumsan felis. Donec et accumsan est. Phasellus tristique tellus sit amet turpis sodales tempor. Donec eleifend turpis hendrerit neque scelerisque, eu feugiat mi pretium. Sed fermentum arcu sed mi consectetur, ut ultrices est fringilla. Mauris ac elementum libero. Aliquam rhoncus leo eros, eu interdum purus laoreet sit amet. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur at lorem vel sapien mattis malesuada. Integer imperdiet vulputate dignissim. Donec quis accumsan felis. Donec et accumsan est. Phasellus tristique tellus sit amet turpis sodales tempor. Donec eleifend turpis hendrerit neque scelerisque, eu feugiat mi pretium. Sed fermentum arcu sed mi consectetur, ut ultrices est fringilla. Mauris ac elementum libero. Aliquam rhoncus leo eros, eu interdum purus laoreet sit amet.",
                    fontSize = 18.sp,
                    color = colorResource(R.color.demo_black),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DoneButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.demo_black)),
        shape = MaterialTheme.shapes.medium,
        elevation = ButtonDefaults.buttonElevation(4.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        Text(
            text = "Close",
            fontSize = 18.sp,
            color = colorResource(R.color.demo_white),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ExpandButton(expanded: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.demo_teal_700)),
        shape = MaterialTheme.shapes.medium,
        elevation = ButtonDefaults.buttonElevation(4.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        Text(
            text = if (expanded) "Collapse" else "Expand",
            fontSize = 18.sp,
            color = colorResource(R.color.demo_white),
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(widthDp = 360, heightDp = 640, name = "Portrait Preview")
@Composable
fun SheetsInfoDetailsPreview() {
    PreviewTheme(ScreenHandler(), true) {
        SheetsInfoDetailsScreen(true)
    }
}

@Preview(widthDp = 640, heightDp = 360, name = "Landscape Preview")
@Composable
fun SheetsInfoDetailsScreenPreview() {
    PreviewTheme(ScreenHandler(), true) {
        SheetsInfoDetailsScreen(expanded = true)
    }
}