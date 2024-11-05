package com.smart.album

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.smart.album.adapters.DriveFileAdapter
import com.smart.album.utils.PreferencesHelper
import com.smart.album.views.ZoomableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

class DriveFileListActivity : AppCompatActivity() {

    private lateinit var imgZoom: ImageView
    private lateinit var signInButton: Button
    private lateinit var signOutButton: Button
    private lateinit var chooseButton: Button
    private lateinit var fileRecyclerView: RecyclerView
    private lateinit var statusText: TextView
    private lateinit var googleSignInClient: GoogleSignInClient
    private var driveService: Drive? = null

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
        setContentView(R.layout.activity_drive_file_list)

        imgZoom = findViewById(R.id.img_zoom)

        var imageUrl = "https://www.kuhw.com/d/file/p/2021/10-22/0d9525784ee4e7a74746eae20258bb79.jpg"
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // 设置图片到 ImageView
                    imgZoom.setImageBitmap(resource)
//                    imgZoom.setScreenSize(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
//                    imgZoom.setIntrinsicSize(resource.width, resource.height)
//                    // 开始动画
//                    imgZoom.startZoomAnimation()

                    // 计算图片初始尺寸
                    val imageWidth = resource.width
                    val imageHeight = resource.height
                    val minDimension = min(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
                    val maxDimension = max(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)

                    // 计算初始缩放比例
                    val initialScale = if (imageWidth > imageHeight) {
                        minDimension.toFloat() / imageHeight
                    } else {
                        minDimension.toFloat() / imageWidth
                    }
                    imgZoom.scaleX = initialScale
                    imgZoom.scaleY = initialScale

                    // 创建动画
                    val animatorSet = AnimatorSet()

                    val finalScale = if (imageWidth > imageHeight) {
                        maxDimension.toFloat() / imageWidth
                    } else {
                        maxDimension.toFloat() / imageHeight
                    }

                    Log.d("scale","scaleX==$initialScale $finalScale")
                    val scaleXAnimator = ObjectAnimator.ofFloat(imgZoom, "scaleX", finalScale, initialScale)
                    val scaleYAnimator = ObjectAnimator.ofFloat(imgZoom, "scaleY", finalScale, initialScale)

                    // 将动画添加到AnimatorSet中
                    animatorSet.playTogether(scaleXAnimator, scaleYAnimator)
                    animatorSet.duration = 2000  // 动画持续时间2秒

                    // 开始动画
                    animatorSet.start()

                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // 图片加载失败时的处理
                }
            })


        signInButton = findViewById(R.id.signInButton)
        signOutButton = findViewById(R.id.signOutButton)
        chooseButton = findViewById(R.id.chooseButton)
        fileRecyclerView = findViewById(R.id.fileRecyclerView)
        statusText = findViewById(R.id.statusText)

        setupGoogleSignIn()
        setupRecyclerView()

        signInButton.setOnClickListener {
            signIn()
        }
        signOutButton.setOnClickListener {
            signOut()
        }
        chooseButton.setOnClickListener {
            listFolders()
        }

        // 加载列表
        val spFileList = PreferencesHelper.getInstance(this).loadFileList()
        if(!spFileList.isNullOrEmpty()){
            Log.d("albs===","files==="+spFileList.size)
            (fileRecyclerView.adapter as DriveFileAdapter).updateFiles(spFileList)
            updateUI("Found ${spFileList.size} files")
        }

        // Check if already signed in
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            updateUI("Already signed in")
            setupDriveService()
            listFiles()
            signInButton.visibility = View.GONE
            signOutButton.visibility = View.VISIBLE
            chooseButton.visibility = View.VISIBLE
        } else {
            signInButton.visibility = View.VISIBLE
            signOutButton.visibility = View.GONE
            chooseButton.visibility = View.GONE
        }
    }

    private fun setupGoogleSignIn() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_READONLY))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, signInOptions)
    }

    private fun setupRecyclerView() {
        fileRecyclerView.layoutManager = LinearLayoutManager(this)
        fileRecyclerView.adapter = DriveFileAdapter(this,emptyList())
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun signOut(){
        googleSignInClient.signOut()
            .addOnCompleteListener(this) {
                // 注销完成
                updateUI("Already signed out")
                PreferencesHelper.getInstance(this@DriveFileListActivity).clearData()
                signInButton.visibility = View.VISIBLE
                signOutButton.visibility = View.GONE
            }
    }

    private fun handleSignInResult(data: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnSuccessListener { account ->
                updateUI("Signed in as ${account.email}")
                setupDriveService()
                listFiles()
                signInButton.visibility = View.GONE
                signOutButton.visibility = View.VISIBLE
                chooseButton.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                updateUI("Sign in failed: ${e.message}")
                Log.e("MainActivity", "Sign in failed", e)
            }
    }

    private fun setupDriveService() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
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

    private fun listFiles() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val query = "mimeType='image/jpeg' or mimeType='image/png'"
                val files = withContext(Dispatchers.IO) {
                    driveService?.files()?.list()
                        ?.setPageSize(30)
                        ?.setQ(query)
                        ?.setFields("files(id, name, mimeType)")
//                        ?.setFields("files(id, name, mimeType, modifiedTime)")
                        ?.execute()
                        ?.files ?: emptyList()
                }
                (fileRecyclerView.adapter as DriveFileAdapter).updateFiles(files)
                updateUI("Found ${files.size} files")
                // 保存列表
                PreferencesHelper.getInstance(this@DriveFileListActivity).saveFileList(files)

//                startActivity(Intent(this@MainActivity,FadeActivity::class.java))
            } catch (e: Exception) {
                updateUI("Error listing files: ${e.message}")
                Log.e("albs===", "Error listing files", e)
            }
        }
    }

    private fun updateUI(message: String) {
        statusText.text = message
    }

    //文件夹列表
    private fun listFolders() {
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
                Log.d("albs===","folders==size=="+folders.size)
                folders.forEach { folder ->
                    Log.d("albs===","folders==Found folder: ${folder.name} with ID: ${folder.id}")
                    // 这里可以显示文件夹列表供用户选择
                    listImagesInFolder(folder.id)
                }
            }catch (e:Exception){
                Log.d("albs===","folders=="+e.message)
            }
        }

    }

    //读取指定文件夹下图片列表
    private fun listImagesInFolder(folderId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val query = "mimeType='image/jpeg' or mimeType='image/png' and '$folderId' in parents"
                val files = withContext(Dispatchers.IO) {
                    driveService?.files()?.list()
                        ?.setPageSize(30)
                        ?.setQ(query)
                        ?.setFields("files(id, name, mimeType)")
                        ?.execute()
                        ?.files ?: emptyList()
                }
                files.forEach{ file ->
                    Log.d("albs===","${folderId} folders==Found file: ${file.name}")
                }

//                (fileRecyclerView.adapter as DriveFileAdapter).updateFiles(files)
//                updateUI("Found ${files.size} files")
//                // 保存列表
//                PreferencesHelper.getInstance(this@MainActivity).saveFileList(files)
            } catch (e: Exception) {
                updateUI("Error listing files: ${e.message}")
                Log.e("MainActivity", "Error listing files", e)
            }
        }
    }
}
