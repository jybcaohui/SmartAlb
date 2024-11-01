package com.smart.album

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.smart.album.adapters.SelectAdapter

class SettingActivity : AppCompatActivity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var imgBack: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        rootLayout = findViewById(R.id.root)
        imgBack = findViewById(R.id.img_back)

        imgBack.setOnClickListener{
            finish()
        }

        findViewById<ConstraintLayout>(R.id.cl_display_time).setOnClickListener{
            showPopupWindow()
        }

    }
    private fun showPopupWindow() {
        val popupView = layoutInflater.inflate(R.layout.bottom_pop, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )
        popupView.findViewById<LinearLayout>(R.id.lv_root).setOnClickListener { popupWindow.dismiss() }
        val listView = popupView.findViewById<ListView>(R.id.listView)
        val options = listOf("10 Seconds", "15 Seconds", "30 Seconds",
            "1 Minutes", "5 Minutes", "10 Minutes", "Custom:")
        val adapter = SelectAdapter(this, options)
        adapter.onItemSelectedListener = object : SelectAdapter.OnItemSelectedListener {
            override fun onItemSelected(item: String, position: Int) {
                Toast.makeText(this@SettingActivity, "Selected: $item", Toast.LENGTH_SHORT).show()
//                popupWindow.dismiss()
            }
        }
        listView.adapter = adapter
        popupWindow.animationStyle = R.style.PopupAnimation
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0)
    }


}
