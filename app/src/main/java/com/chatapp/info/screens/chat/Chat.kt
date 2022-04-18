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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkManager
import com.chatapp.info.*
import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.databinding.ChatBinding
import com.chatapp.info.screens.users.UsersViewModel
import com.chatapp.info.utils.*
import gun0912.tedimagepicker.builder.TedImagePicker
import gun0912.tedimagepicker.builder.type.MediaType
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.util.*
import kotlin.collections.ArrayList


class Chat: Fragment() {


    companion object {
        const val TAG = "ChatFragment"
        const val LAST_ITEM = 10000 * 10000
    }

    private val ctx by lazy { requireContext() }

    private val worker by lazy { WorkManager.getInstance(requireContext()) }
    private lateinit var binding : ChatBinding
    private val viewModel by sharedViewModel<ChatViewModel>()
    private val usersViewModel by activityViewModels<UsersViewModel>()
    private lateinit var chatController : ChatController
    private var selectedUriList = ArrayList<Uri>()

    private lateinit var chatId: String
    private lateinit var recipient: User
//    private val recipient = arguments?.get(KEY_RECIPIENT) as User

    // TODO: create layout for send image & message.


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.chat,container,false)

        chatId = arguments?.get(KEY_CHAT_ID) as String
        recipient = arguments?.get(KEY_RECIPIENT) as User

        Toast.makeText(context,"recipient: ${recipient.name} \n chatId: $chatId",Toast.LENGTH_SHORT).show()

        initChat()
        setViews()
        setObserves()

        binding.apply {


            /** button option **/
            btnOption.setOnClickListener {
                viewModel.removeAllMessages()
            }


            /** button send **/
            btnSend.setOnClickListener {

                val text = binding.message.text.trim().toString()
                // TODO: compress the images before send it inside work manager

                if (text != "" && selectedUriList.isEmpty()) { // message type text.
                    Log.d(TAG,"Send message type text")
                    viewModel.sendMessage(text,chatId,recipient,MessageType.TEXT, emptyList())
                }
                if (text == "" && selectedUriList.isNotEmpty()) { // message type image.
                    Log.d(TAG,"Send message type image: ${selectedUriList[0]}")

                    viewModel.sendMessage(text,chatId,recipient,MessageType.IMAGE, selectedUriList)
                }
                if(text != "" && selectedUriList.isNotEmpty()){ // message type text & image
                    Log.d(TAG,"Send message type text & image")
                    viewModel.sendMessage(text,chatId,recipient,MessageType.TEXT_IMAGE, selectedUriList)
                }

                selectedUriList.clear()
                message.setText("")
                recyclerViewImages.visibility = View.GONE
                scrollingToPosition(LAST_ITEM)


            }


            /** button back **/
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }


            /** button select image **/
            btnAttach.setOnClickListener { selectImages() }



        }


        return binding.root
    }



    override fun onResume() {
        super.onResume()

//        viewModel.initChat()
    }

    override fun onPause() {
        super.onPause()

        viewModel.observeRemoteChat(chatId)
    }

    private fun setViews() {
        binding.apply {


            /** onMessageClick **/
            chatController = ChatController(requireContext(),ChatController.MessageClickListener{ message ->
                // TODO: check if item sender or recipient image or text.
                // TODO: if image navigate to new screen and display images
                viewModel.deleteMessage(message)
            })
            chatController.setData(emptyList())
            recyclerViewChat.adapter = chatController.adapter





//            recyclerViewChat.setOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                    super.onScrollStateChanged(recyclerView, newState) }
//
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    if (dy < 0){
//
//                        Log.d(TAG,"scrolling up")
//                    }else{
//                        Log.d(TAG,"scrolling down")
//                    }
//                }
//            })


        }
    }


    private fun scrollingToPosition(p: Int){
        binding.recyclerViewChat.smoothScrollToPosition(p)
    }



    private fun isLastItemVisible(): Boolean {
        val layoutManager: LinearLayoutManager = binding.recyclerViewChat.layoutManager as LinearLayoutManager
        val pos: Int = layoutManager.findLastCompletelyVisibleItemPosition()
        val numItems: Int = binding.recyclerViewChat.adapter!!.itemCount
        return (pos >= numItems - 1)
    }

    private fun setObserves() {


//        viewModel.allMessages.observe(viewLifecycleOwner){
//            if (it != null){
//
//            }
//
//        }
//

        /** live data messages **/
        viewModel.chatMessages.observe(viewLifecycleOwner){ chatMessages ->

            // the recycler view will be scrolling to display new data if last item is visible.
            if (isLastItemVisible()) {
                scrollingToPosition(LAST_ITEM)
            }

            if (chatMessages != null){
                chatController.setData(chatMessages)
            }else{ // no messages
                chatController.setData(emptyList())
            }

        }


        /** live data new messages **/
        viewModel.newMessages.observe(viewLifecycleOwner){ newMessages ->
            if (newMessages.isNotEmpty()){

            }
        }


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







    private fun pupUpMenu(v: View, message: Message){
        val popupMenu = PopupMenu(ctx,v)
        popupMenu.menuInflater.inflate(R.menu.message_menu,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.itemRemove -> {
                    viewModel.deleteMessage(message)
                }
            }
            true
        }
        popupMenu.show()
    }





    private fun initChat() {
//        val recipient =  arguments?.get(KEY_RECIPIENT) as User
//        val currentUser = usersViewModel.currentUser.value
//        val currentUserChats = currentUser!!.chats
//        val recipientUserChats = recipient.chats
//        val chatId = findCommon(currentUserChats,recipientUserChats)
//        Log.d(TAG,"common chatId: $chatId")
        val chatId = arguments?.get(KEY_CHAT_ID) as String
        viewModel.observeLocalMessages(chatId)

//        viewModel.recipientU.value = recipient
        viewModel.chatId.value = chatId
        viewModel.observeLocalMessages(chatId)
        viewModel.observeRemoteChat(chatId)
//        binding.chatViewModel = viewModel

        binding.lifecycleOwner = this@Chat

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


