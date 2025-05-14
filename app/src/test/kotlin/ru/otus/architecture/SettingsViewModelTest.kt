package ru.otus.architecture

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import ru.otus.architecture.prefs.KeyValueStorage
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SettingsViewModelTest {
    private lateinit var storage: KeyValueStorage
    private lateinit var liveData: MutableLiveData<String?>

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        liveData = MutableLiveData(null)
        storage = mockk {
            every { liveData(any()) } returns liveData
            every { set(any(), anyNullable()) } just Runs
        }
    }

    @Test
    fun returnsStoredPrefix() {
        val observedValues = mutableListOf<String?>()

        val viewModel = SettingsViewModel(storage)
        viewModel.prefix.observeForever {
            observedValues.add(it)
        }
        liveData.value = "stored"

        assertEquals(listOf(null, "stored"), observedValues)
        verify {
            storage.liveData(PREFIX_KEY)
        }
    }

    @Test
    fun updatesPrefix() {
        val viewModel = SettingsViewModel(storage)
        viewModel.updatePrefix("new")
        verify {
            storage[PREFIX_KEY] = "new"
        }
    }
}