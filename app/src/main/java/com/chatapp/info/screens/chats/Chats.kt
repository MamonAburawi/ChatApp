package com.chatapp.info.screens.chats

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

import com.chatapp.info.R
import com.chatapp.info.databinding.UserChatsBinding
import com.chatapp.info.screens.chat.ChatViewModel
import com.chatapp.info.screens.users.UsersViewModel
import com.chatapp.info.utils.KEY_RECIPIENT


class Chats : Fragment() {

    // here you can find the all user chats

    companion object{
        const val TAG = "ChatsFragment"
    }

    private val userViewModel by activityViewModels<UsersViewModel>()
    private val chatViewModel by activityViewModels<ChatViewModel>()
    private lateinit var viewModel: ChatsViewModel


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


            /** on chat clisk **/
            chatsController = ChatsController(requireContext(),ChatsController.ChatClickListener { chat ->
                viewModel.navigateToChat(chat.recipientId)
            })

            binding.recyclerViewChats.adapter = chatsController.adapter


        }
    }

    private fun setObservers() {

//        /** live data current user **/
//        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
//            if (user != null){
//                val chats = user.chats
//                viewModel.initChats(chats)
//            }
//        }
//

        /** live data chats **/
        chatViewModel.chats.observe(viewLifecycleOwner){ chats ->
            if (chats != null){
                chatsController.setData(chats)
            }else{
                chatsController.setData(emptyList())
            }

        }


        /** live data navigate to chat **/
        viewModel.navigateToChat.observe(viewLifecycleOwner){ recipient ->
            if (recipient != null){
                val data = bundleOf(KEY_RECIPIENT to recipient)
                findNavController().navigate(R.id.action_userChats_to_chat,data)
                viewModel.navigateToChatDone()
            }
        }



    }


}