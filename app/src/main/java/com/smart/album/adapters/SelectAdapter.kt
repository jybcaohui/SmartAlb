package com.smart.album.adapters

import android.content.Context
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
import android.widget.Toast
import com.smart.album.R

class SelectAdapter(private val context: Context, private var items: List<String>, private var selectedItemPosition: Int = -1) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var customNum:String = ""
    private var customUnit:String = ""
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
            notifyDataSetChanged()
            onItemSelectedListener?.onItemSelected(item, position)
        }

        if(position == items.size-1){
            viewHolder.edCustom.visibility = View.VISIBLE
            viewHolder.spinner.visibility = View.VISIBLE
            // 创建 ArrayAdapter，使用字符串数组作为数据源
            val adapter = ArrayAdapter.createFromResource(
                context,
                R.array.custom_unit,
                android.R.layout.simple_spinner_item
            )
            // 设置下拉列表的样式
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // 设置 Adapter
            viewHolder.spinner.adapter = adapter
            // 设置选择监听器
            viewHolder.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedOption = parent.getItemAtPosition(position).toString()
                    customUnit = selectedOption
                    Toast.makeText(context, "Selected: $selectedOption", Toast.LENGTH_SHORT).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Do nothing
                }
            }

        } else {
            viewHolder.spinner.visibility = View.GONE
            viewHolder.edCustom.visibility = View.GONE
        }


        return view
    }

    private class ViewHolder(view: View) {
        val radioButton: RadioButton = view.findViewById(R.id.radioButton)
        val textView: TextView = view.findViewById(R.id.textView)
        val spinner: Spinner = view.findViewById(R.id.spinner)
        val edCustom: EditText = view.findViewById(R.id.ed_custom)
    }

    var onItemSelectedListener: OnItemSelectedListener? = null

    interface OnItemSelectedListener {
        fun onItemSelected(item: String, position: Int)
    }
}