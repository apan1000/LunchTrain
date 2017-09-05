package se.isotop.apan1000.lunchtrain

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import se.isotop.apan1000.lunchtrain.fragments.TrainDetailFragment
import se.isotop.apan1000.lunchtrain.fragments.TrainListFragment
import se.isotop.apan1000.lunchtrain.model.Train
import java.io.Serializable


/**
 * An activity representing a list of Trains. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [TrainDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class TrainListActivity : AppCompatActivity(), TrainListFragment.OnTrainInteractionListener,
        CreateTrainFragment.OnCreateTrainInteractionListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false

    private val TAG = "TrainListActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = title

        if (findViewById<View>(R.id.train_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        if (savedInstanceState == null) {
            val fragment = TrainListFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .add(R.id.train_list_container, fragment)
                    .commit()
        }

        // Setup timestamp
        JodaTimeAndroid.init(this);
        val zone = DateTimeZone.forID("Europe/Stockholm")
        DateTimeZone.setDefault(zone)

        val timeStamp = DateTime.now()
        timeStamp.withHourOfDay(2)

        val fab = findViewById<FloatingActionButton>(R.id.add_train_fab)
        fab.setOnClickListener { view ->
            startCreateTrain()
        }
    }

    override fun onTrainSelected(view: View, model: Train, position: Int) {
        if (twoPane) {
            val fragment = TrainDetailFragment
                    .newInstance(position.toString(), model.toMap() as Serializable)
            supportFragmentManager.beginTransaction()
                    .replace(R.id.train_detail_container, fragment)
                    .commit()
        } else {
            val context = view.context
            val intent = Intent(context, TrainDetailActivity::class.java)
            intent.putExtra(TrainDetailFragment.ARG_ITEM_ID, position.toString())
            intent.putExtra(TrainDetailFragment.ARG_MAP, model.toMap() as Serializable)

            context.startActivity(intent)
        }
    }

    private fun startCreateTrain() {
        if (twoPane) {
            val fragment = CreateTrainFragment
                    .newInstance()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.train_detail_container, fragment)
                    .commit()
        } else {
            val context = baseContext
            val intent = Intent(context, CreateTrainActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreateTrain(train: Train) {
        FirebaseManager.writeNewTrain(train)
    }

    private fun signOut() {
        // Start LoginActivity, tell it to sign out
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("signout", true)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        R.id.action_settings -> consume { navigateToSettings() }
        R.id.action_sign_out -> consume { signOut() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun navigateToSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }
}
