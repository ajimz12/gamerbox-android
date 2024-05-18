package com.example.gamerbox.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewParent
import android.widget.ScrollView

class NestedScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            // Desactivar scroll padre si se puede hacer scroll
            val parent: ViewParent? = parent
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        if (e?.action == MotionEvent.ACTION_UP || e?.action == MotionEvent.ACTION_CANCEL) {
            // Activar scroll padre
            val parent: ViewParent? = parent
            parent?.requestDisallowInterceptTouchEvent(false)
        }
        return super.onTouchEvent(e)
    }
}
