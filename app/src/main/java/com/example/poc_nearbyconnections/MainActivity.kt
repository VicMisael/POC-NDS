package com.example.poc_nearbyconnections

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val entrar:Button = findViewById(R.id.Entrar);
        val registrar:Button = findViewById(R.id.Registrar);
        val serviceName:TextView = findViewById(R.id.serviceName);
        val userName:TextView = findViewById(R.id.name);

        val PERMISSION_ALL = 1
        val PERMISSIONS = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        entrar.setOnClickListener {
            val service=serviceName.text
            val intent=Intent(this,Logged::class.java).apply{
                putExtra("serviceName",service.toString())
                putExtra("userName",userName.text.toString())
            }
            startActivity(intent)
        }
        registrar.setOnClickListener {
            val service=serviceName.text
            val intent=Intent(this,Registered::class.java).apply{
                putExtra("serviceName",service.toString())
                putExtra("userName",userName.text.toString())
            }
            startActivity(intent)

        }
    }


}