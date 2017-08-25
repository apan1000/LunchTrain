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
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import android.widget.Button
import android.widget.ProgressBar
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*


class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private val TAG = "LoginActivity"
    private val RC_SIGN_IN = 325

    val databaseRef = FirebaseDatabase.getInstance().reference
    lateinit private var auth: FirebaseAuth
    lateinit private var googleApiClient: GoogleApiClient

    lateinit private var loadingIndicator: ProgressBar
    lateinit private var signInButton: SignInButton
    lateinit private var signOutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        googleApiClient.connect()

        auth = FirebaseAuth.getInstance()

        setContentView(R.layout.activity_login)

        loadingIndicator = findViewById(R.id.login_loading_indicator)

        signInButton = findViewById(R.id.sign_in_button)
        signInButton.setSize(SignInButton.SIZE_WIDE)
        signInButton.setOnClickListener(this)

        signOutButton = findViewById(R.id.sign_out_button)
        signOutButton.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()

        googleApiClient.connect()
        val shouldSignOut = intent.getBooleanExtra("signout", false)
        if(shouldSignOut) {
            signOut()
        }

        if(auth.currentUser != null) {
            showSignOutButton()
            startTrainListActivity(auth.currentUser)
        } else {
            showSignInButton()
        }
    }

    override fun onStop() {
        super.onStop()
        googleApiClient.disconnect()
    }

    private fun signOut() {
        showSignInButton()
        auth.signOut()
        // Google sign out
        AuthUI.getInstance().signOut(this).addOnCompleteListener {
            // do something here
        }
    }

    private fun signIn() {
        showLoading()
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
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
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser

                    onAuthSuccess(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    showSignInButton()
                }
            }
    }

    private fun onAuthSuccess(user: FirebaseUser?) {
        checkIfNewUser(user)
    }

    private fun checkIfNewUser(user: FirebaseUser?) {
        databaseRef.child("users").child(user?.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                onUserCheckData(user, snapshot?.exists())
            }

            override fun onCancelled(error: DatabaseError?) {
                Log.e(TAG, "CheckIfNewUser error: $error")
            }
        })
    }

    private fun onUserCheckData(user: FirebaseUser?, exists: Boolean?) {
        if(exists == false)
            writeNewUser(user?.uid, user?.displayName, user?.email, user?.photoUrl.toString())

        startTrainListActivity(user)
    }

    private fun writeNewUser(userId: String?, username: String?, email: String?, photoUrl: String?) {
        val user = mutableMapOf<String, Any?>(
                "username" to username,
                "email" to email,
                "photoUrl" to photoUrl,
                "passengerAt" to "")

        databaseRef.child("users").child(userId).setValue(user)
    }

    private fun startTrainListActivity(user: FirebaseUser?) {
        showSignOutButton()
        val intent = Intent(this, TrainListActivity::class.java)
        intent.putExtra("id", user?.providerId)
        intent.putExtra("name", user?.displayName)
        startActivity(intent)
        finish()
    }

    private fun showLoading() {
        signInButton.visibility = View.INVISIBLE
        signOutButton.visibility = View.INVISIBLE

        loadingIndicator.visibility = View.VISIBLE
    }

    private fun showSignInButton() {
        loadingIndicator.visibility = View.INVISIBLE
        signOutButton.visibility = View.INVISIBLE

        signInButton.visibility = View.VISIBLE
    }

    private fun showSignOutButton() {
        signInButton.visibility = View.INVISIBLE

        signOutButton.visibility = View.VISIBLE
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
