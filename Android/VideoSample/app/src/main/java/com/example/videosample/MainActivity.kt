package com.example.videosample

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.tencent.rtmp.ui.TXCloudVideoView
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef
import com.tencent.trtc.TRTCCloudDef.TRTCParams
import com.tencent.trtc.TRTCCloudListener

class MainActivity : AppCompatActivity() {
    private var granted = false
    private lateinit var trtcCloud: TRTCCloud
    private var joined = false
    private lateinit var trtcLocalVideoView: TXCloudVideoView
    private lateinit var trtcRemoteVideoView: TXCloudVideoView
    private lateinit var remoteUserIdLabel: TextView
    private lateinit var joinButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions()

        trtcLocalVideoView = findViewById(R.id.txcvv_main)
        trtcRemoteVideoView = findViewById(R.id.trtcRemoteVideoView)
        remoteUserIdLabel = findViewById(R.id.remoteUserIdLabel)
        joinButton = findViewById(R.id.joinButton)

        joinButton.setOnClickListener {
            if (!granted) {
                Toast.makeText(applicationContext, "Please allow access permissions.", Toast.LENGTH_LONG).show()
            } else {
                if (joined) {
                    exitRoom()
                } else {
                    enterRoom()
                }
            }
        }
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
                continue
            } else {
                val requestPermissionsLauncher = registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions(),
                    ActivityResultCallback<Map<String?, Boolean?>> { grantResults: Map<String?, Boolean?> ->
                        if (grantResults.containsValue(false)) {
                            // denied
                            Toast.makeText(applicationContext, "Please allow access permissions.", Toast.LENGTH_LONG).show()
                        } else {
                            // permitted
                            granted = true
                        }
                    }
                )
                requestPermissionsLauncher.launch(permissions)
                requestPermission = true
                break
            }
        }
        if (!requestPermission) {
            // already permitted
            granted = true
        }
    }

    private fun enterRoom() {
        val roomId = 1
        val userId = "Android demo1"
        val isFrontCamera = true

        trtcCloud = TRTCCloud.sharedInstance(applicationContext)
        trtcCloud.setListener(TRTCCloudImplListener())

        val sdkSecret = TrtcUserSig()
        val trtcParams = TRTCParams()
        trtcParams.sdkAppId = sdkSecret.SDKAPPID
        trtcParams.userId = userId
        trtcParams.roomId = roomId
        trtcParams.userSig = sdkSecret.genTestUserSig(trtcParams.userId)
        trtcCloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL)

        trtcCloud.startLocalAudio(TRTCCloudDef.TRTC_AUDIO_QUALITY_SPEECH)
        trtcCloud.startLocalPreview(isFrontCamera, trtcLocalVideoView)
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
            joinButton.text = "Leave"
            joinButton.setBackgroundColor(Color.RED)
        }

        override fun onExitRoom(reason: Int) {
            Log.d(logTag, "onExitRoom reason: $reason")
            joined = false
            joinButton.text = "Join"
            joinButton.setBackgroundColor(Color.GREEN)
        }

        override fun onUserVideoAvailable(userId: String, available: Boolean) {
            Log.d(logTag, "onUserVideoAvailable userId: $userId, available: $available")
            if (available) {
                trtcCloud.startRemoteView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, trtcRemoteVideoView)
                remoteUserIdLabel.text = userId
            } else {
                trtcCloud.stopRemoteView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG)
                remoteUserIdLabel.text = ""
            }
        }

        override fun onError(errCode: Int, errMsg: String, extraInfo: Bundle) {
            Log.d(logTag, "errCode errMsg: $errMsg")
        }
    }
}
