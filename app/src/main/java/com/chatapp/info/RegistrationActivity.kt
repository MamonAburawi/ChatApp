package com.chatapp.info

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
    }






    override fun onStart() {
        super.onStart()

        try {

            val mAuth = FirebaseAuth.getInstance()
            val uid = mAuth.currentUser?.uid
            if(uid != null){
                if (mAuth.currentUser?.isEmailVerified != null){
                    startActivity(
                        Intent(this,MainActivity::class.java).addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        ))
                }else{
                    // email is not verified..
                }

            }else{
                  // email is exist
            }

        }catch (ex: Exception){
            val error =  ex.message.toString()
           // error
        }
    }



}