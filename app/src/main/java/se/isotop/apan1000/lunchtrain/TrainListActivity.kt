package se.isotop.apan1000.lunchtrain

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.DateTime

import se.isotop.apan1000.lunchtrain.model.Train
import se.isotop.apan1000.lunchtrain.viewholder.TrainViewHolder
import java.io.Serializable
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import org.joda.time.DateTimeZone



/**
 * An activity representing a list of Trains. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [TrainDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class TrainListActivity : AppCompatActivity() {

    // TODO: Merge MainActivity into this class
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false

    private val TAG = "TrainListActivity"

    lateinit private var databaseRef: DatabaseReference

    lateinit private var adapter: FirebaseRecyclerAdapter<Train, TrainViewHolder>
    lateinit private var recyclerView: RecyclerView

    lateinit private var loadingIndicator: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train_list)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        toolbar.title = title

        recyclerView = findViewById(R.id.train_list)
        setupRecyclerView(recyclerView)

        if (findViewById<View>(R.id.train_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton

        JodaTimeAndroid.init(this);
        val zone = DateTimeZone.forID("Europe/Stockholm")
        DateTimeZone.setDefault(zone)

        val timeStamp = DateTime.now().withTimeAtStartOfDay().toString()

//        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())

        fab.setOnClickListener { view ->
            writeNewTrain("Ett café", "Äta?",
                    timeStamp, "",
                    0)
        }

        loadingIndicator = findViewById(R.id.pb_loading_indicator)
        showLoading()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.cleanup()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        val layoutManager =  LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager

        databaseRef = FirebaseDatabase.getInstance().reference
        val trainsQuery: Query = getQuery(databaseRef)

        adapter = TrainRecyclerAdapter(Train::class.java, R.layout.train_list_item,
                TrainViewHolder::class.java, trainsQuery)
        recyclerView.adapter = adapter
    }

    private fun writeNewTrain(title: String, description: String, time: String, imgUrl: String, passengerCount: Int) : Task<Void> {
        // TODO: Fix?
        val key = databaseRef.child("trains").push().key
        val train = Train(title, description, time, imgUrl)
        val trainValues = train.toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates.put("/trains/" + key, trainValues)

        return databaseRef.updateChildren(childUpdates)
    }

    private fun showTrainsView() {
        loadingIndicator.visibility = View.INVISIBLE

        recyclerView.visibility = View.VISIBLE
    }

    private fun showLoading() {
        recyclerView.visibility = View.INVISIBLE

        loadingIndicator.visibility = View.VISIBLE
    }

    private fun getQuery(databaseReference: DatabaseReference): Query {
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        val timeStamp = DateTime.now().withTimeAtStartOfDay().toString()
        Log.e(TAG, "Timestamp: $timeStamp")
        return databaseReference.child("trains")
                .orderByChild("time")
                .startAt(timeStamp)
                .limitToFirst(100)
    }

    private fun signOut() {
        // Start LoginActivity, tell it to sign out
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("signout", true)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
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

    inner class TrainRecyclerAdapter(modelClass: Class<Train>,
                                     modelLayout: Int,
                                     viewHolderClass: Class<TrainViewHolder>,
                                     query: Query)
        : FirebaseRecyclerAdapter<Train, TrainViewHolder>(modelClass,modelLayout, viewHolderClass, query) {

        override fun populateViewHolder(viewHolder: TrainViewHolder, model: Train, position: Int) {
            val trainRef = getRef(position)

            // Set click listener for the whole train view
            val trainKey = trainRef.key
            viewHolder.itemView.setOnClickListener { v ->
                if (twoPane) {
                    val arguments = Bundle()
                    arguments.putString(TrainDetailFragment.ARG_ITEM_ID, position.toString())
                    arguments.putSerializable(TrainDetailFragment.ARG_MAP, model.toMap() as Serializable)
                    val fragment = TrainDetailFragment()
                    fragment.arguments = arguments
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.train_detail_container, fragment)
                            .commit()
                } else {
                    val context = v.context
                    val intent = Intent(context, TrainDetailActivity::class.java)
                    intent.putExtra(TrainDetailFragment.ARG_ITEM_ID, position.toString())
                    intent.putExtra(TrainDetailFragment.ARG_MAP, model.toMap() as Serializable)

                    context.startActivity(intent)
                }
            }

            // Bind Train to ViewHolder
            viewHolder.bindTrain(model, trainKey)

            showTrainsView()
        }

    }
}
