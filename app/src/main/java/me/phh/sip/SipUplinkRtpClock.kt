//SPDX-License-Identifier: GPL-2.0
package me.phh.sip
import android.os.SystemClock
import java.util.concurrent.atomic.AtomicInteger
/**
 * RTP timestamps for live microphone frames. Audio lost while capture stalls
 * (AudioRecord overrun) must still advance the timestamp (RFC 3550 5.1); otherwise
 * every stall permanently adds its length to the far end's playout delay.
 *
 * The send lag behind the wall clock is constant in steady state. Once it stays
 * above its minimum for PERSIST_MS, the missing audio was lost rather than queued,
 * so the timestamp skips over it.
 */
internal class SipUplinkRtpClock(
    private val samples: AtomicInteger,
    private val step: Int,
    private val sampleRate: Int,
    private val onSkip: (skippedMs: Long) -> Unit = {},
    private val nowMs: () -> Long = SystemClock::elapsedRealtime,
) {
    private var startMs = -1L
    private var startSamples = 0
    private var minLagMs = Long.MAX_VALUE
    private var lateSinceMs = -1L

    fun next(): Int {
        val now = nowMs()
        if (startMs < 0) {
            startMs = now
            startSamples = samples.get()
        }
        val lagMs = (now - startMs) - (samples.get() - startSamples).toLong() * 1000 / sampleRate
        minLagMs = minOf(minLagMs, lagMs)
        val excessMs = lagMs - minLagMs
        if (excessMs < LOSS_THRESHOLD_MS) {
            lateSinceMs = -1L
        } else if (lateSinceMs < 0) {
            lateSinceMs = now
        } else if (now - lateSinceMs >= PERSIST_MS) {
            val skipFrames = (excessMs * sampleRate / 1000 / step).toInt()
            samples.addAndGet(skipFrames * step)
            lateSinceMs = -1L
            onSkip(skipFrames.toLong() * step * 1000 / sampleRate)
        }
        return samples.getAndAdd(step)
    }

    companion object {
        const val LOSS_THRESHOLD_MS = 60L
        const val PERSIST_MS = 1000L
    }
}
