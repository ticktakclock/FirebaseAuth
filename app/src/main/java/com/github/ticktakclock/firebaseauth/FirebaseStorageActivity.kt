package com.github.ticktakclock.firebaseauth

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotterknife.bindView
import java.io.ByteArrayOutputStream

class FirebaseStorageActivity : AppCompatActivity() {
    lateinit var mStorage: FirebaseStorage
    lateinit var mAuth: FirebaseAuth

    private val RESULT_CAMERA = 0
    private val FILE_NAME = "thumbnail.jpg"

    val imageView: ImageView by bindView(R.id.activity_firebase_storage_iv)
    val downloadBtn: Button by bindView(R.id.activity_firebase_storage_download_btn)
    val uploadBtn: Button by bindView(R.id.activity_firebase_storage_upload_btn)

    var progressDialog: ProgressDialog? = null

    var pathUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase_storage)
        mStorage = FirebaseStorage.getInstance()
        mAuth = FirebaseAuth.getInstance()

        downloadBtn.setOnClickListener { v ->
            downloadFile(mAuth.currentUser, mStorage.reference, FILE_NAME)
        }
        uploadBtn.setOnClickListener { v ->
            startCamera()
        }
    }

    fun startCamera() {
        val photoName = "" + System.currentTimeMillis() + ".jpg"
        val contentValue = ContentValues()
        contentValue.put(MediaStore.Images.Media.TITLE, photoName)
        contentValue.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

        pathUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValue)


        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pathUri)
        startActivityForResult(intent, RESULT_CAMERA)
    }

    /**
     * 正方形にトリミングしつつ、中央寄せする
     * */
    fun resizeAndConvertBitmap(uri: Uri): Bitmap? {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        bitmap ?: return null
        val width = bitmap.width
        val height = bitmap.height
        val scale = Math.max(200.0f / width, 200.0f / height)
        val size = Math.min(width, height)
        val matrix = Matrix()
        matrix.postScale(scale, scale)

        val bmp2 = Bitmap.createBitmap(bitmap, (width - size) / 2, (height - size) / 2, size, size, matrix, true)

        return bmp2
    }

    fun uploadFile(user: FirebaseUser?, ref: StorageReference, fileName: String, bitmap: Bitmap) {
        user ?: return

        val fileRef = ref.child("user").child(user.uid).child(fileName)

        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
        bos.flush()
        val bArray = bos.toByteArray()
        bos.close()

        showProgress()
        fileRef.putBytes(bArray).addOnCompleteListener { task ->
            hideProgress()
            val uritask = task.result?.downloadUrl
            Log.d("", "upload complete :" + uritask.toString())
        }.addOnFailureListener { exception ->
            hideProgress()
            exception.printStackTrace()
        }
    }

    fun uploadFile(user: FirebaseUser?, ref: StorageReference, fileName: String, uri: Uri) {
        user ?: return
        val bitmap = resizeAndConvertBitmap(uri)
        bitmap ?: return
        uploadFile(user, ref, fileName, bitmap)
    }

    fun downloadFile(user: FirebaseUser?, ref: StorageReference, fileName: String) {
        user ?: return
        val fileRef = ref.child("user").child(user.uid).child(fileName)

        showProgress()
        fileRef.getStream { taskSnapshot, inputStream ->
            hideProgress()
            val bmp = BitmapFactory.decodeStream(inputStream)
            runOnUiThread {
                imageView.setImageBitmap(bmp)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_CAMERA) {
            if (resultCode != RESULT_OK) return
            if (data == null) return

            val resultUri = data.data ?: pathUri

            resultUri ?: return

            MediaScannerConnection.scanFile(
                    this,
                    arrayOf(pathUri?.path),
                    arrayOf("image/jpeg"),
                    null
            );

            uploadFile(mAuth.currentUser, mStorage.reference, FILE_NAME, resultUri)
        }
    }

    fun showProgress() {
        runOnUiThread {
            progressDialog = ProgressDialog.show(this, "つうしんちゅう", "・・・")
            progressDialog?.show()
        }

    }

    fun hideProgress() {
        runOnUiThread {
            progressDialog?.hide()
        }
    }
}
