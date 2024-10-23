package com.smart.album

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.smart.album.views.PanningImageView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val panningImageView = findViewById<PanningImageView>(R.id.panningImageView)
        Glide.with(this)
//             .load("https://pic.616pic.com/bg_w1180/00/19/28/7gPY8D8pmb.jpg")
            .load("https://www.145758.com/uploads/allimg/20230703/4-230F31H953643.jpg")
//            .load("https://photocdn.sohu.com/20150826/mp29415155_1440604461249_2.jpg")
//            .load("https://file.nbfox.com/wp-content/uploads/2020/04/1665_Girl_with_a_Pearl_Earring_nbfox.jpg")
//            .load("https://www.kuhw.com/d/file/p/2021/10-22/0d9525784ee4e7a74746eae20258bb79.jpg")
//            .load("https://p5.itc.cn/q_70/images03/20221108/bc97e952dd2f4fa4a0a27402bcd8cad9.jpeg")
//            .load("https://k.sinaimg.cn/n/collect/crawl/20160224/HK7h-fxprucu3187034.jpg/w700d1q75cms.jpg")
            .into(panningImageView)

        panningImageView.setOnClickListener{
            val intent = Intent(this, CarouselActivity::class.java)
            startActivity(intent)
        }
    }
}
