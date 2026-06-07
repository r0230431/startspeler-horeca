package com.startspeler.horeca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.startspeler.horeca.app.App

class MainActivity : ComponentActivity() {
    private fun extractTableNumberFromIntent(): String? {
        val data = intent?.data ?: return null
        return data.getQueryParameter("table")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val deeplinkTableNumber = extractTableNumberFromIntent()

        setContent {
            App(initialQrTableNumber = deeplinkTableNumber)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}