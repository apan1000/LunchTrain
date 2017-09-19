package se.isotop.apan1000.lunchtrain

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import se.isotop.apan1000.lunchtrain.model.Train
import java.util.HashMap
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Transaction
import com.google.firebase.database.MutableData



/**
 * Created by Fredrik Berglund on 2017-09-04.
 *
 * Helper for Firebase interaction
 */
object FirebaseHelper {
    private val TAG = "FirebaseHelper"
    private val databaseRef = FirebaseDatabase.getInstance().reference

    private lateinit var trainEventListener: ValueEventListener
    private lateinit var currentTrainRef: DatabaseReference

    fun addTrainEventListener(trainId: String, eventListener: ValueEventListener) {
        currentTrainRef = databaseRef.child("trains").child(trainId)

        trainEventListener = currentTrainRef.addValueEventListener(eventListener)
    }

    fun removeTrainEventListener() {
        currentTrainRef.removeEventListener(trainEventListener)
    }

    fun getUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun getNewTrainKey() : String {
        return databaseRef.child("trains").push().key
    }

    fun writeNewTrain(train: Train) : Task<Void> {
        val trainValues = train.toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates.put(train.id, trainValues)

        return databaseRef.child("trains").updateChildren(childUpdates)
    }

    /*
     * Leaves the train with trainId if user is a passenger
     * Otherwise, leave the train which user is passenger at and join train with trainId
     */
    fun joinOrLeaveTrain(trainId: String) {
        val userRef : DatabaseReference = databaseRef.child("users").child(getUid())

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val passengerAt = snapshot.child("passengerAt").value as String

                if(passengerAt == trainId) {
                    Log.d(TAG, "Same train :) ${snapshot.child("passengerAt").value}")
                    leaveTrain(passengerAt)
                } else {
                    Log.d(TAG, "Other train :O $trainId")

                    leaveTrain(passengerAt)
                    joinTrain(trainId)
                }
            }

            override fun onCancelled(error: DatabaseError?) {
                Log.e(TAG, "userRef cancelled: $error")
            }
        })
    }

    fun leaveTrain(trainId: String, joinTrainId: String = "") {
        if(trainId.isNotBlank()) {
            val userRef: DatabaseReference = databaseRef.child("users").child(getUid())
            val trainToLeaveRef = databaseRef.child("trains").child(trainId)

            val uid = getUid()

            /*
             * Start transaction
             */
            trainToLeaveRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val t = mutableData.getValue<Train>(Train::class.java) ?: return Transaction.success(mutableData)

                    if (t.passengers.containsKey(uid)) {
                        // Leave train
                        t.passengerCount = t.passengerCount - 1
                        t.passengers[uid!!] = false
                    }

                    mutableData.value = t
                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, b: Boolean,
                                        dataSnapshot: DataSnapshot) {
                    // Transaction completed
                    Log.d(TAG, "postTransaction:onComplete: $databaseError. $dataSnapshot")
                    userRef.child("passengerAt").setValue("")
                }
            })
            /*
             * End transaction
             */
        }
    }

    fun joinTrain(trainId: String) {
        if(trainId.isNotBlank()) {
            val userId = getUid()

            val userRef: DatabaseReference = databaseRef.child("users").child(userId)
            val trainToJoinRef: DatabaseReference = databaseRef.child("trains").child(trainId)

            /*
             * Start transaction
             */
            trainToJoinRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val t = mutableData.getValue<Train>(Train::class.java) ?: return Transaction.success(mutableData)

                    // Join train
                    t.passengerCount = t.passengerCount + 1
                    t.passengers.put(getUid()!!, true)

                    // Set value and report transaction success
                    mutableData.value = t
                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, b: Boolean,
                                        dataSnapshot: DataSnapshot) {
                    // Transaction completed
                    Log.d(TAG, "postTransaction:onComplete:" + databaseError)
                    userRef.child("passengerAt").setValue(trainId)
                }
            })
            /*
             * End transaction
             */
        }
    }

    fun uploadImage(bitmap: Bitmap) {
        // TODO: Upload image to firebase storage
    }

}
