package com.github.ticktakclock.firebaseauth

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import butterknife.bindView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener

    private val signUpBtn: Button by bindView(R.id.maintActivityLogUpBtn)
    private val signInBtn: Button by bindView(R.id.maintActivityLogInBtn)
    private val signOutBtn: Button by bindView(R.id.maintActivityLogOutBtn)

    private val emailEt: EditText by bindView(R.id.mainActivityEmailEt)
    private val passwordEt: EditText by bindView(R.id.mainActivityPasswordEt)
    private val loginNameTv: TextView by bindView(R.id.mainActivityloginNameTv)

    private var isSignedIn
        get() = _isSignedIn
        set(value) {
            _isSignedIn = value
            updateUI()
        }
    private var _isSignedIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener() { firebaseAuth ->
            userInformation()
            val user = firebaseAuth.getCurrentUser()
            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid())
                loginNameTv.setText("signed in")
                isSignedIn = true

            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out")
                loginNameTv.setText("please sign in")
                isSignedIn = false
            }
        }
        setUpUI()
    }

    private fun setUpUI() {
        signUpBtn.setOnClickListener { v ->
            if (emailEt.text.isEmpty() || passwordEt.text.isEmpty()) {
                Toast.makeText(this, "empty text", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createAccount(emailEt.text.toString(), passwordEt.text.toString())
        }
        signInBtn.setOnClickListener { v ->
            if (emailEt.text.isEmpty() || passwordEt.text.isEmpty()) {
                Toast.makeText(this, "empty text", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            signIn(emailEt.text.toString(), passwordEt.text.toString())
        }
        signOutBtn.setOnClickListener { v ->
            signOut()
        }
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthListener)
    }

    private fun createAccount(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, OnCompleteListener<AuthResult>() {
                    task ->

                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful())

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, R.string.auth_failed,
                                Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun signIn(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, OnCompleteListener<AuthResult>() {
                    task ->
                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful())

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "signInWithEmail:failed", task.getException())
                        Toast.makeText(this, R.string.auth_failed,
                                Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun signOut() {
        if (!isSignedIn) return
        mAuth.signOut()
    }

    private fun userInformation() {
        val user = FirebaseAuth.getInstance().getCurrentUser()
        if (user != null) {
            // Name, email address, and profile photo Url
            val name = user.getDisplayName()
            val email = user.getEmail()
            val photoUrl = user.getPhotoUrl()

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            val uid = user.getUid()
        }
    }

    private fun updateUI() {
        // サインイン中はサインアウトボタンを表示
        signInBtn.visibility = if (isSignedIn) View.GONE else View.VISIBLE
        signUpBtn.visibility = if (isSignedIn) View.GONE else View.VISIBLE
        signOutBtn.visibility = if (!isSignedIn) View.GONE else View.VISIBLE
    }
}
