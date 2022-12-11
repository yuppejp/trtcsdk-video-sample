package com.example.videosamplecompose

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.videosamplecompose.ui.theme.VideoSampleComposeTheme


class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<TrtcViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()

        setContent {
            VideoSampleComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainView(viewModel)
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
                // already permitted
                continue
            } else {
                val requestPermissionsLauncher = registerForActivityResult(
                    RequestMultiplePermissions(),
                    ActivityResultCallback<Map<String?, Boolean?>> { grantResults: Map<String?, Boolean?> ->
                        if (grantResults.containsValue(false)) {
                            // denied
                            Toast.makeText(
                                applicationContext, "Please allow access permissions.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            // permitted
                            viewModel.setup(applicationContext)
                        }
                    }
                )
                requestPermissionsLauncher.launch(permissions)
                requestPermission = true
                break
            }
        }
        if (!requestPermission) {
            // permitted
            viewModel.setup(applicationContext)
        }
    }
}

@Composable
fun MainView(viewModel: TrtcViewModel, modifier: Modifier = Modifier) {
    val videoAvailable = viewModel.videoAvailable.observeAsState()
    val isFrontCamera = viewModel.isFrontCamera.observeAsState()

    Box(modifier = modifier) {
        if (videoAvailable.value == true && !viewModel.debugPreview) {
            TrtcLocalVideoView(isFrontCamera.value!!)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray)
            )
        }

        RemoteUserView(
            viewModel,
            Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        ControlPanelView(
            viewModel,
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    VideoSampleComposeTheme {
        val viewModel = TrtcViewModel(true)
        MainView(viewModel)
    }
}
