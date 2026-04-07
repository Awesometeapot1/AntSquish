package com.example.antsquish

class GameLoop(private val gameView: GameView) : Thread() {
    var running = false
    private val targetFPS = 60L

    override fun run() {
        var lastTime = System.nanoTime()
        val targetNanos = 1_000_000_000L / targetFPS

        while (running) {
            val now = System.nanoTime()
            val deltaTime = ((now - lastTime) / 1_000_000_000f).coerceAtMost(0.05f)
            lastTime = now

            gameView.update(deltaTime)
            gameView.render()

            val sleepMs = (targetNanos - (System.nanoTime() - now)) / 1_000_000
            if (sleepMs > 0) sleep(sleepMs)
        }
    }
}
