package com.github.ticktakclock.firebaseauth

import android.content.ContentValues
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import butterknife.bindView
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirebaseStorageActivity : AppCompatActivity() {
    lateinit var mStorage: FirebaseStorage
    lateinit var mAuth: FirebaseAuth

    private val RESULT_CAMERA = 0

    val imageView: ImageView by bindView(R.id.activity_firebase_storage_iv)
    val downloadBtn: Button by bindView(R.id.activity_firebase_storage_download_btn)
    val uploadBtn: Button by bindView(R.id.activity_firebase_storage_upload_btn)

    var pathUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase_storage)
        mStorage = FirebaseStorage.getInstance()
        mAuth = FirebaseAuth.getInstance()
        //downloadFile(mAuth.currentUser, mStorage.reference, "thumbnail.jpg")
        //uploadFile(mAuth.currentUser, mStorage.reference, "thumbnail.jpg")
        downloadBtn.setOnClickListener { v ->
            downloadFile(mAuth.currentUser, mStorage.reference, "thumbnail.jpg")
        }
        uploadBtn.setOnClickListener { v ->
            startCamera()
        }
    }

    fun startCamera() {
        val photoName = "photo" + ".jpg"
        val contentValue = ContentValues()
        contentValue.put(MediaStore.Images.Media.TITLE, photoName)
        contentValue.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

        pathUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValue)


        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pathUri)
        startActivityForResult(intent, RESULT_CAMERA)
    }

    fun uploadFile(user: FirebaseUser?, ref: StorageReference, fileName: String, uri: Uri) {
        user ?: return
        val uid = user.uid
        val fileRef = ref.child("user").child(user.uid).child(fileName)

        fileRef.putFile(uri).addOnCompleteListener { task ->
            val uritask = task.result?.downloadUrl
            Log.d("", "upload complete :" + uritask.toString())
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }


        ref.child("thumbnail.jpg").downloadUrl.addOnSuccessListener { uri ->
            ref.child(user.uid).child("images").putFile(uri)
                    .addOnSuccessListener { taskSnapshot ->
                        Log.d("", taskSnapshot.downloadUrl?.encodedPath)
                    }
        }

    }

    fun downloadFile(user: FirebaseUser?, ref: StorageReference, fileName: String) {
        user ?: return
        val uid = user.uid
        val fileRef = ref.child("user").child(user.uid).child(fileName)
        Glide.with(this)
                .using(FirebaseImageLoader())
                .load(fileRef)
                .into(imageView)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_CAMERA) {
            if (resultCode != RESULT_OK) return
            if (data == null) return

            val resultUri = data.data ?: pathUri

            if (resultUri == null) return

            MediaScannerConnection.scanFile(
                    this,
                    arrayOf(pathUri?.path),
                    arrayOf("image/jpeg"),
                    null
            );

            uploadFile(mAuth.currentUser, mStorage.reference, "thumbnail.jpg", resultUri)
        }
    }
}
