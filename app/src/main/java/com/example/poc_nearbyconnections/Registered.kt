package com.example.poc_nearbyconnections

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.os.Handler
import android.system.Os.socket
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
import java.net.ServerSocket
import java.net.Socket


class Registered : AppCompatActivity() {
    val TYPE = "_poc_tcp";
    val nsdManager = (getSystemService(Context.NSD_SERVICE) as NsdManager)
    lateinit var serviceName: String;
    lateinit var serverSocket: ServerSocket;
    var SERVERPORT: Int = -1;
    lateinit var text: TextView;
    var serverThread: Thread? = null
    var socket: Socket? = null
    var updateConversationHandler: Handler? = null

    private val registrationListener = object : NsdManager.RegistrationListener {

        override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
            // Save the service name. Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
            serviceName = NsdServiceInfo.serviceName
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Registration failed! Put debugging code here to determine why.
            Log.e("PDM", "Failed")
        }

        override fun onServiceUnregistered(arg0: NsdServiceInfo) {
            // Service has been unregistered. This only happens when you call
            // NsdManager.unregisterService() and pass in this listener.
            Log.e("PDM", "Unregistered")
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Unregistration failed. Put debugging code here to determine why.
            Log.e("PDM", "Unregister failed")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registered)
        text = findViewById(R.id.chat)
         ServerSocket(0).also { socket ->
            // Store the chosen port.
            SERVERPORT = socket.localPort
        }
        val name = intent.getStringExtra("serviceName").toString()
        registerService(name, SERVERPORT)

        val button=findViewById<Button>(R.id.send);
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
            }catch (_:Exception){

            }
        }
        updateConversationHandler = Handler()

        this.serverThread = Thread(ServerThread())
        this.serverThread!!.start()

    }

    override fun onStop() {
        super.onStop()
        nsdManager.apply {
            unregisterService(registrationListener)
        }
        try {
            serverSocket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    inner class ServerThread : Runnable {
        override fun run() {

            try {
                serverSocket = ServerSocket(SERVERPORT)
                socket = serverSocket.accept()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val commThread = socket?.let { CommunicationThread(it) }
                    Thread(commThread).start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
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


    fun registerService(service: String, port: Int) {
        // Create the NsdServiceInfo object, and populate it.
        val serviceInfo = NsdServiceInfo().apply {
            // The name is subject to change based on conflicts
            // with other services advertised on the same network.
            serviceName = service
            serviceType = TYPE
            setPort(port)
        }

        nsdManager.apply {
            registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        }
    }
}