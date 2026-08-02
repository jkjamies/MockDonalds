package com.jkjamies.sampleplatter.features.order.data.wiring

import dev.zacsweers.metro.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class SpoonacularHttpClient
