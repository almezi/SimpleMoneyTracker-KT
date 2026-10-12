package id.almezi.simplemoneytracker_kt.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val SakuLogoStart = Color(0xFF7C6CF6)
val SakuLogoEnd = Color(0xFF6EE7A0)

private const val VIEWPORT = 512f

/**
 * The Saku mark: a rounded gradient tile with a wallet on top. Drawn from the
 * same geometry as `third_party/saku-logo.svg` and the launcher icon.
 */
@Composable
fun SakuLogo(
    modifier: Modifier = Modifier,
    size: Dp = 112.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        drawLogo()
    }
}

private fun DrawScope.drawLogo() {
    val unit = size.width / VIEWPORT
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(SakuLogoStart, SakuLogoEnd),
            start = Offset.Zero,
            end = Offset(size.width, size.height),
        ),
        size = size,
        cornerRadius = CornerRadius(112f * unit, 112f * unit),
    )

    scale(unit, pivot = Offset.Zero) {
        drawRoundRect(
            color = Color.White.copy(alpha = 0.32f),
            topLeft = Offset(112f, 150f),
            size = Size(288f, 212f),
            cornerRadius = CornerRadius(44f, 44f),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.6f),
            topLeft = Offset(112f, 150f),
            size = Size(288f, 56f),
            cornerRadius = CornerRadius(28f, 28f),
        )
        drawRect(
            color = Color.White.copy(alpha = 0.6f),
            topLeft = Offset(112f, 178f),
            size = Size(288f, 28f),
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(152f, 256f),
            size = Size(124f, 18f),
            cornerRadius = CornerRadius(9f, 9f),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(152f, 290f),
            size = Size(84f, 18f),
            cornerRadius = CornerRadius(9f, 9f),
        )
        drawCircle(
            color = Color.White,
            radius = 26f,
            center = Offset(338f, 296f),
            style = Stroke(width = 14f, cap = StrokeCap.Round),
        )
    }
}