package com.smart.album.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import com.smart.album.R

class DisplayEffectAdapter(private val context: Context, private var items: List<String>, private var selectedItemPosition:Int = 0) : BaseAdapter() {

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
        viewHolder.textView.text = item
        viewHolder.radioButton.isChecked = position == selectedItemPosition

        viewHolder.radioButton.setOnClickListener {
            selectedItemPosition = position
            onItemSelectedListener?.onItemSelected(item, position)
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