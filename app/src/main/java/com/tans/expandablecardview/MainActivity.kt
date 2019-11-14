package com.tans.expandablecardview

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import com.tans.expandablecardview.ViewSizeAnimator.Companion.expandWithObservable
import com.tans.expandablecardview.ViewSizeAnimator.Companion.foldWithObservable

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val expandableAdapter: ExpandableAdapter<Unit, ExpandableAdapter.ViewHolder> = object : ExpandableAdapter<Unit, ExpandableAdapter.ViewHolder>() {

            override fun createViewHolder(
                position: Int,
                inflater: LayoutInflater,
                parent: ViewGroup
            ): ViewHolder {
                val view = inflater.inflate(R.layout.layout_card, parent, false)
                return object : ViewHolder() { override val view: View = view }
            }

            override fun bindItemViewData(position: Int, itemData: Unit, vh: ViewHolder) {
                val view = vh.view
                val context: Context = view.context
                when (position % 3) {
                    0 -> {
                        (view as? CardView)?.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    }
                    1 -> {
                        (view as? CardView)?.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
                    }

                    2 -> {
                        (view as? CardView)?.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                    }
                }
            }

        }

        exd_view.setAdapter(expandableAdapter)

        exd_view.post {
            expandableAdapter.notifyDataChange(List(10) { Unit })
        }

        var headerSizeAnimator: ViewSizeAnimator? = null
        header_tv.post {
            headerSizeAnimator = ViewSizeAnimator.Companion.Builder(
                view = header_tv,
                viewSize = ViewSizeAnimator.Companion.Size(header_tv.measuredWidth, header_tv.measuredHeight))
                .build()
        }

        hide_or_show_header_bt.setOnClickListener {
            headerSizeAnimator?.expandOrFold(ViewSizeAnimator.Companion.AnimatorType.Height)
        }

        expand_or_show_bt.setOnClickListener {
            val state = exd_view.currentState()
            if (state == ExpandableCardView.Companion.AnimatorState.Fold) {
                exd_view.expand(animatorBuilder = ExpandableCardView.Companion.ExpandableCardViewAnimatorBuilder(duration = 1000)) {  }
            } else if (state == ExpandableCardView.Companion.AnimatorState.Expand) {
                exd_view.fold(animatorBuilder = ExpandableCardView.Companion.ExpandableCardViewAnimatorBuilder(duration = 1000)) {  }
            }
        }
    }
}
