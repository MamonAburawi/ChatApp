package com.chatapp.info

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.chatapp.info.databinding.ActivityLaunchBinding

import com.chatapp.info.screens.users.UsersViewModel
import com.chatapp.info.utils.ChatAppSessionManager
import com.google.firebase.auth.FirebaseAuth

class LaunchActivity : AppCompatActivity() {

    companion object{
        const val ONE_SECOND = 1000L
        const val THREE_SECOND = 3000L
    }
    private val sessionManager by lazy { ChatAppSessionManager(this) }

    private lateinit var binding: ActivityLaunchBinding
    private val usersViewModel = viewModels<UsersViewModel>()


    private lateinit var timer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_launch)


        initTime()
        initData()



        // TODO: get all messages and save it in room .
        // TODO: get user data .........


    }


    // todo make the users data live

    private fun initData(){
        usersViewModel.value.hardRefreshUsesData()
//        usersViewModel.value.hardRefreshData()
    }



    fun initTime(){
        timer = object : CountDownTimer(THREE_SECOND, ONE_SECOND){
            override fun onTick(p0: Long) {

            }

            override fun onFinish() {
                isRememberOn()
            }

        }
        timer.start()

    }

    fun isRememberOn(){
        if (sessionManager.isRememberMeOn()){
            navigateToMain()
        }else{
            navigateToRegistration()
        }
    }


    override fun onResume() {
        super.onResume()

        if (FirebaseAuth.getInstance().currentUser != null){
            initData()
            initTime()
        }else{
            navigateToRegistration()
        }

    }

    private fun navigateToMain(){
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun navigateToRegistration(){
        val intent = Intent(this, RegistrationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}

