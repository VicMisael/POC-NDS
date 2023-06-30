package com.example.poc_nearbyconnections

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.lang.Exception
import java.net.InetAddress
import java.net.Socket


class Logged : AppCompatActivity() {
    val TYPE = "_poc._tcp";
    val TAG = "PDM"
    lateinit var  nsdManager:NsdManager;
    var SERVERPORT: Int = -1;
    lateinit var text: TextView;
    var mServiceName: String = ""
    lateinit var mService: NsdServiceInfo
    var port: Int = 4499
    lateinit var host: InetAddress
    private var socket: Socket? = null
    var updateConversationHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nsdManager = (getSystemService(Context.NSD_SERVICE) as NsdManager)
        setContentView(R.layout.activity_logged)
        mServiceName = intent.getStringExtra("serviceName").toString()
        updateConversationHandler = Handler()
        text = findViewById(R.id.chat2)

        nsdManager.discoverServices(TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        val commThread = socket?.let { CommunicationThread(it) }
        val button: Button = findViewById(R.id.send2)
        button.setOnClickListener {
            val message=findViewById<TextView>(R.id.message).text.toString()
            try {
                val out = PrintWriter(
                    BufferedWriter(
                        OutputStreamWriter(socket?.getOutputStream() )
                    ),
                    true
                )
                out.println(message)
            }catch (_: Exception){

            }
        }
        Thread(commThread).start()

    }

    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.e(TAG, "Resolve Succeeded. $serviceInfo")

            if (serviceInfo.serviceName == mServiceName) {
                Log.d(TAG, "Same IP.")
                return
            }

            mService = serviceInfo
            port = serviceInfo.port
            host = serviceInfo.host
            socket = Socket(host, SERVERPORT)
        }
    }


    private val discoveryListener = object : NsdManager.DiscoveryListener {

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            Log.d("PDM", "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found! Do something with it.
            Log.d("PDM", "Service discovery success$service")
            when {
                service.serviceType != TYPE -> // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: ${service.serviceType}")

                service.serviceName == mServiceName -> // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: $mServiceName")

                service.serviceName.contains(mServiceName) -> nsdManager.resolveService(
                    service,
                    resolveListener
                )
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.e(TAG, "service lost: $service")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }
    }

    inner class CommunicationThread(private val clientSocket: Socket) : Runnable {
        private var input: BufferedReader? = null

        init {
            try {
                input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        override fun run() {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val read = input!!.readLine()
                    Log.e("PDM",read)
                    updateConversationHandler?.post(updateUIThread(read))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    inner class updateUIThread(private val msg: String) : Runnable {
        override fun run() {
            text.text = text.getText().toString() + "Usuario: " + msg + "\n"
        }
    }

}