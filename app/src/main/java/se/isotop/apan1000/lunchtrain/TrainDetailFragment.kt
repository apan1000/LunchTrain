package se.isotop.apan1000.lunchtrain

import android.app.Activity
import android.support.design.widget.CollapsingToolbarLayout
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import se.isotop.apan1000.lunchtrain.dummy.DummyContent

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments.containsKey(ARG_MAP)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.

            val activity = this.activity
            val appBarLayout = activity.findViewById<View>(R.id.toolbar_layout) as CollapsingToolbarLayout

            trainMap = arguments.getSerializable(ARG_MAP) as Map<*, *>
            appBarLayout.title = trainMap["title"] as String
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.train_detail, container, false)

        (rootView?.findViewById<TextView>(R.id.train_detail) as TextView).text = trainMap["description"] as String

        return rootView
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
