package com.chatapp.info

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.chatapp.info.databinding.ActivityMainBinding
import com.chatapp.info.screens.chat.ChatViewModel
import com.chatapp.info.screens.users.UsersViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val usersViewModel by viewModels<UsersViewModel>()
    private val chatViewModel by viewModels<ChatViewModel>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =  DataBindingUtil.setContentView(this,R.layout.activity_main)


        initData()
        setUpNav()


    }

    private fun initData(){
//        usersViewModel.getCurrentUser()
        usersViewModel.getLocalUsers()
//        usersViewModel.observeLocalUser()
        usersViewModel.observeRemoteUser()
        chatViewModel.observeLocalMessages()


    }

    private fun setUpNav() {
        val navFragment = supportFragmentManager.findFragmentById(R.id.home_nav_host_fragment) as NavHostFragment
        NavigationUI.setupWithNavController(binding.homeBottomNavigation, navFragment.navController)

        navFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.users -> setBottomNavVisibility(View.VISIBLE)
                R.id.userChats -> setBottomNavVisibility(View.VISIBLE)
                else -> setBottomNavVisibility(View.GONE)
            }
        }


    }


    private fun setBottomNavVisibility(visibility: Int) {
        binding.homeBottomNavigation.visibility = visibility
    }

}