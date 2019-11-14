package com.tans.expandablecardview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

/**
 *
 * author: pengcheng.tan
 * date: 2019-11-14
 */

class DataBindingViewHolder<Binding : ViewDataBinding>(val binding: Binding) : ExpandableAdapter.ViewHolder() {
    override val view: View = binding.root
}

abstract class DataBindingExpandableAdapter<ItemData, Binding : ViewDataBinding>
    : ExpandableAdapter<ItemData, DataBindingViewHolder<Binding>>()

fun <ItemData, Binding : ViewDataBinding> bindingExpandableAdapter(
    layoutId: Int,
    dataProvider: Observable<List<ItemData>>,
    bindingData: (position: Int, data: ItemData, binding: Binding) -> Unit = { _, _, _ -> }
): DataBindingExpandableAdapter<ItemData, Binding> {
    return object : DataBindingExpandableAdapter<ItemData, Binding>() {

        private var disposable: Disposable? = null

        override fun createViewHolder(
            position: Int,
            inflater: LayoutInflater,
            parent: ViewGroup
        ): DataBindingViewHolder<Binding> {
            val binding = DataBindingUtil.inflate<Binding>(inflater, layoutId, parent, false)
            return DataBindingViewHolder(binding)
        }

        override fun bindItemViewData(
            position: Int,
            itemData: ItemData,
            vh: DataBindingViewHolder<Binding>
        ) {
            bindingData(position, itemData, vh.binding)
        }

        override fun onAttachedToView(view: ExpandableCardView) {
            super.onAttachedToView(view)
            disposable = dataProvider
                .doOnNext { notifyDataChange(it) }
                .subscribe({}, {e -> e.printStackTrace()}, {})
        }

        override fun onDetachedToView(view: ExpandableCardView) {
            super.onDetachedToView(view)
            if (this.view == view) {
                disposable?.dispose()
            }
        }

    }
}