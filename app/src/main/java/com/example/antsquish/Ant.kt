package com.example.antsquish

import android.graphics.RectF

enum class AntState { ALIVE, SQUISHED, REACHED_BIN }

class Ant(
    var x: Float,
    var y: Float,
    val speed: Float = 80f
) {
    val size = 80f
    var state = AntState.ALIVE
    var squishTimer = 0.5f // seconds to show splat before removing
    var animTime = 0f      // accumulated time used to pick walk-cycle frame

    val bounds get() = RectF(x - size / 2, y - size / 2, x + size / 2, y + size / 2)

    fun update(deltaTime: Float, binX: Float, binY: Float) {
        when (state) {
            AntState.SQUISHED -> {
                squishTimer -= deltaTime
            }
            AntState.ALIVE -> {
                animTime += deltaTime

                val dx = binX - x
                val dy = binY - y
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (dist < 40f) {
                    state = AntState.REACHED_BIN
                    return
                }

                x += (dx / dist) * speed * deltaTime
                y += (dy / dist) * speed * deltaTime
            }
            else -> {}
        }
    }

    fun squish() {
        if (state == AntState.ALIVE) {
            state = AntState.SQUISHED
            squishTimer = 0.5f
        }
    }
}
