package estg.ipvc.pm_app.LoginAPI

data class LoginOutputPost(
        val success: Boolean,
        val username: String,
        val msg : String
)