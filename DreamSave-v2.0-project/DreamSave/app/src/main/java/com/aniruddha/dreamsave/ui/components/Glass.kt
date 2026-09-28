package com.aniruddha.dreamsave.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aniruddha.dreamsave.logic.formatMoney
import com.aniruddha.dreamsave.ui.theme.Cyan
import com.aniruddha.dreamsave.ui.theme.ElectricBlue
import com.aniruddha.dreamsave.ui.theme.Gold
import com.aniruddha.dreamsave.ui.theme.LocalDarkTheme
import com.aniruddha.dreamsave.ui.theme.Magenta
import com.aniruddha.dreamsave.ui.theme.Violet
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

val BrandBrush: Brush = Brush.horizontalGradient(listOf(ElectricBlue, Violet))

/** Press-scale bounce + click, without the default ripple. */
fun Modifier.bouncyClick(onClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "press"
    )
    this
        .scale(scale)
        .clickable(interactionSource = source, indication = null, onClick = onClick)
}

@Composable
fun AppBackground(content: @Composable BoxScope.() -> Unit) {
    val dark = LocalDarkTheme.current
    val base = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(base)
            .drawBehind {
                val alpha = if (dark) 0.30f else 0.16f
                val radius = size.width
                val topLeft = Offset(size.width * 0.05f, size.height * 0.02f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ElectricBlue.copy(alpha = alpha), Color.Transparent),
                        center = topLeft,
                        radius = radius
                    ),
                    radius = radius,
                    center = topLeft
                )
                val bottomRight = Offset(size.width * 0.95f, size.height * 0.92f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Violet.copy(alpha = alpha), Color.Transparent),
                        center = bottomRight,
                        radius = radius
                    ),
                    radius = radius,
                    center = bottomRight
                )
            }
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 24.dp,
    contentPadding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val dark = LocalDarkTheme.current
    val shape = RoundedCornerShape(cornerRadius)
    val fill = if (dark) {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.11f), Color.White.copy(alpha = 0.04f)))
    } else {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.92f), Color.White.copy(alpha = 0.66f)))
    }
    val borderColor = if (dark) Color.White.copy(alpha = 0.15f) else Color(0x262A3568)
    var base = modifier
    if (onClick != null) base = base.bouncyClick(onClick)
    Column(
        modifier = base
            .clip(shape)
            .background(fill)
            .border(1.dp, borderColor, shape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(16.dp)
    val background = if (enabled) BrandBrush else Brush.horizontalGradient(listOf(Color(0x44808080), Color(0x44808080)))
    val clickModifier = if (enabled) Modifier.bouncyClick(onClick) else Modifier
    Row(
        modifier = modifier
            .then(clickModifier)
            .clip(shape)
            .background(background)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Color = ElectricBlue
) {
    GlassCard(modifier = modifier, cornerRadius = 20.dp, contentPadding = 16.dp) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(8.dp))
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val animated by animateFloatAsState(
        targetValue = if (started) progress.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "bar"
    )
    val track = if (LocalDarkTheme.current) Color.White.copy(alpha = 0.10f) else Color(0x1A2A3568)
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(BrandBrush)
        )
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    diameter: Dp,
    strokeWidth: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val animated by animateFloatAsState(
        targetValue = if (started) progress.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "ring"
    )
    val track = if (LocalDarkTheme.current) Color.White.copy(alpha = 0.10f) else Color(0x1A2A3568)
    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            rotate(degrees = -90f) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(Cyan, ElectricBlue, Violet, Magenta)),
                    startAngle = 0f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
        content()
    }
}

@Composable
fun AnimatedMoneyText(
    value: Double,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = Color.Unspecified
) {
    val animated by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "money"
    )
    val shown = if (abs(animated - value.toFloat()) < 0.005f) value else animated.toDouble()
    Text(
        text = formatMoney(shown),
        modifier = modifier,
        style = style,
        color = color,
        maxLines = 1
    )
}

private class Particle(
    val x: Float,
    val delay: Float,
    val sway: Float,
    val phase: Float,
    val size: Float,
    val spin: Float,
    val color: Color
)

@Composable
fun ConfettiOverlay(modifier: Modifier = Modifier) {
    val particles = remember {
        val random = Random(42)
        val palette = listOf(ElectricBlue, Cyan, Violet, Magenta, Gold, Color.White)
        List(110) {
            Particle(
                x = random.nextFloat(),
                delay = random.nextFloat() * 0.45f,
                sway = 10f + random.nextFloat() * 40f,
                phase = random.nextFloat() * 6.28f,
                size = 8f + random.nextFloat() * 10f,
                spin = 1f + random.nextFloat() * 3f,
                color = palette[random.nextInt(palette.size)]
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 4200, easing = LinearEasing))
    }
    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        for (p in particles) {
            val local = ((t - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            if (local <= 0f || local >= 1f) continue
            val y = -30f + local * (size.height + 60f)
            val x = p.x * size.width + sin(local * 7f + p.phase) * p.sway
            val alpha = 1f - local * local * local
            rotate(degrees = local * 360f * p.spin, pivot = Offset(x, y)) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(x - p.size / 2f, y - p.size / 2f),
                    size = Size(p.size, p.size * 0.6f)
                )
            }
        }
    }
}
