package com.startspeler.horeca.screens.customer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import horeca.composeapp.generated.resources.Res
import horeca.composeapp.generated.resources.logoweb

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // Fade in
    var startAnimation by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing)
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(WindowInsets.systemBars.asPaddingValues()),
        contentAlignment = Alignment.Center
    ) {
        // Logo
        Image(
            painter = painterResource(Res.drawable.logoweb),
            contentDescription = "StartSpeler Logo",
            modifier = Modifier
                .height(140.dp)
                .fillMaxWidth(0.7f)
                .alpha(alpha)
        )
    }
}
