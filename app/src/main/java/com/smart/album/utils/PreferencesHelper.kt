package com.smart.album.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.api.services.drive.model.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.ArrayList

class PreferencesHelper private constructor(context: Context) {
    companion object {
        private const val PREFS_NAME = "SmartAlbs"
        private const val LIST_KEY = "img_list"

        @Volatile
        private var INSTANCE: PreferencesHelper? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesHelper(context).also { INSTANCE = it }
            }
    }

    private val sharedPreferences: SharedPreferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun clearData(){
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.clear()
        editor.apply() // 或者使用 commit() 方法
    }

    fun saveFileList(list: List<File>) {
        // 使用Gson将列表转换为JSON字符串
        val gson = Gson()
        val json = gson.toJson(list)

        // 将JSON字符串保存到SharedPreferences
        with(sharedPreferences.edit()) {
            putString(LIST_KEY, json)
            apply() // 或者使用commit()方法
        }
    }

    fun loadFileList(): List<File> {
        // 从SharedPreferences获取JSON字符串
        val json = sharedPreferences.getString(LIST_KEY, null)

        // 使用Gson将JSON字符串转换回列表
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<File>>() {}.type
        return gson.fromJson(json, type) ?: arrayListOf()
    }
}