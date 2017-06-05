package com.google.experimental.zhihan

import java.net.URL

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.AsyncTask
import android.util.Log
import android.view.ViewGroup
import android.view.View
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.ListView
import android.widget.BaseAdapter


class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val rv = findViewById(R.id.my_list_view) as ListView
		val adapter = MainAdapter()
		rv.setAdapter(adapter)
		Log.d("MyApp", "Start")

		val downloadTest = DownloadTest(adapter,
		"http://2.testdebit.info/fichiers/1Mo.dat", { -> Unit})
		val connectTest = ConnectTest(adapter,
		"www.google.com", { -> downloadTest.execute()})
		connectTest.execute()
  }
}



class MainAdapter() : BaseAdapter() {
	var connectTestResult: ConnectTestResult = ConnectTestResult.Fail("")
	var downloadTestResult: DownloadTestResult = DownloadTestResult.None("")

	override fun getCount(): Int { return 2 }
	override fun getItemId(position: Int): Long { return position.toLong()}
	override fun getItem(position: Int):Object { return "1" as Object}
	
	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.text_view, parent, false)
		val titleTextView = v.findViewById(R.id.title) as TextView
		val contentTextView = v.findViewById(R.id.textView) as TextView

		when (position) {
			0 -> updateConnectTestResult(titleTextView, contentTextView)
			1 -> updateDownloadTestResult(titleTextView, contentTextView)
		}
		return v
	}

	private fun updateConnectTestResult(title: TextView, tv: TextView) {
		title.setText("Connect:")
		when (connectTestResult) {
			is ConnectTestResult.Pass -> {
				val r = connectTestResult as ConnectTestResult.Pass
				tv.setText("Connected in " + r.time + "ms.")
			}
			is ConnectTestResult.None -> tv.setText("N/A")
		}
	}

	private fun updateDownloadTestResult(title: TextView, tv: TextView) {
		title.setText("Download:")
		when (downloadTestResult) {
			is DownloadTestResult.Pass -> {
				val r = downloadTestResult as DownloadTestResult.Pass
				tv.setText("Download 1Mb in " + r.time + "ms.")
			}
			is DownloadTestResult.None -> tv.setText("waiting")
		}
	}
}

/**
 *  Test the total time to establish a TCP connection to a server.
 */
class ConnectTest(val adapter: MainAdapter, val url: String, val next: () -> Unit):
AsyncTask<Void, Void, Boolean>() {
	
	override fun doInBackground(vararg params: Void): Boolean {
		val result = connectionTest(url, 5000)
		adapter.connectTestResult = result
		return true
	}

	override fun onPostExecute(result: Boolean) {
		adapter.notifyDataSetChanged()
		next()
	}
}


class DownloadTest(val adapter: MainAdapter, val url: String, val next: () -> Unit):
AsyncTask<Void, Void, Boolean>() {
	override fun doInBackground(vararg params: Void): Boolean {
		val result = downloadTest(URL(url), 5000)
		adapter.downloadTestResult = result
		return true
	}

	override fun onPostExecute(x: Boolean) {
		adapter.notifyDataSetChanged()
		next()
	}
}
