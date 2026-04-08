@file:Suppress("MaxLineLength") // URLs in fake data

package com.mockdonalds.app.features.login.data

import com.mockdonalds.app.features.login.api.domain.LoginContent
import com.mockdonalds.app.features.login.domain.LoginRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
class LoginRepositoryImpl : LoginRepository {

    override fun getLoginContent(): Flow<LoginContent> = flowOf(
        LoginContent(
            logoUrl = "https://lh3.googleusercontent.com/aida/ADBb0ug8LykaV5_CQED6Y93QSHlt_s1X5Hg2mRDOMcHbsQvHUNtEFKDY_0z9I35zmg_ko5nUk5h6KdZ7BbBV19yQwK6I-erjbf1ATcnvJ4Jw_GSz9XdCKJoUcHeo8iZAEjxG7vTusoZNJ52DWxL314hnQUb-VXT8lVs00vXY1YlMnCbIgj3Xt6doOl4FVylhv1KC8PMk63tbb0xwAFTsSo70GPebivpmH5g1KkcRu2Stp-lKrYSF00Y02-1omRVcTIAdUXnKOARMksKC7g",
        ),
    )
}
