package com.smart.album

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

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

    }

}
