package com.cnunez.docufast.loginUser.Model

import com.cnunez.docufast.loginUser.LoginUserContract

class LoginUserModel : LoginUserContract.Model {
    override fun authenticate(username: String, password: String, callback: (Boolean, String?) -> Unit) {

        if (username == "user" && password == "password") {
            callback(true, null)
        } else {
            callback(false, "Invalid username or password")
        }
    }
}