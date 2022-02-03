package com.chatapp.info.screens.login

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.chatapp.info.MainActivity
import com.chatapp.info.R
import com.chatapp.info.databinding.LoginBinding


class Login : Fragment() {

    companion object{
        const val TAG = "Login"
    }

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: LoginBinding

    private var e: String = ""
    private var p: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.login,container,false)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]


        binding.apply {

            binding.viewModelBinding = viewModel
            binding.lifecycleOwner = this@Login


            /** live data **/
            viewModel.isLogin.observe(viewLifecycleOwner, { isLogin ->
                if(isLogin){
                    val intent = Intent(activity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
            })


            /** live data **/
            viewModel.inProgress.observe(viewLifecycleOwner, Observer { inProgress->
                if(inProgress){
                    progress.visibility = View.VISIBLE
                }else{
                    progress.visibility = View.GONE
                }
            })

//            /** live data **/
//            viewModel.error.observe(viewLifecycleOwner, Observer { error->
//                if(error != null){
//                    tvVerifyWarning.text = error
//                    tvVerifyWarning.visibility = View.VISIBLE
//                }else{
//                    tvVerifyWarning.text = ""
//                    tvVerifyWarning.visibility = View.GONE
//                }
//            })
//


            /** button **/
            btnLogin.setOnClickListener {
                e = email.text.trim().toString()
                p = password.text.trim().toString()

                if (e.isEmpty()){
                    email.error = "please enter your email!"
                    email.requestFocus()
                    return@setOnClickListener
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()){
                    email.error = "please enter a valid email"
                    email.requestFocus()
                    return@setOnClickListener
                }

                if(p.isEmpty()){
                    password.error = "please enter your password!"
                    password.requestFocus()
                    return@setOnClickListener
                }

                if (p.length < 6){
                    password.error = "6 char required!"
                    password.requestFocus()
                    return@setOnClickListener
                }

                viewModel.login(e,p)
            }


            /** button **/
            btnNewAccount.setOnClickListener{
                findNavController().navigate(R.id.action_login_to_registration)
            }


        }

        return binding.root
    }


//    private fun initData() {
//        try {
//            val user = navArgs<LoginArgs>().value.userData
//            binding.email.setText(user.email)
//            binding.password.setText(user.password)
//            if (user != null){
//                binding.tvVerifyWarning.visibility = View.VISIBLE
//            }
//        }catch (ex: InvocationTargetException){
//            val error = ex.targetException.message.toString()
//            binding.tvVerifyWarning.visibility = View.GONE
//            Log.e(TAG,error)
//        }
//    }



}