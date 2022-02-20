package com.chatapp.info

import android.text.format.DateFormat
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
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
