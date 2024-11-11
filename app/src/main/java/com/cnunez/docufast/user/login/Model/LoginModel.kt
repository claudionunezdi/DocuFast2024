package com.cnunez.docufast.user.login.Model

import com.cnunez.docufast.userLogin.LoginContract

class LoginModel : LoginContract.Model {
    override fun authenticate(username: String, password: String, callback: (Boolean, String?) -> Unit) {

        if (username == "user" && password == "password") {
            callback(true, null)
        } else {
            callback(false, "Invalid username or password")
        }
    }
}