package com.cnunez.docufast.common.clases


import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class UserManager {
    private val users = mutableListOf<User>()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun createUser(name: String, email: String, password: String,organization: String, workGroups: List<String>, onComplete: (Boolean, String?)-> Unit){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{task ->
                if(task.isSuccessful){
                    val firebaseUser: FirebaseUser? = auth.currentUser
                    val user = User(name = name, email = email, password = password, organization = organization, workGroups = workGroups)
                    users.add(user)
                    onComplete(true,null)

                }else{
                    onComplete(false,task.exception?.message)
                }
            }
    }

    fun getUserById(id: Int): User? {
        return users.find { it.id == id }
    }

    fun updateUser(user: User){
        val index = users.indexOfFirst { it.id == user.id }
        if (index != -1){
            users[index]= user
        }
    }

    fun deleteUser(id :Int){
        users.removeIf{it.id == id}

    }


    fun removeUserFromGroup(userId: Int, group: String) {
        val user = getUserById(userId)
        user?.let {
            if (it.workGroups.contains(group)) {
                val updatedWorkGroups = it.workGroups - group
                val updatedUser = it.copy(workGroups = updatedWorkGroups)
                updateUser(updatedUser)
            }
        }
    }

}

