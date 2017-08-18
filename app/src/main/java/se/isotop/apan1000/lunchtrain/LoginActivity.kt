package se.isotop.apan1000.lunchtrain

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import android.widget.Button
import android.widget.ProgressBar
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthCredential


class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private val TAG = "LoginActivity"
    private val RC_SIGN_IN = 325

    lateinit private var mDatabase: DatabaseReference
    lateinit private var mAuth: FirebaseAuth
    lateinit private var mGoogleApiClient: GoogleApiClient

    lateinit private var mLoadingIndicator: ProgressBar
    lateinit private var mSignInButton: SignInButton
    lateinit private var mSignOutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance()

        setContentView(R.layout.activity_login)

        mLoadingIndicator = findViewById(R.id.login_loading_indicator)

        mSignInButton = findViewById(R.id.sign_in_button)
        mSignInButton.setSize(SignInButton.SIZE_WIDE)
        mSignInButton.setOnClickListener(this)

        mSignOutButton = findViewById(R.id.sign_out_button)
        mSignOutButton.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()

        if(mAuth.currentUser != null) {
            showSignOutButton()
            startMainActivity(mAuth.currentUser)
        } else {
            showSignInButton()
        }
    }

   /* private fun onAuthSuccess(user: FirebaseUser) {
        val username = usernameFromEmail(user.email)

        // Write new user
        writeNewUser(user.uid, username, user.email)

        // Go to MainActivity
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        finish()
    }

    private fun writeNewUser(userId: String, name: String, email: String) {
        val user = User(name, email)

        mDatabase.child("users").child(userId).setValue(user)
    }*/

    private fun signOut() {
        showSignInButton()
        mAuth.signOut()
    }

    private fun signIn() {
        showLoading()
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if(requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleSignInResult(result)
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult) {
        Log.d(TAG, "handleSignInResult: ${result.isSuccess}")
        if(result.isSuccess) {
            // Signed in successfully, show authenticated UI.
            val account: GoogleSignInAccount? = result.signInAccount
            firebaseAuthWithGoogle(account)
        } else {
            Toast.makeText(this, R.string.signed_in_err, Toast.LENGTH_LONG)
                    .show()
            showSignInButton()
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        Log.d(TAG, "\nfirebaseAuthWithGoogle: ${acct?.id}")

        Log.d(TAG, "Google JWT : ${acct?.idToken}\n")

        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = mAuth.currentUser
                    startMainActivity(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    showSignInButton()
                }
            }
    }

    private fun startMainActivity(user: FirebaseUser?) {
        showSignOutButton()
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("id", user?.providerId)
        intent.putExtra("name", user?.displayName)
        startActivity(intent)
    }

    private fun showLoading() {
        mSignInButton.visibility = View.INVISIBLE
        mSignOutButton.visibility = View.INVISIBLE

        mLoadingIndicator.visibility = View.VISIBLE
    }

    private fun showSignInButton() {
        mLoadingIndicator.visibility = View.INVISIBLE
        mSignOutButton.visibility = View.INVISIBLE

        mSignInButton.visibility = View.VISIBLE
    }

    private fun showSignOutButton() {
        mSignInButton.visibility = View.INVISIBLE

        mSignOutButton.visibility = View.VISIBLE
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.sign_in_button -> signIn()
            R.id.sign_out_button -> signOut()
            else -> {
                Snackbar.make(view as View, "Nope", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
            }
        }
    }

}
