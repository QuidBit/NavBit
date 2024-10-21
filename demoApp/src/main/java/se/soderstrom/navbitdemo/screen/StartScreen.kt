package se.soderstrom.navbitdemo.screen

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.soderstrom.navbitdemo.navbit.Interaction
import se.soderstrom.navbitdemo.R
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.res.colorResource
import se.quidbit.navbit.types.PreviewTheme
import se.soderstrom.navbitdemo.BaseActivity
import se.soderstrom.navbitdemo.navbit.ScreenHandler

@Composable
fun StartScreen(count: Int) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            Spacer(Modifier.padding(top = 128.dp))

            // Static UI components that do not depend on count
            key(Unit) {
                WelcomeText()
                InfoButtonSection()
            }

            // Dynamic UI component that depends on count
            IncrementSection(count)
            Spacer(Modifier.padding(top = 64.dp))
        }

        items(List(10) { it }) { _ ->
            Text(text = "Some item for scroll testing", modifier = Modifier.padding(vertical = 16.dp))
        }
    }
}

@Composable
fun WelcomeText() {
    Log.i("NavBitDemo", "--StartScreen - RECOMPOSE TEXT")
    Text(
        text = "Welcome to NavBit",
        fontSize = 30.sp,
        color = colorResource(R.color.demo_black)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Easy and fluid navigation",
        fontSize = 15.sp,
        color = colorResource(R.color.demo_black)
    )
}

@Composable
fun InfoButtonSection() {
    Log.i("NavBitDemo", "--StartScreen - RECOMPOSE INFO BUTTON")
    val configuration = LocalConfiguration.current

    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        // Row layout for landscape orientation
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            InfoButton("View Info") { BaseActivity.instance().send(Interaction.ViewSheetsInfo) }
            Spacer(modifier = Modifier.width(16.dp))
            InfoButton("Go to Screen") { BaseActivity.instance().send(Interaction.GoToNext) }
        }
    } else {
        // Column layout for portrait orientation
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 48.dp)
        ) {
            InfoButton("View Info") { BaseActivity.instance().send(Interaction.ViewSheetsInfo) }
            Spacer(modifier = Modifier.height(24.dp))
            InfoButton("Go to Screen") { BaseActivity.instance().send(Interaction.GoToNext) }
        }
    }
}


@Composable
fun InfoButton(text: String, onClick: () -> Unit) {
    Button (
        onClick = onClick,
        modifier = Modifier.wrapContentSize(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            color = Color.Black,
        )
    }
}

@Composable
fun IncrementSection(count: Int) {
    Log.i("NavBitDemo", "--StartScreen - RECOMPOSE INCREMENT")
    val configuration = LocalConfiguration.current

    Card(
        modifier = Modifier.wrapContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Gray
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            AnimatedIncrementText(count)

            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                // In landscape mode, place the buttons in a Row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    InfoButton(text = "Increment", onClick = { BaseActivity.instance().send(Interaction.Increment) })
                    Spacer(modifier = Modifier.width(16.dp))
                    ClearButton(count = count, onClear = { })
                }
            } else {
                // In portrait mode, place the buttons in a single column (no extra column)
                InfoButton(text = "Increment", onClick = { BaseActivity.instance().send(Interaction.Increment) })
                Spacer(modifier = Modifier.height(16.dp))
                ClearButton(count = count, onClear = { BaseActivity.instance().send(Interaction.ClearCheck) })
            }
        }
    }
}

@Composable
fun AnimatedIncrementText(count: Int) {
    Log.i("NavBitDemo", " --incremented value $count")
    Row(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Incremented Value: ",
            fontSize = 15.sp,
            color = colorResource(R.color.demo_black)
        )

        AnimatedContent(
            targetState = count,
            transitionSpec = {
                slideInVertically(
                    animationSpec = tween(durationMillis = 500),
                    initialOffsetY = { fullHeight -> fullHeight } // Start from below
                ) togetherWith
                slideOutVertically(
                    animationSpec = tween(durationMillis = 500),
                    targetOffsetY = { fullHeight -> -fullHeight } // Slide out upwards
                )
            },
            label = ""
        ) { targetCount ->
            Text(
                text = "$targetCount",
                fontSize = 15.sp,
                color = colorResource(R.color.demo_black)
            )
        }
    }
}

@Composable
fun ClearButton(count: Int, onClear: () -> Unit) {
    val isEnabled = count > 0

    // Animate the background color
    val backgroundColor by animateColorAsState(
        targetValue = if (isEnabled) Color.Red else Color.LightGray,
        animationSpec = tween(durationMillis = 300),
        label = ""
    )

    Button(
        onClick = onClear,
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        elevation = ButtonDefaults.buttonElevation(4.dp),
    ) {
        Text(
            text = "Clear Count",
            fontSize = 15.sp,
            color = Color.Black,
        )
    }
}

@Preview(name = "Portrait Mode with count = 0")
@Composable
fun PreviewStartScreenPortraitWithZeroCount() {
    PreviewTheme(ScreenHandler(), true) {
        StartScreen(count = 0)
    }
}

@Preview(widthDp = 600, heightDp = 300, name = "Landscape Mode with count = 5")
@Composable
fun PreviewStartScreenLandscapeWithFiveCount() {
    PreviewTheme(ScreenHandler(), true) {
        StartScreen(count = 5)
    }
}