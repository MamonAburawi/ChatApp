package com.chatapp.info.screens.chat

import android.annotation.SuppressLint
import android.app.Activity
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
import com.airbnb.epoxy.EpoxyController
import com.chatapp.info.*
import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.databinding.ChatBinding
import com.google.firebase.auth.FirebaseAuth
import gun0912.tedimagepicker.builder.TedImagePicker
import gun0912.tedimagepicker.builder.type.MediaType
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random



class Chat: Fragment() {

    companion object{
        const val IMAGE = 4
    }


    private lateinit var viewModel: ChatViewModel
    private lateinit var binding : ChatBinding
    private lateinit var ctx : Activity
    private lateinit var userInfo: User
    private var selectedUriList = ArrayList<Uri>()

    private var m: Message? = null

    private var controller: EpoxyController? = null
    private var oldMessages = ArrayList<Message>()

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
            binding.lifecycleOwner = this@Chat


            // TODO: set message for the user tell him you message will be sent once the connection is back with snake bar

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


            /** live data progress sending **/
            viewModel.isSending.observe(viewLifecycleOwner,{ isSending ->
                if (isSending != null){
                    if (isSending){
                        progressMessage.visibility = View.VISIBLE
                    }else{
                        progressMessage.visibility = View.GONE
                    }
                }
            })


            /** live data messages **/
            viewModel.messages.observe(viewLifecycleOwner,{ messages ->
                if (messages != null){
                    oldMessages = messages as ArrayList<Message>
                    initChatItems(messages)
                }else{
                    Toast.makeText(ctx,"no messages found!",Toast.LENGTH_SHORT).show()
                }
            })



            /** live data send message worker info **/
            viewModel.sendMessageWorkerId.observe(viewLifecycleOwner, { workerId ->
                if (workerId != null){
                    val worker = WorkManager.getInstance(requireActivity())
                    worker.getWorkInfoByIdLiveData(workerId).observe(viewLifecycleOwner, {
                        val state = it.state.name
                        if (state == "RUNNING"){
                            Log.i("worker","send message is running..")
                        }
                        if (it.state.isFinished){ // message is sent successfully
                            recyclerViewChat.smoothScrollToPosition(oldMessages.size + 2)

                        }
                    })
                }
            })

            // TODO: add global work for all live data worker and put them inside one function


            /** live data remove message worker info **/
            viewModel.removeMessageWorkerId.observe(viewLifecycleOwner,{ workerId ->
                if (workerId != null){
                    val worker = WorkManager.getInstance(requireActivity())
                    worker.getWorkInfoByIdLiveData(workerId).observe(viewLifecycleOwner,{

                        if (it.state.isFinished){ // message is removed successfully

                        }
                    })
                }
            })


            /** live data upload image worker info **/
            viewModel.uploadImageWorkerId.observe(viewLifecycleOwner,{ workerId ->
                if (workerId != null){
                    val worker = WorkManager.getInstance(requireContext())
                    worker.getWorkInfoByIdLiveData(workerId).observe(viewLifecycleOwner,{
                        if (it.state.isFinished){
                            viewModel.sendingComplete()
                        }
                    })
                }
            })



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


    // TODO: get the images from chat and put them inside epoxy recycler view

    // TODO: add remove image function for sender side


    private fun initChatItems(messages: List<Message>){
        binding.recyclerViewChat.withModels {
            controller = this
            messages.forEachIndexed { index, message ->
                if (message.senderId == senderId){ // sender layout

                    if(message.type == MessageType.TEXT){
                        controller!!.senderMessage {
                            id(message.id)
                            message(message)
                            clickListener { v->
                                pupUpMenu(v,message)
                            }
                        }
                    }else{ // Image

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
                    m = message
                    viewModel.removeMessage(message)
                }
            }
            true
        }
        popupMenu.show()
    }




}