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

    lateinit private var adapter: FirebaseRecyclerAdapter<Train, TrainViewHolder>
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
        // Inflate the layout for this fragment
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

        adapter = TrainRecyclerAdapter(Train::class.java, R.layout.train_list_item,
                TrainViewHolder::class.java, trainsQuery)
        recyclerView.adapter = adapter
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

    private fun showTrainsView() {
        loadingIndicator.visibility = View.INVISIBLE

        recyclerView.visibility = View.VISIBLE
    }

    private fun showLoading() {
        recyclerView.visibility = View.INVISIBLE

        loadingIndicator.visibility = View.VISIBLE
    }

    // TODO: Rename method, update argument and hook method into UI event
//    fun onButtonPressed(view: View, model: Train, position: Int) {
//        if (listener != null) {
//            listener!!.onTrainSelected(view, model, position)
//        }
//    }

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
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnTrainInteractionListener {
        fun onTrainSelected(view: View, model: Train, position: Int)
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
                listener?.onTrainSelected(v, model, position)
            }

            viewHolder.bindTrain(model, trainKey)

            showTrainsView()
        }

    }

    companion object {
//        private val ARG_PARAM1 = "param1"
//        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TrainListFragment.
         */
        fun newInstance(): TrainListFragment {
            val fragment = TrainListFragment()
//            val args = Bundle()
//            args.putString(ARG_PARAM1, param1)
//            args.putString(ARG_PARAM2, param2)
//            fragment.arguments = args
            return fragment
        }
    }
}
