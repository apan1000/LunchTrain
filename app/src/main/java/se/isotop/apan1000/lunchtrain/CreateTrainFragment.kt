package se.isotop.apan1000.lunchtrain

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_create_train.view.*
import se.isotop.apan1000.lunchtrain.model.Train
import java.util.HashMap

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CreateTrainFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [CreateTrainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateTrainFragment : Fragment() {

    private var listener: OnCreateTrainInteractionListener? = null

    lateinit private var databaseRef: DatabaseReference

    lateinit private var titleEdit: EditText
    lateinit private var descriptionEdit: EditText
    lateinit private var urlEdit: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        databaseRef = FirebaseDatabase.getInstance().reference
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater!!.inflate(R.layout.fragment_create_train, container, false)

        titleEdit = root.edit_title
        descriptionEdit = root.edit_description
        urlEdit = root.edit_img_url

        return root
    }

    // TODO: Attach listeners to TextEdit variables

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnCreateTrainInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnCreateTrainInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun writeNewTrain(title: String, description: String, time: String, imgUrl: String, passengerCount: Int) : Task<Void> {
        val key = databaseRef.child("trains").push().key
        val train = Train(title, description, time, imgUrl)
        val trainValues = train.toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates.put("/trains/" + key, trainValues)

        return databaseRef.updateChildren(childUpdates)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnCreateTrainInteractionListener {
        fun onCreateTrain(uri: Uri)
    }

    companion object {
        fun newInstance(): CreateTrainFragment {
            return CreateTrainFragment()
        }
    }
}
