package com.devbrian.osebo.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.*

class PrinterConnectionManager(private val context: Context) {

    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    
    private val PRINTER_NAME_PATTERNS = listOf(
        "PM200", "Pegasus", "PRINTER", "POS", "Thermal", "MPOS",
        "BLUETOOTH PRINTER", "58MM", "80MM"
    )

    data class PrintResult(val isSuccess: Boolean, val errorMessage: String = "")

    
    private val prefs: SharedPreferences = context.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)

    suspend fun isPrinterConnected(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (bluetoothSocket?.isConnected == true) {
                    
                    try {
                        outputStream?.write(byteArrayOf(0x0A)) 
                        outputStream?.flush()
                        return@withContext true
                    } catch (e: Exception) {
                        closeConnection()
                    }
                }

                
                val lastPrinterMac = prefs.getString("last_printer_mac", null)
                if (lastPrinterMac != null) {
                    val device = bluetoothAdapter?.getRemoteDevice(lastPrinterMac)
                    if (device != null) {
                        return@withContext connectToBluetoothPrinter(device)
                    }
                }

                false
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun connectToPrinter(deviceName: String? = null): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (bluetoothAdapter == null) {
                    return@withContext false
                }

                if (!bluetoothAdapter.isEnabled) {
                    withContext(Dispatchers.Main) {
                        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        enableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(enableIntent)
                    }
                    return@withContext false
                }

                if (!deviceName.isNullOrEmpty()) {
                    val pairedDevices = bluetoothAdapter.bondedDevices
                    val targetDevice = pairedDevices.firstOrNull {
                        it.name?.contains(deviceName, ignoreCase = true) == true
                    }

                    if (targetDevice != null) {
                        return@withContext connectToBluetoothPrinter(targetDevice)
                    }
                }

                val pairedDevices = bluetoothAdapter.bondedDevices
                val printerDevice = findPegasusPrinter(pairedDevices)

                if (printerDevice != null) {
                    connectToBluetoothPrinter(printerDevice)
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun findPegasusPrinter(devices: Set<BluetoothDevice>): BluetoothDevice? {
        for (device in devices) {
            val name = device.name ?: ""
            if (name.contains("PM200", ignoreCase = true)) {
                return device
            }
        }

        for (pattern in PRINTER_NAME_PATTERNS) {
            for (device in devices) {
                val name = device.name ?: ""
                if (name.contains(pattern, ignoreCase = true)) {
                    return device
                }
            }
        }

        return null
    }

    suspend fun connectToBluetoothPrinter(device: BluetoothDevice): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                closeConnection()

                var connected = false
                var lastError: Exception? = null

                
                bluetoothAdapter?.cancelDiscovery()
                delay(500)

                
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                    bluetoothSocket?.connect()
                    connected = true
                } catch (e: IOException) {
                    lastError = e
                    e.printStackTrace()

                    
                    try {
                        delay(500)
                        val method = device.javaClass.getMethod("createRfcommSocket", Int::class.java)
                        bluetoothSocket = method.invoke(device, 1) as BluetoothSocket
                        bluetoothSocket?.connect()
                        connected = true
                    } catch (e2: Exception) {
                        lastError = e2
                        e2.printStackTrace()
                    }
                }

                if (connected && bluetoothSocket?.isConnected == true) {
                    outputStream = bluetoothSocket?.outputStream

                    
                    delay(500)
                    initializePrinter()

                    
                    prefs.edit().putString("last_printer_mac", device.address).apply()

                    withContext(Dispatchers.Main) {
                        val deviceName = device.name ?: "Printer"
                        Toast.makeText(context, "✅ Connected to $deviceName", Toast.LENGTH_SHORT).show()
                    }

                    true
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "❌ Connection failed: ${lastError?.message}", Toast.LENGTH_SHORT).show()
                    }
                    false
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Connection error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                false
            }
        }
    }

    private fun initializePrinter() {
        try {
            
            val initPrinter = byteArrayOf(0x1B, 0x40) 
            val lineSpacing = byteArrayOf(0x1B, 0x32) 
            val alignCenter = byteArrayOf(0x1B, 0x61, 0x01) 
            val alignLeft = byteArrayOf(0x1B, 0x61, 0x00) 

            outputStream?.write(initPrinter)
            outputStream?.flush()
            Thread.sleep(100)

            outputStream?.write(lineSpacing)
            outputStream?.flush()
            Thread.sleep(100)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun printReceipt(receiptText: String): PrintResult {
        return withContext(Dispatchers.IO) {
            try {
                if (bluetoothSocket?.isConnected != true) {
                    val connected = connectToPrinter()
                    if (!connected) {
                        return@withContext PrintResult(false, "No printer connected. Please pair your PM200 printer first.")
                    }
                }

                
                initializePrinter()
                delay(200)

                
                val printData = buildPM200PrintData(receiptText)

                
                val bytes = try {
                    
                    printData.toByteArray(charset("GBK"))
                } catch (e: Exception) {
                    try {
                        
                        printData.toByteArray(charset("UTF-8"))
                    } catch (e2: Exception) {
                        
                        printData.toByteArray(charset("US-ASCII"))
                    }
                }

                
                var offset = 0
                while (offset < bytes.size) {
                    val length = minOf(64, bytes.size - offset) 
                    outputStream?.write(bytes, offset, length)
                    outputStream?.flush()
                    offset += length
                    delay(150) 
                }

                
                delay(500)

                
                outputStream?.write(byteArrayOf(0x0A, 0x0A, 0x0A)) 
                outputStream?.flush()
                delay(200)

                
                try {
                    outputStream?.write(byteArrayOf(0x1D, 0x56, 0x01)) 
                    outputStream?.flush()
                } catch (e: Exception) {
                    
                    try {
                        outputStream?.write(byteArrayOf(0x1D, 0x56, 0x00)) 
                        outputStream?.flush()
                    } catch (e2: Exception) {
                        
                    }
                }

                PrintResult(true)

            } catch (e: IOException) {
                e.printStackTrace()
                PrintResult(false, "Print failed: ${e.message}")
            }
        }
    }

    private fun buildPM200PrintData(text: String): String {
        val sb = StringBuilder()

        
        sb.append("\n")

        val lines = text.split("\n")
        lines.forEach { line ->
            when {
                line.contains("=") || line.contains("-") -> {
                    
                    sb.append(line).append("\n")
                }
                line.contains("THANK YOU") || line.contains("Visit us") -> {
                    
                    sb.append(centerText(line, 32)).append("\n")
                    sb.append("\n")
                }
                line.contains("PRINTER TEST") || line.contains("OSEBO POS") -> {
                    
                    sb.append(line).append("\n")
                }
                else -> {
                    
                    if (line.length > 32) {
                        var start = 0
                        while (start < line.length) {
                            val end = minOf(start + 32, line.length)
                            sb.append(line.substring(start, end)).append("\n")
                            start = end
                        }
                    } else {
                        sb.append(line).append("\n")
                    }
                }
            }
        }

        
        sb.append("\n\n\n\n")

        return sb.toString()
    }

    private fun centerText(text: String, width: Int): String {
        val cleanText = text.trim()
        val padding = width - cleanText.length
        if (padding <= 0) return cleanText
        val leftPadding = padding / 2
        return " ".repeat(leftPadding) + cleanText
    }

    fun printTestPage() {
        try {
            val testText = """
                
                ================================
                    PRINTER TEST PAGE
                ================================
                
                Printer: PM200 Pegasus
                Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}
                
                If you can read this,
                your printer is working!
                
                --------------------------------
                Line 1: Normal text
                Line 2: More text
                Line 3: Even more text
                
                Thank you for choosing
                Osebo POS!
                ================================
                
                
                
            """.trimIndent()

            outputStream?.write(testText.toByteArray(charset("GBK")))
            outputStream?.flush()
            outputStream?.write(byteArrayOf(0x0A, 0x0A, 0x0A))
            outputStream?.flush()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun closeConnection() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            outputStream = null
            bluetoothSocket = null
        }
    }
}
