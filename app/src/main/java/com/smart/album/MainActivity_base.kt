package com.smart.album

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.smart.album.utils.OkhttpUtil
import com.github.ybq.android.spinkit.SpinKitView
import org.json.JSONObject

class MainActivity_base : AppCompatActivity() {

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

        }

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