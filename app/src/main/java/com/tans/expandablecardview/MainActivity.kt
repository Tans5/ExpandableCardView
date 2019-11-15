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
import com.tans.expandablecardview.ViewSizeAnimator.Companion.expandOrFoldWithObservable
import com.tans.expandablecardview.ExpandableCardView.Companion.expandOrFoldWithObservable
import com.tans.expandablecardview.databinding.LayoutCardBinding
import io.reactivex.Observable

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val expandableAdapter = bindingExpandableAdapter<Unit, LayoutCardBinding>(R.layout.layout_card,
            dataProvider =  Observable.just(List(10) { Unit }),
            bindingData = { position, data, binding ->
                val context: Context = binding.root.context
                when (position % 3) {
                    0 -> {
                        binding.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    }
                    1 -> {
                        binding.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
                    }

                    2 -> {
                        binding.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                    }
                }
            })

        exd_view.setAdapter(expandableAdapter)

        var headerSizeAnimator: ViewSizeAnimator? = null
        header_tv.post {
            headerSizeAnimator = ViewSizeAnimator.Companion.Builder(
                view = header_tv,
                viewSize = ViewSizeAnimator.Companion.Size(header_tv.measuredWidth, header_tv.measuredHeight))
                .build()
        }

        hide_or_show_header_bt.setOnClickListener {
            // headerSizeAnimator?.expandOrFoldWithObservable(ViewSizeAnimator.Companion.AnimatorType.Height)?.subscribe()
            val state = headerSizeAnimator?.currentState()
            if (state is ViewSizeAnimator.Companion.AnimatorState.Expand) {
                headerSizeAnimator?.foldWithoutAnimator(type = ViewSizeAnimator.Companion.AnimatorType.Height)
            } else if (state is ViewSizeAnimator.Companion.AnimatorState.Fold) {
                headerSizeAnimator?.expandWithoutAnimator()
            }
        }

        expand_or_show_bt.setOnClickListener {
            exd_view.expandOrFoldWithObservable().subscribe()
        }
    }
}
