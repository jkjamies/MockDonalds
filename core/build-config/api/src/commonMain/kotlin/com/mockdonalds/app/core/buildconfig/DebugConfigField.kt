package com.mockdonalds.app.core.buildconfig

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class DebugConfigField(val group: BuildConfigField.Group)
