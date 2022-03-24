package com.chatapp.info.screens.users

import androidx.core.os.bundleOf
import com.airbnb.epoxy.TypedEpoxyController
import com.chatapp.info.data.User
import com.chatapp.info.user
import com.chatapp.info.utils.KEY_RECIPIENT

class UserController(val onClickListener: UserClickListener) : TypedEpoxyController<List<User>>() {


    override fun buildModels(data: List<User>?) {
        data?.forEachIndexed { index, user ->
            user {
                id(index)
                user(user)
                clickListener { v ->
                    val data = bundleOf(KEY_RECIPIENT to user)
                    onClickListener.onClick(user)
                }
            }
        }
    }


    class UserClickListener(val clickListener: (user: User) -> Unit) {
        fun onClick(user: User) = clickListener(user)
    }

}