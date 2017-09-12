package se.isotop.apan1000.lunchtrain

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*


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

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        googleApiClient.connect()

        auth = FirebaseAuth.getInstance()

        setContentView(R.layout.activity_login)

        loadingIndicator = login_loading_indicator

        signInButton = sign_in_button
        signInButton.setSize(SignInButton.SIZE_WIDE)
        signInButton.setOnClickListener(this)

        signOutButton = sign_out_button
        signOutButton.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()

        googleApiClient.connect()
        val shouldSignOut = intent.getBooleanExtra("signout", false)
        if(shouldSignOut) {
            signOut()
            intent.putExtra("signout", false)
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
            showSnack(getString(R.string.signed_out), Snackbar.LENGTH_LONG)
        }
    }

    private fun showSnack(text: CharSequence, duration: Int) {
        Snackbar.make(login_container, text, duration).apply {
            view.apply {
                val navSize = getNavigationBarSize(context)
                Log.d(TAG, "navSize: $navSize")
                if(navSize.x > navSize.y)
                    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + navSize.y)
            }
        }.show()
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
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser

                    onAuthSuccess(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    showSnack("Authentication failed.", Snackbar.LENGTH_LONG)
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
                "passengerAt" to "",
                "driver" to false)

        databaseRef.child("users").child(userId).setValue(user)
    }

    private fun startTrainListActivity(user: FirebaseUser?) {
        showSignOutButton()
        val intent = Intent(this, TrainListActivity::class.java)
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
       showSnack(result.errorMessage.toString(), Snackbar.LENGTH_LONG)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.sign_in_button -> signIn()
            R.id.sign_out_button -> signOut()
            else -> showSnack("Nope", Snackbar.LENGTH_LONG)
        }
    }

}
