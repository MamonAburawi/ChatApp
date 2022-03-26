package com.chatapp.info.utils

import android.content.Context
import android.content.SharedPreferences
import com.chatapp.info.data.User


class ChatAppSessionManager(context: Context) {

	var userSession: SharedPreferences =
		context.getSharedPreferences("userSessionData", Context.MODE_PRIVATE)
	var editor: SharedPreferences.Editor = userSession.edit()


	fun createLoginSession(id: String, name: String, isRemOn: Boolean) {
		editor.putBoolean(IS_LOGIN, true)
		editor.putString(KEY_ID, id)
		editor.putString(KEY_NAME, name)
		editor.putBoolean(KEY_REMEMBER_ME, isRemOn)
		editor.commit()
	}

	fun update(user: User){
		editor.putString(KEY_ID, user.userId)
		editor.putString(KEY_NAME, user.name)
		editor.commit()
	}


	fun isRememberMeOn(): Boolean = userSession.getBoolean(KEY_REMEMBER_ME, false)

	fun getPhoneNumber(): String? = userSession.getString(KEY_PHONE, null)

	fun getUserDataFromSession(): HashMap<String, String?> {
		return hashMapOf(
			KEY_ID to userSession.getString(KEY_ID, null),
			KEY_NAME to userSession.getString(KEY_NAME, null),
		)
	}


	fun getUserIdFromSession(): String? = userSession.getString(KEY_ID, "")

	fun isLoggedIn(): Boolean = userSession.getBoolean(IS_LOGIN, false)

	fun logoutFromSession() {
		editor.clear()
		editor.commit()
	}

	companion object {
		private const val IS_LOGIN = "isLoggedIn"
		private const val KEY_NAME = "userName"
		private const val KEY_PHONE = "userMobile"
		private const val KEY_ID = "userId"
		private const val KEY_REMEMBER_ME = "isRemOn"
		private const val KEY_FAV_PRODUCTS_IDs = "favoriteProductsIds"
	}
}