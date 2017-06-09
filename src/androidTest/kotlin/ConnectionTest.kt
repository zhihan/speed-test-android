package com.google.experimental.zhihan.test

import com.google.experimental.zhihan.connectionTest
import com.google.experimental.zhihan.ConnectTestResult

import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.fail

@RunWith(AndroidJUnit4::class)
class HelloTest {

	@Test
	fun testConnect() {
		val result = connectionTest("www.google.com", 5000)
		when (result) {
			is ConnectTestResult.Pass -> {}
			else -> fail("Connection failed")
		}
	}
}
