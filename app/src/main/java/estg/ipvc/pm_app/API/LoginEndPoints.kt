package estg.ipvc.pm_app.API

import retrofit2.Call
import retrofit2.http.*

interface LoginEndPoints {

    @FormUrlEncoded
    @POST("/api/login/post")
    fun postLoginTest(@Field("username") username: String, @Field("password") password: String): Call<LoginOutputPost>

    @FormUrlEncoded
    @POST("/api/register/post")
    fun postRegisterTest(@Field("username") username: String, @Field("password") password: String): Call<LoginOutputPost>
}