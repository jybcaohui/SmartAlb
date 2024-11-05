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
    }

    fun showLoading(){
        loadingDialog?.show(supportFragmentManager, "loading_dialog")
    }
    fun hideLoading(){
        loadingDialog?.dismiss()
    }

}

