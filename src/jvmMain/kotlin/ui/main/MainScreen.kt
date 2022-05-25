package ui.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.StreamSource
import data.websocket.model.artistNames
import data.websocket.model.coverArt
import util.imageFromString

@Composable
@Preview
fun MainScreen(
    viewModel: MainViewModel
) {
    val currentlyPlaying by viewModel.currentlyPlaying.collectAsState(null)

    MaterialTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (currentlyPlaying != null) {
                val image by imageFromString(currentlyPlaying!!.song.coverArt)
                image?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "",
                        modifier = Modifier.size(512.dp)
                    )
                }
                Text(
                    currentlyPlaying!!.song.title,
                    style = MaterialTheme.typography.h5
                )
                Text(
                    currentlyPlaying!!.song.artistNames,
                    style = MaterialTheme.typography.subtitle1
                )
                FloatingActionButton(
                    onClick = {
                        viewModel.togglePlaying()
                    }
                ) {
                    val icon = if (viewModel.isPlaying) Icons.Filled.Close else Icons.Filled.PlayArrow
                    Icon(icon, "")
                }
                Slider(
                    value = viewModel.volume.toFloat(),
                    onValueChange = {
                        viewModel.volume = it.toInt()
                    },
                    onValueChangeFinished = {
                        viewModel.onChangeVolume()
                    },
                    valueRange = (0f..100f),
                    steps = 100
                )
                StreamSource.values().forEach {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = viewModel.source == it,
                            onClick = {
                                viewModel.setStream(it)
                            }
                        )
                        Text(it.displayName)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.start()
    }
}