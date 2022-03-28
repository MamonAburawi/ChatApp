package com.chatapp.info.screens.account

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.chatapp.info.R
import com.chatapp.info.RegistrationActivity
import com.chatapp.info.databinding.AccountBinding

class Account : Fragment() {



    private lateinit var viewModel: AccountViewModel
    private lateinit var binding: AccountBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.account,container,false)
        viewModel = ViewModelProvider(this)[AccountViewModel::class.java]


        setObserves()
        setViews()



        return binding.root
    }

    private fun setViews() {
        binding.apply {

            /** button signOut **/
            btnSignOut.setOnClickListener {
                viewModel.signOut()
            }


        }
    }

    private fun setObserves() {

        /** live data signOut **/
        viewModel.signOut.observe(viewLifecycleOwner){
            if (it != null){
                if (it){
                    navigateToRegistrationActivity()
                }
            }
        }


    }


    private fun navigateToRegistrationActivity(){
        val intent = Intent(requireContext(), RegistrationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

}