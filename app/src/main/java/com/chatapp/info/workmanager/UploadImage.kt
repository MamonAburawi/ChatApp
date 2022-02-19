package com.chatapp.info.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class UploadImage(context : Context, paramsWorker: WorkerParameters) : CoroutineWorker(context,paramsWorker) {
    override suspend fun doWork(): Result {



        return Result.success()
    }
}