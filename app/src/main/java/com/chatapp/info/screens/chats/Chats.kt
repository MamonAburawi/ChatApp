package com.chatapp.info.screens.chats

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider

import com.chatapp.info.R
import com.chatapp.info.data.ChatDetails
import com.chatapp.info.databinding.UserChatsBinding
import com.chatapp.info.screens.chat.ChatViewModel
import com.chatapp.info.screens.users.UsersViewModel


class Chats : Fragment() {

    // here you can find the all user chats

    companion object{
        const val TAG = "ChatsFragment"
    }

    private val userViewModel by activityViewModels<UsersViewModel>()
    private val chatViewModel by activityViewModels<ChatViewModel>()
    private lateinit var viewModel: ChatsViewModel
    val list = ArrayList<ChatDetails>()

    private lateinit var binding: UserChatsBinding
    private lateinit var chatsController: ChatsController


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.user_chats, container, false)
        viewModel = ViewModelProvider(this)[ChatsViewModel::class.java]



        // todo: make the chats data live.

        // TODO: fitch the User chats from the room database

        setViews()
        setObservers()



        return binding.root
    }




    private fun setViews(){
        binding.apply {


            chatsController = ChatsController(requireContext(),ChatsController.ChatClickListener {
                Toast.makeText(context,it.lastMessage,Toast.LENGTH_SHORT).show()
            })

            binding.recyclerViewChats.adapter = chatsController.adapter


        }
    }

    private fun setObservers() {

        /** live data current user **/
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null){
                val chats = user.chats
                viewModel.initChats(chats)
            }
        }


        /** live data chats **/
        viewModel.chats.observe(viewLifecycleOwner){ chats ->
            if (chats != null){
                chatsController.setData(chats)
            }else{
                chatsController.setData(emptyList())
            }

        }




    }


}