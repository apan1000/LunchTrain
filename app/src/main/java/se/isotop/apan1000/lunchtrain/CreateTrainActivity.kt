package se.isotop.apan1000.lunchtrain

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_create_train.*
import se.isotop.apan1000.lunchtrain.model.Train
import java.io.Serializable

class CreateTrainActivity : AppCompatActivity(), CreateTrainFragment.CreateTrainInteractionListener {

    private val TAG = "CreateTrainActivity"
    private val TAG_CREATE_TRAIN_FRAGMENT = "CreateTrainFragment"

    private var createTrainFragment: CreateTrainFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_train)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        createTrainFragment = supportFragmentManager
                .findFragmentByTag(TAG_CREATE_TRAIN_FRAGMENT) as CreateTrainFragment?

        if (createTrainFragment == null) {
            Log.e(TAG, "CreateTrainFragment is null!")
            createTrainFragment = CreateTrainFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .add(R.id.create_train_container, createTrainFragment,
                            TAG_CREATE_TRAIN_FRAGMENT)
                    .commit()
        }
    }

    override fun onPause() {
        super.onPause()

        if (isFinishing) {
            // we will not need this fragment anymore, this may also be a good place to signal
            // to the retained fragment object to perform its own cleanup.
            supportFragmentManager.beginTransaction().remove(createTrainFragment).commit()
        }
    }

    override fun onCreateTrain(train: Train) {
        train.id = FirebaseHelper.getNewTrainKey()
        FirebaseHelper.writeNewTrain(train).addOnSuccessListener {
            train.passengerCount = 1
            val uid = FirebaseHelper.getUid()
            if(uid != null)
                train.passengers.put(uid, true)

            FirebaseHelper.joinOrLeaveTrain(train.id)

            val intent = Intent(this, TrainListActivity::class.java)
            intent.putExtra(TrainListActivity.EXTRA_TRAIN_MAP, train.toMap() as Serializable)
            this.startActivity(intent)
            finish()
        }
    }

    private fun signOut() {
        // Start LoginActivity, tell it to sign out
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("signout", true)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        android.R.id.home -> {
            navigateUpTo(Intent(this, TrainListActivity::class.java))
            true
        }
        R.id.action_settings -> {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }
        R.id.action_sign_out -> {
            signOut()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
