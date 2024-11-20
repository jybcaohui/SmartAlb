package com.smart.album

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile
import com.smart.album.events.RefreshPageDataEvent
import com.smart.album.utils.PreferencesHelper
import org.greenrobot.eventbus.EventBus

class MainActivity : BaseActivity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var chooseButton: LinearLayout
    private lateinit var settingButton: LinearLayout
    private lateinit var statusText: TextView

    private val REQUEST_CODE_PICK_FOLDER = 10001

    private fun pickFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, REQUEST_CODE_PICK_FOLDER)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootLayout = findViewById(R.id.root)


        chooseButton = findViewById(R.id.chooseButton)
        settingButton = findViewById(R.id.settingButton)
        statusText = findViewById(R.id.statusText)

        chooseButton.requestFocus()
        rootLayout.setOnClickListener {
            finish()
        }
        chooseButton.setOnClickListener {
            pickFolder()
        }
        settingButton.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        val imageUrls = getImagesFromFolder()
        if (!imageUrls.isNullOrEmpty()) {
            updateUI("Found ${imageUrls.size} files")
        }
    }

    override fun onResume() {
        super.onResume()
        chooseButton.requestFocus()
    }

    private fun updateUI(message: String) {
        statusText.text = message
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_FOLDER && resultCode == Activity.RESULT_OK) {
            data?.data?.let { treeUri ->
                PreferencesHelper.getInstance(this@MainActivity).saveStr(PreferencesHelper.LOCAL_FOLDER_URI,treeUri.toString())
                // 持久化这个URI
                contentResolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                EventBus.getDefault().post(RefreshPageDataEvent("RefreshData"))
                finish()
            }
        }
    }

    private fun getImagesFromFolder(): MutableList<String> {
        val images = mutableListOf<String>()
        val localUriStr = PreferencesHelper.getInstance(this).getStr(PreferencesHelper.LOCAL_FOLDER_URI)
        if(org.apache.http.util.TextUtils.isEmpty(localUriStr)){
            return images
        }
        val pickedDir = DocumentFile.fromTreeUri(this, Uri.parse(localUriStr))
        pickedDir?.listFiles()?.forEach { file ->
            if (file.isFile) {
                val name = file.name ?: ""
                if (name.lowercase().endsWith(".jpg") || name.lowercase().endsWith(".jpeg") || name.lowercase().endsWith(".png")) {
                    images.add(file.uri.toString())//file.uri
                }
            }
        }
        return images
    }

}
