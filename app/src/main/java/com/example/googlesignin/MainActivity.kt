package com.example.googlesignin

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.IOUtils
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    companion object {
        const val CONST_SIGN_IN = 34
        const val TAG="MainActivity"
    }

    lateinit var mDrive: Drive
    private lateinit var auth: FirebaseAuth
    private lateinit var googleAuth: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        updateUI(auth.currentUser)
        val login = findViewById<Button>(R.id.signinButton)
        login.setOnClickListener {
            GoogleSignIN()
        }
        findViewById<Button>(R.id.uploadToGDrive).setOnClickListener {
            mDrive = getDriveService(this)

            GlobalScope.async(Dispatchers.IO) {
                val intent = Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT)
                startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)

            }
        }

        // Configure Google Sign In
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("875471208877-hl9j0soaulcfueojs4maj3egubp37f91.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleAuth = GoogleSignIn.getClient(this, gso)
        val pwd=findViewById<Button>(R.id.pwd).also{
            it.setText(System.getProperty("user.dir"))
        }
        pwd.setOnClickListener {
            createTemporaryFile()
        }
    }

    private fun GoogleSignIN() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account == null) {
            val signInIntent = googleAuth.signInIntent
            startActivityForResult(signInIntent, CONST_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONST_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val accout = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(accout.idToken)
            } catch (e: ApiException) {
                Toast.makeText(this, "${e}", Toast.LENGTH_LONG).show()
            }
        }
        if (requestCode == 111 && resultCode == RESULT_OK) {
            val selectedFile = data!!.data //The uri with the location of the file
            val file = makeCopy(selectedFile!!)
            Toast.makeText(this, selectedFile.toString(), Toast.LENGTH_LONG).show()
            uploadFileToGDrive(this, file)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user == null) {
            findViewById<TextView>(R.id.loginTextView).setText("NO USER")
        } else {
            findViewById<TextView>(R.id.loginTextView).setText(user.displayName)

        }
    }

    fun getDriveService(context: Context): Drive {
        GoogleSignIn.getLastSignedInAccount(context).let { googleAccount ->
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = googleAccount!!.account!!
            return Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName(context.getString(R.string.app_name))
                .build()
        }
        var tempDrive: Drive
        return tempDrive
    }

    fun uploadFileToGDrive(context: Context, file: File) {
        mDrive.let { googleDriveService ->
            lifecycleScope.launch {
                try {


//                    val fileName = "Ticket"
                    val raunit = file
                    val gfile = com.google.api.services.drive.model.File()
                    gfile.name = "SHASHANK LATEST UPLOAD.png"
                    val mimetype = "*/*"
                    val fileContent = FileContent(mimetype, raunit)
                    var fileid = ""


                    withContext(Dispatchers.Main) {

                        withContext(Dispatchers.IO) {
                            launch {
                                var mFile =
                                     googleDriveService.Files().create(gfile, fileContent).execute().also {
                                         Log.d("Shashank", "uploadFileToGDrive: ${it.parents}")
                                     }
                            }
                        }


                    }


                } catch (userAuthEx: UserRecoverableAuthIOException) {
                    startActivity(
                        userAuthEx.intent
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
//                    Log.d("", e.toString())
                    Toast.makeText(
                        context,
                        "Some Error Occured in Uploading Files" + e.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//    }


    private fun makeCopy(fileUri: Uri): File {
        val parcelFileDescriptor =
            applicationContext.contentResolver.openFileDescriptor(fileUri, "r", null)
        val inputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)
        val file = File(
            applicationContext.filesDir,
            getFileName(applicationContext.contentResolver, fileUri)
        )
        val outputStream = FileOutputStream(file)
        IOUtils.copy(inputStream, outputStream)
        return file;
    }

    private fun getFileName(contentResolver: ContentResolver, fileUri: Uri): String {

        var name = ""
        val returnCursor = contentResolver.query(fileUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }

        return name
    }
    @Throws(IOException::class)
    private fun createTemporaryFile(){
        val prefix = "exampleFile"
        val suffix = ".txt"
        val outputDir: File = getCacheDir() // context being the Activity pointer
        val outputFile = File.createTempFile(prefix, suffix, outputDir)
        outputFile.writeText("Helloworld SHASHANK")
        Log.d(TAG, "createTemporaryFile: ${outputFile.absolutePath}")
//        uploadFileToGDrive(this, outputFile)
    }
}