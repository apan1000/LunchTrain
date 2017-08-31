package se.isotop.apan1000.lunchtrain

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_create_train.*

class CreateTrainActivity : AppCompatActivity(), CreateTrainFragment.OnCreateTrainInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_train)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            val fragment = CreateTrainFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .add(R.id.create_train_container, fragment)
                    .commit()
        }
    }

    override fun onCreateTrain(uri: Uri) {
        // TODO: Do something
    }
}
