package com.chatapp.info.screens.user_chats

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.chatapp.info.R
import com.chatapp.info.databinding.UserChatsBinding


class UserChats : Fragment() {

    private lateinit var viewModel: UserChatsViewModel

    private lateinit var binding: UserChatsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.user_chats, container, false)

        viewModel = ViewModelProvider(this)[UserChatsViewModel::class.java]


        // todo: make the chats data live.

        // TODO: fitch the User chats from the room database




        return binding.root
    }



}