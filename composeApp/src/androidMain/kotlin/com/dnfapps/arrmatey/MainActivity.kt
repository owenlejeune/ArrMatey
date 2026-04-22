package com.dnfapps.arrmatey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dnfapps.arrmatey.arr.service.ActivityQueueService
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        get<InstanceManager>().cleanup()
        get<ActivityQueueService>().cleanup()
    }
}