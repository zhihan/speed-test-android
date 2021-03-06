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
	/** Timeout error */
	data class Timeout(val error:String) : DownloadTestResult()
	
	/** General error, e.g., I/O error while getting the content. */
	data class Error(val error: String): DownloadTestResult()

	/** Pass */
	data class Pass(val time: Int, val kbps: Float): DownloadTestResult()

	/** Test has not finished. */
	object None: DownloadTestResult() {}
	
	/** Test has started */
	object Running: DownloadTestResult() {}
}


/**
 * Download a file with known size and report the download time.
 */
fun downloadTest(url: URL, timeout: Int): DownloadTestResult {
	var result: DownloadTestResult = DownloadTestResult.Pass(-1, 0.0F)
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

		val kbps = 1024.0F * 1000 / timeTaken * 8
		result = DownloadTestResult.Pass(timeTaken.toInt(), kbps)
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


