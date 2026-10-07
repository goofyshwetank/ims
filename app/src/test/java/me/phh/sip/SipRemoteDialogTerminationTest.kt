// SPDX-License-Identifier: GPL-2.0
package me.phh.sip

import java.io.ByteArrayOutputStream
import java.io.IOException
import org.junit.Assert.assertSame
import org.junit.Test

class SipRemoteDialogTerminationTest {
    @Test
    fun `main flow is preferred over dialog writers`() {
        val mainWriter = ByteArrayOutputStream()

        val selected = SipRemoteDialogTermination.localDialogRequestWriter(
            incomingResponseWriter = ByteArrayOutputStream(),
            registeredDialogWriter = ByteArrayOutputStream(),
            mainWriter = { mainWriter },
        )

        assertSame(mainWriter, selected)
    }

    @Test
    fun `dialog writer is used when the main flow is unavailable`() {
        val registeredWriter = ByteArrayOutputStream()

        val selected = SipRemoteDialogTermination.localDialogRequestWriter(
            incomingResponseWriter = null,
            registeredDialogWriter = registeredWriter,
            mainWriter = { throw IOException("main flow down") },
        )

        assertSame(registeredWriter, selected)
    }

    @Test(expected = IOException::class)
    fun `main flow error surfaces without any dialog writer`() {
        SipRemoteDialogTermination.localDialogRequestWriter(
            incomingResponseWriter = null,
            registeredDialogWriter = null,
            mainWriter = { throw IOException("main flow down") },
        )
    }
}
