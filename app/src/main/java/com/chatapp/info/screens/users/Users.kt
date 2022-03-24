package com.chatapp.info.screens.users


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.TypedEpoxyController
import com.chatapp.info.R
import com.chatapp.info.RegistrationActivity
import com.chatapp.info.data.User
import com.chatapp.info.databinding.UsersBinding
import com.chatapp.info.user
import com.chatapp.info.utils.KEY_RECIPIENT
import com.chatapp.info.utils.MyOnFocusChangeListener
import kotlinx.coroutines.*


class Users : Fragment() {


    private val viewModel by activityViewModels<UsersViewModel>()
    private lateinit var binding : UsersBinding
    private lateinit var ctx: Activity
    private val focusChangeListener = MyOnFocusChangeListener()
    private lateinit var userController: UserController


//    private var list = ArrayList<User>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {



        binding = DataBindingUtil.inflate(inflater, R.layout.users,container,false)

        ctx = requireActivity()


        setViews()
        setTopAppBar()
        setObserves()


        // TODO: update the search bar design.
        // TODO: set the functionality for search bar.


        binding.apply {


        }



        return binding.root
    }

    override fun onResume() {
        super.onResume()

        viewModel.getUser()
    }


    private fun setViews(){

        binding.apply {

            /** onUserClick **/
            userController = UserController(UserController.UserClickListener { user ->
                val data = bundleOf(KEY_RECIPIENT to user)
                findNavController().navigate(R.id.action_users_to_chat,data)
            })

            recyclerViewUsers.adapter = userController.adapter
            userController.setData(emptyList())

        }

    }

    private fun setObserves(){


        /** live data users **/
        viewModel.users.observe(viewLifecycleOwner){ users ->
            if (users != null){
                Toast.makeText(requireContext(),"users: ${users.size}",Toast.LENGTH_SHORT).show()

                userController.setData(users)


            }
        }



        /** live data sign out **/
        viewModel.signOut.observe(viewLifecycleOwner){
            if(it != null){
                if(it){
                    val intent = Intent(activity, RegistrationActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
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
                R.id.itemSignOut -> {
                    viewModel.signOut()
                }
            }
            true
        }
        popupMenu.show()
    }




}