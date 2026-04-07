package com.example.antsquish

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Loads sprites by name from res/drawable/.
 * Returns null if the sprite isn't found — callers draw a coloured box instead.
 *
 * Sprite file names to use:
 *   ant.png        — walk-cycle sheet: 4 frames × 80 px = 320 × 80 px
 *   splat.png      — impact sheet:     3 frames × 80 px = 240 × 80 px
 *   bin.png        — static 120 × 120 px
 *   background.png — full-screen static image
 */
class SpriteManager(private val context: Context) {
    private val bitmapCache = mutableMapOf<String, Bitmap?>()
    private val animCache   = mutableMapOf<String, AnimatedSprite?>()

    fun get(name: String): Bitmap? {
        if (bitmapCache.containsKey(name)) return bitmapCache[name]
        val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
        val bitmap = if (resId != 0) BitmapFactory.decodeResource(context.resources, resId) else null
        bitmapCache[name] = bitmap
        return bitmap
    }

    fun getAnimated(name: String, frameCount: Int): AnimatedSprite? {
        val key = "$name:$frameCount"
        if (animCache.containsKey(key)) return animCache[key]
        val bitmap = get(name) ?: return null.also { animCache[key] = null }
        val sprite = AnimatedSprite(bitmap, frameCount)
        animCache[key] = sprite
        return sprite
    }
}
