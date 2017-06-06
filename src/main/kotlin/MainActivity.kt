package com.google.experimental.zhihan

import java.net.URL

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.os.Bundle
import android.os.AsyncTask
import android.util.Log
import android.view.ViewGroup
import android.view.View
import android.view.LayoutInflater
import android.view.Menu
import android.widget.TextView
import android.widget.ListView
import android.widget.BaseAdapter


class MainActivity : AppCompatActivity() {
	val downloadFile: String = "http://2.testdebit.info/fichiers/1Mo.dat"
	val connectServer: String = "www.google.com"

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		val inflater = getMenuInflater()
		inflater.inflate(R.menu.main_menu, menu)
		return true
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val myToolbar = findViewById(R.id.my_toolbar) as Toolbar;
    setSupportActionBar(myToolbar);
		
		val rv = findViewById(R.id.my_list_view) as ListView
		val adapter = MainAdapter()
		rv.setAdapter(adapter)

		val btn = findViewById(R.id.run)
		btn.setOnClickListener(object : View.OnClickListener {
			override fun onClick(view: View) {
				Log.d("SpeedTest", "Starting tests.")
				adapter.reset()
				val downloadTest = DownloadTest(adapter, downloadFile, { -> Unit})
				val connectTest = ConnectTest(adapter, connectServer,
				{ -> downloadTest.execute()})
				connectTest.execute()
			}
		})
  }
}


class MainAdapter() : BaseAdapter() {
	var connectTestResult: ConnectTestResult = ConnectTestResult.None
	var downloadTestResult: DownloadTestResult = DownloadTestResult.None

	fun reset() {
		connectTestResult = ConnectTestResult.None
		downloadTestResult = DownloadTestResult.None
		notifyDataSetChanged()
	}
	
	override fun getCount(): Int { return 2 }
	override fun getItemId(position: Int): Long { return position.toLong()}
	override fun getItem(position: Int): Object {
		if (position == 0) {
			return connectTestResult as Object
		} else {
			return downloadTestResult as Object 
		}
	}
	
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
			is ConnectTestResult.Fail -> {
				val r = connectTestResult as ConnectTestResult.Fail
				tv.setText("Fail: " + r.error)
			}
			ConnectTestResult.None -> tv.setText("Waiting...")
		}
	}

	private fun updateDownloadTestResult(title: TextView, tv: TextView) {
		title.setText("Download:")
		when (downloadTestResult) {
			is DownloadTestResult.Pass -> {
				val r = downloadTestResult as DownloadTestResult.Pass
				tv.setText("Download 1Mb in " + r.time + "ms.")
			}
			is DownloadTestResult.Error -> {
				val r = downloadTestResult as DownloadTestResult.Error
				tv.setText("Error: " + r.error)
			}
			is DownloadTestResult.Timeout -> {
				val r = downloadTestResult as DownloadTestResult.Timeout
				tv.setText("Timeout: " + r.error)
			}
			DownloadTestResult.None -> tv.setText("Waiting...")
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
