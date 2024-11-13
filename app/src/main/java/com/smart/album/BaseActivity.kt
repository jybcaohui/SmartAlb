package com.smart.album

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.smart.album.views.LoadingDialog


open class BaseActivity : AppCompatActivity() {

    private var loadingDialog: LoadingDialog? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = LoadingDialog()

        // 添加到活动列表
        App.instance.addActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 从活动列表中移除
        App.instance.removeActivity(this)
    }

    fun showLoading(){
        loadingDialog?.show(supportFragmentManager, "loading_dialog")
    }
    fun hideLoading(){
        loadingDialog?.dismiss()
    }

}

