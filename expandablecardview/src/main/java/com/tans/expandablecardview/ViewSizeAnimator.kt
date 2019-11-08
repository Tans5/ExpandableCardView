package com.tans.expandablecardview

import android.animation.TimeInterpolator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 *
 * author: pengcheng.tan
 * date: 2019-11-07
 */
class ViewSizeAnimator private constructor(val view: View,
                                           val size: Size,
                                           private val valueAnimator: ValueAnimator) {

    private var state: AnimatorState = AnimatorState.Expand

    private val sizeTypeEvaluator =
        TypeEvaluator<Size> { fraction, startValue, endValue ->
            Size(startValue.width + ((endValue.width - startValue.width) * fraction).toInt(),
                startValue.height + ((endValue.height - startValue.height) * fraction).toInt())
        }

    fun expand(type: AnimatorType, listener: (state: AnimatorState) -> Unit = {}): Boolean {
        return if (state is AnimatorState.Fold && type == (state as? AnimatorState.Fold)?.type) {
            startViewSizeAnimator(isExpand = true, type = type ,listener = listener)
            true
        } else {
            false
        }
    }

    fun fold(type: AnimatorType, listener: (state: AnimatorState) -> Unit = {}): Boolean {

        return if (state == AnimatorState.Expand) {
            startViewSizeAnimator(isExpand = false, type = type, listener = listener)
            true
        } else {
            false
        }

    }

    fun expandOrFold(type: AnimatorType, listener: (state: AnimatorState) -> Unit = {}): Boolean {
        return when {
            state is AnimatorState.Fold && type == (state as? AnimatorState.Fold)?.type -> {
                startViewSizeAnimator(isExpand = true, type = type ,listener = listener)
                true
            }
            state == AnimatorState.Expand -> {
                startViewSizeAnimator(isExpand = false, type = type, listener = listener)
                true
            }
            else -> false
        }
    }

    private fun startViewSizeAnimator(isExpand: Boolean,
                                      type: AnimatorType,
                                      listener: (state: AnimatorState) -> Unit = {}) {

        valueAnimator.removeAllUpdateListeners()
        val changeToSize = when (type) {
            AnimatorType.Height -> size.copy(height = 0)
            AnimatorType.Width -> size.copy(width = 0)
            AnimatorType.WidthAndHeight -> Size(0, 0)
        }
        if (isExpand) {
            valueAnimator.setObjectValues(changeToSize, size)
        } else {
            valueAnimator.setObjectValues(size, changeToSize)
        }
        valueAnimator.setEvaluator(sizeTypeEvaluator)
        valueAnimator.addUpdateListener {
            val currentSize = it.animatedValue as Size
            view.layoutParams.height = currentSize.height
            view.layoutParams.width = currentSize.width
            view.requestLayout()
            val state = when (type) {
                AnimatorType.Width -> {
                    if (currentSize.width <=0 ) {
                        AnimatorState.Fold(type)
                    } else if (currentSize.width >= size.width) {
                        AnimatorState.Expand
                    } else {
                        if (isExpand) AnimatorState.RunningToExpand(type) else AnimatorState.RunningToFold(type)
                    }
                }
                AnimatorType.Height -> {
                    if (currentSize.height <=0 ) {
                        AnimatorState.Fold(type)
                    } else if (currentSize.height >= size.height) {
                        AnimatorState.Expand
                    } else {
                        if (isExpand) AnimatorState.RunningToExpand(type) else AnimatorState.RunningToFold(type)
                    }
                }
                AnimatorType.WidthAndHeight -> {
                    if (currentSize.height <=0 && currentSize.width <= 0) {
                        AnimatorState.Fold(type)
                    } else if (currentSize.height >= size.height && currentSize.width >= size.width) {
                        AnimatorState.Expand
                    } else {
                        if (isExpand) AnimatorState.RunningToExpand(type) else AnimatorState.RunningToFold(type)
                    }
                }
            }
            listener(state)
            this.state = state
        }
        valueAnimator.start()
    }

    fun currentState(): AnimatorState = this.state


    companion object {

        enum class AnimatorType {
            Width, Height, WidthAndHeight
        }

        sealed class AnimatorState {
            class Fold(val type: AnimatorType) : AnimatorState()
            object Expand : AnimatorState()
            class RunningToFold(val type: AnimatorType) : AnimatorState()
            class RunningToExpand(val type: AnimatorType) : AnimatorState()
        }

        data class Size(val width: Int, val height: Int)

        data class Builder(val duration: Long = 500,
                           val startDelay: Long = 0,
                           val timeInterpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
                           val view: View,
                           val viewSize: Size) {


            fun build(): ViewSizeAnimator {

                val animator = ValueAnimator()
                animator.duration = duration
                animator.startDelay = startDelay
                animator.interpolator = timeInterpolator

                return ViewSizeAnimator(view = view,
                    size = viewSize,
                    valueAnimator = animator)
            }

        }
    }
}