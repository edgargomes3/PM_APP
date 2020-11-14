package estg.ipvc.pm_app.dataclasses

data class Nota(
        val id: Int,
        val problema: String,
        val foto: String,
        val user_id: Int ,
        val marker : Marker
)

data class Marker(
        val latitude: String,
        val longitude: String
)