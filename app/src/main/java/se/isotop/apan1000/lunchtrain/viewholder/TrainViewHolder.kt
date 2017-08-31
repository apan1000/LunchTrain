package se.isotop.apan1000.lunchtrain.viewholder

import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.train_list_item.view.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import se.isotop.apan1000.lunchtrain.R
import se.isotop.apan1000.lunchtrain.model.Train


/**
 * Created by Fredrik Berglund
 */
class TrainViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val TAG = "TrainViewHolder"

    private val databaseRef = FirebaseDatabase.getInstance().reference

    fun bindTrain(train: Train, trainKey: String) {
        with(train) {
            itemView.train_image_loader.visibility = View.VISIBLE

            val fmt = DateTimeFormat.forPattern("HH:mm")
            val shortTime = fmt.print(DateTime(time))

            itemView.train_title.text = title
            itemView.train_description.text = description
            itemView.train_time.text = shortTime
            itemView.train_passenger_count.text = passengerCount.toString()

            if(imgUrl != "") {
                val requestOptions = RequestOptions()
                requestOptions.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)

                Glide.with(itemView.context)
                        .load(imgUrl)
                        .listener(object : RequestListener<Drawable> {
                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                itemView.train_image_loader.visibility = View.GONE
                                return false
                            }

                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                itemView.train_image_loader.visibility = View.GONE
                                return false
                            }
                        })
                        .apply(requestOptions)
                        .into(itemView.train_image)
            } else {
                itemView.train_image_loader.visibility = View.GONE
                itemView.train_image.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.food_train))
            }
        }

        showLoadingJoinButton()
        databaseRef.child("passengers").child(trainKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Determine if the current user has joined this train and set UI accordingly
                if (snapshot.hasChild(getUid())) {
                    itemView.join_button.setImageResource(R.drawable.ic_check_circle_light_blue_24dp)
                } else {
                    itemView.join_button.setImageResource(R.drawable.ic_check_circle_grey_24dp)
                }
                showJoinButton()
            }

            override fun onCancelled(error: DatabaseError?) {
                Log.e(TAG, "Get passengers canceled: $error")
                Toast.makeText(itemView.context, "Network error: Could not get data about joined train", Toast.LENGTH_SHORT).show()
            }
        })

        itemView.join_button.setOnClickListener { onJoinClicked(trainKey) }
    }

    private fun getUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    private fun showLoadingJoinButton() {
        itemView.join_button.visibility = View.INVISIBLE
        itemView.join_loading_indicator.visibility = View.VISIBLE
    }

    private fun showJoinButton() {
        itemView.join_loading_indicator.visibility = View.INVISIBLE
        itemView.join_button.visibility = View.VISIBLE
    }

    private fun onJoinClicked(trainKey: String) {
        showLoadingJoinButton()

        val userId = getUid()

        val userRef : DatabaseReference = databaseRef.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val passengerAt = snapshot.child("passengerAt").value as String

                if(passengerAt == trainKey) {
                    // Leave train
                    Log.d(TAG, "Same train :) ${snapshot.child("passengerAt").value}")
                    leaveOldTrain(passengerAt)
                } else {
                    // Change to clicked train
                    Log.d(TAG, "Other train :O $trainKey")

                    leaveOldTrain(passengerAt)

                    joinNewTrain(trainKey)
                }
            }

            override fun onCancelled(error: DatabaseError?) {
                Log.e(TAG, "userRef cancelled: $error")
            }
        })
    }

    private fun leaveOldTrain(passengerAt: String) {
        if(passengerAt != "") {
            val userRef : DatabaseReference = databaseRef.child("users").child(getUid())
            val oldPassengerCountRef: DatabaseReference = databaseRef.child("trains").child(passengerAt).child("passengerCount")
            val oldPassengersRef: DatabaseReference = databaseRef.child("passengers").child(passengerAt)

            oldPassengerCountRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot?) {
                    val count = snapshot?.value as Long
                    snapshot.ref?.setValue(count - 1)
                }

                override fun onCancelled(error: DatabaseError?) {
                    Log.e(TAG, "oldPassengerCountRef cancelled: $error")
                }
            })

            userRef.child("passengerAt").setValue("")
            oldPassengersRef.child(getUid()).removeValue()
        }
    }

    private fun joinNewTrain(trainKey: String) {
        val userId = getUid()

        val userRef : DatabaseReference = databaseRef.child("users").child(userId)
        val trainRef: DatabaseReference = databaseRef.child("trains").child(trainKey)
        val passengersRef: DatabaseReference = databaseRef.child("passengers").child(trainKey)

        trainRef.child("passengerCount").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                val count = snapshot?.value as Long
                snapshot.ref?.setValue(count + 1)
            }

            override fun onCancelled(error: DatabaseError?) {
                Log.e(TAG, "trainRef cancelled: $error")
            }
        })
        userRef.child("passengerAt").setValue(trainKey)
        passengersRef.child(userId).setValue(true)
    }
}