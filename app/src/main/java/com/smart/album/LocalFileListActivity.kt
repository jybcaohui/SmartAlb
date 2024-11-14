package com.smart.album

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smart.album.adapters.LocalFileAdapter
import com.smart.album.utils.PreferencesHelper
import org.apache.http.util.TextUtils

class LocalFileListActivity : AppCompatActivity() {

    private lateinit var chooseButton: Button
    private lateinit var fileRecyclerView: RecyclerView

    private val REQUEST_CODE_PICK_FOLDER = 10001

    fun pickFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, REQUEST_CODE_PICK_FOLDER)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_file_list)

        chooseButton = findViewById(R.id.chooseButton)
        fileRecyclerView = findViewById(R.id.fileRecyclerView)

        setupRecyclerView()

        chooseButton.setOnClickListener {
            pickFolder()
        }

        val localUriStr = PreferencesHelper.getInstance(this@LocalFileListActivity).getStr(PreferencesHelper.LOCAL_FOLDER_URI)

        if(!TextUtils.isEmpty(localUriStr)){
            getImagesFromFolder(Uri.parse(localUriStr))
        }
    }

    private fun setupRecyclerView() {
        fileRecyclerView.layoutManager = LinearLayoutManager(this)
        fileRecyclerView.adapter = LocalFileAdapter(this,emptyList())
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_FOLDER && resultCode == Activity.RESULT_OK) {
            data?.data?.let { treeUri ->
                PreferencesHelper.getInstance(this@LocalFileListActivity).saveStr(PreferencesHelper.LOCAL_FOLDER_URI,treeUri.toString())
                // 持久化这个URI
                contentResolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // 获取文件夹下的所有图片
                getImagesFromFolder(treeUri)
            }
        }
    }

    private fun getImagesFromFolder(treeUri: Uri) {
        val pickedDir = DocumentFile.fromTreeUri(this, treeUri)
        val images = mutableListOf<DocumentFile>()
        pickedDir?.listFiles()?.forEach { file ->
            if (file.isFile) {
                val name = file.name ?: ""
                if (name.lowercase().endsWith(".jpg") || name.lowercase().endsWith(".jpeg") || name.lowercase().endsWith(".png")) {
                    images.add(file)//file.uri
                }
            }
        }

        // 更新UI
        updateUIWithImages(images)
    }

    private fun updateUIWithImages(images: List<DocumentFile>) {
        // 这里更新你的RecyclerView
        (fileRecyclerView.adapter as LocalFileAdapter).updateFiles(images)
    }
}
