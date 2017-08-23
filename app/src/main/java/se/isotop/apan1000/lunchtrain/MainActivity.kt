package se.isotop.apan1000.lunchtrain

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import android.widget.ProgressBar
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import se.isotop.apan1000.lunchtrain.model.Train
import se.isotop.apan1000.lunchtrain.viewholder.TrainViewHolder
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    lateinit private var databaseRef: DatabaseReference

    lateinit private var adapter: FirebaseRecyclerAdapter<Train, TrainViewHolder>
    lateinit private var recycler: RecyclerView

    lateinit private var loadingIndicator: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Do something with the intent
        val userName = intent.getStringExtra("name")
        Toast.makeText(this, "Started by $userName", Toast.LENGTH_SHORT).show()
        //

        recycler = findViewById(R.id.recyclerview_trains)
        recycler.setHasFixedSize(true)

        val layoutManager =  LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.layoutManager = layoutManager

        databaseRef = FirebaseDatabase.getInstance().reference
        val trainsQuery: Query = getQuery(databaseRef)

        adapter = object : FirebaseRecyclerAdapter<Train, TrainViewHolder>(Train::class.java, R.layout.train_list_item,
                TrainViewHolder::class.java, trainsQuery) {

            override fun populateViewHolder(viewHolder: TrainViewHolder, model: Train, position: Int) {
                val trainRef = getRef(position)

                // Set click listener for the whole train view
                val trainKey = trainRef.key
                viewHolder.itemView.setOnClickListener {
                    val intent = Intent(baseContext, TrainDetailActivity::class.java)
                    intent.putExtra(TrainDetailActivity.EXTRA_TRAIN_KEY, trainKey)
                    startActivity(intent)
                }

                // Bind Train to ViewHolder, setting OnClickListener for the join button
                viewHolder.bindTrain(model, trainKey)

                showTrainsView()
            }

        }

        recycler.adapter = adapter

        loadingIndicator = findViewById(R.id.pb_loading_indicator)
        showLoading()

        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())

        fab.setOnClickListener { view ->
            writeNewTrain("Något ställe", "Vi går och käkar",
                    timeStamp, "http://i.huffpost.com/gen/4451422/images/o-FOOD-facebook.jpg",
                    0)
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.cleanup()
    }

    private fun createMockData() {
        // TODO: Create some trains
    }

    private fun writeNewTrain(title: String, description: String, time: String, imgUrl: String, passengerCount: Int) : Task<Void> {
        // TODO: Fix
        val key = databaseRef.child("trains").push().key
        val train = Train(title, description, time, imgUrl)
//        val train = HashMap<String, Any>()
//        train.put("title", "Något ställe")
//        train.put("description", "Vi går och käkar")
//        train.put("time", "2017-08-30 11:15:00")
//        train.put("imgUrl", "http://i.huffpost.com/gen/4451422/images/o-FOOD-facebook.jpg")
//        train.put("passengerCount", 0)

         val trainValues = train.toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates.put("/trains/" + key, trainValues)

        return databaseRef.updateChildren(childUpdates)
    }

    private fun showTrainsView() {
        loadingIndicator.visibility = View.INVISIBLE

        recycler.visibility = View.VISIBLE
    }

    private fun showLoading() {
        recycler.visibility = View.INVISIBLE

        loadingIndicator.visibility = View.VISIBLE
    }

    private fun signOut() {
        // Start LoginActivity, tell it to sign out
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("signout", true)
        startActivity(intent)

        // Finish activity
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
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

    private fun getQuery(databaseReference: DatabaseReference): Query {
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        return databaseReference.child("trains")
                .limitToFirst(100)
    }
}
