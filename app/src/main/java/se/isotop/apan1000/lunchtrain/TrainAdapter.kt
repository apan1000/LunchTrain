package se.isotop.apan1000.lunchtrain

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.train_list_item.view.*
import se.isotop.apan1000.lunchtrain.model.Train
import se.isotop.apan1000.lunchtrain.model.TrainList

/**
 * Created by apan1000 on 2017-08-16.
 */
class TrainAdapter(val mContext: Context, val mClickHandler: TrainAdapterOnClickHandler) :
        RecyclerView.Adapter<TrainAdapter.TrainAdapterViewHolder>() {

    private val mTrains = TrainList(mutableListOf())

    interface TrainAdapterOnClickHandler {
        fun onClick(id: Long)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainAdapterViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TrainAdapterViewHolder(layoutInflater.inflate(R.layout.train_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: TrainAdapterViewHolder?, position: Int) {
        holder?.bindTrain(mTrains[position])
    }

    override fun getItemCount(): Int {
        return mTrains.size
    }

    class TrainAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindTrain(train: Train) {
            with(train) {
                Glide.with(itemView.context).load(imgUrl).into(itemView.train_image)
                itemView.train_title.text = "Restaurang Tegelbacken"
                itemView.train_time.text = "Kl 11:50"
            }
        }
    }
}