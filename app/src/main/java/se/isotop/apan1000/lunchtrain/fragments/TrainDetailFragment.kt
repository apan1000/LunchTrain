package se.isotop.apan1000.lunchtrain.fragments

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_train_detail.*
import kotlinx.android.synthetic.main.train_detail.view.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
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

    private lateinit var trainMap: Map<*, *>
    private lateinit var root: View
    private lateinit var parentActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentActivity = this.activity

        if (arguments.containsKey(ARG_MAP)) {
            trainMap = arguments.getSerializable(ARG_MAP) as Map<*, *>
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.train_detail, container, false)

        if (arguments.containsKey(ARG_MAP)) {
            // Set toolbar title
            val appBarLayout = parentActivity.toolbar_layout
            if (appBarLayout != null)
                appBarLayout.title = trainMap["title"] as String

            // Format time
            val timeString = trainMap["time"] as String
            val fmt = DateTimeFormat.forPattern("HH:mm")
            val time = fmt.print(DateTime(timeString))

            root.detail_description.text = trainMap["description"] as String
            root.detail_time.text = time
            root.detail_passenger_count.text = (trainMap["passengerCount"] as Int).toString()

            if (trainMap["imgUrl"] as String != "")
                loadImage()
        }

        return root
    }

    private fun loadImage() {
        val imageView = parentActivity.detail_image

        val requestOptions = RequestOptions()
        requestOptions.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)

        Glide.with(parentActivity)
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
        fun newInstance(itemId: String, serializedTrainMap: Serializable): TrainDetailFragment {
            val fragment = TrainDetailFragment()
            val args = Bundle()
            args.putString(ARG_ITEM_ID, itemId)
            args.putSerializable(ARG_MAP, serializedTrainMap)
            fragment.arguments = args
            return fragment
        }
    }
}
