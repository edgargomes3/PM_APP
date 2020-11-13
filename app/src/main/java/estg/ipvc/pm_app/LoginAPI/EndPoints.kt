package estg.ipvc.pm_app.LoginAPI

import retrofit2.Call
import retrofit2.http.*

interface LoginEndPoints {

    @FormUrlEncoded
    @POST("/api/login/post")
    fun postTest(@Field("username") username: String, @Field("password") password: String?): Call<LoginOutputPost>
}