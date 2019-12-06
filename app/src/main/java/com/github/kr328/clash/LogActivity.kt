package com.github.kr328.clash

import android.os.Bundle
import android.os.Handler
import androidx.collection.CircularArray
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kr328.clash.adapter.LogAdapter
import com.github.kr328.clash.core.event.Event
import com.github.kr328.clash.core.event.LogEvent
import kotlinx.android.synthetic.main.activity_logs.*


class LogActivity : BaseActivity() {
    companion object {
        const val MAX_EVENT_COUNT = 100
    }

    private var syncLog = false
    private val buffer = CircularArray<LogEvent>(MAX_EVENT_COUNT)
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        setSupportActionBar(activity_logs_toolbar)

        activity_logs_list.also {
            val dividerItemDecoration = DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
            it.addItemDecoration(dividerItemDecoration)

            it.adapter = LogAdapter(this, buffer)
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun onStart() {
        super.onStart()

        syncLog = true

        activity_logs_list.adapter?.notifyDataSetChanged()

        runClash {
            it.eventService.registerEventObserver(LogActivity::class.java.name,
                this,
                intArrayOf(Event.EVENT_LOG))
        }
    }

    override fun onStop() {
        super.onStop()

        syncLog = false
    }

    override fun onDestroy() {
        super.onDestroy()

        runClash {
            it.eventService.unregisterEventObserver(LogActivity::class.java.name)
        }
    }

    override fun onLogEvent(event: LogEvent?) {
        handler.post {
            buffer.addFirst(event)

            if ( syncLog ) {
                activity_logs_list.adapter!!.notifyItemInserted(0)
                if ( activity_logs_list.computeVerticalScrollOffset() < 30 )
                    activity_logs_list.scrollToPosition(0)
            }

            if ( buffer.size() >= MAX_EVENT_COUNT ) {
                buffer.removeFromEnd(buffer.size() - MAX_EVENT_COUNT - 1)

                if ( syncLog ) {
                    activity_logs_list.adapter?.notifyItemRangeRemoved(MAX_EVENT_COUNT - 1,
                        buffer.size() - MAX_EVENT_COUNT - 1 )
                }
            }
        }
    }
}