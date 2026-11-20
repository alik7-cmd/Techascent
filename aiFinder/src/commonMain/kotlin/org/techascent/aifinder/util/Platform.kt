package org.techascent.aifinder.util

enum class Platform{
    ANDROID, IOS
}

expect fun getPlatform(): Platform