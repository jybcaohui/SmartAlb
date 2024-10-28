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
                val query = "mimeType='image/jpeg' or mimeType='image/png'"
                val files = withContext(Dispatchers.IO) {
                    driveService?.files()?.list()
                        ?.setPageSize(30)
                        ?.setQ(query)
                        ?.setFields("files(id, name, mimeType, modifiedTime)")
                        ?.execute()
                        ?.files ?: emptyList()
                }
                (fileRecyclerView.adapter as DriveFileAdapter).updateFiles(files)
                updateUI("Found ${files.size} files")
            } catch (e: Exception) {
                updateUI("Error listing files: ${e.message}")
                Log.e("MainActivity", "Error listing files", e)
            }
        }
    }

    private fun updateUI(message: String) {
        statusText.text = message
    }
}
