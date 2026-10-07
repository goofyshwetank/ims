//SPDX-License-Identifier: GPL-2.0
package me.phh.sip

import android.telephony.Rlog
import java.net.DatagramPacket

object SipRtpPacketLogger {
    private var lastReceivedAtMs = 0L
    private var maxGapMs = 0L
    private fun shouldLogReceivedPacket(receivedCount: Int): Boolean =
        receivedCount <= 10 || receivedCount % 50 == 0

    fun logReceivedPacket(
        logTag: String,
        receivedCount: Int,
        packet: DatagramPacket,
        payloadType: Int,
        frameType: Int,
        codecFrameSize: Int,
    ) {
        val now = android.os.SystemClock.elapsedRealtime()
        if (receivedCount > 1) maxGapMs = maxOf(maxGapMs, now - lastReceivedAtMs)
        lastReceivedAtMs = now
        if (!shouldLogReceivedPacket(receivedCount)) return

        Rlog.d(
            logTag,
            "Received RTP packet #$receivedCount: " +
                "from=${packet.address}:${packet.port} " +
                "length=${packet.length} pt=$payloadType ft=$frameType " +
                "codecBytes=$codecFrameSize maxGapMs=$maxGapMs"
        )
        maxGapMs = 0L
    }
}
