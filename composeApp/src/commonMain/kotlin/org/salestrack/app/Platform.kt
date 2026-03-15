package org.salestrack.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform