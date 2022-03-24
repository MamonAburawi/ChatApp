package com.chatapp.info.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.chatapp.info.R
import com.chatapp.info.data.Message
import com.google.firebase.storage.FirebaseStorage
import gun0912.tedimagepicker.util.ToastUtil.context
import kotlinx.coroutines.*
import java.util.*
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory





//@SuppressLint("StaticFieldLeak")
//lateinit var contextx: Context

@BindingAdapter("dateToString")
fun dateToString(textView: TextView, date: Date){
    textView.text = DateFormat.format("h:mm a", date).toString()
}


@BindingAdapter("isMessageSent")
fun isMessageSent(progress: ProgressBar,isSending: Boolean?){
    if (isSending != null){
        if (isSending){
            progress.visibility = View.VISIBLE
        }else{
            progress.visibility = View.GONE
        }
    }else{
        progress.visibility = View.GONE
    }
}


@BindingAdapter("setImage")
fun setImage(imageView: ImageView,imgUrl: Uri){

    val options = RequestOptions()
        .placeholder(R.drawable.progress_animator)
//        .error(com.chatapp.info.R.drawable.user_image)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .priority(Priority.HIGH)
        .dontAnimate()
        .dontTransform()

    val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
    if (imgUrl.toString().contains("https://")) {
        Glide.with(context)
            .asBitmap()
            .transition(withCrossFade(factory))
            .apply(options)
            .load(imgUrl.buildUpon().scheme("https").build())
            .into(imageView)
        Log.d("utils","contain https://")
    } else {
        imageView.setImageURI(imgUrl)
    }
}




