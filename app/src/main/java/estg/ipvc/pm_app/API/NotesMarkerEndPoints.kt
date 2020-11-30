package estg.ipvc.pm_app.API

import estg.ipvc.pm_app.dataclasses.MapMarker
import estg.ipvc.pm_app.dataclasses.TipoProblema
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface NotesMarkerEndPoints {
    @GET("/api/markers")
    fun getNotesMarker(): Call<List<MapMarker>>

    @GET("/api/tipos")
    fun getTiposProblema(): Call<List<TipoProblema>>

    @GET("/api/marker/{id}")
    fun getNotesMarkerById(@Path("id") id: Int): Call<List<MapMarker>>

    @FormUrlEncoded
    @POST("/api/marker/update")
    fun putNoteMarkerById(@Field("id") id: Int, @Field("tipo_problema") tipo_problema: String, @Field("problema") problema: String, @Field("foto") foto: String, @Field("latitude") latitude: String, @Field("longitude") longitude: String): Call<NotesMarkerOutputPost>

    @GET("/api/marker/delete/{id}")
    fun deleteNoteMarker(@Path("id") id: Int): Call<NotesMarkerOutputPost>

    @Multipart
    @POST("/api/marker/post_image")
    fun postNotesMarkerImage(@Part imagem: MultipartBody.Part): Call<NotesMarkerOutputPost>

    @FormUrlEncoded
    @POST("/api/marker/post")
    fun postNotesMarker(@Field("tipo_problema") tipo_problema: String, @Field("problema") problema: String, @Field("foto") foto: String, @Field("latitude") latitude: String, @Field("longitude") longitude: String, @Field("username") username: String): Call<NotesMarkerOutputPost>
}