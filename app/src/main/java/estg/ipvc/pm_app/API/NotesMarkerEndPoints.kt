package estg.ipvc.pm_app.API

import estg.ipvc.pm_app.dataclasses.Nota
import estg.ipvc.pm_app.dataclasses.TipoProblema
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface NotesMarkerEndPoints {
    @GET("/api/notas")
    fun getNotesMarker(): Call<List<Nota>>

    @GET("/api/tipos")
    fun getTiposProblema(): Call<List<TipoProblema>>

    /*@GET("/api/notas/{id}")
    fun getNotasById(@Path("id") id: Int): Call<Nota>

    @GET("/api/notas/user/{id}")
    fun getNotasByUserId(@Path("id") id: Int): Call<Nota>*/

    @Multipart
    @POST("/api/nota/post_image")
    fun postNotesMarkerImage(@Part imagem: MultipartBody.Part): Call<NotesMarkerOutputPost>

    @FormUrlEncoded
    @POST("/api/nota/post")
    fun postNotesMarker(@Field("tipo_problema") tipo_problema: String, @Field("problema") problema: String, @Field("foto") foto: String, @Field("latitude") latitude: String, @Field("longitude") longitude: String, @Field("username") username: String): Call<NotesMarkerOutputPost>
}