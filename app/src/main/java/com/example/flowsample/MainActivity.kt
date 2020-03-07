package com.example.flowsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.RuntimeException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var job = startCollect()

        button.setOnClickListener {
            Log.d("onClick:", "isActive?: ${job.isActive}, isCancelled?: ${job.isCancelled}")
            if (job.isActive) job.cancel()
            else job = startCollect()
        }
    }

    private fun startCollect(): Job {
        return lifecycleScope.launch {
            makeUnlimitedFlow()
                    .catch {
                        Toast.makeText(this@MainActivity, "failure", Toast.LENGTH_LONG).show()
                    }
                    .collect { elem ->
                        text_view.text = elem
                    }
        }
    }

    private fun makeUnlimitedFlow() = flow {
        var count = 0
        while (true) {
            emit(count)
            if (count > 0 && count % 10 == 0) throw RuntimeException()
            Log.d("flow elem:", "$count")
            count++
            delay(500)
        }
    }.map { it.toString() }
            .flowOn(Dispatchers.IO)
}
