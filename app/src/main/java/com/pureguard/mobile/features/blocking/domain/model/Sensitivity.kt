package com.pureguard.mobile.features.blocking.domain.model

enum class Sensitivity {
    LOW,
    MEDIUM,
    HIGH;

    val urlThreshold: Int
        get() = when (this) {
            HIGH -> 18
            MEDIUM -> 32
            LOW -> 55
        }

    val imageThreshold: Float
        get() = when (this) {
            HIGH -> 0.28f
            MEDIUM -> 0.4f
            LOW -> 0.55f
        }
}
