package com.example.flowsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var flowJob = startCollectFlow()
        flowButton.setOnClickListener {
            if (flowJob.isActive) flowJob.cancel()
            else flowJob = startCollectFlow()
        }

        var channelJob = startCollectChannel()
        offerUnlimitedChannel() // channel job executed independently.
        channelButton.setOnClickListener {
            if (channelJob.isActive) channelJob.cancel()
            else channelJob = startCollectChannel()
        }

    }

    /*---------flow-----------*/
    private fun startCollectFlow(): Job {
        return lifecycleScope.launch {
            unlimitedFlow
                .catch {
                    Toast.makeText(this@MainActivity, "failure", Toast.LENGTH_LONG).show()
                }
                .collect { elem ->
                    flowConsumerText.text = "flow consume: $elem"
                }
        }
    }

    private val unlimitedFlow  = flow {
        var count = 0
        while (true) {
            emit(count)
//            if (count > 0 && count % 10 == 0) throw RuntimeException() // check error handling
            Log.d("flow elem:", "$count")
            count++
            delay(500)
        }
    }.flowOn(Dispatchers.IO)
        .map {
            flowProducerText.text = "flow produce: $it"
            it
        }
        .flowOn(Dispatchers.Main)

    /*---------flow-----------*/



    /*---------channel-----------*/
    private fun startCollectChannel(): Job {
        return lifecycleScope.launch {
            channel.asFlow()
                .catch {
                    Toast.makeText(this@MainActivity, "failure", Toast.LENGTH_LONG).show()
                }
                .collect { elem ->
                    channelConsumerText.text = "channel consume: $elem"
                }
        }
    }

    private val channel = ConflatedBroadcastChannel<Int>()
    private fun offerUnlimitedChannel() {
        lifecycleScope.launch(Dispatchers.Default) {
            var count = 0
            while (true) {
                channel.offer(count)
                count++
                withContext(Dispatchers.Main) {
                    channelProducerText.text = "channel produce: $count"
                }
                Log.d("channel elem", "$count")
                delay(500L)
            }
        }
    }
    /*---------channel-----------*/
}
