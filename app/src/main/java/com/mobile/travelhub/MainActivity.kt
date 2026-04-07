package com.mobile.travelhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
<<<<<<< HEAD
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mobile.travelhub.ui.theme.TravelHubTheme
=======
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mobile.travelhub.ui.screens.TravelHubScreen
import com.mobile.travelhub.ui.theme.TravelHubTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
>>>>>>> main

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelHubTheme {
<<<<<<< HEAD
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
=======
                TravelHubScreen()
>>>>>>> main
            }
        }
    }
}

<<<<<<< HEAD
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TravelHubTheme {
        Greeting("Android")
    }
}
=======

>>>>>>> main
