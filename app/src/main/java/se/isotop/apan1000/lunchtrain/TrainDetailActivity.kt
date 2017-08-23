package se.isotop.apan1000.lunchtrain

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TrainDetailActivity : AppCompatActivity() {

    private lateinit var mTrainReference: DatabaseReference
    private lateinit var mTrainKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train_detail)

        mTrainKey = intent.getStringExtra(EXTRA_TRAIN_KEY)

        // Initialize database
        mTrainReference = FirebaseDatabase.getInstance().reference.child("trains").child(mTrainKey)


        // Initialize views

    }

    companion object {
        val EXTRA_TRAIN_KEY: String = "train_key"
    }
}
