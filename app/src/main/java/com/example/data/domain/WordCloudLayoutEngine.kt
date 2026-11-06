package com.example.data.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.example.data.local.CloudShape
import com.example.data.local.ColorPalette
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

data class PlacedWord(
    val id: String,
    val word: String,
    val count: Int,
    val percentage: Float,
    val fontSizeSp: Float,
    val x: Float, // Center offset relative to canvas center
    val y: Float, // Center offset relative to canvas center
    val width: Float,
    val height: Float,
    val isVertical: Boolean,
    val color: Color,
    val rawWords: List<String> = emptyList()
)

object WordCloudLayoutEngine {

    /**
     * Lay out words on a canvas of width x height using Archimedean spiral and shape constraints.
     */
    fun layoutWords(
        wordCounts: Map<String, Int>,
        groupRawMap: Map<String, List<String>>, // Canonical word -> list of raw words
        canvasWidth: Float,
        canvasHeight: Float,
        shape: CloudShape,
        palette: ColorPalette,
        minFontSize: Int,
        maxFontSize: Int,
        allowRotation: Boolean
    ): List<PlacedWord> {
        if (wordCounts.isEmpty() || canvasWidth <= 0f || canvasHeight <= 0f) return emptyList()

        val totalResponses = wordCounts.values.sum().toFloat().coerceAtLeast(1f)
        val sortedEntries = wordCounts.entries.sortedByDescending { it.value }

        val maxFreq = sortedEntries.first().value.toFloat()
        val minFreq = sortedEntries.last().value.toFloat()

        val placedList = mutableListOf<PlacedWord>()
        val placedRects = mutableListOf<Rect>()

        val colors = palette.hexColors.map { Color(it) }
        val cx = canvasWidth / 2f
        val cy = canvasHeight / 2f
        val maxRadius = min(canvasWidth, canvasHeight) * 0.45f

        for ((index, entry) in sortedEntries.withIndex()) {
            val word = entry.key
            val count = entry.value
            val percentage = (count / totalResponses) * 100f

            // Calculate font size
            val sizeRatio = if (maxFreq == minFreq) 0.5f else (count - minFreq) / (maxFreq - minFreq)
            val fontSp = minFontSize + (maxFontSize - minFontSize) * sizeRatio

            // Estimate text dimensions in pixels (approximate 0.65 width factor per character)
            val charCount = word.length.coerceAtLeast(1)
            val approxCharWidth = fontSp * 0.72f
            val approxCharHeight = fontSp * 1.1f

            val isVertical = allowRotation && (index % 4 == 1) // 25% rotated words if rotation enabled
            val textWidth = if (isVertical) approxCharHeight else (charCount * approxCharWidth)
            val textHeight = if (isVertical) (charCount * approxCharWidth) else approxCharHeight

            val wordColor = colors[index % colors.size]

            // Search for non-overlapping position along spiral
            var found = false
            var bestX = 0f
            var bestY = 0f

            var theta = (index * 1.37f) // start with index angle offset
            val dTheta = 0.15f
            val step = 3.5f

            while (theta < 200f) {
                val r = step * theta
                if (r > maxRadius) break

                val px = r * cos(theta)
                val py = r * sin(theta)

                // Check if inside shape boundary
                if (isInsideShape(px, py, maxRadius, shape)) {
                    val rect = Rect(
                        left = cx + px - textWidth / 2f,
                        top = cy + py - textHeight / 2f,
                        right = cx + px + textWidth / 2f,
                        bottom = cy + py + textHeight / 2f
                    )

                    // Collision check
                    var intersects = false
                    for (placed in placedRects) {
                        if (rect.overlaps(placed)) {
                            intersects = true
                            break
                        }
                    }

                    if (!intersects) {
                        found = true
                        bestX = px
                        bestY = py
                        placedRects.add(rect)
                        break
                    }
                }

                theta += dTheta
            }

            if (found) {
                placedList.add(
                    PlacedWord(
                        id = "$word-$index",
                        word = word,
                        count = count,
                        percentage = percentage,
                        fontSizeSp = fontSp,
                        x = bestX,
                        y = bestY,
                        width = textWidth,
                        height = textHeight,
                        isVertical = isVertical,
                        color = wordColor,
                        rawWords = groupRawMap[word] ?: listOf(word)
                    )
                )
            }
        }

        return placedList
    }

    private fun isInsideShape(x: Float, y: Float, maxR: Float, shape: CloudShape): Boolean {
        val nx = x / maxR // Normalized -1 to 1
        val ny = y / maxR

        return when (shape) {
            CloudShape.RECTANGLE -> abs(nx) <= 0.85f && abs(ny) <= 0.85f
            CloudShape.CIRCLE -> (nx * nx + ny * ny) <= 0.85f
            CloudShape.HEART -> {
                // Heart equation: (x^2 + y^2 - 1)^3 - x^2 * y^3 <= 0
                val hx = nx * 1.1f
                val hy = -ny * 1.1f + 0.15f // Invert Y and offset
                val val1 = hx * hx + hy * hy - 0.7f
                (val1 * val1 * val1 - hx * hx * hy * hy * hy) <= 0f
            }
            CloudShape.STAR -> {
                val r = kotlin.math.sqrt(nx * nx + ny * ny)
                val angle = kotlin.math.atan2(ny, nx)
                val starR = 0.5f + 0.35f * cos(5 * angle)
                r <= starR
            }
            CloudShape.CLOUD -> {
                // Compound circles simulating cloud bounds
                val distCenter = (nx * nx + ny * ny)
                if (distCenter <= 0.5f) return true

                // Cloud puffs
                val puff1 = ((nx - 0.3f).pow(2) + (ny + 0.1f).pow(2)) <= 0.22f
                val puff2 = ((nx + 0.3f).pow(2) + (ny + 0.1f).pow(2)) <= 0.22f
                val puff3 = ((nx).pow(2) + (ny - 0.25f).pow(2)) <= 0.25f
                val puff4 = ((nx).pow(2) + (ny + 0.25f).pow(2)) <= 0.30f
                puff1 || puff2 || puff3 || puff4
            }
        }
    }
}
