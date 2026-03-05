package com.mobikul.bagisto.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PermissionUtilsTest {

    private lateinit var mockContext: Context

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)

        mockkStatic(ContextCompat::class)
        mockkStatic(Looper::class)
        mockkStatic(Log::class)

        // Mock Log methods
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0

        val mainLooper = mockk<Looper>()
        every { Looper.getMainLooper() } returns mainLooper
        every { Looper.myLooper() } returns mainLooper

        every { mockContext.applicationContext } returns mockContext
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `camera permission already granted returns true`() {
        every {
            ContextCompat.checkSelfPermission(any(), android.Manifest.permission.CAMERA)
        } returns PackageManager.PERMISSION_GRANTED

        var result = false
        PermissionUtils.checkAndRequestCameraPermission(mockContext) { result = it }

        assertTrue("Camera permission should be granted", result)
    }

    @Test
    fun `location permission checks both fine and coarse`() {
        every {
            ContextCompat.checkSelfPermission(mockContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED

        every {
            ContextCompat.checkSelfPermission(mockContext, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED

        var result = false
        PermissionUtils.checkAndRequestLocationPermission(mockContext) { result = it }

        assertTrue("Location permission should be granted when both are granted", result)
    }

    @Test
    fun `empty permissions array returns error`() {
        var result: PermissionUtils.PermissionResult? = null

        PermissionUtils.requestPermissions(mockContext, emptyArray()) { r ->
            result = r
        }

        assertTrue("Should return Error result", result is PermissionUtils.PermissionResult.Error)
        assertEquals("No permissions specified", (result as PermissionUtils.PermissionResult.Error).message)
    }

    @Test
    fun `already granted permissions return Granted immediately`() {
        val permissions = arrayOf(android.Manifest.permission.CAMERA)

        every {
            ContextCompat.checkSelfPermission(any(), android.Manifest.permission.CAMERA)
        } returns PackageManager.PERMISSION_GRANTED

        var result: PermissionUtils.PermissionResult? = null
        PermissionUtils.requestPermissions(mockContext, permissions) { r ->
            result = r
        }

        assertTrue("Should return Granted result", result is PermissionUtils.PermissionResult.Granted)
    }

    @Test
    fun `permission result sealed class has correct types`() {
        // Verify sealed class structure
        val granted = PermissionUtils.PermissionResult.Granted
        val denied = PermissionUtils.PermissionResult.Denied(listOf("test"))
        val permanentlyDenied = PermissionUtils.PermissionResult.PermanentlyDenied(listOf("test"))
        val error = PermissionUtils.PermissionResult.Error("test")

        assertNotNull(granted)
        assertNotNull(denied)
        assertNotNull(permanentlyDenied)
        assertNotNull(error)
    }

    @Test
    fun `interface implementation is correct`() {
        // Verify PermissionUtils implements PermissionManager
        val manager: PermissionManager = PermissionUtils
        assertNotNull(manager)
    }
}

