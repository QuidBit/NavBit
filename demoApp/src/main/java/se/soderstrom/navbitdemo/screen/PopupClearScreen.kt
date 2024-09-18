package se.soderstrom.navbitdemo.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.res.colorResource
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
fun PopupClearScreen() {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .padding(vertical = 32.dp, horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Clear the counter?",
            fontSize = 20.sp,
            color = colorResource(R.color.demo_black),
            modifier = Modifier
                .padding(vertical = 32.dp, horizontal = 16.dp),
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
                backgroundColor = colorResource(R.color.demo_teal_700),
                onClick = { BaseActivity.instance().send(Interaction.ClearCheck)}
            )
            Spacer(modifier = Modifier.width(32.dp))
            ActionButton(
                text = "No",
                textColor = colorResource(R.color.demo_white),
                backgroundColor = colorResource(R.color.demo_black),
                onClick = {BaseActivity.instance().sendBack()}
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 400, name = "Landscape Preview")
@Composable
fun PopupPreview() {
    PreviewTheme(ScreenHandler(), true) {
        PopupClearScreen()
    }
}