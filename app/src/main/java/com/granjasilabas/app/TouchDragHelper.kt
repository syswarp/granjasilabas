package com.granjasilabas.app

import android.view.MotionEvent
import android.view.View

/**
 * Helper para iniciar drag con un toque simple (sin long press),
 * más amigable para niños en celular.
 */
class TouchDragHelper(
    private val onDragStart: (syllable: String, fromSlot: Int?) -> Unit
) : View.OnTouchListener {

    private var startX = 0f
    private var startY = 0f
    private val DRAG_THRESHOLD = 20f

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = Math.abs(event.x - startX)
                val dy = Math.abs(event.y - startY)
                if (dx > DRAG_THRESHOLD || dy > DRAG_THRESHOLD) {
                    val syl  = v.tag as? String ?: return false
                    val slot = (v.parent?.parent as? android.widget.FrameLayout)?.tag as? Int
                    onDragStart(syl, slot)
                    v.performLongClick()
                    return true
                }
            }
        }
        return false
    }
}
