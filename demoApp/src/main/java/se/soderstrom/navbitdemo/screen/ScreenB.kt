package se.soderstrom.navbitdemo.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.soderstrom.navbitdemo.BaseActivity
import se.soderstrom.navbitdemo.R
import se.soderstrom.navbitdemo.navbit.Interaction
import se.soderstrom.navbitdemo.parts.ActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenB() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Screen B") },
                navigationIcon = {
                    IconButton(onClick = {  BaseActivity.instance().sendBack() }) {
                        IconButton(onClick = {  BaseActivity.instance().sendBack() }) {
                            Image(
                                painter = painterResource(R.drawable.back),
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "End of the road",
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))
                ActionButton(text = "Go home", backgroundColor = Color(0xFF018786), onClick = { BaseActivity.instance().send(Interaction.Done) })
            }
        }
    )
}