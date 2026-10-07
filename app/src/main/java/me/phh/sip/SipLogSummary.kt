// SPDX-License-Identifier: GPL-2.0
package me.phh.sip

/** A diagnostic SIP description that excludes URIs, identities, auth, and body data. */
internal fun SipMessage.safeLogSummary(): String {
    // Full messages contain phone numbers and SIM identities; debug only.
    if (android.os.SystemProperties.getBoolean("persist.sys.phhims.sip_full_log", false)) {
        return toString()
    }
    val kind = when (this) {
        is SipRequest -> "request=$method"
        is SipResponse -> "response=$statusCode"
        else -> "message"
    }
    val cseq = headers["cseq"]?.firstOrNull().orEmpty()
    val callIdToken = headers["call-id"]?.firstOrNull()
        ?.hashCode()
        ?.let(Integer::toHexString)
        .orEmpty()
    val contentType = headers["content-type"]?.firstOrNull()
        ?.substringBefore(';')
        ?.trim()
        .orEmpty()
    return "$kind cseq=$cseq callIdHash=$callIdToken " +
        "contentType=$contentType bodyBytes=${body.size}"
}
