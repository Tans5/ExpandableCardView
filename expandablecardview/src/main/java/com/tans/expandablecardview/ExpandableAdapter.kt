package com.tans.expandablecardview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 *
 * author: pengcheng.tan
 * date: 2019-11-06
 */

abstract class ExpandableAdapter<ItemData, VH: ExpandableAdapter.ViewHolder> {

    private var view: ExpandableCardView? = null

    private var dataList: List<ItemData> = emptyList()

    abstract fun createViewHolder(position: Int, inflater: LayoutInflater, parent: ViewGroup): VH

    abstract fun bindItemViewData(position: Int, itemData: ItemData, vh: VH)

    fun notifyDataChange(newDataList: List<ItemData>) {
        this.dataList = newDataList
        val view = this.view ?: error("ExpandableCardView is null.")
        val children = newDataList.withIndex().map { (index, item) ->
            val vh = createViewHolder(index, LayoutInflater.from(view.context), view)
            bindItemViewData(index, item, vh)
            vh.view
        }
        view.newChildren(children)
    }

    fun onAttachedToView(view: ExpandableCardView) {
        this.view = view
    }

    fun onDetachedToView(view: ExpandableCardView) {
        val currentView = this.view
        if (currentView == view) this.view = null
    }

    abstract class ViewHolder { abstract val view: View }

}