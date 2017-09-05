package se.isotop.apan1000.lunchtrain

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
 */
object FirebaseManager {
    private val TAG = "FirebaseManager"
    private val databaseRef = FirebaseDatabase.getInstance().reference

    private fun getUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun writeNewTrain(train: Train) : Task<Void> {
        val key = databaseRef.child("trains").push().key
        val trainValues = train.toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates.put("/trains/" + key, trainValues)

        return databaseRef.updateChildren(childUpdates)
    }

    fun leaveTrain(trainId: String) {
        if(trainId != "") {
            val userRef: DatabaseReference = databaseRef.child("users").child(getUid())
            val trainRef = databaseRef.child("trains").child(trainId)

//            val updates = mutableMapOf("/users/${getUid()}" to "",
//                    "/passengers/$trainId" to null)

            /*
             * Start transaction
             */
            trainRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val t = mutableData.getValue<Train>(Train::class.java) ?: return Transaction.success(mutableData)

                    if (t.passengers.containsKey(getUid())) {
                        // Leave train
                        t.passengerCount = t.passengerCount - 1
                        t.passengers.remove(getUid())
                    }

                    // Set value and report transaction success
                    mutableData.value = t
                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, b: Boolean,
                                        dataSnapshot: DataSnapshot) {
                    // Transaction completed
                    Log.d(TAG, "postTransaction:onComplete:" + databaseError)
                    userRef.child("passengerAt").setValue("")
                }
            })
            /*
             * End transaction
             */
        }
    }

    fun joinTrain(trainId: String) {
        val userId = getUid()

        val userRef : DatabaseReference = databaseRef.child("users").child(userId)
        val trainRef: DatabaseReference = databaseRef.child("trains").child(trainId)

        /*
         * Start transaction
         */
        trainRef.runTransaction(object : Transaction.Handler {
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