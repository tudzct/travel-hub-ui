package com.mobile.travelhub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.platform.LocalContext

private object NoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return object : Modifier.Node(), DrawModifierNode {
            override fun ContentDrawScope.draw() {
                drawContent()
            }
        }
    }

    override fun equals(other: Any?): Boolean = other === this

    override fun hashCode(): Int = -1
}

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    primaryContainer = PrimaryContainer,
    secondary = Secondary,
    secondaryContainer = SecondaryContainer,
    tertiary = Tertiary,
    tertiaryContainer = TertiaryContainer,
    background = DarkSurfaceBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceContainer,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outlineVariant = DarkOutlineVariant,
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    background = LightSurfaceBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceContainer,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outlineVariant = LightOutlineVariant,
    tertiary = Tertiary,
    tertiaryContainer = TertiaryContainer,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalIndication provides NoIndication,
        LocalRippleConfiguration provides null
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
