package se.soderstrom.navbitdemo.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import se.soderstrom.navbitdemo.parts.ActionButton

@Composable
fun SheetsInfoScreen() {
    val configuration = LocalConfiguration.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.demo_teal_200))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "It can open sheets",
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.padding(top = 24.dp, bottom = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 72.dp else 24.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal
            )

            LazyRow(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(vertical = 16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
            ) {
                items(20) {
                    Box(Modifier.padding(horizontal = 6.dp)) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color.Gray,
                                    shape = RoundedCornerShape(8.dp)
                                ).clip(RoundedCornerShape(8.dp)),
                        ) {
                            Box(
                                modifier = Modifier.height(140.dp).padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("Nested Scrolling")
                            }
                        }
                    }
                }
            }

            Text(
                text = "This sheet is locked from being dragged down",
                fontSize = 14.sp,
                color = Color.Black,
            )

            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    ActionButton(text = "Details", backgroundColor = colorResource(R.color.demo_teal_700), onClick = { BaseActivity.instance().send(Interaction.ViewSheetsInfoDetails) })
                    Spacer(modifier = Modifier.width(32.dp))
                    ActionButton(text = "Close", backgroundColor = Color.Black, onClick = { BaseActivity.instance().sendBack()})
                }
            } else {
                ActionButton(text = "Details", backgroundColor = colorResource(R.color.demo_teal_700), onClick = {BaseActivity.instance().send(Interaction.ViewSheetsInfoDetails) })
                Spacer(modifier = Modifier.height(32.dp))
                ActionButton(text = "Close", backgroundColor = Color.Black, onClick = { BaseActivity.instance().sendBack()})
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
val ButtonRippleConfig = RippleConfiguration(
    color = Color.Gray,
    rippleAlpha = RippleAlpha(0f, 0f, 0f, 0.1f)
)


@Preview(widthDp = 360, heightDp = 640, name = "Portrait Preview")
@Composable
fun PortraitPreview() {
    PreviewTheme(ScreenHandler(), true) {
        SheetsInfoScreen()
    }
}

@Preview(widthDp = 640, heightDp = 360, name = "Landscape Preview")
@Composable
fun LandscapePreview() {
    PreviewTheme(ScreenHandler(), true) {
        SheetsInfoScreen()
    }
}