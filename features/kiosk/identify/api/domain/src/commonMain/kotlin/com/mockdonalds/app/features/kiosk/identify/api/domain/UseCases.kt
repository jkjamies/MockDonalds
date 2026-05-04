package com.mockdonalds.app.features.kiosk.identify.api.domain

import com.mockdonalds.app.core.centerpost.CenterPostInteractor
import com.mockdonalds.app.core.centerpost.CenterPostSubjectInteractor

abstract class GetIdentifyContent : CenterPostSubjectInteractor<Unit, IdentifyContent>()

abstract class IdentifyByPhoneNumber : CenterPostInteractor<String, IdentifyResult>()

abstract class IdentifyByQrCode : CenterPostInteractor<String, IdentifyResult>()

abstract class ContinueAsGuest : CenterPostInteractor<Unit, IdentifyResult>()
