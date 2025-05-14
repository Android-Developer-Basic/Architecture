package ru.otus.architecture.stat

import androidx.lifecycle.LifecycleOwner
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import kotlin.test.assertEquals

class PageEventHelperTest {
    private val statService = mockk<StatService>(relaxed = true)
    private val pageEventHelper = PageEventHelper(statService)

    private val owner: LifecycleOwner = mockk(relaxed = true)

    @Test
    fun testOnCreate() {
        pageEventHelper.onCreate(mockk())
        verify {
            statService.logEvent(withArg {
                assertEquals(PageEvent.PAGE, it.name)
                assertEquals("onCreate", it.properties[PageEvent.ACTION])
                assertEquals(owner::class.simpleName, it.properties[PageEvent.NAME])
            })
        }
    }
}