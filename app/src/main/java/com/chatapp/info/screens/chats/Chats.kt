package com.chatapp.info.screens.chats

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider

import com.chatapp.info.R
import com.chatapp.info.data.ChatDetails
import com.chatapp.info.data.Message
import com.chatapp.info.databinding.UserChatsBinding
import com.chatapp.info.screens.chat.ChatViewModel
import com.chatapp.info.screens.users.UsersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class Chats : Fragment() {

    // here you can find the all user chats

    companion object{
        const val TAG = "ChatsFragment"
    }

    private val userViewModel by activityViewModels<UsersViewModel>()
    private val chatViewModel by activityViewModels<ChatViewModel>()
    private lateinit var chatsViewModel: ChatsViewModel
    val list = ArrayList<ChatDetails>()

    private lateinit var binding: UserChatsBinding
    private lateinit var chatsController: ChatsController


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.user_chats, container, false)
        chatsViewModel = ViewModelProvider(this)[ChatsViewModel::class.java]




        // todo: make the chats data live.

        // TODO: fitch the User chats from the room database

        setViews()





        return binding.root
    }

    override fun onResume() {
        super.onResume()

        setObservers()
    }

    override fun onPause() {
        super.onPause()

        setObservers()
    }

//    private fun initChats(allMessages: List<Message>) {
//        GlobalScope.launch(Dispatchers.Main) {
//            list.clear()
//            userViewModel.currentUser.value?.chats?.forEach { chat ->
//                val chatId = chat.chatId
//                chatsViewModel.observeRemoteChat(chatId)
//                val chatMessages = allMessages.filter { it.chatId == chatId }
//                val lastMessage = chatMessages.last()
//                val recipient = userViewModel.getUser(chat.recipientId)
//                val chatDetails = ChatDetails(recipient!!,lastMessage.text,lastMessage.date,"")
//                list.add(chatDetails)
//            }
//            chatsViewModel._chatsDetails.value = list
//        }
//    }



    private fun setViews(){
        binding.apply {


            chatsController = ChatsController(requireContext(),ChatsController.ChatClickListener {
                Toast.makeText(context,it.lastMessage,Toast.LENGTH_SHORT).show()
            })

            binding.recyclerViewChats.adapter = chatsController.adapter


        }
    }

    private fun setObservers() {

        chatsViewModel.currentUser.observe(viewLifecycleOwner) {
            val chats = it?.chats
            chats?.forEach {
//                chatViewModel.getLastMessage(it.chatId)
                Log.d(TAG,"chat details : lastMessage ${it.lastMessage}")
                Toast.makeText(context,"lastMessage: ${it.lastMessage}",Toast.LENGTH_SHORT).show()
            }

            Log.d(TAG,"chats : ${chats?.size}")
        }



//        // chats data is not live ! you must fitch the live user data.
//        userViewModel.currentUser.observe(viewLifecycleOwner) {
//            val chats = it?.chats
//            chats?.forEach {
////                chatViewModel.getLastMessage(it.chatId)
//                Log.d(TAG,"chat details : lastMessage ${it.lastMessage}")
//                Toast.makeText(context,"lastMessage: ${it.lastMessage}",Toast.LENGTH_SHORT).show()
//            }
//
//            Log.d(TAG,"chats : ${chats?.size}")
//        }
//




    }


}