package estg.ipvc.pm_app.dataclasses

data class Nota(
        val id: Int,
        val tipo_problema: TipoProblema,
        val problema: String,
        val foto: String,
        val latitude: String,
        val longitude: String,
        val user_id: Int
)

data class  TipoProblema(
        val id: Int,
        val tipo: String
)