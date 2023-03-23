//package com.example.googlesignin
//
//import android.content.ContentResolver
//import android.content.Context
//import android.net.Uri
//import android.provider.OpenableColumns
//import android.provider.Settings.Global.getString
//import android.util.Log
//import android.widget.Toast
//import androidx.core.content.ContextCompat.startActivity
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.api.client.extensions.android.http.AndroidHttp
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
//import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
//import com.google.api.client.http.FileContent
//import com.google.api.client.json.jackson2.JacksonFactory
//import com.google.api.client.util.IOUtils
//import com.google.api.services.drive.Drive
//import com.google.api.services.drive.DriveScopes
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.File
//import java.io.FileInputStream
//import java.io.FileOutputStream
//
//class DriveFileUpload(val context:Context) {
//    lateinit var mDrive : Drive
//    init {
//
//    }
//    fun getDriveService(context: Context): Drive {
//        GoogleSignIn.getLastSignedInAccount(context).let { googleAccount ->
//            val credential = GoogleAccountCredential.usingOAuth2(
//                context, listOf(DriveScopes.DRIVE_FILE)
//            )
//            credential.selectedAccount = googleAccount!!.account!!
//            return Drive.Builder(
//                AndroidHttp.newCompatibleTransport(),
//                JacksonFactory.getDefaultInstance(),
//                credential
//            )
//                .setApplicationName(context.getString(R.string.app_name))
//                .build()
//        }
//        var tempDrive: Drive
//        return tempDrive
//    }
//
//    fun uploadFileToGDrive(context: Context) {
//        mDrive.let { googleDriveService ->
//            lifecycleScope.launch {
//                try {
//
//
////                    val fileName = "Ticket"
//                    val raunit = File("storage/emulated/0/Download", "download.png")
//                    val gfile = com.google.api.services.drive.model.File()
//                    gfile.name = "Subscribe"
//                    val mimetype = "image/png"
//                    val fileContent = FileContent(mimetype, raunit)
//                    var fileid = ""
//
//
//                    withContext(Dispatchers.Main) {
//
//                        withContext(Dispatchers.IO) {
//                            launch {
//                                var mFile =
//                                    googleDriveService.Files().create(gfile, fileContent).execute()
//
//                            }
//                        }
//
//
//                    }
//
//
//                } catch (userAuthEx: UserRecoverableAuthIOException) {
//                    startActivity(
//                        userAuthEx.intent
//                    )
//                } catch (e: Exception) {
//                    e.printStackTrace()
////                    Log.d("", e.toString())
//                    Toast.makeText(
//                        context,
//                        "Some Error Occured in Uploading Files" + e.toString(),
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//        }
//
//    }
//
//
////    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
////        super.onActivityResult(requestCode, resultCode, data)
////        if (requestCode == 111 && resultCode == RESULT_OK) {
////            val selectedFile = data!!.data //The uri with the location of the file
////            makeCopy(selectedFile!!)
////            Toast.makeText(this,selectedFile.toString(),Toast.LENGTH_LONG).show()
////        }
////    }
//
//    private fun makeCopy(fileUri: Uri) {
//        val parcelFileDescriptor = context.applicationContext.contentResolver.openFileDescriptor(fileUri, "r", null)
//        val inputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)
//        val file = File(context.applicationContext.filesDir, getFileName(context.applicationContext.contentResolver, fileUri))
//        val outputStream = FileOutputStream(file)
//        IOUtils.copy(inputStream, outputStream)
//
//    }
//
//    private fun getFileName(contentResolver: ContentResolver, fileUri: Uri): String {
//
//        var name = ""
//        val returnCursor = contentResolver.query(fileUri, null, null, null, null)
//        if (returnCursor != null) {
//            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//            returnCursor.moveToFirst()
//            name = returnCursor.getString(nameIndex)
//            returnCursor.close()
//        }
//
//        return name
//    }
//}