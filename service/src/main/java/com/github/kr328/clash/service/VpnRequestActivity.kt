package com.github.kr328.clash.service

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle

class VpnRequestActivity : Activity() {
    companion object {
        const val REQUEST_CODE = 124
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = VpnService.prepare(this)

        if ( intent == null )
            finish()
        else
            startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ( requestCode == REQUEST_CODE && resultCode == RESULT_OK ) {
            startService(Intent(this, TunService::class.java))
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}