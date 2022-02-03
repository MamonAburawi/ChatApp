package com.chatapp.info.screens.chat

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.chatapp.info.*
import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.databinding.ChatBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random


class Chat: Fragment() {


    private lateinit var viewModel: ChatViewModel
    private lateinit var binding : ChatBinding
    private lateinit var ctx : Activity
    private lateinit var userInfo: User
    private val senderId = FirebaseAuth.getInstance().currentUser!!.uid

    private lateinit var list :  ArrayList<Message>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        binding = DataBindingUtil.inflate(inflater, R.layout.chat,container,false)
        ctx = requireActivity()

        userInfo = navArgs<ChatArgs>().value.user
        val factory = ChatViewModelFactory(ctx.application,userInfo)

        viewModel = ViewModelProvider(this,factory)[ChatViewModel::class.java]
        list =  ArrayList()




        binding.apply {

            binding.user = userInfo



            /** button send **/
            btnSend.setOnClickListener {
                val text = message.text.trim().toString()
                if (text.isNotEmpty()){
                    val m = Message(Random.nextLong(),text,Calendar.getInstance().time,senderId,userInfo.id,"", MessageType.TEXT)
                    viewModel.addMessage(m)
                    message.setText("")
                }
            }


            /** button back **/
            btnBack.setOnClickListener {
                navigateToBackScreen()
            }


            /** live data state **/
            viewModel.messageState.observe(viewLifecycleOwner,{state->
                if(state != null){
                    when(state){
                        MessageState.INSERT->{
                           binding.recyclerViewChat.smoothScrollToPosition(90000000)
                            Toast.makeText(ctx,"message inserted",Toast.LENGTH_SHORT).show()
                        }
                        MessageState.DELETE->{}
                        MessageState.UPDATE->{}
                    }
                    viewModel.resetMessageState()
                }
            })


            /** live data messages **/
            viewModel.messages.observe(viewLifecycleOwner,{ messages ->
                if (messages != null){
                    initRecyclerView(messages)
                }else{
                    Toast.makeText(ctx,"no messages found!",Toast.LENGTH_SHORT).show()
                }
            })




        }


        return binding.root
    }



    private fun initRecyclerView(messages: List<Message>){
        /** recycler view messages **/
        binding.recyclerViewChat.withModels {
            messages.forEach { message ->
                if (message.senderId == senderId){ // sender layout

                    senderMessage {
                        id(message.id)
                        message(message)
                        clickListener { v->
                            pupUpMenu(v,message)
                        }
                    }

                }else{ // recipient layout

                    recipientMessage {
                        id(message.id)
                        message(message)
                        clickListener { v ->

                        }
                    }
                }
            }
        }

    }


    private fun updateUI(){
        binding.recyclerViewChat.requestModelBuild()
    }

    private fun navigateToBackScreen(){
        requireActivity().onBackPressed()
    }



    private fun pupUpMenu(v: View, message: Message){
        val popupMenu = PopupMenu(ctx,v)
        popupMenu.menuInflater.inflate(R.menu.message_menu,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.itemDelete -> {
//                    list.remove(message)
                    viewModel.removeMessage(message)
                    Toast.makeText(ctx,"${message.text} is deleted!",Toast.LENGTH_SHORT).show()

                }
            }
            true
        }
        popupMenu.show()
    }



}