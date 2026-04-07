package com.mockdonalds.app.features.home.data

import com.mockdonalds.app.features.home.api.domain.Craving
import com.mockdonalds.app.features.home.api.domain.ExploreItem
import com.mockdonalds.app.features.home.api.domain.HeroPromotion
import com.mockdonalds.app.features.home.domain.HomeRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
class HomeRepositoryImpl : HomeRepository {

    override fun getUserName(): Flow<String> = flowOf("Alex Mercer")

    override fun getHeroPromotion(): Flow<HeroPromotion> = flowOf(
        HeroPromotion(
            title = "Midnight Truffle Burger",
            description = "Double-aged Wagyu beef, shaved Perigord truffles, and gold-dusted brioche.",
            tag = "LIMITED EDITION",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD3XLmAyDLCbsMni75FhYeTO5jDtRg2NYPJKTxNbg4aleYFXC5xEXR3Y5hbq8-q9a5FKcQX6SBsY9mW5l3LFaBdXetovbtu6rFyBGMpAtYcCGVhtAhSsLhMOA9SzEQq6UUAVNhJMkSlDOtmR_L3LrJaSc-HN2aOIu7iEpAwKNrEWGP6Dk9E0tzIWrUEUQZCkrXKLjo2RtdB-aI_oRSLdnTwbnQPdXM5KV-VPld0gAdzjzoJrlCTM-h3Q5dEoLNSC587K_0ossB5VKQ",
            ctaText = "Experience Now",
        )
    )

    override fun getRecentCravings(): Flow<List<Craving>> = flowOf(
        listOf(
            Craving(
                id = "1",
                title = "Smoky Bleu Royale",
                subtitle = "\$18.50 • 2 days ago",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAEUcSbBQQ1D_rTaoTt2M29Lrvc1k31ru-Yy2v9jETAX6Imb1Z3xkCnOGzwODlYEAmG-R6fG5GntSwCtMAyPKuKob4H0uGzqPSVCOkfE7akZl2l1MV_VPmX-9ixfI5RwMKqW6N2IX27gFWM7CErxP79myBoFx-sbk1M8I7I1lQ0aDnB0ung7sBer2nkJH2VaCbA4dlpnq0vXETYCe1I0rCRBosLC-VKu0aQONfSgfkE_LcYwSpNIIExgurOMxr6uB4R8nQlTeS622s",
            ),
            Craving(
                id = "2",
                title = "Roseway Fries (L)",
                subtitle = "\$6.20 • 5 days ago",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAek0b7xlNHx9hvtl19bOmndTLwRB59O51tbuaU6ddCqFCFHI6dWkI_9_1NS1MSfhg9-VPJGH9Ete-6g-o1qPhb8Q1HKjd_LA2w_Si34P5v2NyiJwlP3SH-zp3fXQwvSZQHuv_VWKgBEqo1pHgqATbotVPqPv876wMCIDmrlT8U6vNb3MgskQ1Jg2mBjPu2mecMMi697HR-20rVDQQ4aFpxkIUTJwQg1WFVa9moh-LRTpsH-jKrtb9PiyIGEXeTtCLGJkrY8WQN5sM",
            ),
            Craving(
                id = "3",
                title = "Velvet Cola Fizz",
                subtitle = "\$4.50 • 1 week ago",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDIID3SWN4ATvLcZ6sUpRRaN-NTdxu7MHXohCtFpcxQLSR5DjiSX14VgesUoV5lcNFJ-HGoLoEUkQDJNu7_WH9ftDn_N7OR63_ryAaOtRfIqegQ2VSeWkPQNfJuQtsFdElJH3gLhP-m-fGpl_mkNAzbEO-gfRSGjUxEWVGbNyOmn1OkstqlIRZinzg0cBA_jBBo7O7nGbos-OFvb1m6yeAJwLr53Yw32vXo62Tnkzxe8jg5wVEeBzEWFIxwZqt5MDwlm7HEG8_4Ln4",
            ),
        )
    )

    override fun getExploreItems(): Flow<List<ExploreItem>> = flowOf(
        listOf(
            ExploreItem(id = "1", icon = "O", title = "Find", subtitle = "A Lounge Near You"),
            ExploreItem(id = "2", icon = "🎁", title = "Gift Cards", subtitle = "Share the Flavor"),
            ExploreItem(id = "3", icon = "🎧", title = "Concierge Support", subtitle = "Direct line to our kitchen masters"),
        )
    )
}
