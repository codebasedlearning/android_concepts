// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.composable_examples

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.fh_aachen.android.composable_examples.ui.theme.MyAppTheme

/*
The MainActivity of this application demonstrates various ways to arrange and display
UI elements using Jetpack Compose. It showcases different layout components like Row, Column,
Box, and LazyRow, along with modifiers for styling and interaction.

Refs.:
  - https://developer.android.com/jetpack/compose/components
 */

const val TAG = "MAIN"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MyAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    fun MainScreen(modifier: Modifier = Modifier) {
        Column(modifier) {
            // all in one...
            Row {
                Text("This")
                Text("is")
                Text("Text")
            }
            // or as a Composable
            SimpleRow()
            RowWithModifiers()
            RowAndBoxWithAlignments()
            CrowdedRow()
            ScrollableRow()
            RowWithImage()
        }
    }

    @Composable
    fun SimpleRow() {
        Row {
            Text("This")
            Text("is")
            Text("Text")
        }
    }

    @Composable
    fun RowWithModifiers() {
        Row(modifier = Modifier
            .padding(all = 14.dp)
            .background(Color.Gray)
        ) {
            Text("This")
            Text("is", modifier = Modifier.padding(all = 4.dp))
            Text("Text")
        }
    }

    @Composable
    fun RowAndBoxWithAlignments() {
        Row(modifier = Modifier
            .background(Color.Cyan)
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("This")
            }
            Box(modifier = Modifier
                .weight(3f)
                .background(Color.Magenta),
                contentAlignment = Alignment.Center
            ) {
                Text("is")
            }
            Box(modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text("Text")
            }
        }
    }

    @Composable
    fun CrowdedRow() {
        Row(modifier = Modifier
            .background(Color.Gray)
            .clickable { Log.v("main", "clicked1") }
        ) {
            for (i in 1..15)
                Text("Text-$i ")
        }
    }

    @Composable
    fun ScrollableRow() {
        LazyRow  {
            items((1..15).toList()) { i ->
                Text("Text-$i ")
            }
        }
    }

    @Composable
    fun RowWithImage() {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.beach),
                contentDescription = null,
                modifier = Modifier
                    .size(75.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { Log.v(TAG, "Image clicked") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Beach")
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                modifier = Modifier.size(width = 75.dp, height = 75.dp).clip(CircleShape),
                onClick = { Log.v(TAG, "Icon clicked") }
            ) {
                Image(
                    painter = painterResource(R.drawable.beach),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}
