package com.google.experimental.zhihan

import java.io.IOException
import java.net.InetSocketAddress
import java.net.InetAddress
import java.net.Socket

sealed class ConnectTestResult {
	data class Fail(val error: String) : ConnectTestResult()
	data class Pass(val time: Int) : ConnectTestResult()
	data class None(val value: String): ConnectTestResult()
}


/**
 * ConnectionTest
 *
 * A tester that test connection speed to a server. A TCP connection 
 * typically takes three packets to establish connection, the time takes
 * to establish a connection is therefore roughly three times of latency
 * and 1.5 times of rount-trip ping time.
 *
 * Reference
 *  - https://github.com/stealthcopter/AndroidNetworkTools
*/
fun connectionTest(addr: String, timeout: Int): ConnectTestResult {
	try {
		val startTime = System.nanoTime()
		val socketAddr = InetSocketAddress(addr, 80)
		val socket = Socket()
		socket.connect(socketAddr, timeout)

		val timeTaken = (System.nanoTime()-startTime)/1e6f
		return ConnectTestResult.Pass(timeTaken.toInt())
  } catch (e: IOException) {
		return ConnectTestResult.Fail(e.toString())
  }
}
