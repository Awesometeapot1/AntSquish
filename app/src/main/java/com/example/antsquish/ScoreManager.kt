package com.example.antsquish

class ScoreManager {
    var score = 0
        private set
    var lives = 3
        private set

    val isGameOver get() = lives <= 0

    fun addPoint() { score++ }
    fun loseLife() { if (lives > 0) lives-- }
    fun reset() { score = 0; lives = 3 }
}
