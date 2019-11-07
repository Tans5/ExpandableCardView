package com.tans.expandablecardview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expand_bt.setOnClickListener {
            val state = exd_view.currentState()
            if (state == ExpandableCardView.Companion.AnimatorState.Fold) {
                exd_view.expand(animatorBuilder = ExpandableCardView.Companion.ExpandableCardViewAnimatorBuilder()) {  }
            } else if (state == ExpandableCardView.Companion.AnimatorState.Expand) {
                exd_view.fold(animatorBuilder = ExpandableCardView.Companion.ExpandableCardViewAnimatorBuilder()) {  }
            }
        }
    }
}
