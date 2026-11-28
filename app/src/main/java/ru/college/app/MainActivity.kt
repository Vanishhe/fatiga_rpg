package ru.college.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.college.app.navigation.AppNavigation
import ru.college.app.ui.theme.FatigaTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val container = (application as App).container
            val language by container.sessionManager.language.collectAsStateWithLifecycle()
            val baseContext = LocalContext.current

            // Overriding LocalContext is enough: every stringResource() reads
            // resources from it, so the whole tree re-localizes instantly
            // without recreating the Activity or losing navigation state.
            val localizedContext = remember(language, baseContext) {
                val configuration = Configuration(baseContext.resources.configuration)
                configuration.setLocale(Locale.forLanguageTag(language))
                baseContext.createConfigurationContext(configuration)
            }

            CompositionLocalProvider(LocalContext provides localizedContext) {
                FatigaTheme {
                    AppNavigation()
                }
            }
        }
    }
}
