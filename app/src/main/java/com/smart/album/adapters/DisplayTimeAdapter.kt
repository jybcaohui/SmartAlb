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

class DisplayTimeAdapter(private val context: Context, private var items: List<String>, private var selectedItemPosition:Int = 0, private var displaySeconds: Int = 0) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var customNum:Int = 1
    private var customUnit:String = "Seconds"
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
            var seconds = 0
            if(position == items.size-1){
                customNum = try {
                    viewHolder.edCustom.text.toString().toInt()
                } catch (e: Exception) {
                    1
                }
                seconds = when(customUnit){
                    "Seconds" -> customNum * 1
                    "Minutes" -> customNum * 60
                    "Hours" -> customNum * 3600
                    else -> 1
                }
            }
            Log.d("TAG===", "position: $position")
            Log.d("TAG===", "seconds: $seconds")
            onItemSelectedListener?.onItemSelected(item, position, seconds)
            notifyDataSetChanged()
        }

        if(position == items.size-1){
            viewHolder.edCustom.visibility = View.VISIBLE
            var textWatcher = object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {
                    var seconds = 0
                    if(position == items.size-1){
                        customNum = try {
                            viewHolder.edCustom.text.toString().toInt()
                        } catch (e: Exception) {
                            1
                        }
                        seconds = when(customUnit){
                            "Seconds" -> customNum * 1
                            "Minutes" -> customNum * 60
                            "Hours" -> customNum * 3600
                            else -> 10
                        }
                    }
                    Log.d("TAG===", "222position: $position")
                    Log.d("TAG===", "222seconds: $seconds")
                    onItemSelectedListener?.onItemSelected(item, position, seconds)
                }
            }
            // 设置焦点变化监听器
            viewHolder.edCustom.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    viewHolder.edCustom.addTextChangedListener(textWatcher)
                } else {
                    viewHolder.edCustom.removeTextChangedListener(textWatcher)
                }
            }
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
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Do nothing
                }
            }
            if(selectedItemPosition == 6 && displaySeconds > 0){
                when {
                    displaySeconds % 3600 == 0 -> {
                        val hours = displaySeconds / 3600
                        viewHolder.edCustom.setText(hours.toString())
                        viewHolder.spinner.setSelection(2)
                    }
                    displaySeconds % 60 == 0 -> {
                        val minutes = displaySeconds / 60
                        viewHolder.edCustom.setText(minutes.toString())
                        viewHolder.spinner.setSelection(1)
                    }
                    else -> {
                        viewHolder.edCustom.setText(displaySeconds.toString())
                        viewHolder.spinner.setSelection(0)
                    }
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
        fun onItemSelected(item: String, position: Int, seconds:Int)
    }
}