package se.isotop.apan1000.lunchtrain.model


/**
 * Created by apan1000 on 2017-08-17.
 */
data class Train(var title: String = "",
                 var description: String = "",
                 var time: String = "",
                 var imgUrl: String = "",
                 var passengerCount: Int = 0,
                 var passengers: MutableMap<String, Any> = mutableMapOf(),
                 var id: String = "") {

    fun toMap() : MutableMap<String, Any> {
        return hashMapOf("title" to title,
                "description" to description,
                "time" to time,
                "imgUrl" to imgUrl,
                "passengerCount" to passengerCount,
                "passengers" to passengers,
                "id" to id)
    }
}