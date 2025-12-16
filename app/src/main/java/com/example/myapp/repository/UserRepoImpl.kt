package com.example.myapp.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.myapp.model.UserModel

class UserRepoImpl : UserRepo{
        val auth : FirebaseAuth = FirebaseAuth.getInstance()

    val database : FirebaseDatabase = FirebaseDatabase.getInstance()

    val ref : DatabaseReference = database.getReference("Users")

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    callback(true,"Login Success")
                }else{
                    callback(false,"${it.exception?.message}")
                }
            }
    }

    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    callback(true,"Registration Success",
                        "${auth.currentUser?.uid}")
                }else{
                    callback(false,"${it.exception?.message}","")
                }
            }
    }

    override fun addUserToDatabase(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).setValue(model).addOnCompleteListener {
            if(it.isSuccessful){
                callback(true,"Registration Success")
            }else{
                callback(false,"${it.exception?.message}")
            }
        }
    }

    override fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Password reset email sent to $email")
                } else {
                    callback(false, task.exception?.message ?: "Failed to send reset email")
                }
            }
    }


    override fun deleteAccount(
        userId: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        // need to add an if statement of the beckend's passowrd matches with the entered password in the frontend
        ref.child(userId).removeValue().addOnCompleteListener {
            if (it.isSuccessful){
                callback(true, "Account deleted Successfully")
            }else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun editProfile(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).updateChildren(model.toMap())
            .addOnCompleteListener {
                if(it.isSuccessful){
                    callback(true,"Profile updates Successfully.")
            }else{
                    callback(false,"${it.exception?.message}")
                }

        }
    }

    override fun getUserById(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        ref.child(userId).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue(UserModel::class.java)
                    if(user!=null){
                        callback(true,"Profile fetched",user)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false,error.message,null)
            }

        })
    }

    override fun getAllUser(callback: (Boolean, String, List<UserModel>) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userList = mutableListOf<UserModel>()

                    for (child in snapshot.children) {
                        val user = child.getValue(UserModel::class.java)
                        if (user != null) {
                            userList.add(user)
                        }
                    }

                    callback(true, "All users fetched", userList)
                } else {
                    callback(false, "No users found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

}
