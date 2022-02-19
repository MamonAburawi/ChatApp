package com.chatapp.info.screens.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.work.WorkManager
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
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

    private var m: Message? = null

    private var controller: EpoxyController? = null
    private lateinit var withModels: Unit
    private var oldMessages = ArrayList<Message>()
    private var newMessages = ArrayList<Message>()
    private var diff = ArrayList<Message>()


    private val senderId = FirebaseAuth.getInstance().currentUser!!.uid


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        binding = DataBindingUtil.inflate(inflater, R.layout.chat,container,false)
        ctx = requireActivity()

        userInfo = navArgs<ChatArgs>().value.user
        val factory = ChatViewModelFactory(ctx.application,userInfo)

        viewModel = ViewModelProvider(this,factory)[ChatViewModel::class.java]





        binding.apply {

            binding.user = userInfo
            withModels = recyclerViewChat.withModels{
                return@withModels
            }


            /** button send **/
            btnSend.setOnClickListener {
                val text = message.text.trim().toString()
                if (text.isNotEmpty()){
                    val mess = Message(Random.nextLong(),text,Calendar.getInstance().time,senderId,userInfo.id,"", MessageType.TEXT)
                    m = mess
                    viewModel.addMessage(mess)
                    message.setText("")
                }
            }


            /** button back **/
            btnBack.setOnClickListener {
                navigateToBackScreen()
            }



            /** live data messages **/
            viewModel.messages.observe(viewLifecycleOwner,{ messages ->
                if (messages != null){
                    oldMessages = messages as ArrayList<Message>
                    initRecyclerView(messages)
                }else{
                    Toast.makeText(ctx,"no messages found!",Toast.LENGTH_SHORT).show()
                }
            })




            /** send message worker info **/
            viewModel.sendMessageWorkerId.observe(viewLifecycleOwner, { workerId ->
                if (workerId != null){

                    val worker = WorkManager.getInstance(requireActivity())
                    worker.getWorkInfoByIdLiveData(workerId).observe(viewLifecycleOwner, {

                        if (it.state.isFinished){ // message is sent successfully
                            recyclerViewChat.smoothScrollToPosition(oldMessages.size + 2)

//                            // TODO: fix this issue ..
//                            recyclerViewChat.withModels {
//
//                                oldMessages.forEach {
//
//                                    m!!.text = "welocme bro how are you all"
//                                    this.senderMessage {
//                                        id(m!!.id)
//                                        message(m)
//
//                                    }
//
//                                }
//
//
//                            }
//                            updateUI()
//



                        }

                    })
                }
            })


            /** remove message worker info **/
            viewModel.removeMessageWorkerId.observe(viewLifecycleOwner,{ workerId ->
                if (workerId != null){
                    val worker = WorkManager.getInstance(requireActivity())
                    worker.getWorkInfoByIdLiveData(workerId).observe(viewLifecycleOwner,{

                        if (it.state.isFinished){ // message is removed successfully

                        }

                    })
                }
            })




        }






        return binding.root
    }


    private fun updateUI(){
        binding.recyclerViewChat.requestModelBuild()
    }


    private fun initRecyclerView(messages: List<Message>){
        /** recycler view messages **/
        binding.recyclerViewChat.withModels {
            controller = this
            messages.forEachIndexed { index, message ->

                if (message.senderId == senderId){ // sender layout

                    controller!!.senderMessage {
                        id(message.id)
                        message(message)
                        clickListener { v->
                            pupUpMenu(v,message)
                        }
                    }

                }else{ // recipient layout

                    controller!!.recipientMessage {
                        id(message.id)
                        message(message)
                        clickListener { v ->

                        }
                    }
                }

            }
        }

    }




    private fun navigateToBackScreen(){
        requireActivity().onBackPressed()
    }



    private fun pupUpMenu(v: View, message: Message){
        val popupMenu = PopupMenu(ctx,v)
        popupMenu.menuInflater.inflate(R.menu.message_menu,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.itemRemove -> {
//                    list.remove(message)
                    m = message
                    viewModel.removeMessage(message)
                }
            }
            true
        }
        popupMenu.show()
    }



}