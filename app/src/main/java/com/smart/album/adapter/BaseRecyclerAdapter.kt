package com.smart.album.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

abstract class BaseRecyclerAdapter<T : ViewHolder, B>(
  val mContext: Context?,
  var dataList: List<B>
) : RecyclerView.Adapter<T>() {
  private var itemClick: (holder: T, b: B) -> Unit = { _, _ -> }
  private var itemChildClick: (holder: T, b: B, viewId: Int) -> Unit = { _, _, _ -> }
  private var childClickIdsList = mutableListOf<Int>()

  override fun getItemCount(): Int {
    return dataList.size
  }

  override fun onBindViewHolder(holder: T, position: Int) {
    holder.itemView.setOnClickListener { itemClick.invoke(holder, dataList[position]) }

    childClickIdsList.forEach { vid ->
      holder.itemView.findViewById<View>(vid)?.setOnClickListener {
        itemChildClick.invoke(holder, dataList[position], vid)
      }
    }
  }

  protected fun initView(parent: ViewGroup?, rid: Int): View {
    return LayoutInflater.from(mContext).inflate(rid, parent, false)
  }


  fun setOnItemClickListener(function: (holder: T, b: B) -> Unit) {
    itemClick = function
  }

  fun setOnItemChildClickListener(function: (holder: T, b: B, viewId: Int) -> Unit) {
    itemChildClick = function
  }

  fun setItemChildClickIds(list: MutableList<Int>) {
    this.childClickIdsList = list
  }
}