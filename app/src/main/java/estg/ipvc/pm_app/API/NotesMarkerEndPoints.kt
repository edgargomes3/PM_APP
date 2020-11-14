package estg.ipvc.pm_app.API

import estg.ipvc.pm_app.dataclasses.*
import retrofit2.Call
import retrofit2.http.*

interface NotesMarkerEndPoints {
    @GET("/api/notas")
    fun getNotas(): Call<List<Nota>>

    /*@GET("/api/notas/{id}")
    fun getNotasById(@Path("id") id: Int): Call<Nota>

    @GET("/api/notas/user/{id}")
    fun getNotasByUserId(@Path("id") id: Int): Call<Nota>

    @FormUrlEncoded
    @POST("/api/notas/post")
    fun postTest(@Field("username") username: String, @Field("password") password: String?): Call<LoginOutputPost>*/
}