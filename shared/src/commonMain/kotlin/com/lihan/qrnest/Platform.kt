package com.lihan.qrnest

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform