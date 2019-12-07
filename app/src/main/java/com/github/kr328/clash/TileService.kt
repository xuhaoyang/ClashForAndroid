package com.github.kr328.clash

import android.content.*
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.github.kr328.clash.core.event.ProcessEvent
import com.github.kr328.clash.service.ClashService
import com.github.kr328.clash.service.Constants
import com.github.kr328.clash.service.IClashService
import com.github.kr328.clash.utils.ServiceUtils

class TileService : TileService() {
    override fun onClick() {
        val tile = qsTile

        when ( tile.state ) {
            Tile.STATE_INACTIVE -> {
                ServiceUtils.startProxyService(this)
            }
            Tile.STATE_ACTIVE -> {
                val binder = clashStatusReceiver.peekService(this, Intent(this, ClashService::class.java))

                runCatching {
                    val clash = IClashService.Stub.asInterface(binder)

                    clash?.stop()
                }
            }
        }
    }

    override fun onStartListening() {
        refreshTileStatus()
    }

    private fun refreshTileStatus() {
        when ( currentStatus ) {
            ProcessEvent.STARTED -> {
                qsTile.state = Tile.STATE_ACTIVE
            }
            ProcessEvent.STOPPED -> {
                qsTile.state = Tile.STATE_INACTIVE
            }
        }

        qsTile.updateTile()
    }

    private val clashStatusReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshTileStatus()
        }
    }

    override fun onCreate() {
        super.onCreate()

        registerReceiver(clashStatusReceiver,
            IntentFilter(Constants.CLASH_PROCESS_BROADCAST_ACTION))
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(clashStatusReceiver)
    }

    private val currentStatus: ProcessEvent
    get() {
        val service = clashStatusReceiver.peekService(this, Intent(this, ClashService::class.java))

        return runCatching {
            IClashService.Stub.asInterface(service)?.currentProcessStatus
        }.getOrNull() ?: ProcessEvent.STOPPED
    }
}