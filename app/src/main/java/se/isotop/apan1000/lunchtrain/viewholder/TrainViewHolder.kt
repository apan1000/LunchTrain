package se.isotop.apan1000.lunchtrain.viewholder

import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.train_list_item.view.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import se.isotop.apan1000.lunchtrain.FirebaseHelper
import se.isotop.apan1000.lunchtrain.R
import se.isotop.apan1000.lunchtrain.model.Train


/**
 * Created by Fredrik Berglund
 */
class TrainViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val TAG = "TrainViewHolder"

    fun bindTrain(train: Train, trainId: String) {
        with(train) {
            itemView.train_image_loader.visibility = View.VISIBLE

            val fmt = DateTimeFormat.forPattern("HH:mm")
            val shortTime = fmt.print(DateTime(time))

            itemView.train_title.text = title
            itemView.train_description.text = description
            itemView.train_time.text = shortTime
            itemView.train_passenger_count.text = passengerCount.toString()

            val uid = FirebaseHelper.getUid()
            if (passengers.containsKey(uid) && passengers[uid] == true) {
                itemView.join_button.setImageResource(R.drawable.ic_check_circle_light_blue_24dp)
            } else {
                itemView.join_button.setImageResource(R.drawable.ic_check_circle_grey_24dp)
            }
            enableJoinButton()

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

        itemView.join_button.setOnClickListener { onJoinClicked(trainId) }
    }

    private fun disableJoinButton() {
        itemView.join_button.isEnabled = false
    }

    private fun enableJoinButton() {
        itemView.join_button.isEnabled = true
    }

    private fun onJoinClicked(trainId: String) {
        disableJoinButton()
        FirebaseHelper.joinOrLeaveTrain(trainId)
    }
}