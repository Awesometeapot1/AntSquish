package com.example.antsquish

import android.graphics.Bitmap
import android.graphics.Rect

/**
 * A horizontally-stripped sprite sheet.
 * All frames are the same width; the sheet is a single row.
 *
 * Sprite sheet layout:
 *   ant.png   — 4 frames × 80 px = 320 × 80 px  (walk cycle, loops)
 *   splat.png — 3 frames × 80 px = 240 × 80 px  (impact, plays once)
 */
class AnimatedSprite(val bitmap: Bitmap, val frameCount: Int) {
    val frameW = bitmap.width / frameCount
    val frameH = bitmap.height

    fun sourceRect(frameIndex: Int): Rect {
        val i = frameIndex.coerceIn(0, frameCount - 1)
        return Rect(i * frameW, 0, (i + 1) * frameW, frameH)
    }
}
