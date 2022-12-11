package com.example.videosamplecompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.videosamplecompose.ui.theme.VideoSampleComposeTheme

@Composable
fun ControlPanelView(viewModel: TrtcViewModel, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.3f))
            .padding(12.dp)
    ) {
        Column {
            StatusView(viewModel)
            ControlsView(viewModel)
            ErrorView(viewModel)
        }
    }
}

@Composable
fun StatusView(viewModel: TrtcViewModel, modifier: Modifier = Modifier) {
    val userId = viewModel.userId.observeAsState()
    val roomId = viewModel.roomId.observeAsState()
    val remoteUsers = viewModel.remoteUsers.observeAsState()

    Row(modifier) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = "Person"
        )
        //Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(userId.value!!)
        Box(Modifier.weight(1f))
        Text("Room: #${roomId.value} (+${remoteUsers.value?.count()} Joined)")
    }
}

@Composable
fun ControlsView(viewModel: TrtcViewModel, modifier: Modifier = Modifier) {
    val joined = viewModel.joined.observeAsState()
    val audioAvailable = viewModel.audioAvailable.observeAsState()
    val videoAvailable = viewModel.videoAvailable.observeAsState()

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f))

        IconButton(modifier = Modifier.padding(0.dp), onClick = {
            if (videoAvailable.value == true) {
                viewModel.muteLocalVideo(true)
            } else {
                viewModel.muteLocalVideo(false)
            }
        }) {
            if (videoAvailable.value == true) {
                Icon(Icons.Filled.Videocam, contentDescription = "Videocam")
            } else {
                Icon(Icons.Filled.VideocamOff, contentDescription = "VideocamOff")
            }
        }

        IconButton(modifier = Modifier.padding(0.dp), onClick = {
            viewModel.switchCamera()
        }) {
            Icon(Icons.Default.Cameraswitch, contentDescription = "switchCamera")
        }

        IconButton(onClick = {
            if (audioAvailable.value == true) {
                viewModel.muteLocalAudio(true)
            } else {
                viewModel.muteLocalAudio(false)
            }
        }) {
            if (audioAvailable.value == true) {
                Icon(Icons.Filled.Mic, contentDescription = "Mic")
            } else {
                Icon(Icons.Filled.MicOff, contentDescription = "MicOff")
            }
        }

        Button(
            onClick = {
                if (joined.value == true) {
                    viewModel.leave()
                } else {
                    viewModel.join(1)
                }
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (joined.value == true) Color.Red else Color.Green,
                Color.White
            )
        ) {
            if (joined.value == true) {
                Icon(imageVector = Icons.Filled.CallEnd, contentDescription = "Call")
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Leave")
            } else {
                Icon(imageVector = Icons.Filled.Call, contentDescription = "Call")
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Join")
            }
        }
    }
}

@Composable
fun ErrorView(viewModel: TrtcViewModel, modifier: Modifier = Modifier) {
    Row(modifier) {
        if (viewModel.errorCode.value != 0) {
            val text = "${viewModel.errorMessage.value}(${viewModel.errorCode.value})"
            Text(text)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ControlePaneViewPreview() {
    VideoSampleComposeTheme {
        val viewModel = TrtcViewModel(true)

        Box(Modifier
            .size(600.dp, 800.dp)
            .background(Color.Gray)
        ) {
            ControlPanelView(
                viewModel,
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}
