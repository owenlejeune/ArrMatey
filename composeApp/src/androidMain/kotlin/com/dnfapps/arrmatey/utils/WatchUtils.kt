package com.dnfapps.arrmatey.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import com.dnfapps.arrmatey.seerr.state.MediaProvider
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.MokoStrings

fun handleWatchClick(
    url: String,
    provider: MediaProvider,
    context: Context,
    moko: MokoStrings
) {
    when (provider) {
        MediaProvider.Plex,
        MediaProvider.Jellyfin -> {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, moko.getString(MR.strings.no_app_found), Toast.LENGTH_SHORT).show()
            }
        }

        MediaProvider.None -> {
            Toast.makeText(context, moko.getString(MR.strings.no_app_found), Toast.LENGTH_SHORT).show()
        }
    }
}
