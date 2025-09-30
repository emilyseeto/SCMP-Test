package com.example.scmptest.ext

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.View
import com.example.scmptest.R
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.Runs
import io.mockk.verify
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionsTest {

    private val context = mockk<Context>(relaxed = true)
    private val resources = mockk<Resources>(relaxed = true)
    private val errorMsg = "API failed"
    private val genericErrorMsg = "Generic error"

    init {
        every { context.resources } returns resources
        every { context.applicationContext } returns context
        every { context.theme } returns mockk(relaxed = true)
        every { context.assets } returns mockk(relaxed = true)
        every { context.packageName } returns "com.example.test"
        every { context.classLoader } returns javaClass.classLoader
    }

    @Test
    fun `orFalse should return false for null Boolean`() {
        val boolean: Boolean? = null

        assertFalse(boolean.orFalse())
    }

    @Test
    fun `orFalse should return true for true Boolean`() {
        val boolean: Boolean? = true

        assertTrue(boolean.orFalse())
    }

    @Test
    fun `orFalse should return false for false Boolean`() {
        val boolean: Boolean? = false

        assertFalse(boolean.orFalse())
    }

    @Test
    fun `orZero should return 0 for null Int`() {
        val int: Int? = null

        assertEquals(0, int.orZero())
    }

    @Test
    fun `orZero should return value for non-null Int`() {
        val int: Int? = 1

        assertEquals(int, int.orZero())
    }

    @Test
    fun `visible should set view visibility to visible`() {
        val view = mockk<View>(relaxed = true)

        every { view.visibility = View.VISIBLE } just Runs

        view.visible()

        verify { view.visibility = View.VISIBLE }
    }

    @Test
    fun `gone should set view visibility to gone`() {
        val view = mockk<View>(relaxed = true)

        every { view.visibility = View.GONE } just Runs

        view.gone()

        verify { view.visibility = View.GONE }
    }

    @Test
    fun `visibleElseGone should set view visibility to visible when shouldVisible is true`() {
        val view = mockk<View>(relaxed = true)

        every { view.visibility = View.VISIBLE } just Runs

        view.visibleElseGone(true)

        verify { view.visibility = View.VISIBLE }
    }

    @Test
    fun `visibleElseGone should set view visibility to gone when shouldVisible is false`() {
        val view = mockk<View>(relaxed = true)

        every { view.visibility = View.GONE } just Runs

        view.visibleElseGone(false)

        verify { view.visibility = View.GONE }
    }

    @Test
    fun `getErrorMsg should return parsed error message`() {
        val errorJson = """{"error": "${errorMsg}"}"""
        val responseBody = mockk<ResponseBody>()

        every { responseBody.string() } returns errorJson

        val result = responseBody.getErrorMsg(genericErrorMsg)

        assertEquals(errorMsg, result)
    }

    @Test
    fun `getErrorMsg should return generic error for exception`() {
        val invalidJson = "Invalid JSON"
        val responseBody = mockk<ResponseBody>()

        every { responseBody.string() } returns invalidJson

        val result = responseBody.getErrorMsg(genericErrorMsg)

        assertEquals(
            "java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 1 path \$",
            result
        )
    }

    @Test
    fun `getErrorMsg should return generic error for null response body`() {
        val responseBody: ResponseBody? = null

        val result = responseBody.getErrorMsg(genericErrorMsg)

        assertEquals(genericErrorMsg, result)
    }

    @Test
    fun `getErrorMsg should return generic error for blank error field`() {
        val errorJson = """{"error": " "}"""
        val responseBody = mockk<ResponseBody>()

        every { responseBody.string() } returns errorJson

        val result = responseBody.getErrorMsg(genericErrorMsg)

        assertEquals(genericErrorMsg, result)
    }

    @Test
    fun `getErrorMsg should return generic error for null error field`() {
        val errorJson = """{"error": null}"""
        val responseBody = mockk<ResponseBody>()

        every { responseBody.string() } returns errorJson

        val result = responseBody.getErrorMsg(genericErrorMsg)

        assertEquals(genericErrorMsg, result)
    }
}
