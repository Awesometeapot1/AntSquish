package com.example.antsquish

class AntManager {
    val ants = mutableListOf<Ant>()
    private var spawnTimer = 0f
    private var spawnInterval = 2.0f
    private var screenWidth = 0f
    private var screenHeight = 0f

    fun setScreenSize(w: Float, h: Float) {
        screenWidth = w
        screenHeight = h
    }

    // Returns number of ants that reached the bin this frame
    fun update(deltaTime: Float, binX: Float, binY: Float): Int {
        spawnTimer += deltaTime
        if (spawnTimer >= spawnInterval && screenWidth > 0f) {
            spawnTimer = 0f
            spawnAnt()
            if (spawnInterval > 0.5f) spawnInterval -= 0.04f // gradually speed up
        }

        var reached = 0
        val iter = ants.iterator()
        while (iter.hasNext()) {
            val ant = iter.next()
            ant.update(deltaTime, binX, binY)
            when {
                ant.state == AntState.REACHED_BIN -> { iter.remove(); reached++ }
                ant.state == AntState.SQUISHED && ant.squishTimer <= 0f -> iter.remove()
            }
        }
        return reached
    }

    private fun spawnAnt() {
        val edge = (0..3).random()
        val x: Float
        val y: Float
        when (edge) {
            0 -> { x = (0..screenWidth.toInt()).random().toFloat(); y = 0f }
            1 -> { x = screenWidth; y = (0..screenHeight.toInt()).random().toFloat() }
            2 -> { x = (0..screenWidth.toInt()).random().toFloat(); y = screenHeight }
            else -> { x = 0f; y = (0..screenHeight.toInt()).random().toFloat() }
        }
        ants.add(Ant(x, y, speed = (60..130).random().toFloat()))
    }

    fun trySquish(tapX: Float, tapY: Float): Boolean {
        for (ant in ants) {
            if (ant.state == AntState.ALIVE && ant.bounds.contains(tapX, tapY)) {
                ant.squish()
                return true
            }
        }
        return false
    }

    fun reset() {
        ants.clear()
        spawnTimer = 0f
        spawnInterval = 2.0f
    }
}
