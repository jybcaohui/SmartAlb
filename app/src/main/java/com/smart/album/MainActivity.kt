package com.smart.album

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.ybq.android.spinkit.SpinKitView
import com.smart.album.utils.OkhttpUtil
import com.smart.album.views.SlideImageView
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private lateinit var spinKit: SpinKitView
    private lateinit var tvSetting: TextView
    private var curRate: Double = 0.0

    private var inCurStr = ""
    private var outCurStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvSetting = findViewById(R.id.setting)

        tvSetting.setOnClickListener{
//            openUrlInBrowser(this,"https://sites.google.com/view/sismaprivacy/home")
            val intent = Intent(this, CarouselActivity::class.java)
            startActivity(intent)
        }

        val slideImageView = findViewById<SlideImageView>(R.id.panningImageView)
        Glide.with(this)
//            .load("https://pic.616pic.com/bg_w1180/00/19/28/7gPY8D8pmb.jpg")
            .load("https://photocdn.sohu.com/20150826/mp29415155_1440604461249_2.jpg")
//            .load("https://file.nbfox.com/wp-content/uploads/2020/04/1665_Girl_with_a_Pearl_Earring_nbfox.jpg")
//            .load("https://www.kuhw.com/d/file/p/2021/10-22/0d9525784ee4e7a74746eae20258bb79.jpg")
//            .load("https://p5.itc.cn/q_70/images03/20221108/bc97e952dd2f4fa4a0a27402bcd8cad9.jpeg")
//            .load("https://k.sinaimg.cn/n/collect/crawl/20160224/HK7h-fxprucu3187034.jpg/w700d1q75cms.jpg")
            .into(slideImageView)
    }


    private fun calculateRate(){
        spinKit.visibility = View.VISIBLE
        //https://open.er-api.com/v6/latest/USD
        OkhttpUtil.makeSimpleGetRequest(this,
            "https://open.er-api.com/v6/latest/$inCurStr"
        ) { response ->
            if (response != null) {
                spinKit.visibility = View.GONE
                var data = JSONObject(response)
                if(data.has("result") && data.get("result") == "success"){
                    if(data.has("rates")){
                        var rates = data.getJSONObject("rates")
                        if(rates.has(outCurStr)){
                            curRate = rates.getDouble(outCurStr)

                        }
                    }
                }
            } else {
                spinKit.visibility = View.GONE
                Toast.makeText(this,"Tidak Ditemukan",Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun openUrlInBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Tidak Ditemukan", Toast.LENGTH_SHORT).show()
        }
    }
}
