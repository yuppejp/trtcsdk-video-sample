package com.example.videosamplecompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.videosamplecompose.ui.theme.VideoSampleComposeTheme

@Composable
fun RemoteUserView(viewModel: TrtcViewModel, modifier: Modifier = Modifier) {
    val remoteUsers = viewModel.remoteUsers.observeAsState()

    Column(modifier) {
        remoteUsers.value?.forEach { user ->
            RemoteVideoView(user)
        }
    }
}

@Composable
fun RemoteVideoView(user: RemoteUser, modifier: Modifier = Modifier) {
    val videoAvailable = user.videoAvailable.observeAsState()
    val audioAvailable = user.audioAvailable.observeAsState()

    Box(
        modifier
            .padding(bottom = 16.dp)
            .size(100.dp, 150.dp)
            .background(Color.White.copy(alpha = 0.3f))
    ) {
        if (videoAvailable.value == true) {
            TrtcRemoteVideoView(user.userId)
        } else {
            Icon(
                imageVector = Icons.Filled.VideocamOff,
                contentDescription = "VideocamOff",
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Row(modifier = Modifier.align(Alignment.BottomStart)) {
            Icon(
                imageVector = if (audioAvailable.value == true) Icons.Outlined.Mic else Icons.Outlined.MicOff,
                contentDescription = "Mic Status"
            )
            Text(user.userId)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RemoteUserViewPreview() {
    VideoSampleComposeTheme {
        val viewModel = TrtcViewModel(true)
        Box(
            Modifier
                .size(600.dp, 800.dp)
                .background(Color.Gray)
        ) {
            RemoteUserView(
                viewModel,
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
    }
}

