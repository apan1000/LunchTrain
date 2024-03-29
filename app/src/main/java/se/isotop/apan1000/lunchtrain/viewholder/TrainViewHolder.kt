package se.isotop.apan1000.lunchtrain.viewholder

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.fragment_create_train.view.*
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

    private val timeFormat = DateTimeFormat.forPattern("HH:mm")

    private var oldTrain = Train()

    fun bindTrain(train: Train) {
        with(train) {
            itemView.train_image_loader.visibility = View.VISIBLE

            if(oldTrain.title.isEmpty())
                setTitle(title)
            else if(title != oldTrain.title)
                setTitle(title, true)

            if(oldTrain.description.isEmpty())
                setDescription(description)
            else if(description != oldTrain.description)
                setDescription(description)


            setTime(time)
            setPassengerCount(passengerCount.toString())

            val uid = FirebaseHelper.getUid()
            itemView.join_button.isSelected = passengers[uid] == true
            enableJoinButton()

            setImage(imgUrl)

            itemView.join_button.setOnClickListener { onJoinClicked(id) }
        }
        oldTrain = train
    }

    fun reset() {
        oldTrain = Train()
    }

    fun setTitle(title: String, animate: Boolean = false) {
        itemView.train_title.text = title
        if(animate) {
            val anim = AnimationUtils.loadAnimation(itemView.context, R.anim.clockwise)
//            anim.duration = 890
//            anim.setInterpolator(itemView.context, android.R.anim.bounce_interpolator)

            itemView.train_title.startAnimation(anim)
            setAnimation(itemView.train_title)
        }
    }

    fun setDescription(description: String) {
        if(description.isNotEmpty())
            itemView.train_description.text = description
        else {
            oldTrain.description = itemView.resources.getString(R.string.default_description)
            itemView.train_description.text = oldTrain.description
        }
    }

    fun setTime(time: String) {
        itemView.train_time.text = timeFormat.print(DateTime(time))
    }

    fun setPassengerCount(passengerCount: String, animate: Boolean = false) {
        itemView.train_passenger_count.text = passengerCount
        if(animate) {
            val anim = ScaleAnimation(
                    1.0f, 1.0f,
                    0.1f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.0f
            )
            anim.duration = 890
            anim.setInterpolator(itemView.context, android.R.anim.bounce_interpolator)

            itemView.train_passenger_count.startAnimation(anim)
        }
    }

    fun setImage(imgUrl: String) {
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

    fun changePassengerCount(passengerCount: String) {
        setPassengerCount(passengerCount)
    }

    private fun disableJoinButton() {
        itemView.join_button.isEnabled = false
    }

    private fun enableJoinButton() {
        itemView.join_button.isEnabled = true
    }

    private fun setAnimation(viewToAnimate: View) {

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
        if(itemView.join_button.isSelected)
            itemView.join_button.isSelected = false
        disableJoinButton()
        FirebaseHelper.joinOrLeaveTrain(trainId)
    }
}