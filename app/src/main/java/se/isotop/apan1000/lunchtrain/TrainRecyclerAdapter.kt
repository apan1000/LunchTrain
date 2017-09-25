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
import com.google.firebase.database.*
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

    private val childEventListeners = mutableMapOf<String, ChildEventListener>()
    private val references = mutableMapOf<String, DatabaseReference>()

    init {
        startListening()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainViewHolder {
        val view = LayoutInflater
                .from(parent.context)
                .inflate(modelLayout, parent, false)
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

    override fun onBindViewHolder(holder: TrainViewHolder, position: Int, payloads: List<Any>) {
        if(payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else {
            payloads.forEach {
                when(it) {
                    is DataSnapshot -> {
                        when(it.key) {
                            "title" -> holder.setTitle(it.value as String)
                            "description" -> holder.setDescription(it.value as String)
                            "passengerCount" -> holder.setPassengerCount(it.value.toString())
                            else -> super.onBindViewHolder(holder, position, payloads)
                        }
                    }
                    else -> holder.changePassengerCount("5")
                }
            }
        }
        for((key, ref) in references) {
            ref.removeEventListener(childEventListeners[key])
        }
    }

    fun getItem(position: Int): Train {
        return trainSnapshots.getObject(position)
    }

    override fun getItemCount() = trainSnapshots.size

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun startListening() {
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

    override fun onChildChanged(type: ChangeEventListener.EventType, snapshot: DataSnapshot,
                                index: Int, oldIndex: Int) {
        when (type) {
            ChangeEventListener.EventType.ADDED -> notifyItemInserted(index)
            ChangeEventListener.EventType.CHANGED -> notifyChildChanged(snapshot, index)
            ChangeEventListener.EventType.MOVED -> notifyItemMoved(oldIndex, index)
            ChangeEventListener.EventType.REMOVED -> notifyItemRemoved(index)
            else -> throw IllegalStateException("Incomplete case statement")
        }
    }

    private fun notifyChildChanged(snapshot: DataSnapshot, index: Int) {
        Log.i(TAG, "Train has changed: $snapshot")

        val eventListener = object : ChildEventListener {
            override fun onChildAdded(childSnapshot: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(childSnapshot: DataSnapshot, p1: String?) {
                Log.i(TAG, "Train child: $childSnapshot")
                notifyItemChanged(index, childSnapshot)
            }

            override fun onChildMoved(childSnapshot: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(childSnapshot: DataSnapshot) {
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toString())
            }
        }

        childEventListeners[snapshot.key] = eventListener
        references[snapshot.key] = snapshot.ref

//        val hasListener = childEventListeners.containsKey(snapshot.key)
        snapshot.ref.addChildEventListener(eventListener)

//        if(!hasListener) {
//            val childMap = snapshot.children.associate { it.key to it.value }
//            snapshot.ref.updateChildren(childMap)
//        }
    }

    interface TrainListListener {
        fun onTrainListResult(viewHolder: TrainViewHolder, model: Train, position: Int)
    }
}