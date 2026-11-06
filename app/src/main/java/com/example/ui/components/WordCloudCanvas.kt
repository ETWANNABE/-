package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.domain.PlacedWord
import com.example.data.domain.WordCloudLayoutEngine
import com.example.data.local.CloudShape
import com.example.data.local.ColorPalette
import com.example.data.repository.AggregatedWordData
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun WordCloudCanvas(
    aggregatedData: AggregatedWordData?,
    shape: CloudShape,
    palette: ColorPalette,
    minFontSize: Int,
    maxFontSize: Int,
    allowRotation: Boolean,
    selectedWordId: String?,
    onWordClick: (PlacedWord) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxSize().testTag("word_cloud_canvas_box")) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        val wordCounts = aggregatedData?.wordCounts ?: emptyMap()
        val groupRawMap = aggregatedData?.groupRawMap ?: emptyMap()

        // Calculate layout
        val placedWords = remember(wordCounts, shape, palette, minFontSize, maxFontSize, allowRotation, widthPx, heightPx) {
            WordCloudLayoutEngine.layoutWords(
                wordCounts = wordCounts,
                groupRawMap = groupRawMap,
                canvasWidth = widthPx,
                canvasHeight = heightPx,
                shape = shape,
                palette = palette,
                minFontSize = minFontSize,
                maxFontSize = maxFontSize,
                allowRotation = allowRotation
            )
        }

        val scaleAnim = remember { Animatable(0.85f) }
        LaunchedEffect(placedWords) {
            scaleAnim.snapTo(0.85f)
            scaleAnim.animateTo(1.0f, animationSpec = tween(durationMillis = 400))
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("word_cloud_canvas")
                .pointerInput(placedWords, widthPx, heightPx) {
                    detectTapGestures { tapOffset ->
                        val cx = widthPx / 2f
                        val cy = heightPx / 2f

                        // Find tapped word
                        val tapped = placedWords.find { word ->
                            val wx = cx + word.x
                            val wy = cy + word.y
                            val halfW = word.width / 2f + 8f
                            val halfH = word.height / 2f + 8f

                            tapOffset.x in (wx - halfW)..(wx + halfW) &&
                                    tapOffset.y in (wy - halfH)..(wy + halfH)
                        }

                        if (tapped != null) {
                            onWordClick(tapped)
                        }
                    }
                }
        ) {
            val cx = widthPx / 2f
            val cy = heightPx / 2f
            val maxR = min(widthPx, heightPx) * 0.45f

            // Draw background shape guide outline
            val path = Path()
            when (shape) {
                CloudShape.CLOUD -> {
                    path.addOval(androidx.compose.ui.geometry.Rect(cx - maxR * 0.7f, cy - maxR * 0.45f, cx + maxR * 0.7f, cy + maxR * 0.45f))
                    path.addOval(androidx.compose.ui.geometry.Rect(cx - maxR * 0.4f, cy - maxR * 0.65f, cx + maxR * 0.4f, cy + maxR * 0.35f))
                }
                CloudShape.CIRCLE -> {
                    path.addOval(androidx.compose.ui.geometry.Rect(cx - maxR * 0.85f, cy - maxR * 0.85f, cx + maxR * 0.85f, cy + maxR * 0.85f))
                }
                CloudShape.HEART -> {
                    path.moveTo(cx, cy + maxR * 0.7f)
                    path.cubicTo(cx - maxR * 0.9f, cy, cx - maxR * 0.6f, cy - maxR * 0.8f, cx, cy - maxR * 0.3f)
                    path.cubicTo(cx + maxR * 0.6f, cy - maxR * 0.8f, cx + maxR * 0.9f, cy, cx, cy + maxR * 0.7f)
                }
                CloudShape.STAR -> {
                    for (i in 0 until 10) {
                        val r = if (i % 2 == 0) maxR * 0.85f else maxR * 0.4f
                        val angle = (i * 36 - 90) * (Math.PI / 180.0)
                        val x = cx + (r * cos(angle)).toFloat()
                        val y = cy + (r * sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                }
                CloudShape.RECTANGLE -> {
                    path.addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            rect = androidx.compose.ui.geometry.Rect(cx - maxR * 0.9f, cy - maxR * 0.65f, cx + maxR * 0.9f, cy + maxR * 0.65f),
                            cornerRadius = CornerRadius(32f, 32f)
                        )
                    )
                }
            }

            // Draw subtle shape guide outline
            drawPath(
                path = path,
                color = palette.hexColors.first().let { Color(it).copy(alpha = 0.08f) },
                style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f))
            )

            // Draw words
            val scale = scaleAnim.value
            placedWords.forEach { word ->
                val isSelected = word.id == selectedWordId

                val textStyle = TextStyle(
                    fontSize = (word.fontSizeSp * scale).sp,
                    fontWeight = if (word.count > 3) FontWeight.Bold else FontWeight.Medium,
                    fontFamily = FontFamily.Default,
                    color = if (isSelected) Color(0xFFFF2A6D) else word.color
                )

                val textLayoutResult = textMeasurer.measure(
                    text = word.word,
                    style = textStyle
                )

                val wx = cx + (word.x * scale)
                val wy = cy + (word.y * scale)

                val drawX = wx - textLayoutResult.size.width / 2f
                val drawY = wy - textLayoutResult.size.height / 2f

                if (word.isVertical) {
                    rotate(degrees = 90f, pivot = Offset(wx, wy)) {
                        if (isSelected) {
                            drawRoundRect(
                                color = Color(0xFFFF2A6D).copy(alpha = 0.15f),
                                topLeft = Offset(drawX - 8f, drawY - 4f),
                                size = Size(textLayoutResult.size.width + 16f, textLayoutResult.size.height + 8f),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                        }
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(drawX, drawY)
                        )
                    }
                } else {
                    if (isSelected) {
                        drawRoundRect(
                            color = Color(0xFFFF2A6D).copy(alpha = 0.15f),
                            topLeft = Offset(drawX - 8f, drawY - 4f),
                            size = Size(textLayoutResult.size.width + 16f, textLayoutResult.size.height + 8f),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                    }
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(drawX, drawY)
                    )
                }
            }
        }
    }
}
