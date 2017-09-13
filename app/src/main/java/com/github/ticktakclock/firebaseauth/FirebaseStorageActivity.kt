package com.github.ticktakclock.firebaseauth

import android.os.Bundle
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

    val imageView: ImageView by bindView(R.id.activity_firebase_storage_iv)
    val downloadBtn: Button by bindView(R.id.activity_firebase_storage_download_btn)

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
    }

    fun uploadFile(user: FirebaseUser?, ref: StorageReference, fileName: String) {
        user ?: return
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
}
