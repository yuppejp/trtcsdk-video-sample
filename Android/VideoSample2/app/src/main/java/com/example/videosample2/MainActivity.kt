package com.example.videosample2

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.tencent.rtmp.ui.TXCloudVideoView
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef
import com.tencent.trtc.TRTCCloudDef.TRTCParams
import com.tencent.trtc.TRTCCloudListener

class MainActivity : AppCompatActivity() {
    private lateinit var trtcCloud: TRTCCloud
    private val activity = this
    private var roomId = 1
    private var userId = "User-" + getRandomString(4)
    private var joined = false
    private var audioAvailable = true
    private var videoAvailable = true
    private var isFrontCamera = true
    private var remoteUsers: MutableList<RemoteUser> = mutableListOf()
    private var maxRemoteUserCount = 3
    private lateinit var trtcLocalVideoView: TXCloudVideoView
    private lateinit var trtcRemoteVideo1: TXCloudVideoView
    private lateinit var trtcRemoteVideo2: TXCloudVideoView
    private lateinit var trtcRemoteVideo3: TXCloudVideoView
    private lateinit var remoteMicImage1: ImageView
    private lateinit var remoteMicImage2: ImageView
    private lateinit var remoteMicImage3: ImageView
    private lateinit var remoteUserIdLabel1: TextView
    private lateinit var remoteUserIdLabel2: TextView
    private lateinit var remoteUserIdLabel3: TextView
    private lateinit var userIdLabel: TextView
    private lateinit var roomNameLabel: TextView
    private lateinit var videoButton: ImageButton
    private lateinit var cameraSwitchButton: ImageButton
    private lateinit var audioButton: ImageButton
    private lateinit var joinButton: Button
    private lateinit var remoteVideos: List<TXCloudVideoView>
    private lateinit var remoteMicImages: List<ImageView>
    private lateinit var remoteUserIdLabels: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions()
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        var requestPermission = false
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED
            ) {
                // already permitted
                continue
            } else {
                val requestPermissionsLauncher = registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions(),
                    ActivityResultCallback<Map<String?, Boolean?>> { grantResults: Map<String?, Boolean?> ->
                        if (grantResults.containsValue(false)) {
                            // denied
                            Toast.makeText(
                                applicationContext, "Please allow access permissions.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            // permitted
                            setup()
                        }
                    }
                )
                requestPermissionsLauncher.launch(permissions)
                requestPermission = true
                break
            }
        }
        if (!requestPermission) {
            setup()
        }
    }

    private fun setup() {
        setupUI()
        setupTrtc()
        updateRemoteView()
        updateControlPanelView()
    }

    private fun setupUI() {
        trtcLocalVideoView = findViewById(R.id.txcvv_main)
        trtcRemoteVideo1 = findViewById(R.id.trtcRemoteVideo1)
        trtcRemoteVideo2 = findViewById(R.id.trtcRemoteVideo2)
        trtcRemoteVideo3 = findViewById(R.id.trtcRemoteVideo3)
        remoteMicImage1 = findViewById(R.id.remoteMicImage1)
        remoteMicImage2 = findViewById(R.id.remoteMicImage2)
        remoteMicImage3 = findViewById(R.id.remoteMicImage3)
        remoteUserIdLabel1 = findViewById(R.id.remoteUserIdLabel1)
        remoteUserIdLabel2 = findViewById(R.id.remoteUserIdLabel2)
        remoteUserIdLabel3 = findViewById(R.id.remoteUserIdLabel3)
        userIdLabel = findViewById(R.id.userIdLabel)
        roomNameLabel = findViewById(R.id.roomNameLabel)
        videoButton = findViewById(R.id.videoButton)
        cameraSwitchButton = findViewById(R.id.cameraSwitchButton)
        audioButton = findViewById(R.id.audioButton)
        joinButton = findViewById(R.id.joinButton)

        remoteVideos = listOf(trtcRemoteVideo1, trtcRemoteVideo2, trtcRemoteVideo3)
        remoteMicImages = listOf(remoteMicImage1, remoteMicImage2, remoteMicImage3)
        remoteUserIdLabels = listOf(remoteUserIdLabel1, remoteUserIdLabel2, remoteUserIdLabel3)

        videoButton.setOnClickListener {
            videoAvailable = !videoAvailable
            if (videoAvailable) {
                //trtcCloud.muteLocalVideo(TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, false)
                trtcCloud.startLocalPreview(isFrontCamera, trtcLocalVideoView)
            } else {
                //trtcCloud.muteLocalVideo(TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, true)
                trtcCloud.stopLocalPreview()
            }

            val r = if (videoAvailable) R.drawable.ic_baseline_videocam_24 else R.drawable.ic_baseline_videocam_off_24
            val drawable = ResourcesCompat.getDrawable(resources, r, null)
            videoButton.setImageDrawable(drawable)
        }

        cameraSwitchButton.setOnClickListener {
            isFrontCamera = !isFrontCamera
            trtcCloud.deviceManager.switchCamera(this.isFrontCamera)
        }

        audioButton.setOnClickListener {
            audioAvailable = !audioAvailable
            trtcCloud.muteLocalAudio(!audioAvailable)

            val r = if (audioAvailable) R.drawable.ic_baseline_mic_24 else R.drawable.ic_baseline_mic_off_24
            val drawable = ResourcesCompat.getDrawable(resources, r, null)
            audioButton.setImageDrawable(drawable)
        }

        joinButton.setOnClickListener {
            if (joined) {
                exitRoom()
            } else {
                enterRoom()
            }
        }
    }

    private fun updateRemoteView() {
        // hide once
        for (remoteVideo in remoteVideos) {
            remoteVideo.visibility = View.GONE
        }

        for ((i, user) in remoteUsers.withIndex()) {
            if (i >= maxRemoteUserCount) {
                break // TODO: Allow flexibility in number of participants
            }
            user.videoView = remoteVideos[i]
            user.micImage = remoteMicImages[i]
            user.userIdLabel = remoteUserIdLabels[i]
            user.userIdLabel?.text = user.userId
            user.updateRemoteVideo(user.videoAvailable)
            user.videoView?.visibility = View.VISIBLE
        }
    }

    private fun updateControlPanelView() {
        userIdLabel.text = userId
        roomNameLabel.text = "Room: #$roomId (+${remoteUsers.count()} Joined)"

        if (joined) {
            joinButton.text = "Leave"
            joinButton.setBackgroundColor(Color.RED)
        } else {
            joinButton.text = "Join"
            joinButton.setBackgroundColor(Color.GREEN)
        }
    }

    private fun setupTrtc() {
        trtcCloud = TRTCCloud.sharedInstance(applicationContext)
        trtcCloud.setListener(TRTCCloudImplListener())
        trtcCloud.startLocalPreview(isFrontCamera, trtcLocalVideoView)
    }

    private fun enterRoom() {
        // workaround: stop the video once
        videoAvailable = false
        trtcCloud.stopLocalPreview()

        val sdkSecret = TrtcUserSig()
        val trtcParams = TRTCParams()
        trtcParams.sdkAppId = sdkSecret.SDKAPPID
        trtcParams.userId = userId
        trtcParams.roomId = roomId
        trtcParams.userSig = sdkSecret.genTestUserSig(trtcParams.userId)
        trtcCloud.startLocalAudio(TRTCCloudDef.TRTC_AUDIO_QUALITY_SPEECH)
        trtcCloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL)
    }

    private fun exitRoom() {
        trtcCloud.stopLocalAudio()
        trtcCloud.stopLocalPreview()
        trtcCloud.exitRoom()
    }

    private inner class TRTCCloudImplListener : TRTCCloudListener() {
        private val logTag = "MyDebug#TRTCCloudImplListener"

        override fun onEnterRoom(result: Long) {
            Log.d(logTag, "onEnterRoom result: $result")
            joined = true
            videoAvailable = true
            trtcCloud.startLocalPreview(isFrontCamera, trtcLocalVideoView)
            updateControlPanelView()
        }

        override fun onExitRoom(reason: Int) {
            Log.d(logTag, "onExitRoom reason: $reason")
            joined = false
            updateControlPanelView()
        }

        override fun onRemoteUserEnterRoom(userId: String?) {
            Log.d(logTag, "onRemoteUserEnterRoom userId: $userId")
            if (userId != null) {
                val user = RemoteUser(userId, trtcCloud, activity)
                remoteUsers.add(user)
                updateRemoteView()
                updateControlPanelView()

            }
        }

        override fun onRemoteUserLeaveRoom(userId: String?, reason: Int) {
            Log.d(logTag, "onRemoteUserLeaveRoom userId: $userId, reason: $reason")
            if (userId != null) {
                val user = findRemoteUser(userId)
                if (user != null) {
                    remoteUsers.remove(user)
                    updateRemoteView()
                    updateControlPanelView()
                }
            }
        }

        override fun onUserAudioAvailable(userId: String?, available: Boolean) {
            Log.d(logTag, "onUserAudioAvailable userId: $userId, available: $available")
            val user = findRemoteUser(userId)
            user?.updateRemoteMic(available)
        }

        override fun onUserVideoAvailable(userId: String, available: Boolean) {
            Log.d(logTag, "onUserVideoAvailable userId: $userId, available: $available")
            val user = findRemoteUser(userId)
            user?.updateRemoteVideo(available)
        }

        override fun onError(errCode: Int, errMsg: String, extraInfo: Bundle) {
            Log.d(logTag, "errCode errMsg: $errMsg")
        }
    }

    private fun findRemoteUser(userId: String?): RemoteUser? {
        if (userId != null) {
            val users = remoteUsers.filter { user ->
                user.userId == userId
            }
            if (users.isNotEmpty()) {
                return users[0]
            }
        }
        return null
    }

    private fun getRandomString(length: Int): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }
}
