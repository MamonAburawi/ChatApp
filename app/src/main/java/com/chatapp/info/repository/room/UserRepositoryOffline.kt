//package com.chatapp.info.repository.room
//
//
//import com.chatapp.info.dao.UserDao
//import com.chatapp.info.data.User
//
//
//class UserRepositoryOffline(private val userDao: UserDao) {
//
//    suspend fun loadAllUsers() = userDao.loadAllUsers()
//
//    suspend fun insertUser(user: User){
//        userDao.insertUser(user)
//    }
//
//    suspend fun updateUser(user: User){
//        userDao.updateUser(user)
//    }
//
//    suspend fun deleteUser(user: User){
//        userDao.deleteUser(user)
//    }
//
//
//
//}