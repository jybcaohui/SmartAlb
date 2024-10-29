package com.smart.album

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import com.smart.album.adapters.DriveFileAdapter
import com.smart.album.utils.PreferencesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_main)

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
                PreferencesHelper.getInstance(this@MainActivity).clearData()
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
                PreferencesHelper.getInstance(this@MainActivity).saveFileList(files)

                startActivity(Intent(this@MainActivity,FadeActivity::class.java))
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
