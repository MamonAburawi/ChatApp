package com.chatapp.info.screens.users


import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.chatapp.info.MainActivity
import com.chatapp.info.R
import com.chatapp.info.RegistrationActivity
import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.databinding.UsersBinding
import com.chatapp.info.user
import io.grpc.InternalChannelz.id
import java.util.*
import kotlin.collections.ArrayList


class Users : Fragment() {


    private lateinit var viewModel: UsersViewModel
    private lateinit var binding : UsersBinding
    private lateinit var ctx: Activity

//    private var list = ArrayList<User>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewModel = ViewModelProvider(this)[UsersViewModel::class.java]

        binding = DataBindingUtil.inflate(inflater, R.layout.users,container,false)

        ctx = requireActivity()




        binding.apply {



            /** live data sign out **/
            viewModel.signOut.observe(viewLifecycleOwner,{
                if(it != null){
                    if(it){
                        val intent = Intent(activity, RegistrationActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                }
                })


            /** button sign out **/
            btnOption.setOnClickListener {
                pupUpMenu(btnOption)
            }


            /** button search **/
            btnSearch.setOnClickListener {

                val search = search.text.trim().toString().lowercase()
                viewModel.filter(search){ users ->

                    recyclerViewUsers.withModels {

                        val l = users as ArrayList<User>
                        l.remove(viewModel.currentUser())
                        l.forEach { user->
                            user{
                                id(user.id)
                                user(user)
                                clickListener { v ->
                                    findNavController().navigate(UsersDirections.actionUsersToChat(user))
                                }
                            }

                        }

                    }
                }

            }



//            /** edit text search **/
//            search.addTextChangedListener(object : TextWatcher{
//                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//
//                override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                    if (charSequence != null){
//                        val char = charSequence.toString().lowercase()
//                        viewModel.filter(char){ users ->
//
//                            recyclerViewUsers.withModels {
//
//                                val l = users as ArrayList<User>
//                                l.remove(viewModel.currentUser())
//                                l.forEach { user->
//                                    user{
//                                        id(user.id)
//                                        user(user)
//                                        clickListener { v ->
//                                            findNavController().navigate(UsersDirections.actionUsersToChat(user))
//                                        }
//                                    }
//
//                                }
//
//                            }
//                        }
//                    }
//
//                }
//
//                override fun afterTextChanged(p0: Editable?) {}
//
//            })
//
//






        }



        return binding.root
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