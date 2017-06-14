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

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthResult

class MainActivity : AppCompatActivity() {
	val downloadFile: String = "https://firebasestorage.googleapis.com/v0/b/speed-test-android.appspot.com/o/download-test%2F1Mo.dat?alt=media&token=36b91619-f061-431c-9a2c-55b1bcfbcda2"
	val connectServer: String = "www.google.com"
	var auth: FirebaseAuth? = null

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		val inflater = getMenuInflater()
		inflater.inflate(R.menu.main_menu, menu)
		return true
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		auth = FirebaseAuth.getInstance()

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
				val uploadTest = UploadTest(adapter)
				val downloadTest = DownloadTest(adapter, downloadFile)
				val connectTest = ConnectTest(adapter, connectServer)
				connectTest.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
				downloadTest.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
				uploadTest.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
			}
		})
  }
	
	override fun onStart() {
		super.onStart()
		val currentUser = auth?.getCurrentUser()
		if (auth != null) {
			val mAuth = auth as FirebaseAuth
			mAuth.signInAnonymously().addOnFailureListener(this, object : OnFailureListener {
				override fun onFailure(ex: Exception){
					Log.e("SignIn", "Error: " + ex)
				}
			}).addOnSuccessListener(this, object : OnSuccessListener<AuthResult> {
				override fun onSuccess(result: AuthResult) {
					Log.d("SignIn", "Signed in " + result)
				}
			})
		}
	}
}


class MainAdapter() : BaseAdapter() {
	var connectTestResult: ConnectTestResult = ConnectTestResult.None
	var downloadTestResult: DownloadTestResult = DownloadTestResult.None
	var uploadTestResult: UploadTestResult = UploadTestResult.None
	
	fun reset() {
		connectTestResult = ConnectTestResult.None
		downloadTestResult = DownloadTestResult.None
		uploadTestResult = UploadTestResult.None
		notifyDataSetChanged()
	}
	
	override fun getCount(): Int { return 3 }
	override fun getItemId(position: Int): Long { return position.toLong()}
	override fun getItem(position: Int): Object {
		if (position == 0) {
			return connectTestResult as Object
		} else if (position == 1) {
			return downloadTestResult as Object 
		} else {
			return uploadTestResult as Object
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
			2 -> updateUploadTestResult(titleTextView, contentTextView)
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
			ConnectTestResult.Running -> tv.setText("Running...")
		}
	}

	private fun updateDownloadTestResult(title: TextView, tv: TextView) {
		title.setText("Download:")
		when (downloadTestResult) {
			is DownloadTestResult.Pass -> {
				val r = downloadTestResult as DownloadTestResult.Pass
				tv.setText("Download 1MB in " + r.time + "ms, " + r.kbps.toInt() + " kbps.")
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
			DownloadTestResult.Running -> tv.setText("Running...")
		}
	}

	private fun updateUploadTestResult(title: TextView, tv: TextView) {
		title.setText("Upload:")
		when (uploadTestResult) {
			is UploadTestResult.Pass -> {
				val r = uploadTestResult as UploadTestResult.Pass
				tv.setText("Upload 1 MB in " + r.time + "ms, " + r.kbps.toInt() + " kbps.")
			}
			is UploadTestResult.Fail -> {
				val r = uploadTestResult as UploadTestResult.Fail
				tv.setText("Fail: " + r.error)
			}
			UploadTestResult.None -> tv.setText("Waiting...")
			UploadTestResult.Running -> tv.setText("Running...")
		}
	}
}

/**
 *  Test the total time to establish a TCP connection to a server.
 */
class ConnectTest(val adapter: MainAdapter, val url: String):
AsyncTask<Void, Void, Boolean>() {
	
	override fun doInBackground(vararg params: Void): Boolean {
		adapter.connectTestResult = ConnectTestResult.Running
		adapter.notifyDataSetChanged()
		val result = connectionTest(url, 5000)
		adapter.connectTestResult = result
		return true
	}

	override fun onPostExecute(result: Boolean) {
		adapter.notifyDataSetChanged()
	}
}


class DownloadTest(val adapter: MainAdapter, val url: String):
AsyncTask<Void, Void, Boolean>() {
	override fun doInBackground(vararg params: Void): Boolean {
		adapter.downloadTestResult = DownloadTestResult.Running
		adapter.notifyDataSetChanged()
		
		val result = downloadTest(URL(url), 5000)
		adapter.downloadTestResult = result
		return true
	}

	override fun onPostExecute(x: Boolean) {
		adapter.notifyDataSetChanged()
	}
}


class UploadTest(val adapter: MainAdapter):
AsyncTask<Void, Void, Boolean>() {
	override fun doInBackground(vararg params: Void): Boolean {
		adapter.uploadTestResult = UploadTestResult.Running
		adapter.notifyDataSetChanged()
		
		val result = uploadTest(ByteArray(1024 * 1024), 5000)
		adapter.uploadTestResult = result
		return true
	}

	override fun onPostExecute(x: Boolean) {
		adapter.notifyDataSetChanged()
	}
}
