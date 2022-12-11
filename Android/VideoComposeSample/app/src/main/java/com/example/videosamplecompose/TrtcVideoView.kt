package com.example.videosamplecompose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.tencent.liteav.base.ContextUtils.getApplicationContext
import com.tencent.rtmp.ui.TXCloudVideoView
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef

@Composable
fun TrtcLocalVideoView(isFrontCamera: Boolean, modifier: Modifier = Modifier) {
    val trtcCloud = TRTCCloud.sharedInstance(getApplicationContext())
    AndroidView(
        factory = ::TXCloudVideoView,
        update = { view ->
            trtcCloud.startLocalPreview(isFrontCamera, view)
        },
        modifier = modifier
    )
}

@Composable
fun TrtcRemoteVideoView(remoteUid: String, modifier: Modifier = Modifier) {
    val trtcCloud = TRTCCloud.sharedInstance(getApplicationContext())
    AndroidView(
        factory = ::TXCloudVideoView,
        update = { view ->
            trtcCloud.startRemoteView(remoteUid, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, view)
        },
        modifier = modifier
    )
}
