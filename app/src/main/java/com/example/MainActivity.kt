package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.screens.CharuMainScreen
import com.example.ui.theme.CharuDarkBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CharuViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: CharuViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = CharuDarkBg
        ) {
          CharuMainScreen(viewModel = viewModel)
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.refreshDeviceState()
    viewModel.loadInstalledApps()
  }
}

