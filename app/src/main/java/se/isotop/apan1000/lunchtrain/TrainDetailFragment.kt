package se.isotop.apan1000.lunchtrain

import android.app.Activity
import android.support.design.widget.CollapsingToolbarLayout
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_train_detail.*
import kotlinx.android.synthetic.main.activity_train_detail.view.*
import kotlinx.android.synthetic.main.train_detail.view.*
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.joda.time.format.DateTimeFormat

import se.isotop.apan1000.lunchtrain.dummy.DummyContent
import java.time.format.DateTimeFormatter

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

    lateinit var trainMap: Map<*, *>
    lateinit var root: View
    lateinit var parentActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments.containsKey(ARG_MAP)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.

            parentActivity = this.activity
            val appBarLayout = parentActivity.toolbar_layout

            trainMap = arguments.getSerializable(ARG_MAP) as Map<*, *>
            appBarLayout.title = trainMap["title"] as String
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.train_detail, container, false)

        val timeString = trainMap["time"] as String
        val fmt = DateTimeFormat.forPattern("HH:mm")
        val time = fmt.print(DateTime(timeString))

        root.detail_description.text = trainMap["description"] as String
        root.detail_time.text = time
        root.detail_passenger_count.text = (trainMap["passengerCount"] as Int).toString()

        if(trainMap["imgUrl"] as String != "")
            loadImage()

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
    }
}
