package com.smart.album.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RadioButton
import android.widget.TextView
import com.google.api.services.drive.model.File
import com.smart.album.R

class FolderAdapter(private val context: Context, private var items: List<File>, private var selectedItemId:String = "") : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.select_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val item = items[position]
        viewHolder.textView.text = item.name
        viewHolder.radioButton.isChecked = item.id == selectedItemId

        viewHolder.radioButton.setOnClickListener {
            selectedItemId = item.id
            onItemSelectedListener?.onItemSelected(item.id, position)
            notifyDataSetChanged()
        }
        return view
    }

    private class ViewHolder(view: View) {
        val radioButton: RadioButton = view.findViewById(R.id.radioButton)
        val textView: TextView = view.findViewById(R.id.textView)
    }

    var onItemSelectedListener: OnItemSelectedListener? = null

    interface OnItemSelectedListener {
        fun onItemSelected(item: String, position: Int)
    }
}