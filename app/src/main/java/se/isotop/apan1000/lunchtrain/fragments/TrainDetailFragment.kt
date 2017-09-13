package se.isotop.apan1000.lunchtrain.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.train_detail.view.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import se.isotop.apan1000.lunchtrain.FirebaseHelper
import se.isotop.apan1000.lunchtrain.R
import se.isotop.apan1000.lunchtrain.model.Train
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

    private lateinit var train: Train
    private lateinit var root: View
    private var listener: TrainDetailUpdateListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is TrainDetailUpdateListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement TrainDetailUpdateListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments.containsKey(ARG_MAP)) {
            val map = arguments.getSerializable(ARG_MAP) as MutableMap<String, Any>
            train = Train(
                    map["title"] as String,
                    map["description"] as String,
                    map["time"] as String,
                    map["imgUrl"] as String,
                    map["passengerCount"] as Int,
                    map["passengers"] as MutableMap<String, Any>,
                    map["id"] as String
            )
        } else {
            throw RuntimeException(context!!.toString() + " must provide ARG_MAP")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.train_detail, container, false)

        updateUI()

        FirebaseHelper.addTrainEventListener(train.id, TrainEventListener())

        return root
    }

    override fun onDestroy() {
        super.onDestroy()

        FirebaseHelper.removeTrainEventListener()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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
            if(snapshot.childrenCount == train.toMap().count().toLong()) {
                train = Train(
                        snapshot.child("title").value as String,
                        snapshot.child("description").value as String,
                        snapshot.child("time").value as String,
                        snapshot.child("imgUrl").value as String,
                        (snapshot.child("passengerCount").value as Long).toInt(),
                        snapshot.child("passengers").value as MutableMap<String, Any>,
                        snapshot.child("id").value as String
                )
            }
        }
    }

    private fun updateUI() {
        root.detail_description.text = train.description
        root.detail_time.text = formatTime(train.time)

        root.detail_passenger_count.text = train.passengerCount.toString()

        listener?.onTrainDetailUpdate(train)
    }

    private fun formatTime(timeString: String) : String {
        val fmt = DateTimeFormat.forPattern("HH:mm")
        return fmt.print(DateTime(timeString))
    }

    interface TrainDetailUpdateListener {
        fun onTrainDetailUpdate(train: Train)

        fun loadTrainDetailImage(imgUrl: String)
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
