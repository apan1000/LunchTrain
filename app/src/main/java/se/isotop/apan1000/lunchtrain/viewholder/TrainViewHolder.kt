package se.isotop.apan1000.lunchtrain.viewholder

import android.content.res.ColorStateList
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
            if(description.isNotEmpty())
                itemView.train_description.text = description
            else
                itemView.train_description.text = itemView.resources.getString(R.string.default_description)
            itemView.train_time.text = shortTime
            itemView.train_passenger_count.text = passengerCount.toString()

            val uid = FirebaseHelper.getUid()
            if (passengers.containsKey(uid) && passengers[uid] == true) {
//                itemView.join_button.setImageResource(R.drawable.ic_check_circle_light_blue_24dp)
                setJoinButtonColor(R.color.btn_train_joined,
                        R.color.btn_train_leave,
                        R.color.btn_train_not_joined)
            } else {
//                itemView.join_button.setImageResource(R.drawable.ic_check_circle_grey_24dp)
                setJoinButtonColor(R.color.btn_train_not_joined)
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

    private fun setJoinButtonColor(defaultId: Int, pressedId: Int = defaultId, disabledId: Int = defaultId) {
        val defaultColor = ContextCompat.getColor(itemView.context, defaultId)
        val pressedColor = ContextCompat.getColor(itemView.context, pressedId)
        val disabledColor = ContextCompat.getColor(itemView.context, disabledId)

        val tintList = ColorStateList(
                arrayOf(intArrayOf(-android.R.attr.state_enabled),
                        intArrayOf(android.R.attr.state_pressed),
                        intArrayOf()
                ),
                intArrayOf(disabledColor,
                        pressedColor,
                        defaultColor
                )
        )

        itemView.join_button.backgroundTintList = tintList
    }

    private fun onJoinClicked(trainId: String) {
        disableJoinButton()
        FirebaseHelper.joinOrLeaveTrain(trainId)
    }
}