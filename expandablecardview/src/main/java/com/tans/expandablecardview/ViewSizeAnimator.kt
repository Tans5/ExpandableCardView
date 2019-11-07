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
                                           private val valueAnimator: ValueAnimator){

    var state: AnimatorState = AnimatorState.Expand

    private val sizeTypeEvaluator =
        TypeEvaluator<Size> { fraction, startValue, endValue ->
            Size(startValue.width + ((endValue.width - startValue.width) * fraction).toInt(),
                startValue.height + ((endValue.height - startValue.height) * fraction).toInt())
        }

    fun expand(type: AnimatorType, listener: (state: AnimatorState) -> Unit = {}): Boolean {
        return if (state is AnimatorState.Fold && type == (state as? AnimatorState.Fold)?.type) {
            valueAnimator.removeAllUpdateListeners()
            when (type) {
                AnimatorType.Width -> {
                    valueAnimator.setIntValues(0, size.width)
                    valueAnimator.addUpdateListener {
                        val width = it.animatedValue as Int
                        view.layoutParams.width = width
                        view.requestLayout()
                        val state = if (width >= size.width) {
                            AnimatorState.Expand
                        } else {
                            AnimatorState.RunningToExpand(type)
                        }
                        listener(state)
                        this.state = state
                    }
                    valueAnimator.start()
                }

                AnimatorType.Height -> {

                    valueAnimator.setIntValues(0, size.height)
                    valueAnimator.addUpdateListener {
                        val height = it.animatedValue as Int
                        view.layoutParams.height = height
                        view.requestLayout()
                        val state = if (height >= size.height) {
                            AnimatorState.Expand
                        } else {
                            AnimatorState.RunningToExpand(type)
                        }
                        listener(state)
                        this.state = state
                    }
                    valueAnimator.start()

                }

                AnimatorType.WidthAndHeight -> {
                    valueAnimator.setObjectValues(Size(0, 0), size)
                    valueAnimator.setEvaluator(sizeTypeEvaluator)
                    valueAnimator.addUpdateListener {
                        val currentSize = it.animatedValue as Size
                        view.layoutParams.height = currentSize.height
                        view.layoutParams.width = currentSize.width
                        view.requestLayout()
                        val state = if (currentSize.height >= size.height &&
                            currentSize.width >= size.width) {
                            AnimatorState.Expand
                        } else {
                            AnimatorState.RunningToExpand(type)
                        }
                        listener(state)
                        this.state = state
                    }
                    valueAnimator.start()
                }
            }
            true
        } else {
            false
        }

    }

    fun fold(type: AnimatorType, listener: (state: AnimatorState) -> Unit = {}): Boolean {

        return if (state == AnimatorState.Expand) {
            valueAnimator.removeAllUpdateListeners()
            when (type) {
                AnimatorType.Width -> {
                    valueAnimator.setIntValues(size.width, 0)
                    valueAnimator.addUpdateListener {
                        val width = it.animatedValue as Int
                        view.layoutParams.width = width
                        view.requestLayout()
                        state = if (width <= 0) {
                            AnimatorState.Fold(type)
                        } else {
                            AnimatorState.RunningToFold(type)
                        }
                    }
                    valueAnimator.start()
                }

                AnimatorType.Height -> {

                    valueAnimator.setIntValues(size.height, 0)
                    valueAnimator.addUpdateListener {
                        val height = it.animatedValue as Int
                        view.layoutParams.height = height
                        view.requestLayout()
                        state = if (height <= 0) {
                            AnimatorState.Fold(type)
                        } else {
                            AnimatorState.RunningToFold(type)
                        }
                    }
                    valueAnimator.start()

                }

                AnimatorType.WidthAndHeight -> {
                    valueAnimator.setObjectValues(size, Size(0, 0))
                    valueAnimator.setEvaluator(sizeTypeEvaluator)
                    valueAnimator.addUpdateListener {
                        val currentSize = it.animatedValue as Size
                        println("CurrentSize: $currentSize")
                        view.layoutParams.height = currentSize.height
                        view.layoutParams.width = currentSize.width
                        view.requestLayout()
                        val state = if (currentSize.height <= 0 &&
                            currentSize.width <= 0) {
                            AnimatorState.Fold(type)
                        } else {
                            AnimatorState.RunningToFold(type)
                        }
                        listener(state)
                        this.state = state
                    }
                    valueAnimator.start()
                }
            }
            true
        } else {
            false
        }


    }




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