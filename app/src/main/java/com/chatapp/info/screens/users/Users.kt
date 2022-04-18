package com.chatapp.info.screens.users


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.chatapp.info.R
import com.chatapp.info.data.User
import com.chatapp.info.databinding.UsersBinding
import com.chatapp.info.utils.KEY_CHAT_ID
import com.chatapp.info.utils.KEY_RECIPIENT
import com.chatapp.info.utils.MyOnFocusChangeListener
import kotlinx.coroutines.*
import org.koin.android.viewmodel.ext.android.sharedViewModel


class Users : Fragment() {

    private val viewModel by sharedViewModel<UsersViewModel>()

    private lateinit var binding : UsersBinding
    private lateinit var ctx: Activity
    private val focusChangeListener = MyOnFocusChangeListener()
    private lateinit var userController: UserController


    var recipientData: User? = null

//    private var list = ArrayList<User>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        // TODO: enhance the user item.

        binding = DataBindingUtil.inflate(inflater, R.layout.users,container,false)

        ctx = requireActivity()


        setViews()
        setTopAppBar()
        setObserves()





        return binding.root
    }

    override fun onResume() {
        super.onResume()

//        viewModel.getCurrentUser()
    }


    private fun setViews(){

        binding.apply {

            /** on user click **/
            userController = UserController(UserController.UserClickListener { recipient ->
                recipientData = recipient
                viewModel.navigateToChat(recipient)
            })

            recyclerViewUsers.adapter = userController.adapter

        }

    }

    private fun setObserves(){



        viewModel.localChats.observe(viewLifecycleOwner){
            if (it != null){
                Log.d("hhh","chats size: " + it.size)
                if (it.isNotEmpty()){
                    Log.d("hhh","chatId: " + it.first().chatId)
                }
            }
        }



        // todo fix the bundle issue.

        /** live data navigate to chat **/
        viewModel.navigateToChat.observe(viewLifecycleOwner){ chatId ->
            if (chatId != null){
//                Toast.makeText(context,"chatId: $chatId",Toast.LENGTH_SHORT).show()
//                Toast.makeText(context,"recpientName: ${recipientData?.name}",Toast.LENGTH_SHORT).show()

                val data = bundleOf(KEY_CHAT_ID to chatId , KEY_RECIPIENT to recipientData)
                findNavController().navigate(R.id.action_users_to_chat,data)
                viewModel.navigateToChatDone()
            }
        }


        /** live data users **/
        viewModel.users.observe(viewLifecycleOwner){ users ->
            if (users != null){
                userController.setData(users)

            }
        }



    }

    private fun performSearch(query: String) {
        viewModel.filterBySearch(query.trim())
    }


    private fun setTopAppBar() {
        var lastInput = ""
        val debounceJob: Job? = null
        val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        binding.searchBar.search.onFocusChangeListener = focusChangeListener
        binding.searchBar.search.doAfterTextChanged { editable ->
            if (editable != null) {
                val newtInput = editable.toString()
                debounceJob?.cancel()
                if (lastInput != newtInput) {
                    lastInput = newtInput
                    uiScope.launch {
                        delay(500)
                        if (lastInput == newtInput) {
                            performSearch(newtInput)
                        }
                    }
                }
            }else{

            }
        }
        binding.searchBar.search.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                textView.clearFocus()
                val inputManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(textView.windowToken, 0)
                performSearch(textView.text.toString())
                true
            } else {
                false
            }
        }
        binding.searchBar.searchOutlinedTextLayout.setEndIconOnClickListener {
            it.clearFocus()
            binding.searchBar.search.setText("")
            val inputManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }



    private fun pupUpMenu(v: View){
        val popupMenu = PopupMenu(ctx,v)
        popupMenu.menuInflater.inflate(R.menu.main_menu,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {

            }
            true
        }
        popupMenu.show()
    }




}