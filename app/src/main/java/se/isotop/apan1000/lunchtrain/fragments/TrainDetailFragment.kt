package se.isotop.apan1000.lunchtrain.fragments

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_train_detail.*
import kotlinx.android.synthetic.main.train_detail.view.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import se.isotop.apan1000.lunchtrain.FirebaseHelper
import se.isotop.apan1000.lunchtrain.R
import java.io.Serializable

/**
 * A fragment representing a single Train detail screen.
 * This fragment is either contained in a [TrainListActivity]
 * in two-pane mode (on tablets) or a [TrainDetailActivity]
 * on handsets.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class TrainDetailFragment : Fragment() {

    val TAG = "TrainDetailFragment"

    private lateinit var trainMap: MutableMap<String, Any>
    private lateinit var root: View
    private lateinit var parentActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentActivity = this.activity

        if (arguments.containsKey(ARG_MAP)) {
            trainMap = arguments.getSerializable(ARG_MAP) as MutableMap<String, Any>
        } else {
            throw RuntimeException(context!!.toString() + " must provide ARG_MAP")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.train_detail, container, false)

        updateUI()

        FirebaseHelper.addTrainEventListener(trainMap["id"] as String, TrainEventListener())

        return root
    }

    override fun onDestroy() {
        super.onDestroy()

        FirebaseHelper.removeTrainEventlistener()
    }

    inner class TrainEventListener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot?) {
            Log.d(TAG, "DATA HAS CHANGED: ${snapshot.toString()}")
            if(snapshot != null) {
                snapshotToMap(snapshot)
                updateUI()
            }
        }

        override fun onCancelled(error: DatabaseError?) {
            Log.e(TAG, "Train value event cancelled.")
        }

        private fun snapshotToMap(snapshot: DataSnapshot) {
            if(snapshot.childrenCount == trainMap.count().toLong())
                trainMap = mutableMapOf(
                    "title" to snapshot.child("title").value!!,
                    "description" to snapshot.child("description").value!!,
                    "time" to snapshot.child("time").value!!,
                    "imgUrl" to snapshot.child("imgUrl").value!!,
                    "passengerCount" to snapshot.child("passengerCount").value!!,
                    "passengers" to snapshot.child("passengers").value!!,
                    "id" to snapshot.child("id").value!!
                )
        }
    }

    private fun updateUI() {
        parentActivity.toolbar_layout.title = trainMap["title"] as String

        root.detail_description.text = trainMap["description"] as String
        root.detail_time.text = formatTime(trainMap["time"] as String)

        root.detail_passenger_count.text = trainMap["passengerCount"].toString()

        if (trainMap["imgUrl"] as String != "")
            loadImage()

        parentActivity.fab.isEnabled = true
    }

    private fun formatTime(timeString: String) : String {
        val fmt = DateTimeFormat.forPattern("HH:mm")
        return fmt.print(DateTime(timeString))
    }

    private fun loadImage() {
        val imageView = parentActivity.detail_image

        val requestOptions = RequestOptions()
        requestOptions.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)

        Glide.with(this.context)
                .load(trainMap["imgUrl"] as String)
                .apply(requestOptions)
                .into(imageView)
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        val ARG_ITEM_ID = "item_id"
        val ARG_MAP = "map"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param itemId ID of the instance.
         * @param trainMap Map of the train data.
         * @return A new instance of fragment TrainDetailFragment.
         */
        fun newInstance(itemId: Int, serializedTrainMap: Serializable): TrainDetailFragment {
            val fragment = TrainDetailFragment()
            val args = Bundle()
            args.putInt(ARG_ITEM_ID, itemId)
            args.putSerializable(ARG_MAP, serializedTrainMap)
            fragment.arguments = args
            return fragment
        }
    }
}
