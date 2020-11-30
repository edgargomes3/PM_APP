package estg.ipvc.pm_app.dataclasses

data class MapMarker(
        val id: Int,
        val tipo_problema: TipoProblema,
        val problema: String,
        val foto: String,
        val latitude: String,
        val longitude: String,
        val user: User
)

data class  TipoProblema(
        val id: Int,
        val tipo: String
)

data class  User(
        val id: Int,
        val username: String
)