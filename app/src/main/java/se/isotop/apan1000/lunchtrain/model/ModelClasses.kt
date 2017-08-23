package se.isotop.apan1000.lunchtrain.model


/**
 * Created by apan1000 on 2017-08-17.
 */
data class TrainList(private val trains: List<Train>) {
    val size: Int
        get() = trains.size

    operator fun get(position: Int) = trains[position]
}

data class Train(var title: String = "",
                 var description: String = "",
                 var time: String = "",
                 var imgUrl: String = "",
                 var passengerCount: Int = 0,
                 var passengers: List<String> = mutableListOf(),
                 val uid: String = "") {

    fun toMap() : Map<String, Any> {
        return hashMapOf("title" to title,
                "description" to description,
                "time" to time,
                "imgUrl" to imgUrl,
                "passengerCount" to passengerCount)
    }
}