package se.isotop.apan1000.lunchtrain.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.train_list_item.view.*
import se.isotop.apan1000.lunchtrain.R
import se.isotop.apan1000.lunchtrain.model.Train


/**
 * Created by Fredrik Berglund
 */
class TrainViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bindTrain(train: Train, trainKey: String, joinOnClickListener: View.OnClickListener) {
        with(train) {
            itemView.train_title.text = train.title
            itemView.train_time.text = train.time
            Glide.with(itemView.context)
                    .load(imgUrl)
                    .into(itemView.train_image)
        }

        showLoadingJoinButton()
        FirebaseDatabase.getInstance().reference.child("passengers").child(trainKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Determine if the current user has joined this train and set UI accordingly
                if (snapshot.hasChild(getUid())) {
                    itemView.join_button.setImageResource(R.drawable.ic_check_circle_light_blue_24dp)
                } else {
                    itemView.join_button.setImageResource(R.drawable.ic_check_circle_grey_24dp)
                }
                showJoinButton()
            }

            override fun onCancelled(p0: DatabaseError?) {
                Toast.makeText(itemView.context, "Network error: Could not get data about joined train", Toast.LENGTH_SHORT).show()
            }
        })
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
}