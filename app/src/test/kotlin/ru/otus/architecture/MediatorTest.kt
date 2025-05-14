package ru.otus.architecture

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class MediatorTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private fun merge(boy: String?, girl: String?): String = when {
        null != boy && null != girl -> "$boy+$girl"
        null != girl -> girl
        null != boy -> boy
        else -> ""
    }

    @Test
    fun testMediator() {
        val boys = MutableLiveData<String>()
        val girls = MutableLiveData<String>()
        val observedValues = mutableListOf<String>()

        val mediator = MediatorLiveData(merge(boys.value, girls.value)).apply {
            addSource(boys) {
                value = merge(it, girls.value)
            }
            addSource(girls) {
                value = merge(boys.value, it)
            }
        }
        mediator.observeForever { observedValues.add(it) }

        assertEquals(listOf(""), observedValues)

        boys.value = "Vasya"
        assertEquals(listOf("", "Vasya"), observedValues)

        girls.value = "Masha"
        assertEquals(listOf("", "Vasya", "Vasya+Masha"), observedValues)

        boys.value = "Petya"
        assertEquals(listOf("", "Vasya", "Vasya+Masha", "Petya+Masha"), observedValues)
    }
}