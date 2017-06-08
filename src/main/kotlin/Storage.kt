/** Code that interact with Firebase Storage */
package com.google.experimental.zhihan

import java.util.Random
import android.util.Log

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.StorageReference

sealed class UploadTestResult {
	data class Fail(val error: String): UploadTestResult()
	
	data class Pass(val time: Int, val url: String, val kbps: Float): UploadTestResult() 

	object None: UploadTestResult() {}
}

fun uploadTest(bytes: ByteArray, timeout: Int): UploadTestResult {
	val ref = FirebaseStorage.getInstance().getReference()
	val rand = Random().nextInt()
	val testFileRef = ref.child("upload-test/test_file_" + rand.toString())

	var finished = false
	var error = ""
	var url = ""
	val startTime = System.nanoTime()
	var timeTaken = 0
	val timeToStop = System.currentTimeMillis() + timeout
	testFileRef.putBytes(bytes)
			.addOnFailureListener(
					object : OnFailureListener {
						override fun onFailure(ex: Exception){
							Log.e("Upload", "Upload error: " + ex)
							error = "Error: " + ex.toString()
							finished = true
						}
					}
			).addOnSuccessListener(
					object : OnSuccessListener<UploadTask.TaskSnapshot> {
						override fun onSuccess(taskSnapshot: UploadTask.TaskSnapshot) {
							url = taskSnapshot.getDownloadUrl().toString()
							Log.d("Upload", "Finished. Download Url: " + url)
							finished = true
						  timeTaken = ((System.nanoTime() - startTime)/1e6f).toInt()
							deleteFile(testFileRef)
						}
					}
			)

  // Polling to wait for the task to complete.
	while (!finished && System.currentTimeMillis() < timeToStop) {
		Thread.sleep(10)
	}

	if (error != "") {
		return UploadTestResult.Fail(error)
	} else {
		val kbps = 1024.0F * 1000 / timeTaken * 8
		return UploadTestResult.Pass(timeTaken, url, kbps)
	}
}


fun deleteFile(ref: StorageReference) {
	ref.delete()
	.addOnFailureListener(
			object : OnFailureListener {
				override fun onFailure(ex: Exception){
					Log.e("Delete", "Delete error: " + ex)
				}
			}
	).addOnSuccessListener(
			object : OnSuccessListener<Void> {
				override fun onSuccess(param: Void?) {
					Log.d("Delete", "Done.")
				}
			}
	)
}
