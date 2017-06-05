package com.google.experimental.zhihan

import com.google.common.io.ByteStreams
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.HttpURLConnection
import java.net.SocketTimeoutException

import android.util.Log

sealed class DownloadTestResult {
	data class Timeout(val error:String) : DownloadTestResult()
	data class Pass(val time: Int): DownloadTestResult()
	data class Error(val error: String): DownloadTestResult()
	data class None(val msg: String): DownloadTestResult()
}
// A simple downloader to test the download speed.

fun downloadTest(url: URL, timeout: Int): DownloadTestResult {
	var result: DownloadTestResult = DownloadTestResult.Pass(-1)
	var conn: HttpURLConnection? = null
	try {
		conn = url.openConnection() as HttpURLConnection

		conn.setRequestMethod("GET")
		conn.setReadTimeout(timeout)
		conn.setConnectTimeout(timeout)
		conn.connect()

		val startTime = System.nanoTime()
		val bytes = ByteStreams.toByteArray(conn.getInputStream())
		val timeTaken = (System.nanoTime()-startTime)/1e6f
		Log.d("download", "Downloaded " + bytes.size + " bytes.")
		result = DownloadTestResult.Pass(timeTaken.toInt())
	} catch (e: IOException) {
		result = DownloadTestResult.Error(e.toString())
	} catch (e: SocketTimeoutException) {
		result = DownloadTestResult.Timeout(e.toString())
	} finally {
		if (conn != null) {
			conn.disconnect()
		}
	}
	return result
}

