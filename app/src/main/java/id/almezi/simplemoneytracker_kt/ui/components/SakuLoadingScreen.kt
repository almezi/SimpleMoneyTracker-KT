package id.almezi.simplemoneytracker_kt.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import kotlin.math.roundToInt

private val GlowSize = 200.dp
private val LogoSize = 112.dp
private val LoaderWidth = 160.dp
private val LoaderHeight = 4.dp
private val LoaderSegment = 0.4f
private val TaglineWidth = 280.dp
private val Pill = RoundedCornerShape(percent = 50)

private const val RiseMillis = 600
private const val BreatheMillis = 2400
private const val FloatMillis = 2400
private const val SlideMillis = 1400

private const val RiseFrom = 0.96f
private const val RiseShift = 12f

/**
 * Splash shown while the database is opened and seeded.
 */
@Composable
fun SakuLoadingScreen(modifier: Modifier = Modifier) {
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        appear.animateTo(1f, tween(RiseMillis, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SakuTheme.colors.bg)
            .testTag(TestTags.LOADING_SCREEN),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SakuGlowLogo(enterFraction = appear.value, modifier = Modifier.size(GlowSize))

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = SakuTheme.text.screenTitle.copy(
                    fontSize = 36.sp,
                    lineHeight = 44.sp,
                    letterSpacing = (-0.5).sp,
                ),
                color = SakuTheme.colors.text,
                textAlign = TextAlign.Center,
                modifier = Modifier.riseIn(appear.value, delay = 0.1f),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.saku_tagline),
                style = SakuTheme.text.body,
                color = SakuTheme.colors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(TaglineWidth)
                    .riseIn(appear.value, delay = 0.2f),
            )

            Spacer(modifier = Modifier.height(48.dp))

            SakuLoader(visibleFraction = appear.value, modifier = Modifier.riseIn(appear.value, delay = 0.3f))

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.saku_loading_status),
                style = SakuTheme.text.caption,
                color = SakuTheme.colors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.riseIn(appear.value, delay = 0.4f),
            )
        }
    }
}

/** Radial halo behind the mark, breathing in and out while the mark floats. */
@Composable
private fun SakuGlowLogo(
    enterFraction: Float,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "sakuGlow")
    val breathe by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(BreatheMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe",
    )
    val haloAlpha by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(BreatheMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "haloAlpha",
    )
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(FloatMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "float",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(breathe)
                .clip(Pill)
                .background(
                    Brush.radialGradient(
                        0f to SakuLogoStart.copy(alpha = 0.35f * haloAlpha),
                        0.55f to SakuLogoEnd.copy(alpha = 0.12f * haloAlpha),
                        0.7f to Color.Transparent,
                    )
                )
        )
        SakuLogo(
            modifier = Modifier
                .offset(y = drift.dp)
                .scale(RiseFrom + (1f - RiseFrom) * enterFraction),
            size = LogoSize,
        )
    }
}

/** Indeterminate progress bar with a violet-to-green sweep. */
@Composable
private fun SakuLoader(
    visibleFraction: Float,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "sakuLoader")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(SlideMillis, easing = FastOutSlowInEasing)),
        label = "slide",
    )

    var trackWidthPx by remember { mutableStateOf(0f) }
    val travel = trackWidthPx * (1f + LoaderSegment)
    val shift = (-LoaderSegment * trackWidthPx + travel * progress * visibleFraction).roundToInt()

    Box(
        modifier = modifier
            .width(LoaderWidth)
            .height(LoaderHeight)
            .onSizeChanged { trackWidthPx = it.width.toFloat() }
            .clip(Pill)
            .background(SakuTheme.colors.barTrack)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(LoaderSegment)
                .fillMaxHeight()
                .offset { IntOffset(shift, 0) }
                .clip(Pill)
                .background(Brush.horizontalGradient(listOf(SakuLogoStart, SakuLogoEnd)))
        )
    }
}

/** Staggered entrance: fades in and lifts, mirroring the CSS `rise` keyframes. */
private fun Modifier.riseIn(enterFraction: Float, delay: Float): Modifier {
    val progress = ((enterFraction - delay) / (1f - delay)).coerceIn(0f, 1f)
    return this
        .graphicsLayer { alpha = progress }
        .offset(y = ((1f - progress) * RiseShift).dp)
}