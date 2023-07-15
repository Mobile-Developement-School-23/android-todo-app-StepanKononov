package com.example.todocomposable.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColors(
    background = darkGray,
    surface = darkGraySecond,
    primary = blue,
    primaryVariant = purple700,
    onPrimary = white,
    secondary = blue,
    secondaryVariant = violentDark,
    onSecondary = white,
)


private val LightColorScheme = lightColors(
    background = lightGray,
    surface = white,
    primary = blue,
    primaryVariant = purple700,
    onPrimary = white,
    secondary = blue,
    secondaryVariant = violentDark,
    onSecondary = white,
)

@Composable
fun TodoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = ,
        content = content
    )
}