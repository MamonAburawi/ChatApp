//package com.chatapp.info.utils
//
//import android.graphics.drawable.Drawable
//import android.widget.ImageView
//
//import com.bumptech.glide.load.engine.GlideException
//
//import com.bumptech.glide.Glide
//
//import com.bumptech.glide.request.RequestOptions
//
//import android.widget.ProgressBar
//
//
//class GlideImageLoader(imageView: ImageView?, progressBar: ProgressBar?) {
//    private val mImageView: ImageView? = imageView
//    private val mProgressBar: ProgressBar? = progressBar
//
//    fun load(url: String?, options: RequestOptions?) {
//        if (url == null || options == null) return
//        onConnecting()
//
//        //set Listener & start
//        ProgressAppGlideModule.expect(url, object : UIonProgressListener() {
//            fun onProgress(bytesRead: Long, expectedLength: Long) {
//                if (mProgressBar != null) {
//                    mProgressBar.progress = (100 * bytesRead / expectedLength).toInt()
//                }
//            }
//
//            val granualityPercentage: Float
//                get() = 1.0f
//        })
//        //Get Image
//        Glide.with(mImageView.getContext())
//            .load(url)
//            .transition(withCrossFade())
//            .apply(options.skipMemoryCache(true))
//            .listener(object : RequestListener<Drawable?>() {
//                fun onLoadFailed(
//                    @Nullable e: GlideException?,
//                    model: Any?,
//                    target: Target<Drawable?>?,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    ProgressAppGlideModule.forget(url)
//                    onFinished()
//                    return false
//                }
//
//                fun onResourceReady(
//                    resource: Drawable?,
//                    model: Any?,
//                    target: Target<Drawable?>?,
//                    dataSource: DataSource?,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    ProgressAppGlideModule.forget(url)
//                    onFinished()
//                    return false
//                }
//            })
//            .into<com.bumptech.glide.request.target.Target<Drawable>>(mImageView)
//    }
//
//    private fun onConnecting() {
//        if (mProgressBar != null) mProgressBar.visibility = View.VISIBLE
//    }
//
//    private fun onFinished() {
//        if (mProgressBar != null && mImageView != null) {
//            mProgressBar.visibility = View.GONE
//            mImageView.setVisibility(View.VISIBLE)
//        }
//    }
//
//}