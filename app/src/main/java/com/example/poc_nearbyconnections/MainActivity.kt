package com.example.poc_nearbyconnections

import android.content.Context
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.net.ServerSocket


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val entrar:Button = findViewById(R.id.Entrar);
        val registrar:Button = findViewById(R.id.Registrar);
        val serviceName:TextView = findViewById(R.id.serviceName);
        val userName:TextView = findViewById(R.id.name);

        entrar.setOnClickListener {
            val service=serviceName.text
            Intent(this,Logged::class.java).apply{
                putExtra("serviceName",service)
                putExtra("userName",userName.text)
            }

        }
        registrar.setOnClickListener {
            val service=serviceName.text
            Intent(this,Logged::class.java).apply{
                putExtra("serviceName",service)
                putExtra("userName",userName.text)
            }
        }
    }


}