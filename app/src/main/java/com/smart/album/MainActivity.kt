package com.smart.album

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.smart.album.adapters.FolderAdapter
import com.smart.album.utils.MessageEvent
import com.smart.album.utils.PreferencesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

class MainActivity : BaseActivity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var noSignLayout: LinearLayout
    private lateinit var signedLayout: LinearLayout
    private lateinit var signInButton: LinearLayout
    private lateinit var signOutButton: LinearLayout
    private lateinit var chooseButton: LinearLayout
    private lateinit var settingButton: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var googleSignInClient: GoogleSignInClient
    private var driveService: Drive? = null
    private var spFolderId : String = ""

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleSignInResult(result.data)
        } else {
            updateUI("Sign in failed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootLayout = findViewById(R.id.root)
        noSignLayout = findViewById(R.id.login_no)
        signedLayout = findViewById(R.id.login_ed)


        signInButton = findViewById(R.id.signInButton)
        signInButton = findViewById(R.id.signInButton)
        signOutButton = findViewById(R.id.signOutButton)
        chooseButton = findViewById(R.id.chooseButton)
        settingButton = findViewById(R.id.settingButton)
        statusText = findViewById(R.id.statusText)

        setupGoogleSignIn()

        rootLayout.setOnClickListener {
            finish()
        }
        signInButton.setOnClickListener {
            signIn()
        }
        signOutButton.setOnClickListener {
            signOut()
        }
        chooseButton.setOnClickListener {
//            listFolders()
            startActivity(Intent(this, DriveFileListActivity::class.java))
        }
        settingButton.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }


        // Check if already signed in
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            updateUI("Already signed in")
            noSignLayout.visibility = View.GONE
            signedLayout.visibility = View.VISIBLE
        } else {
            noSignLayout.visibility = View.VISIBLE
            signedLayout.visibility = View.GONE
        }
    }

    private fun updateUI(message: String) {
        statusText.text = message
    }

    //初始化GoogleSignInClient
    private fun setupGoogleSignIn() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_READONLY))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, signInOptions)
    }

    //登出
    private fun signOut(){
        showLoading()
        googleSignInClient.signOut()
            .addOnCompleteListener(this) {
                // 注销完成
                updateUI("Already signed out")
                PreferencesHelper.getInstance(this@MainActivity).clearData()
                noSignLayout.visibility = View.VISIBLE
                signedLayout.visibility = View.GONE
                hideLoading()
            }
    }

    //登录
    private fun signIn() {
        showLoading()
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    //登录成功
    private fun handleSignInResult(data: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnSuccessListener { account ->
                updateUI("Signed in as ${account.email}")
                listFiles(true)
                noSignLayout.visibility = View.GONE
                signedLayout.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                updateUI("Sign in failed: ${e.message}")
                hideLoading()
            }
    }

    //初始化DriveService，以读取文件列表
    private fun setupDriveService() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if(account != null){
            val credential = GoogleAccountCredential.usingOAuth2(
                this, listOf(DriveScopes.DRIVE_READONLY)
            )
            credential.selectedAccount = account.account
            driveService = Drive.Builder(
                NetHttpTransport(),  // 使用 NetHttpTransport 替代 AndroidHttp
                GsonFactory(),
                credential
            )
                .setApplicationName("Smart Album")
                .build()
        }
    }

    //文件列表
    private fun listFiles(fromLogin:Boolean) {
        setupDriveService()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                var query = "mimeType='image/jpeg' or mimeType='image/png'"
                val folderId = PreferencesHelper.getInstance(this@MainActivity).getStr(PreferencesHelper.DRIVE_FOLDER_ID)
                if(!TextUtils.isEmpty(folderId)){
                    query = "mimeType='image/jpeg' or mimeType='image/png' and '$folderId' in parents and trashed=false"//指定文件夹下图片列表
                }
                Log.d("albs===","query="+query)
                val files = withContext(Dispatchers.IO) {
                    driveService?.files()?.list()
                        ?.setPageSize(30)
                        ?.setQ(query)
                        ?.setFields("files(id, name, mimeType)")
//                        ?.setFields("files(id, name, mimeType, modifiedTime)")
                        ?.execute()
                        ?.files ?: emptyList()
                }
                files.forEach { file ->
                    Log.d("albs===","files==Found file: ${file.name}")
                }
                updateUI("Found ${files.size} files")
                // 保存列表
                PreferencesHelper.getInstance(this@MainActivity).saveFileList(files)
                hideLoading()
                val event = MessageEvent("RefreshData")
                EventBus.getDefault().post(event)
            } catch (e: Exception) {
                updateUI("Error listing files: ${e.message}")
                hideLoading()
            }
        }
    }


    //文件夹列表
    private fun listFolders() {
        showLoading()
        setupDriveService()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val query = "mimeType='application/vnd.google-apps.folder'"
                val folders = withContext(Dispatchers.IO) {
                    driveService?.files()?.list()
                        ?.setPageSize(30)
                        ?.setQ(query)
                        ?.execute()
                        ?.files ?: emptyList()
                }
                folders.forEach { folder ->
                    Log.d("albs===","folders==Found folder: ${folder.name} with ID: ${folder.id}")
                }
                // 这里可以显示文件夹列表供用户选择
                showFolderPop(folders.toMutableList())
                hideLoading()
            } catch (e:Exception){
                updateUI("Error listing files: ${e.message}")
                hideLoading()
            }
        }

    }

    //文件夹选择
    private fun showFolderPop(files: MutableList<File>) {
        files.add(0, File().apply {
            name = "All"
            id = ""
        })
        spFolderId = PreferencesHelper.getInstance(this@MainActivity).getStr(PreferencesHelper.DRIVE_FOLDER_ID)
            .toString()
        val popupView = layoutInflater.inflate(R.layout.bottom_pop, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )
        popupView.findViewById<LinearLayout>(R.id.lv_root).setOnClickListener { popupWindow.dismiss() }
        val listView = popupView.findViewById<ListView>(R.id.listView)
        val tvTitle = popupView.findViewById<TextView>(R.id.tv_title)
        tvTitle.visibility = View.VISIBLE
        val tvDone = popupView.findViewById<TextView>(R.id.tv_done)
        val adapter = FolderAdapter(this, files, spFolderId)

        adapter.onItemSelectedListener = object : FolderAdapter.OnItemSelectedListener {
            override fun onItemSelected(item: String, position: Int) {
                spFolderId = item
            }
        }
        listView.adapter = adapter
        tvDone.setOnClickListener{
            PreferencesHelper.getInstance(this@MainActivity).saveStr(PreferencesHelper.DRIVE_FOLDER_ID,spFolderId)
            popupWindow.dismiss()
            listFiles(false)
        }
        popupWindow.animationStyle = R.style.PopupAnimation
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0)
    }
}
