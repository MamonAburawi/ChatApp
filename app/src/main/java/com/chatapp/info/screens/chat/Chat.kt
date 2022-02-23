package com.chatapp.info.screens.chat

import android.annotation.SuppressLint
import android.net.Uri
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
import androidx.work.WorkManager
import com.airbnb.epoxy.EpoxyRecyclerView
import com.chatapp.info.*

import com.chatapp.info.data.Message
import com.chatapp.info.databinding.ChatBinding
import com.google.firebase.auth.FirebaseAuth
import gun0912.tedimagepicker.builder.TedImagePicker
import gun0912.tedimagepicker.builder.type.MediaType
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class Chat: Fragment() {

    private val ctx by lazy { requireContext() }
    private val userInfo by lazy {
        navArgs<ChatArgs>().value.user
    }
    private val viewModel by lazy {
        val factory = ChatViewModelFactory(requireActivity().application,userInfo)
        ViewModelProvider(this,factory)[ChatViewModel::class.java]
    }
    private val senderId by lazy {
        FirebaseAuth.getInstance().currentUser!!.uid
    }
    private val worker by lazy {
        WorkManager.getInstance(requireContext())
    }
    private lateinit var binding : ChatBinding
    private var selectedUriList = ArrayList<Uri>()
    private var m: Message? = null
    private var currentMessages = ArrayList<Message>()


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.chat,container,false)

        binding.apply {

            utilContext = context
            binding.user = userInfo
            binding.lifecycleOwner = this@Chat

            liveData()
            workersInfo()
            initChatItems(currentMessages)

            // TODO: set message for the user tell him you message will be sent once the connection is back with snake bar

            btnSend.setOnLongClickListener {
                recyclerViewChat.requestModelBuild()
                true
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

                if (selectedUriList.isNotEmpty()){
                    selectedUriList.forEach { image ->
                        val imageId = genUUID()
                        val message = Message(Random.nextLong(),"",Calendar.getInstance().time,senderId,userInfo.id, imageId,MessageType.IMAGE)
                        viewModel.addMessage(message,image)
                    }
                    selectedUriList.clear()
                    recyclerViewImages.visibility = View.GONE
                }


            }


            /** button back **/
            btnBack.setOnClickListener {
                navigateToBackScreen()
            }


            /** button select image **/
            btnAttach.setOnClickListener { selectImages() }



        }


        return binding.root
    }


    private fun selectImages(){
        TedImagePicker.with(ctx)
            .mediaType(MediaType.IMAGE)
            .errorListener { message -> Log.d("ImagePicker", "message: $message") }
            .cancelListener { Log.d("ImagePicker", "image select cancel") }
            .selectedUri(selectedUriList)
            .startMultiImage { list: List<Uri> -> initSelectedImages(list) }
    }


    private fun initSelectedImages(imagesList: List<Uri>) {
        selectedUriList = imagesList as ArrayList<Uri>
        binding.recyclerViewImages.visibility = View.VISIBLE
        binding.recyclerViewImages.withModels {
            imagesList.forEach { uri->
                image {
                    id (uri.toString())
                    selectImage(uri)
                    btnClear { v->
                        selectedUriList.remove(uri)
                        requestModelBuild()
                        Log.i("ImagePicker","image: $uri removed")
                        if (selectedUriList.size <= 0){
                            binding.recyclerViewImages.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }




    // TODO: add remove image function for sender image item


    private fun initChatItems(messages: ArrayList<Message>){
        binding.recyclerViewChat.withModels {
            messages.forEachIndexed { index, message ->
                if (message.senderId == senderId){ // sender layout
                    if(message.type == MessageType.TEXT){
                        senderMessage {
                            id(message.id)
                            message(message)
                            clickListener { v->
                                pupUpMenu(v,message)
                            }
                        }
                    }else{
                        
                        senderImage {
                            id(message.id)
                            message(message)
                            clickListener { v->

                            }
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


    private fun navigateToBackScreen(){
        requireActivity().onBackPressed()
    }


    private fun pupUpMenu(v: View, message: Message){
        val popupMenu = PopupMenu(ctx,v)
        popupMenu.menuInflater.inflate(R.menu.message_menu,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.itemRemove -> {
                    m = message
                    viewModel.removeMessage(message)
                }
            }
            true
        }
        popupMenu.show()
    }


    private fun liveData() {

        /** live data progress sending **/
        viewModel.isSending.observe(viewLifecycleOwner,{ isSending ->
            if (isSending != null){
                if (isSending){
                    binding.progressMessage.visibility = View.VISIBLE
                }else{
                    binding.progressMessage.visibility = View.GONE
                }
            }
        })

        /** live data messages **/
        viewModel.messages.observe(viewLifecycleOwner,{ allMessages ->
            if (allMessages != null){
                val newMessages = allMessages.minus(currentMessages.toSet()) // here we
                currentMessages.addAll(newMessages)
                updateRV(binding.recyclerViewChat)

                Log.i("chatsdata","messages: ${currentMessages.size}")

            }else{
                Toast.makeText(ctx,"no messages found!",Toast.LENGTH_SHORT).show()
            }
        })


    }


    private fun updateRV(rv: EpoxyRecyclerView){
        rv.requestModelBuild()
    }


    private fun workersInfo(){
        /** live data send message worker info **/
        viewModel.sendMessageWorkerId.observe(viewLifecycleOwner, { workerId ->
            if (workerId != null){
                worker.getWorkInfoByIdLiveData(workerId).observe(viewLifecycleOwner, {
                    val state = it.state.name
                    if (state == "RUNNING"){
                        Log.i("worker","send message is running..")
                    }
                    if (it.state.isFinished){ // message is sent successfully
                        binding.recyclerViewChat.smoothScrollToPosition(currentMessages.size)
                    }
                })
            }
        })



        /** live data remove message worker info **/
        viewModel.removeMessageWorkerId.observe(viewLifecycleOwner,{ workerId ->
            if (workerId != null){
                worker.getWorkInfoByIdLiveData(workerId).observe(viewLifecycleOwner,{

                    if (it.state.isFinished){ // message is removed successfully

                    }
                })
            }
        })


        /** live data upload image worker info **/
        viewModel.uploadImageWorkerId.observe(viewLifecycleOwner,{ workerId ->
            if (workerId != null){
                worker.getWorkInfoByIdLiveData(workerId).observe(viewLifecycleOwner,{
                    if (it.state.isFinished){
                        viewModel.sendingComplete()
                    }
                })
            }
        })



    }



}