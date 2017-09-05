package se.isotop.apan1000.lunchtrain

import android.app.TimePickerDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.fragment_create_train.view.*
import org.joda.time.DateTime
import se.isotop.apan1000.lunchtrain.model.Train
import java.util.regex.Pattern

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CreateTrainFragment.OnCreateTrainInteractionListener] interface
 * to handle interaction events.
 * Use the [CreateTrainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateTrainFragment : Fragment() {

    private var listener: OnCreateTrainInteractionListener? = null

    lateinit private var root: View

    lateinit private var titleEdit: EditText
    lateinit private var descriptionEdit: EditText
    lateinit private var urlEdit: EditText
    lateinit private var timeText: TextView
    lateinit private var submitTrainButton: Button
    lateinit private var imgView: ImageView

    var imgIsOk = false
    var date = DateTime.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        root = inflater!!.inflate(R.layout.fragment_create_train, container, false)

        imgView = root.create_train_image
        initEditTextViews()
        initTimeText()
        initSubmitButton()

        return root
    }

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

    private fun initEditTextViews() {
        titleEdit = root.edit_title
        descriptionEdit = root.edit_description
        urlEdit = root.edit_img_url

        titleEdit.addTextChangedListener(titleTextWatcher())
        urlEdit.addTextChangedListener(urlTextWatcher())
    }

    private fun initTimeText() {
        timeText = root.time_text

        // Set timeText to start of next hour
        date = date.plusHours(1).withMinuteOfHour(0)
        timeText.text = root.resources.getString(R.string.time_text, date.hourOfDay, date.minuteOfHour)

        // Start TimePickerDialog on timeText click
        timeText.setOnClickListener { view ->
            val hour = DateTime.now().hourOfDay
            val minute = DateTime.now().minuteOfHour

            val timePicker = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener {
                picker, selectedHour, selectedMinute ->
                timeText.text = root.resources.getString(R.string.time_text, selectedHour, selectedMinute)
                date = DateTime.now().withHourOfDay(selectedHour).withMinuteOfHour(selectedMinute)
            }, hour+1, 0, true)
            timePicker.show()
        }
    }

    private fun initSubmitButton() {
        submitTrainButton = root.submit_train_button

        submitTrainButton.setOnClickListener {
            val train = Train(titleEdit.text.toString(),
                    descriptionEdit.text.toString(),
                    date.toString(),
                    urlEdit.text.toString())

            submitTrainButton.isEnabled = false
            listener?.onCreateTrain(train)
        }
    }

    private fun checkIfValidTrain() {
        val titleIsOk = titleEdit.text.toString().length in 1..60
//        val descIsOk = descriptionEdit.text.toString().length in 1..60

        submitTrainButton.isEnabled = titleIsOk && imgIsOk
    }

    inner class urlTextWatcher : TextWatcher {
        private val p = Pattern.compile(".*\\.(gif|jpe?g|png|webp)$")

        override fun afterTextChanged(editable: Editable?) {
            val imgUrl = editable.toString()

            when {
                imgUrl.isEmpty() -> {
                    imgIsOk = true

                    imgView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.food_train))
                }
                p.matcher(imgUrl).matches() -> {
                    imgIsOk = true
                    root.image_loader.visibility = View.VISIBLE

                    val requestOptions = RequestOptions()
                    requestOptions.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)

                    Glide.with(context)
                            .load(imgUrl)
                            .listener(object : RequestListener<Drawable> {
                                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    root.image_loader.visibility = View.INVISIBLE
                                    return false
                                }

                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                    root.image_loader.visibility = View.INVISIBLE
                                    return false
                                }
                            })
                            .apply(requestOptions)
                            .into(imgView)
                }
                else -> imgIsOk = false
            }

            checkIfValidTrain()
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // Do nothing
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // Do nothing
        }
    }

    inner class titleTextWatcher : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            checkIfValidTrain()
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // Do nothing
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // Do nothing
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnCreateTrainInteractionListener {
        fun onCreateTrain(train: Train)
    }

    companion object {
        fun newInstance(): CreateTrainFragment {
            return CreateTrainFragment()
        }
    }
}
