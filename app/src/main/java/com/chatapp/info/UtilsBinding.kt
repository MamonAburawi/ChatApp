package com.chatapp.info

import android.net.Uri
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chatapp.info.databinding.ItemImageBinding
import java.util.*

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


