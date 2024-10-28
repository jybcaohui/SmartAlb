package com.smart.album

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.Permission
import com.smart.album.adapters.DriveFileAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var signInButton: Button
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
        fileRecyclerView = findViewById(R.id.fileRecyclerView)
        statusText = findViewById(R.id.statusText)

        setupGoogleSignIn()
        setupRecyclerView()

        signInButton.setOnClickListener {
            signIn()
        }

        // Check if already signed in
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            updateUI("Already signed in")
            setupDriveService()
            listFiles()
        }
    }

    private fun setupGoogleSignIn() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
//            .requestScopes(Scope(DriveScopes.DRIVE_READONLY))
            .requestScopes(Scope(DriveScopes.DRIVE_METADATA))
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

    private fun handleSignInResult(data: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnSuccessListener { account ->
                updateUI("Signed in as ${account.email}")
                setupDriveService()
                listFiles()
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
                val files = withContext(Dispatchers.IO) {
                    driveService?.files()?.list()
                        ?.setPageSize(30)
                        ?.setFields("files(id, name, mimeType, modifiedTime)")
                        ?.execute()
                        ?.files ?: emptyList()
                }

                val filteredFiles = files.filter { file ->
                    file.mimeType == "image/jpeg" || file.mimeType == "image/png"
                }

                if(filteredFiles.isNotEmpty()){
                    withContext(Dispatchers.IO) {
                        filteredFiles?.forEach { file ->
                            setFilePublic(driveService!!, file.id)
                            val fileWithWebContentLink = getFileWithWebContentLink(driveService!!, file.id)
                            if (fileWithWebContentLink != null && fileWithWebContentLink.webContentLink != null) {
                                Log.i("Drive", "Found image: ${file.name} (${file.id}) - URL: ${fileWithWebContentLink.webContentLink}")
                                // 你可以在这里处理获取到的链接
                            } else {
                                Log.w("Drive", "Failed to get web content link for file: ${file.name} (${file.id})")
                            }
                        }
                    }
                }



                (fileRecyclerView.adapter as DriveFileAdapter).updateFiles(filteredFiles)
                updateUI("Found ${files.size} files")
            } catch (e: Exception) {
                updateUI("Error listing files: ${e.message}")
                Log.e("MainActivity", "Error listing files", e)
            }
        }
    }

    private fun setFilePublic(driveService: Drive, fileId: String) {
        val permission = Permission()
            .setType("anyone")
            .setRole("reader") // 可以设置为 "writer" 或 "commenter"
            .setAllowFileDiscovery(false) // 如果不需要其他人通过搜索找到该文件，可以设置为 false

        driveService.permissions().create(fileId, permission)
            .setFields("id")
            .execute()
    }

    private fun getFileWithWebContentLink(driveService: Drive, fileId: String): File? {
        return try {
            val file = driveService.files().get(fileId)
                .setFields("id, name, webContentLink")
                .execute()
            file
        } catch (e: Exception) {
            Log.e("Drive", "Error getting file: ${e.message}")
            null
        }
    }

    private fun updateUI(message: String) {
        statusText.text = message
    }
}
