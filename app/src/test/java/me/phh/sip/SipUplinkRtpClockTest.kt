// SPDX-License-Identifier: GPL-2.0
package me.phh.sip
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Test
class SipUplinkRtpClockTest {
    private var now = 0L
    private fun clock(samples: AtomicInteger) =
        SipUplinkRtpClock(samples, step = 320, sampleRate = 16000, nowMs = { now })

    @Test
    fun `lost audio advances the timestamp`() {
        val samples = AtomicInteger(0)
        val c = clock(samples)
        repeat(50) { c.next(); now += 20 }
        now += 400 // capture stalled, 400 ms of audio lost
        var ts = 0
        repeat(100) { ts = c.next(); now += 20 }
        val lagMs = (now - 20) - ts.toLong() * 1000 / 16000
        require(lagMs < SipUplinkRtpClock.LOSS_THRESHOLD_MS) { "lag=$lagMs" }
    }

    @Test
    fun `queued audio burst keeps contiguous timestamps`() {
        val samples = AtomicInteger(0)
        val c = clock(samples)
        repeat(50) { c.next(); now += 20 }
        now += 100 // stall absorbed by the AudioRecord buffer...
        repeat(5) { c.next() } // ...then the backlog arrives at once
        val ts = IntArray(100) { c.next().also { now += 20 } }
        require(ts.toList().zipWithNext().all { (a, b) -> b - a == 320 })
    }
}
