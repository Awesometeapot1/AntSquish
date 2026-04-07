package com.example.antsquish

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private val gameLoop = GameLoop(this)
    private val antManager = AntManager()
    private val scoreManager = ScoreManager()
    private val spriteManager = SpriteManager(context)

    // Bin sits near the bottom-centre of the screen
    private var binX = 0f
    private var binY = 0f
    private val binSize = 120f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 64f
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    init {
        holder.addCallback(this)
    }

    // ── Surface lifecycle ────────────────────────────────────────────────────

    override fun surfaceCreated(holder: SurfaceHolder) {
        val frame = holder.surfaceFrame
        val w = if (frame.width() > 0) frame.width().toFloat() else width.toFloat()
        val h = if (frame.height() > 0) frame.height().toFloat() else height.toFloat()
        binX = w / 2f
        binY = h * 0.82f
        antManager.setScreenSize(w, h)

        gameLoop.running = true
        gameLoop.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        gameLoop.running = false
        try { gameLoop.join() } catch (e: InterruptedException) { Thread.currentThread().interrupt() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    // ── Input ────────────────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true

        if (scoreManager.isGameOver) {
            scoreManager.reset()
            antManager.reset()
            return true
        }

        if (antManager.trySquish(event.x, event.y)) {
            scoreManager.addPoint()
        }
        return true
    }

    // ── Game logic (called from GameLoop thread) ─────────────────────────────

    fun update(deltaTime: Float) {
        if (scoreManager.isGameOver) return
        val reached = antManager.update(deltaTime, binX, binY)
        repeat(reached) { scoreManager.loseLife() }
    }

    // ── Rendering (called from GameLoop thread) ──────────────────────────────

    fun render() {
        val canvas = holder.lockCanvas() ?: return
        try {
            drawGame(canvas)
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawGame(canvas: Canvas) {
        // Background
        val bg = spriteManager.get("background")
        if (bg != null) {
            canvas.drawBitmap(bg, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), null)
        } else {
            canvas.drawColor(Color.rgb(200, 240, 160)) // placeholder: light green
        }

        // Bin
        val binRect = RectF(binX - binSize / 2, binY - binSize / 2, binX + binSize / 2, binY + binSize / 2)
        val binBitmap = spriteManager.get("bin")
        if (binBitmap != null) {
            canvas.drawBitmap(binBitmap, null, binRect, null)
        } else {
            paint.color = Color.rgb(70, 100, 200) // placeholder: blue box
            paint.alpha = 255
            canvas.drawRect(binRect, paint)
        }

        // Ants & splats
        for (ant in antManager.ants) {
            val rect = ant.bounds
            when (ant.state) {
                AntState.ALIVE -> {
                    // 4-frame walk cycle at 8 fps
                    val sprite = spriteManager.getAnimated("ant", 4)
                    if (sprite != null) {
                        val frame = (ant.animTime * 8f).toInt() % sprite.frameCount
                        canvas.drawBitmap(sprite.bitmap, sprite.sourceRect(frame), rect, null)
                    } else {
                        paint.color = Color.rgb(30, 15, 5) // placeholder: dark brown oval
                        paint.alpha = 255
                        canvas.drawOval(rect, paint)
                    }
                }
                AntState.SQUISHED -> {
                    // 3-frame impact sequence that plays once over the squish lifetime (0.5 s)
                    val sprite = spriteManager.getAnimated("splat", 3)
                    if (sprite != null) {
                        val progress = 1f - (ant.squishTimer / 0.5f) // 0 → 1
                        val frame = (progress * sprite.frameCount).toInt().coerceIn(0, sprite.frameCount - 1)
                        paint.alpha = (ant.squishTimer * 510).toInt().coerceIn(0, 255)
                        canvas.drawBitmap(sprite.bitmap, sprite.sourceRect(frame), rect, paint)
                        paint.alpha = 255
                    } else {
                        paint.color = Color.rgb(100, 50, 10) // placeholder: brown splat
                        paint.alpha = (ant.squishTimer * 510).toInt().coerceIn(0, 255)
                        canvas.drawOval(RectF(rect.left - 12, rect.top + 8, rect.right + 12, rect.bottom - 8), paint)
                        paint.alpha = 255
                    }
                }
                else -> {}
            }
        }

        // HUD — score
        canvas.drawText("Score: ${scoreManager.score}", 30f, 80f, textPaint)

        // HUD — lives as red circles (swap for small ant sprite if you like)
        for (i in 0 until scoreManager.lives) {
            paint.color = Color.RED
            paint.alpha = 255
            canvas.drawCircle(width - 40f - i * 55f, 60f, 22f, paint)
        }

        // Game over overlay
        if (scoreManager.isGameOver) {
            paint.color = Color.argb(170, 0, 0, 0)
            paint.alpha = 170
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.alpha = 255

            textPaint.textSize = 90f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("GAME OVER", width / 2f, height / 2f - 60f, textPaint)
            textPaint.textSize = 55f
            canvas.drawText("Score: ${scoreManager.score}", width / 2f, height / 2f + 20f, textPaint)
            textPaint.textSize = 40f
            canvas.drawText("Tap to play again", width / 2f, height / 2f + 100f, textPaint)

            // Reset alignment for next frame
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = 64f
        }
    }
}
