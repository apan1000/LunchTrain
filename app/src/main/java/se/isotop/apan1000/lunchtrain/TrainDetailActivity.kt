package se.isotop.apan1000.lunchtrain

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.os.Build
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_train_detail.*
import se.isotop.apan1000.lunchtrain.fragments.TrainDetailFragment
import se.isotop.apan1000.lunchtrain.model.Train
import java.io.Serializable
import android.content.res.ColorStateList




/**
 * An activity representing a single Train detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [TrainListActivity].
 */
class TrainDetailActivity : AppCompatActivity(), TrainDetailFragment.TrainDetailUpdateListener {

    val TAG = "TrainDetailActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train_detail)
        val toolbar = detail_toolbar
        if(toolbar != null)
            setSupportActionBar(toolbar)

        val itemId = intent.getIntExtra(TrainDetailFragment.ARG_ITEM_ID, 0)
        val trainMap = intent.getSerializableExtra(TrainDetailFragment.ARG_MAP) as MutableMap<String, Any>

        join_train_fab.setOnClickListener { view ->
            Snackbar.make(view, "Join/leave train with ID: ${trainMap["id"]}", Snackbar.LENGTH_LONG).show()
            if(trainMap.containsKey("id") && trainMap["id"] != "") {
                FirebaseHelper.joinOrLeaveTrain(trainMap["id"] as String)
                join_train_fab.isEnabled = false
            } else {
                Log.e(TAG, "trainMap[\"id\"] empty")
            }
        }

        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // TODO: Do something with itemId?
            val fragment = TrainDetailFragment.newInstance(itemId, trainMap as Serializable)
            supportFragmentManager.beginTransaction()
                    .add(R.id.train_detail_container, fragment)
                    .commit()
        }
    }

    override fun onStart() {
        super.onStart()

        // Make statusBar fully transparent
        if (Build.VERSION.SDK_INT >= 21) {
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    override fun onStop() {
        super.onStop()

        // Make statusBar solid
        if (Build.VERSION.SDK_INT >= 21) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        }
    }

    private fun signOut() {
        // Start LoginActivity, tell it to sign out
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("signout", true)
        startActivity(intent)
    }

    override fun onTrainDetailUpdate(train: Train) {
        toolbar_layout.title = train.title
//        train_title.text = train.title

        if (train.imgUrl.isNotEmpty())
            loadTrainDetailImage(train.imgUrl)

        if(train.passengers.containsKey(FirebaseHelper.getUid()) &&
                train.passengers[FirebaseHelper.getUid()] == true) {
            setJoinTrainFabColor(R.color.btn_train_joined,
                    R.color.btn_train_leave,
                    R.color.btn_train_not_joined)
        } else {
            setJoinTrainFabColor(R.color.btn_train_not_joined)
        }
        join_train_fab.isEnabled = true
    }

    private fun setJoinTrainFabColor(defaultId: Int, pressedId: Int = defaultId, disabledId: Int = defaultId) {
        val defaultColor = ContextCompat.getColor(this, defaultId)
        val pressedColor = ContextCompat.getColor(this, pressedId)
        val disabledColor = ContextCompat.getColor(this, disabledId)

        val tintList = ColorStateList(
                arrayOf(intArrayOf(-android.R.attr.state_enabled),
                        intArrayOf(android.R.attr.state_pressed),
                        intArrayOf()
                ),
                intArrayOf(disabledColor,
                        pressedColor,
                        defaultColor
                )
        )

        join_train_fab.backgroundTintList = tintList
    }

    override fun loadTrainDetailImage(imgUrl: String) {
        val requestOptions = RequestOptions()
        requestOptions.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)

        Glide.with(this)
                .load(imgUrl)
                .apply(requestOptions)
                .into(detail_image)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
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

    companion object {
        val EXTRA_TRAIN_ID: String = "train_id"
    }
}
