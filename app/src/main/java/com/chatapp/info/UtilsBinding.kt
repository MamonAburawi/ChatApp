package com.chatapp.info

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioRecord.MetricsConstants.SOURCE
import android.media.Image
import android.net.Uri
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestOptions
import com.chatapp.info.data.Message
import com.chatapp.info.databinding.ItemSenderImageBinding
import com.google.firebase.analytics.FirebaseAnalytics.Param.SOURCE
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.util.*

@SuppressLint("StaticFieldLeak")
var utilContext: Context? = null


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


@DelicateCoroutinesApi
@BindingAdapter("setImage")
fun setImage(imageView: ImageView,message: Message){

    GlobalScope.launch(Dispatchers.Main) {
        FirebaseStorage.getInstance().reference.child("images/${message.chatId}/${message.imageId}")
            .downloadUrl.addOnSuccessListener {


                Log.i("images","image: $it")

//                val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)


                Glide.with(utilContext!!)
                    .load(it)




//                    .centerInside()
//                    .fitCenter()
                    .placeholder(R.drawable.ic_image)
                    .into(imageView)

            }

    }



}



//@DelicateCoroutinesApi
//@BindingAdapter("setImage")
//fun setImage(imageView: ImageView,uri: Uri){
//    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
//    Glide.with(utilContext!!)
//        .load(uri)
//        .apply(requestOptions)
//        .into(imageView)
//
//}
//
