package com.creators.fb_auth_googlegh

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.creators.fb_auth_googlegh.databinding.ActivityMainBinding
import com.creators.fb_auth_googlegh.ui.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider

//AuthApp on Firebase
class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        enableEdgeToEdge()
        setContentView(view)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val emailSignInButton = findViewById<Button>(R.id.emailSignInButton)
        val googleSignInButton = findViewById<Button>(R.id.googleSignInButton)
        val githubSignInButton = findViewById<Button>(R.id.githubSignInButton)

        emailSignInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            navigateToHome()
                        } else {
                            showToast("Authentication failed: ${task.exception?.message}")
                        }
                    }
            } else {
                showToast("Please fill in all fields.")
            }
        }

        googleSignInButton.setOnClickListener {
        }

        // GitHub Sign-In
        githubSignInButton.setOnClickListener {
            val provider = OAuthProvider.newBuilder("github.com")
            auth.startActivityForSignInWithProvider(this, provider.build())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        navigateToHome()
                    } else {
                        showToast("GitHub Sign-In failed: ${task.exception?.message}")
                    }
                }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
