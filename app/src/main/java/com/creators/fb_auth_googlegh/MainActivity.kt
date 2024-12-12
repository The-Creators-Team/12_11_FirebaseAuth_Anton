package com.creators.fb_auth_googlegh

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.creators.fb_auth_googlegh.databinding.ActivityMainBinding
import com.creators.fb_auth_googlegh.ui.home.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.messaging.FirebaseMessaging

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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Pass the Google ID token to Firebase
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Handle error
                showToast("Google Sign-In failed: ${e.message}")
            }
        }

        googleSignInButton.setOnClickListener {
//            val provider = OAuthProvider.newBuilder("google.com")
//            auth.startActivityForSignInWithProvider(this, provider.build())
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        navigateToHome()
//                    } else {
//                        showToast("Google Sign-In failed: ${task.exception?.message}")
//                    }
//                }
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
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

        askNotificationPermission()



        FirebaseMessaging.getInstance().token.addOnCompleteListener{
            task ->
                if(!task.isSuccessful) {
                    Log.e("NotificationToken", task.exception.toString())
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d("NotificationToken", "Firebase token -> $token")
        }

        binding.registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // User registration successful
                        val user = auth.currentUser
                        // You can now do something with the user
                        // For example, navigate to another screen
                        navigateToHome()
                    } else {
                        // Registration failed
                        val exception = task.exception
                        showToast("Registration failed: ${exception?.message}")
                    }
                }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToHome()
                } else {
                    showToast("Firebase Auth failed: ${task.exception?.message}")
                }
            }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
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

    /*
    Token
    e2y0jKRtTUeSaoZ9D4n56U:APA91bGxj9DjFmLZbLh7vPWgYZ9z0p0Wp-4XBIBWTdxael8GCnjCNSXl2FSZJeh6dHXckrgA7T3WU_tbhVax8vjmMmU4PfGi8Bs1J8MxRXYEy_KIABvA-rw
     */
}
