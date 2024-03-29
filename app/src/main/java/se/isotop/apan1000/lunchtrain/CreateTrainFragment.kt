package se.isotop.apan1000.lunchtrain

import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
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
import android.widget.AutoCompleteTextView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.places.PlaceBuffer
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.android.synthetic.main.fragment_create_train.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CreateTrainFragment.CreateTrainInteractionListener] interface
 * to handle interaction events.
 * Use the [CreateTrainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateTrainFragment : Fragment(), GoogleApiClient.OnConnectionFailedListener {

    // TODO: Hantera clear-knappar

    // TODO: ändra till icke-hårdkodat (kanske använda gps)
    private val MAP_BOUNDS = LatLngBounds(
            LatLng(59.034591, 18.063240), LatLng(59.534591, 18.563240))

    private var listener: CreateTrainInteractionListener? = null

    lateinit private var root: View

    lateinit private var googleApiClient: GoogleApiClient

    lateinit private var placeAdapter: PlaceAutocompleteAdapter

    lateinit private var autocompleteView: AutoCompleteTextView
    lateinit private var clearDestinationButton: ImageButton
    lateinit private var descriptionEdit: EditText
    lateinit private var urlEdit: EditText
    lateinit private var timeText: TextView
    lateinit private var submitTrainButton: Button
    lateinit private var imgView: ImageView
    lateinit private var trainImage: Bitmap
    lateinit private var defaultImage: Bitmap

    var imgIsOk = true
    var date = DateTime.now()

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is CreateTrainInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement CreateTrainInteractionListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true

        defaultImage = BitmapFactory.decodeResource(resources, R.drawable.food_train)
        trainImage = defaultImage
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        root = inflater!!.inflate(R.layout.fragment_create_train, container, false)

        googleApiClient = GoogleApiClient.Builder(context)
                .enableAutoManage(context as FragmentActivity, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build()

        initImageView()
        initEditTextViews()
        initTimeText()
        initSubmitButton()

        // Register a listener that receives callbacks when a suggestion has been selected
        autocompleteView.onItemClickListener = autocompleteClickListener
        placeAdapter = PlaceAutocompleteAdapter(context, googleApiClient, MAP_BOUNDS,
                null)
        autocompleteView.setAdapter(placeAdapter)

        return root
    }

    override fun onResume() {
        super.onResume()
        imgView.setImageBitmap(trainImage)
    }

    override fun onStop() {
        super.onStop()
        googleApiClient.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        autocompleteView.onItemClickListener = null
//        urlEdit.removeTextChangedListener(urlTextWatcher)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun initImageView() {
        imgView = root.create_train_image
        imgView.setImageBitmap(trainImage)
    }

    private fun initEditTextViews() {
        autocompleteView = root.autocomplete_places
        descriptionEdit = root.edit_description
//        urlEdit = root.edit_img_url

//        urlEdit.addTextChangedListener(urlTextWatcher)
        autocompleteView.addTextChangedListener(textWatcher)
        descriptionEdit.addTextChangedListener(textWatcher)
    }

    private fun initClearTextButton() {
//        clearDestinationButton
    }

    private fun initTimeText() {
        timeText = root.time_text

        // Set timeText to start of next hour
        date = date.plusHours(1).withMinuteOfHour(0)
        timeText.text = root.resources.getString(R.string.time_text, date.hourOfDay, date.minuteOfHour)

        // Start TimePickerDialog on timeText click
        timeText.setOnClickListener { view ->
            val hour = date.hourOfDay
            val minute = date.minuteOfHour

            val timePicker = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener {
                picker, selectedHour, selectedMinute ->
                timeText.text = root.resources.getString(R.string.time_text, selectedHour, selectedMinute)
                date = DateTime.now().withHourOfDay(selectedHour).withMinuteOfHour(selectedMinute)
            }, hour, minute, true)
            timePicker.show()
        }
    }

    private fun initSubmitButton() {
        submitTrainButton = root.submit_train_button

        submitTrainButton.setOnClickListener {
            // TODO: Ladda upp bitmap till firebase storage
            val train = Train(autocompleteView.text.toString(),
                    descriptionEdit.text.toString(),
                    date.toString(),
                    urlEdit.text.toString())

            submitTrainButton.isEnabled = false
            listener?.onCreateTrain(train)
        }
    }

    private fun checkIfValidTrain() {
        val titleIsOk = autocompleteView.text.toString().length in 1..60
        val descIsOk = descriptionEdit.text.toString().length in 0..400

        submitTrainButton.isEnabled = titleIsOk && descIsOk && imgIsOk
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // Do nothing
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // Do nothing
        }

        override fun afterTextChanged(p0: Editable?) {
            checkIfValidTrain()
        }
    }

//    private val urlTextWatcher = object : TextWatcher {
//        private val p = Pattern.compile(".*\\.(gif|jpe?g|png|webp)$")
//
//        override fun afterTextChanged(editable: Editable?) {
//            val imgUrl = editable.toString()
//            setImageUrl(imgUrl)
//
//            checkIfValidTrain()
//        }
//
//        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            // Do nothing
//        }
//
//        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            // Do nothing
//        }
//
//        private fun setImageUrl(imgUrl: String) {
//            when {
//                imgUrl.isEmpty() -> {
//                    imgIsOk = true
//
//                    imgView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.food_train))
//                }
//                p.matcher(imgUrl).matches() -> {
//                    imgIsOk = true
//                    root.image_loader.visibility = View.VISIBLE
//
//                    val requestOptions = RequestOptions()
//                    requestOptions.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//
//                    Glide.with(context)
//                            .load(imgUrl)
//                            .listener(object : RequestListener<Drawable> {
//                                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
//                                    root.image_loader.visibility = View.INVISIBLE
//                                    return false
//                                }
//
//                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
//                                    root.image_loader.visibility = View.INVISIBLE
//                                    return false
//                                }
//                            })
//                            .apply(requestOptions)
//                            .into(imgView)
//                }
//                else -> imgIsOk = false
//            }
//        }
//    }

    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
     * String...)
     */
    private val autocompleteClickListener = AdapterView.OnItemClickListener {
        parent, view, position, id ->
        /*
         Retrieve the place ID of the selected item from the Adapter.
         The adapter stores each Place suggestion in a AutocompletePrediction from which we
         read the place ID and title.
          */
        val item = placeAdapter.getItem(position)
        val placeId = item.placeId
        val primaryText = item.getPrimaryText(null)

        Log.i(TAG, "Autocomplete item selected: " + primaryText)
        root.image_loader.visibility = View.VISIBLE

        /*
         Issue a request to the Places Geo Data API to retrieve a Place object with additional
         details about the place.
          */
        val placeResult = Places.GeoDataApi
                .getPlaceById(googleApiClient, placeId)
        placeResult.setResultCallback(updatePlaceDetailsCallback)

        val photosResult = Places.GeoDataApi.getPlacePhotos(googleApiClient, placeId)
        photosResult.setResultCallback { placePhotos ->
            if(!placePhotos.status.isSuccess) {
                return@setResultCallback
            }

            if(placePhotos.photoMetadata.count > 0) {
                // TODO: Visa placePhotos.photoMetadata[0].attributions vid bilden & spara i FB
                placePhotos.photoMetadata[0].getPhoto(googleApiClient).setResultCallback { photoResult ->
                    if (photoResult.status.isSuccess) {
                        trainImage = photoResult.bitmap
                        create_train_image.setImageBitmap(trainImage)
                        root.image_loader.visibility = View.INVISIBLE
                    }
                }
            } else {
                trainImage = defaultImage
                create_train_image.setImageBitmap(trainImage)
                root.image_loader.visibility = View.INVISIBLE
            }
        }

        Toast.makeText(context, "Clicked: " + primaryText,
                Toast.LENGTH_SHORT).show()
        Log.i(TAG, "Called getPlaceById to get Place details for " + placeId)
    }

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private val updatePlaceDetailsCallback = ResultCallback<PlaceBuffer> { places ->
        if (!places.status.isSuccess) {
            // Request did not complete successfully
            Log.e(TAG, "Place query did not complete. Error: " + places.status.toString())
            places.release()
            return@ResultCallback
        }

        if(isAdded) {
            // Get the Place object from the buffer.
            val place = places.get(0)

            // TODO: Use rating, and create layout for info stuff

            // Format details of the place for display and show it in a TextView.
            descriptionEdit.setText(formatPlaceDetails(resources, place.name,
                    place.id, place.address, place.phoneNumber,
                    place.websiteUri))

            Log.i(TAG, "Place details received: " + place.name)
        }

        places.release()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.errorCode)

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(context,
                "Could not connect to Google API Client: Error " + connectionResult.errorCode,
                Toast.LENGTH_SHORT).show()
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface CreateTrainInteractionListener {
        fun onCreateTrain(train: Train)
    }

    companion object {
        private val TAG = "CreateTrainFragment"

        fun newInstance(): CreateTrainFragment {
            return CreateTrainFragment()
        }

        private fun formatPlaceDetails(res: Resources, name: CharSequence, id: String,
                                       address: CharSequence?, phoneNumber: CharSequence?,
                                       websiteUri: Uri?) : Spanned {
            Log.e(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
                    websiteUri))
            return Html.fromHtml(res.getString(R.string.place_details, name, id,
                    address, phoneNumber, websiteUri))

        }
    }
}
