package ru.otus.architecture.prefs

import android.os.Handler
import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch

class SharedPreferencesStorageTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var storage: SharedPreferencesStorage

    @Before
    fun init() {
        storage = SharedPreferencesStorage(ApplicationProvider.getApplicationContext(), "test")
        storage.clear()
    }

    @Test
    fun returnsNullWhenKeyNotFound() {
        assertNull(storage["key"])
    }

    @Test
    fun returnsValueWhenKeyFound() {
        storage["key"] = "value"
        assertEquals("value", storage["key"])
    }

    @Test
    fun returnsLiveData() {
        val countdown = CountDownLatch(1)
        val observedValues = mutableListOf<String?>()

        storage.liveData("key").observeForever {
            observedValues.add(it)
        }
        storage["key"] = "value"

        Handler(Looper.getMainLooper()).post {
            assertEquals(listOf(null, "value"), observedValues)
            countdown.countDown()
        }

        countdown.await()
    }
}