package com.tans.expandablecardview

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import io.reactivex.Observable
import java.lang.RuntimeException
import kotlin.math.abs

/**
 *
 * author: pengcheng.tan
 * date: 2019-11-06
 */

class ExpandableCardView : ViewGroup {

    private var foldOffset: Int = 100

    private var animatorState: AnimatorState = AnimatorState.Fold

    private var adapter: ExpandableAdapter<*, out ExpandableAdapter.ViewHolder>? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        initAttrs(attrs)
    }

    private fun initAttrs(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableCardView)
        (0 until typedArray.indexCount).forEach { index ->
            when (index) {
                R.styleable.ExpandableCardView_fold_offset -> {
                    foldOffset = typedArray.getDimension(index, 100f).toInt()
                }
                R.styleable.ExpandableCardView_default_state -> {
                    animatorState = typedArray.getInt(index, 0).let {
                        when (it) {
                            0 -> {
                                AnimatorState.Fold
                            }
                            1 -> {
                                AnimatorState.Expand
                            }
                            else -> AnimatorState.Fold
                        }
                    }
                }
            }
        }
        typedArray.recycle()
    }

    override fun onMeasure(parentWidthMeasureSpec: Int, parentHeightMeasureSpec: Int) {
        val childCount = childCount
        var childrenHeight = 0
        var childrenWith = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp: MarginLayoutParams = child.layoutParams as MarginLayoutParams
            measureChildWithMargins(child, parentWidthMeasureSpec, 0, parentHeightMeasureSpec, 0)

            val childWith = lp.leftMargin + lp.rightMargin + child.measuredWidth
            if (childWith > childrenWith) {
                childrenWith = childWith
            }

            val childHeight = when (i) {
                0 -> {
                    lp.topMargin + lp.bottomMargin + child.measuredHeight
                }
                else -> {
                    val lastChild = getChildAt(i - 1)
                    val lastChildLp = lastChild.layoutParams as MarginLayoutParams

                    ((lp.topMargin + lp.bottomMargin + child.measuredHeight) - ((lastChild.measuredHeight - foldOffset + lastChildLp.bottomMargin + lp.topMargin) * animatorState.expandProgress).toInt())

                }

            }

            childrenHeight += if (childHeight < 0) 0 else childHeight
        }

        setMeasuredDimension(
            View.resolveSize(childrenWith, parentWidthMeasureSpec),
            View.resolveSize(childrenHeight, parentHeightMeasureSpec)
        )

    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var usedHeight = 0
        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            val lp: MarginLayoutParams = child.layoutParams as MarginLayoutParams

            val offset =
                ((lp.topMargin + lp.bottomMargin + child.measuredHeight) - ((child.measuredHeight - foldOffset + lp.bottomMargin + lp.topMargin) * animatorState.expandProgress).toInt())

            child.layout(
                lp.leftMargin,
                usedHeight + lp.topMargin,
                childWidth + lp.leftMargin,
                usedHeight + childHeight + lp.topMargin
            )
            usedHeight += offset

        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    fun expand(animatorBuilder: ExpandableCardViewAnimatorBuilder? = ExpandableCardViewAnimatorBuilder(),
               stateListener: (state: AnimatorState) -> Unit): Boolean {
        return if (animatorState != AnimatorState.Fold) {
            false
        } else {
            if (animatorBuilder != null) {
                animatorBuilder.build(isExpand = true) {
                    animatorState = it
                    stateListener(it)
                    requestLayout()
                }.start()
            } else {
                animatorState = AnimatorState.Expand
                requestLayout()
            }
            true
        }
    }

    fun fold(animatorBuilder: ExpandableCardViewAnimatorBuilder? = ExpandableCardViewAnimatorBuilder(),
             stateListener: (state: AnimatorState) -> Unit): Boolean {

        return if (animatorState != AnimatorState.Expand) {
            false
        } else {
            if (animatorBuilder != null) {
                animatorBuilder.build(isExpand = false) {
                    animatorState = it
                    stateListener(it)
                    requestLayout()
                }.start()
            } else {
                animatorState = AnimatorState.Fold
                requestLayout()
            }
            true
        }
    }

    fun currentState(): AnimatorState = animatorState

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        adapter?.onAttachedToView(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        adapter?.onDetachedToView(this)
    }

    fun setAdapter(adapter: ExpandableAdapter<*, out ExpandableAdapter.ViewHolder>) {
        this.adapter = adapter
        if (isAttachedToWindow) {
            adapter.onAttachedToView(this)
        }
    }

    fun newChildren(children: List<View>) {
        removeAllViews()
        children.withIndex().forEach { (index, child) -> addView(child, index) }
    }

    companion object {

        sealed class AnimatorState(val expandProgress: Float) {
            object Fold : AnimatorState(1f)
            class RunningToFold(expandProgress: Float) : AnimatorState(expandProgress)
            object Expand : AnimatorState(0f)
            class RunningToExpand(expandProgress: Float) : AnimatorState(expandProgress)
        }

        data class ExpandableCardViewAnimatorBuilder(
            val duration: Long = 500,
            val startDelay: Long = 0,
            val timeInterpolator: TimeInterpolator = AccelerateDecelerateInterpolator()
        ) {
            fun build(isExpand: Boolean = true, stateListener: (state: AnimatorState) -> Unit = {  }): Animator {
                val animator = if (isExpand) {
                    ValueAnimator.ofFloat(1f, 0f)
                } else {
                    ValueAnimator.ofFloat(0f, 1f)
                }
                animator.duration = duration
                animator.startDelay = startDelay
                animator.interpolator = timeInterpolator
                animator.addUpdateListener {
                    val value = it.animatedValue as Float
                    val state = if (isExpand) {
                        if (abs(value - 0f) < 0.001f) {
                            AnimatorState.Expand
                        } else {
                            AnimatorState.RunningToExpand(value)
                        }
                    } else {
                        if (abs(value - 1.0f) < 0.001f) {
                            AnimatorState.Fold
                        } else {
                            AnimatorState.RunningToFold(value)
                        }
                    }
                    stateListener(state)
                }
                return animator
            }
        }

        fun ExpandableCardView.expandWithObservable(animatorBuilder: ExpandableCardViewAnimatorBuilder? = ExpandableCardViewAnimatorBuilder())
                : Observable<AnimatorState> {
            val (rx, call) = callToObservable<AnimatorState> { it == AnimatorState.Expand }
            return rx.doOnSubscribe { expand(animatorBuilder, call).let { if (!it) throw RuntimeException("Animator State is Error") } }
        }

        fun ExpandableCardView.foldWithObservable(animatorBuilder: ExpandableCardViewAnimatorBuilder? = ExpandableCardViewAnimatorBuilder())
                : Observable<AnimatorState> {
            val (rx, call) = callToObservable<AnimatorState> { it == AnimatorState.Fold }
            return rx.doOnSubscribe { fold(animatorBuilder, call).let { if (!it) throw RuntimeException("Animator State is Error") } }
        }

        fun ExpandableCardView.expandOrFoldWithObservable(animatorBuilder: ExpandableCardViewAnimatorBuilder? = ExpandableCardViewAnimatorBuilder()): Observable<AnimatorState> {
            return when (currentState()) {
                AnimatorState.Fold -> {
                    expandWithObservable(animatorBuilder)
                }

                AnimatorState.Expand -> {
                    foldWithObservable(animatorBuilder)
                }
                else -> {
                    Observable.error<AnimatorState>(Throwable("Animator State isn't Fold or Expand"))
                }
            }
        }

    }

}