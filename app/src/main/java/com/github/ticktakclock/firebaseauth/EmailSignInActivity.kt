package com.github.ticktakclock.firebaseauth

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotterknife.bindView


class EmailSignInActivity : AppCompatActivity() {
    private val TAG = "EmailSignInActivity"

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener

    private val signUpBtn: Button by bindView(R.id.activity_firebase_email_signup_btn)
    private val signInBtn: Button by bindView(R.id.activity_firebase_email_signin_btn)
    private val signOutBtn: Button by bindView(R.id.activity_firebase_email_signout_btn)

    private val emailEt: EditText by bindView(R.id.activity_firebase_email_et)
    private val passwordEt: EditText by bindView(R.id.activity_firebase_email_password_et)
    private val loginNameTv: TextView by bindView(R.id.activity_firebase_email_name_tv)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase_email_sign_in)
        mAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener() { firebaseAuth ->
            userInformation()
            val user = firebaseAuth.getCurrentUser()
            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid())
                updateUI(mAuth.currentUser)
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out")
                updateUI(mAuth.currentUser)
            }
        }
        setUpUI()
        updateUI(mAuth.currentUser)
    }

    /**
     * setup layout behavior
     * */
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

    /**
     * update layout text or visible
     * */
    private fun updateUI(user: FirebaseUser?) {
        // サインイン中はサインアウトボタンを表示
        signInBtn.visibility = if (user != null) View.GONE else View.VISIBLE
        signUpBtn.visibility = if (user != null) View.GONE else View.VISIBLE
        signOutBtn.visibility = if (user == null) View.GONE else View.VISIBLE

        loginNameTv.setText(if (user == null) "please sign in" else "signed in")
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
}
