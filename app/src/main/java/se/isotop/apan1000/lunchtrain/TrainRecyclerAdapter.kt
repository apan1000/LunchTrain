package se.isotop.apan1000.lunchtrain

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.database.ChangeEventListener
import com.firebase.ui.database.ObservableSnapshotArray
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import se.isotop.apan1000.lunchtrain.model.Train
import se.isotop.apan1000.lunchtrain.viewholder.TrainViewHolder
import java.lang.reflect.InvocationTargetException

class TrainRecyclerAdapter(private val context: Context,
                           private var trainSnapshots: ObservableSnapshotArray<Train>,
                           private val modelLayout: Int,
                           private val viewHolderClass: Class<TrainViewHolder>,
                           private val listener: TrainListListener)
    : RecyclerView.Adapter<TrainViewHolder>(), ChangeEventListener, LifecycleObserver {

    private val TAG = "TrainRecyclerAdapter"

    init {
        startListening()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(modelLayout, parent, false)
        try {
            val constructor = viewHolderClass.getConstructor(View::class.java)
            return constructor.newInstance(view)
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        } catch (e: Fragment.InstantiationException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }
    }

    override fun onBindViewHolder(holder: TrainViewHolder, position: Int) {
        val model = getItem(position)
        listener.onTrainListResult(holder, model, position)
    }

    override fun onBindViewHolder(holder: TrainViewHolder, position: Int, payloads: MutableList<Any>) {
        if(payloads.isEmpty())
            onBindViewHolder(holder, position)
        else {
            payloads.forEach {
                when(it) {
                // TODO: Fixa saker som payloads kan vara
                    is Int -> holder.bindPassengerCount(it)
                    else -> holder.bindPassengerCount(5)
                }
            }
        }
    }

    fun getItem(position: Int): Train {
        return trainSnapshots.getObject(position)
    }

    override fun getItemCount() = trainSnapshots.size

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun startListening() {
        if (!trainSnapshots.isListening(this)) {
            trainSnapshots.addChangeEventListener(this)
        }
    }

    fun cleanup() {
        trainSnapshots.removeChangeEventListener(this)
        notifyDataSetChanged()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    internal fun cleanup(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_STOP) {
            cleanup()
        } else if (event == Lifecycle.Event.ON_DESTROY) {
            source.lifecycle.removeObserver(this)
        }
    }

    override fun onCancelled(error: DatabaseError) { Log.w(TAG, error.toException()) }

    override fun onDataChanged() {

    }

    override fun onChildChanged(type: ChangeEventListener.EventType, snapshot: DataSnapshot, index: Int, oldIndex: Int) {
        when (type) {
            ChangeEventListener.EventType.ADDED -> notifyItemInserted(index)
            ChangeEventListener.EventType.CHANGED -> notifyChildChange(snapshot, index)
            ChangeEventListener.EventType.REMOVED -> notifyItemRemoved(index)
            ChangeEventListener.EventType.MOVED -> notifyItemMoved(oldIndex, index)
            else -> throw IllegalStateException("Incomplete case statement")
        }
    }

    private fun notifyChildChange(snapshot: DataSnapshot, index: Int) {
        // TODO: Kolla snapshot-barn och notifiera med rätt payload
        notifyItemChanged(index, snapshot.child("title"))
    }

//    fun populateViewHolder(viewHolder: TrainViewHolder, model: Train, position: Int) {
//        // Set click listener for the whole train view
//        viewHolder.itemView.setOnClickListener { v ->
//            listener?.onTrainSelected(v.context, model.toMap(), position)
//        }
//
//        viewHolder.bindTrain(model)
//
//        showTrainsView()
//    }

    interface TrainListListener {
        fun onTrainListResult(viewHolder: TrainViewHolder, model: Train, position: Int)
    }
}