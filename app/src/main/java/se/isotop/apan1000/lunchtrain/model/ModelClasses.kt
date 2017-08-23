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
                 var passengers: List<String> = mutableListOf(),
                 var passengerCount: Int = 0,
                 val uid: String = "")