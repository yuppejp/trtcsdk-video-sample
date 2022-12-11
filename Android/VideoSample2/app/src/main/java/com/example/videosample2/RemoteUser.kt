package com.example.videosample2

import android.app.Activity
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.tencent.rtmp.ui.TXCloudVideoView
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef

class RemoteUser(
    var userId: String,
    private var trtcCloud: TRTCCloud,
    private var activity: Activity
) {
    var videoView: TXCloudVideoView? = null
    var micImage: ImageView? = null
    var userIdLabel: TextView? = null
    var videoAvailable: Boolean = false

    fun updateRemoteMic(available: Boolean) {
        val r = if (available) R.drawable.ic_baseline_mic_24 else R.drawable.ic_baseline_mic_off_24
        val drawable = ResourcesCompat.getDrawable(activity.resources, r, null)
        micImage?.setImageDrawable(drawable)
    }

    fun updateRemoteVideo(available: Boolean) {
        videoAvailable = available
        if (videoView != null) {
            if (available) {
                trtcCloud.startRemoteView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, videoView)
                //videoView.setBackgroundColor(0x000000); // disable transparency
            } else {
                trtcCloud.stopRemoteView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG)
                //videoView.setBackgroundColor(0x3f000000); // transparency
            }
        }
    }
}


