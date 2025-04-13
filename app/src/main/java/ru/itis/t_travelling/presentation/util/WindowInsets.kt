package ru.itis.wishlist.util

import android.view.View
import android.view.WindowInsets

fun View.doOnApplyWindowInsets(
    block: (View, WindowInsets, InitialPadding) -> Unit
) {
    val initialPadding = recordInitialPaddingForView(this)
    setOnApplyWindowInsetsListener { v, insets ->
        block(v, insets, initialPadding)
        insets
    }
    requestApplyInsetsWhenAttached()
}

private fun recordInitialPaddingForView(view: View) =
    InitialPadding(
        start = view.paddingStart,
        top = view.paddingTop,
        end = view.paddingEnd,
        bottom = view.paddingBottom
    )

data class InitialPadding(
    val start: Int,
    val top: Int,
    val end: Int,
    val bottom: Int
)

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}