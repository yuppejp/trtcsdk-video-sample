package com.example.videosamplecompose

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef
import com.tencent.trtc.TRTCCloudListener

class RemoteUser(var userId: String) : ViewModel() {
    var videoAvailable = MutableLiveData(false)
    var audioAvailable = MutableLiveData(false)
}

class TrtcViewModel(var debugPreview: Boolean = false): ViewModel()  {
    val joined = MutableLiveData(false)
    val errorCode = MutableLiveData(0)
    val errorMessage = MutableLiveData("")
    val userId = MutableLiveData("")
    val roomId = MutableLiveData(0)
    val remoteUsers = MutableLiveData<MutableList<RemoteUser>>(mutableStateListOf())
    val audioAvailable = MutableLiveData(false)
    val videoAvailable = MutableLiveData(false)
    val isFrontCamera = MutableLiveData(true)
    private lateinit var trtcCloud: TRTCCloud

    init {
        if (debugPreview) {
            userId.value = "User1"
            for (i in 1..3) {
                val id = "user$i"
                val user = RemoteUser(id)
                remoteUsers.value?.add(user)
            }
        }
    }

    fun setup(context: Context) {
        trtcCloud = TRTCCloud.sharedInstance(context)
        trtcCloud.setListener(TRTCCloudImplListener())

        userId.value = "User-" + genRandomString(4)
        videoAvailable.value = true
        audioAvailable.value = true
    }

    fun join(roomId: Int) {
        this.roomId.value = roomId

        // workaround: stop the video once
        videoAvailable.value = false
        trtcCloud.stopLocalPreview()

        val sdkSecret = TrtcUserSig()
        val trtcParams = TRTCCloudDef.TRTCParams()
        trtcParams.sdkAppId = sdkSecret.SDKAPPID
        trtcParams.userId = userId.value
        trtcParams.roomId = roomId
        trtcParams.userSig = sdkSecret.genTestUserSig(trtcParams.userId)
        //trtcParams.startLocalPreview(isFrontCamera, view)
        trtcCloud.startLocalAudio(TRTCCloudDef.TRTC_AUDIO_QUALITY_SPEECH)
        trtcCloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL)
    }

    fun leave() {
        trtcCloud.stopLocalAudio()
        //trtcCloud.stopLocalPreview()
        trtcCloud.exitRoom()
    }

    fun muteLocalVideo(mute: Boolean) {
        trtcCloud.muteLocalVideo(TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, mute)
        videoAvailable.value = !mute
    }

    fun muteLocalAudio(mute: Boolean) {
        trtcCloud.muteLocalAudio(mute)
        audioAvailable.value = !mute
    }

    fun switchCamera(isFrontCamera: Boolean? = null) {
        if (isFrontCamera == null) {
            this.isFrontCamera.value = !this.isFrontCamera.value!!
        } else {
            this.isFrontCamera.value = isFrontCamera
        }
        trtcCloud.deviceManager.switchCamera(this.isFrontCamera.value!!)
    }

    private inner class TRTCCloudImplListener : TRTCCloudListener() {
        private val logTag = "[MyDebug:TRTCCloudListener]"

        override fun onEnterRoom(result: Long) {
            Log.d(logTag, "onEnterRoom result: $result")
            joined.value = true
            videoAvailable.value = true
        }

        override fun onExitRoom(reason: Int) {
            Log.d(logTag, "onExitRoom reason: $reason")
            joined.value = false
        }

        override fun onRemoteUserEnterRoom(userId: String?) {
            Log.d(logTag, "onRemoteUserEnterRoom userId: $userId")
            if (userId != null) {
                val user = RemoteUser(userId)
                remoteUsers.value?.add(user)
            }
        }

        override fun onRemoteUserLeaveRoom(userId: String?, reason: Int) {
            Log.d(logTag, "onRemoteUserLeaveRoom userId: $userId, reason: $reason")
            val user = findRemoteUser(userId)
            if (user != null) {
                remoteUsers.value?.remove(user)
            }
        }

        override fun onUserAudioAvailable(userId: String?, available: Boolean) {
            Log.d(logTag, "onUserAudioAvailable userId: $userId, available: $available")
            val user = findRemoteUser(userId)
            if (user != null) {
                user.audioAvailable.value = available
            }
        }

        override fun onUserVideoAvailable(userId: String, available: Boolean) {
            Log.d(logTag, "onUserVideoAvailable userId: $userId, available: $available")
            val user = findRemoteUser(userId)
            if (user != null) {
                user.videoAvailable.value = available
            }
        }

        override fun onError(errCode: Int, errMsg: String, extraInfo: Bundle) {
            Log.d(logTag, "onError errCode: $errCode, errMsg: $errMsg")
        }
    }

    private fun findRemoteUser(userId: String?): RemoteUser? {
        if (userId != null) {
            val users = remoteUsers.value?.filter { user ->
                user.userId == userId
            }
            if (users != null && users.isNotEmpty()) {
                return users[0]
            }
        }
        return null
    }

    private fun genRandomString(length: Int) : String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }
}