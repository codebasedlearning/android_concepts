// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.xml_based_app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.buttonPress).setOnClickListener {
            Toast.makeText(this@MainActivity, "First program!", Toast.LENGTH_SHORT).show()
            Log.i("Main", getString(R.string.logcat_text))   // i = info; e (error) only for errors
        }
    }
}

/*
The Old Way: Imperative UI (e.g. XML + findViewById)
The framework held a big mutable view tree; your job was to keep it in sync with the app state:
  - Enable/disable widgets
  - Show/hide progress bars
  - Update lists manually
All that “UI bookkeeping” led to tons of boilerplate and bugs like "why didn’t the button update?"

The New Way: Declarative UI (Jetpack Compose, SwiftUI, etc.)
  - Less error-prone: fewer places for stale UI state.
  - Faster iteration: you can prototype UIs entirely in Kotlin.
  - Easier testing: composables are just functions.
  - Consistent look: Material 3 theming is unified and dynamic (supports Android 12+ color extraction).

Edge-to-edge
  Since targetSdk 35 Android draws every app edge-to-edge, i.e. behind the status and navigation
  bars, XML apps included; from targetSdk 36 on there is no opt-out anymore. In the View world you
  handle that with ViewCompat.setOnApplyWindowInsetsListener; in Compose, Scaffold passes the
  insets as 'innerPadding'. Here the content is centered, so nothing gets covered.
 */
