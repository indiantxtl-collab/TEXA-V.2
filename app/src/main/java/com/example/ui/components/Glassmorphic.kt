package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.TexaGold
import com.example.ui.theme.TexaGoldLight

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val glassBackground = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x1F222222),
                Color(0x0D111111)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xBFFFFFFF),
                Color(0x80F4F4F4)
            )
        )
    }

    val glassBorder = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x12FFFFFF),
                Color(0x05FFFFFF)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x40C59B27), // Gold tinted glass border
                Color(0x10C59B27)
            )
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                clip = false,
                ambientColor = Color(0x10000000),
                spotColor = Color(0x1F000000)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(glassBackground)
            .border(borderWidth, glassBorder, RoundedCornerShape(cornerRadius))
    ) {
        content()
    }
}

@Composable
fun LuxuryGoldCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    val goldGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFDFBA57),
            TexaGold,
            Color(0xFFA67C1E)
        )
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(cornerRadius),
                clip = false,
                spotColor = TexaGold
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(goldGradient)
            .border(1.5.dp, Color(0xFFFFF2D1), RoundedCornerShape(cornerRadius))
            .padding(1.dp)
    ) {
        content()
    }
}

@Composable
fun GlassAvatar(
    name: String,
    avatarId: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val initials = name.split(" ").map { it.firstOrNull() ?: "" }.joinToString("").take(2).uppercase()

    val avatarBrush = when (avatarId) {
        "elyse" -> Brush.linearGradient(colors = listOf(Color(0xFF2F80ED), Color(0xFF56CCF2)))
        "quantum" -> Brush.linearGradient(colors = listOf(Color(0xFFBB6BD9), Color(0xFF2D9CDB)))
        "anonymous" -> Brush.linearGradient(colors = listOf(Color(0xFF333333), Color(0xFF4F4F4F)))
        "me" -> Brush.linearGradient(colors = listOf(Color(0xFFF2C94C), TexaGold))
        "texa" -> Brush.linearGradient(colors = listOf(Color(0xFFDFBA57), Color(0xFF9B7218)))
        else -> Brush.linearGradient(colors = listOf(Color(0xFFEF5350), Color(0xFFFF9800)))
    }

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(50), clip = false)
            .clip(RoundedCornerShape(50))
            .background(avatarBrush)
            .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(50)),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = initials,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}
