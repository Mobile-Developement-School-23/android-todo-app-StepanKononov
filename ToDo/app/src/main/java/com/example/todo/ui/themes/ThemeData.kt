package com.example.todo.ui.themes

import com.example.todo.R

data class ThemeData(
    val night: ThemeDescription = ThemeDescription(R.drawable.moon_stars, ThemeEnum.NIGHT),
    val day: ThemeDescription = ThemeDescription(R.drawable.sun, ThemeEnum.DAY),
    val system: ThemeDescription = ThemeDescription(R.drawable.eclipse_alt, ThemeEnum.SYSTEM),
)