package com.github.kr328.clash

import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.github.kr328.clash.core.event.*
import com.github.kr328.clash.service.IClashEventObserver

abstract class BaseActivity : AppCompatActivity(), IClashEventObserver {
    private val observerBinder = object: IClashEventObserver.Stub() {
        override fun onLogEvent(event: LogEvent?) {
            this@BaseActivity.onLogEvent(event)
        }

        override fun onProcessEvent(event: ProcessEvent?) {
            this@BaseActivity.onProcessEvent(event)
        }

        override fun onProxyChangedEvent(event: ProxyChangedEvent?) {
            this@BaseActivity.onProxyChangedEvent(event)
        }

        override fun onErrorEvent(event: ErrorEvent?) {
            this@BaseActivity.onErrorEvent(event)
        }

        override fun onTrafficEvent(event: TrafficEvent?) {
            this@BaseActivity.onTrafficEvent(event)
        }

        override fun onProfileChanged(event: ProfileChangedEvent?) {
            this@BaseActivity.onProfileChanged(event)
        }
    }

    override fun onLogEvent(event: LogEvent?) {

    }

    override fun onErrorEvent(event: ErrorEvent?) {

    }

    override fun onProfileChanged(event: ProfileChangedEvent?) {

    }

    override fun onProcessEvent(event: ProcessEvent?) {

    }

    override fun onProxyChangedEvent(event: ProxyChangedEvent?) {

    }

    override fun onTrafficEvent(event: TrafficEvent?) {

    }

    override fun asBinder(): IBinder {
        return observerBinder
    }
}