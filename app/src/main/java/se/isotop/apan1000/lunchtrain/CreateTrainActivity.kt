package se.isotop.apan1000.lunchtrain

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_create_train.*
import se.isotop.apan1000.lunchtrain.model.Train
import java.io.Serializable

class CreateTrainActivity : AppCompatActivity(), CreateTrainFragment.OnCreateTrainInteractionListener {

    private val TAG = "CreateTrainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_train)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val fragment = CreateTrainFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .add(R.id.create_train_container, fragment)
                    .commit()
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
