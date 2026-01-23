package de.bixilon.unithen.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import de.bixilon.unithen.ui.navigation.NavigationStack
import de.bixilon.unithen.ui.theme.UniThenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniThenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    NavigationStack()
                }
            }
        }
    }
}
