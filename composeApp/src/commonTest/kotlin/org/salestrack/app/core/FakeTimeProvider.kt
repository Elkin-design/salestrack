package org.salestrack.app.core

import org.salestrack.app.core.utils.TimeProvider

class FakeTimeProvider(
    private var current: Long,
) : TimeProvider {
    override fun nowMillis(): Long = current

    fun setNow(value: Long) {
        current = value
    }
}

