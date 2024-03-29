package se.isotop.apan1000.lunchtrain.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import kotlinx.android.synthetic.main.fragment_train_list.view.*
import kotlinx.android.synthetic.main.train_list.*
import org.joda.time.DateTime
import se.isotop.apan1000.lunchtrain.R
import se.isotop.apan1000.lunchtrain.model.Train
import se.isotop.apan1000.lunchtrain.viewholder.TrainViewHolder
import android.support.v7.widget.DefaultItemAnimator
import android.widget.ImageView
import kotlinx.android.synthetic.main.train_list_item.view.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TrainListFragment.OnTrainInteractionListener] interface
 * to handle interaction events.
 * Use the [TrainListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TrainListFragment : Fragment() {

    private val TAG = "TrainListFragment"

    private lateinit var root: View
    private lateinit var parentActivity: Activity

    lateinit private var databaseRef: DatabaseReference

    lateinit private var firebaseAdapter: FirebaseRecyclerAdapter<Train, TrainViewHolder>
    lateinit private var adapter: TrainRecyclerAdapter
    lateinit private var recyclerView: RecyclerView

    lateinit private var loadingIndicator: ProgressBar

    private var listener: OnTrainInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentActivity = this.activity

        if (arguments != null) {

        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        root = inflater!!.inflate(R.layout.fragment_train_list, container, false)

        recyclerView = root.train_list
        setupRecyclerView(recyclerView)

        loadingIndicator = parentActivity.train_list_loading
        showLoading()

        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.cleanup()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        val layoutManager =  LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager

        databaseRef = FirebaseDatabase.getInstance().reference
        val trainsQuery: Query = getQuery(databaseRef)
//        val parser = ClassSnapshotParser<Train>(Train::class.java)
//        val firebaseArray = FirebaseArray<Train>(trainsQuery, parser)

//        adapter = TrainRecyclerAdapter(context, firebaseArray, R.layout.train_list_item,
//                TrainViewHolder::class.java, this)
        adapter = TrainRecyclerAdapter(Train::class.java, R.layout.train_list_item,
                TrainViewHolder::class.java, trainsQuery)
        recyclerView.adapter = adapter

        val animator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }
        recyclerView.itemAnimator = animator
    }

    private fun getQuery(databaseReference: DatabaseReference): Query {
        val timeStamp = DateTime.now().withTimeAtStartOfDay().toString()
        Log.e(TAG, "Timestamp: $timeStamp")
        return databaseReference.child("trains")
                .orderByChild("time")
                .startAt(timeStamp)
                .limitToFirst(100)
    }

    private fun showTrainsView() {
        loadingIndicator.visibility = View.INVISIBLE

        recyclerView.visibility = View.VISIBLE
    }

    private fun showLoading() {
        recyclerView.visibility = View.INVISIBLE

        loadingIndicator.visibility = View.VISIBLE
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnTrainInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnTrainInteractionListener {
        fun onTrainSelected(context: Context, trainMap: MutableMap<String, Any>, position: Int,
                            trainImage: View? = null,
                            trainImageOverlay: View? = null,
                            trainTitle: View? = null)
    }

    inner class TrainRecyclerAdapter(modelClass: Class<Train>,
                                     modelLayout: Int,
                                     viewHolderClass: Class<TrainViewHolder>,
                                     query: Query)
        : FirebaseRecyclerAdapter<Train, TrainViewHolder>(
            modelClass,
            modelLayout,
            viewHolderClass,
            query) {

        override fun populateViewHolder(viewHolder: TrainViewHolder, model: Train, position: Int) {
            // Set click listener for the whole train view
            viewHolder.itemView.setOnClickListener { v ->
                listener?.onTrainSelected(v.context, model.toMap(), position,
                        viewHolder.itemView.train_image,
                        viewHolder.itemView.image_overlay,
                        viewHolder.itemView.train_title)
            }

            viewHolder.bindTrain(model)

            showTrainsView()
        }

        override fun onViewRecycled(holder: TrainViewHolder) {
            super.onViewRecycled(holder)
            // Reset holder before new data is inserted to avoid "change animations"
            holder.reset()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment.
         *
         * @return A new instance of fragment TrainListFragment.
         */
        fun newInstance(): TrainListFragment {
            return TrainListFragment()
        }
    }
}
