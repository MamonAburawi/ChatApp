package com.chatapp.info.screens.registration

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.chatapp.info.R
import com.chatapp.info.data.User
import com.chatapp.info.databinding.RegistrationBinding
import com.chatapp.info.genUUID
import java.util.*


class Registration : Fragment() {


    private lateinit var viewModel: RegistrationViewModel
    private lateinit var binding : RegistrationBinding

    private var _email = ""
    private var _password = ""
    private var _name = ""
    private var _phone = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        viewModel = ViewModelProvider(this)[RegistrationViewModel::class.java]

        binding = DataBindingUtil.inflate(inflater, R.layout.registration,container,false)


        binding.apply {


            /** live data **/
            viewModel.inProgress.observe(viewLifecycleOwner,{ inProgress->
                if(inProgress){
                    progress.visibility = View.VISIBLE
                }else{
                    progress.visibility = View.GONE
                }
            })


            /** live data **/
            viewModel.isRegistered.observe(viewLifecycleOwner,{ isRegister->
                if(isRegister != null){ // user data
                    findNavController().navigate(R.id.action_registration_to_login)
                }
            })



            /** button **/
            btnIHaveAnAccount.setOnClickListener {
                findNavController().navigate(R.id.action_registration_to_login)
            }


            /** button **/
            btnRegister.setOnClickListener {

                _email = email.text.trim().toString()
                _password = password.text.trim().toString()
                _name = name.text.trim().toString()
                _phone = phone.text.trim().toString()


                if (_email.isEmpty()){
                    name.error = "please enter your name!"
                    name.requestFocus()
                    return@setOnClickListener
                }
                if (_email.isEmpty()){
                    email.error = "please enter your email!"
                    email.requestFocus()
                    return@setOnClickListener
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(_email).matches()){
                    email.error = "please enter a valid email"
                    email.requestFocus()
                    return@setOnClickListener
                }

                if(_password.isEmpty()){
                    password.error = "please enter your password!"
                    password.requestFocus()
                    return@setOnClickListener
                }

                if (_password.length < 6){
                    password.error = "6 char required!"
                    password.requestFocus()
                    return@setOnClickListener
                }

//                if (_phone.length < 9){
//                    phone.error = "9 char required!"
//                    phone.requestFocus()
//                    return@setOnClickListener
//                }
//
//                if (_phone.toInt() < 6){
//                    password.error = "6 char required!"
//                    password.requestFocus()
//                    return@setOnClickListener
//                }

                val userData = User(
                    genUUID(),
                    _name,
                    _email,
                    _password,
                    Calendar.getInstance().time,
                    0,
                    Calendar.getInstance().time)

                viewModel.registration(userData)
            }




        }



        return binding.root
    }


}