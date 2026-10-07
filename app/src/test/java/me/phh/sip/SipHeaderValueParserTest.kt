// SPDX-License-Identifier: GPL-2.0
package me.phh.sip

import org.junit.Test

class SipHeaderValueParserTest {
    @Test
    fun `split list preserves quoted and name addr commas`() {
        val values = splitSipListHeader(
            "\"Doe, John\" <sip:john@example.test>, <sip:proxy@example.test;foo=a,b>",
        )

        require(
            values == listOf(
                "\"Doe, John\" <sip:john@example.test>",
                "<sip:proxy@example.test;foo=a,b>",
            ),
        )
    }

    @Test
    fun `outgoing route set reverses response record route`() {
        val headers = mapOf(
            "record-route" to listOf(
                "<sip:edge.example.test;lr>, <sip:core.example.test;lr>",
                "<sip:terminating.example.test;lr>",
            ),
        )

        require(
            outgoingDialogRouteSet(headers) == listOf(
                "<sip:terminating.example.test;lr>",
                "<sip:core.example.test;lr>",
                "<sip:edge.example.test;lr>",
            ),
        )
    }
    @Test
    fun `pcscf route fallback keeps only the first registered route`() {
        val registered = listOf(
            "<sip:pcscf.example.test:6000;lr>, <sip:orig@scscf.example.test;lr>",
            "<sip:pcscf.example.test:6000;lr>",
        )
        require(pcscfRouteFallback(registered) == listOf("<sip:pcscf.example.test:6000;lr>"))
        require(pcscfRouteFallback(null).isEmpty())
    }
}
