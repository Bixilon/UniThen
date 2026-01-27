package de.bixilon.unithen.ui.main.add

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.ui.auth.AuthenticationScreen


fun ByteArray.toBitmap() = BitmapFactory.decodeByteArray(this, 0, this.size)

@Composable
private fun SiteList(sites: List<Site>, callback: (Site) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = sites,
            key = Site::id
        ) { site ->
            Card(modifier = Modifier.fillMaxWidth(), onClick = { callback.invoke(site) }) {
                val bitmap = remember { site.icon?.toBitmap()?.asImageBitmap() }
                if (bitmap != null) {
                    Image(bitmap = bitmap, contentDescription = "icon")
                }
                Text(text = site.name)
                Text(text = site.url.toString())
            }
        }
    }
}

@Composable
fun SelectSiteSetupScreen(callback: (Site) -> Unit = {}) {
    val sites by remember { DataStorage.STORAGE.sites.stateOf { all() } }

    if (sites.isEmpty()) {
        AddSiteDialog(callback)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("Please select a site where you booked your courses")

        SiteList(sites, callback)
        AddSiteButton(callback)
    }
}


@Composable
fun AddAccountScreen(callback: () -> Unit) {
    var site: Site? by remember { mutableStateOf(null) }

    BackHandler(site != null) { site = null }

    site?.let {
        AuthenticationScreen(it) { callback.invoke() }
        return
    }

    SelectSiteSetupScreen { site = it }
}
