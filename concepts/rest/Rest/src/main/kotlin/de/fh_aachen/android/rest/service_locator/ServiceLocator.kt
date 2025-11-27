// (C) 2024 A.Voß, a.voss@fh-aachen.de, apps@codebasedlearning.dev

package de.fh_aachen.android.rest.service_locator

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.fh_aachen.android.rest.model.UserActivityApi
import de.fh_aachen.android.rest.model.UserActivityRepository
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

// Use lazy for Resource-Intensive or Rarely Used Objects

object ServiceLocator {
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // Use a single Retrofit instance wherever possible (for each BaseUrl).
    // Generate multiple service interfaces using retrofit.create() on the singleton instance.
    private val retrofitTypicode: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private val userActivityApi: UserActivityApi by lazy {
        retrofitTypicode.create(UserActivityApi::class.java)
    }

    val userActivityRepository: UserActivityRepository by lazy {
        UserActivityRepository(userActivityApi)
    }

    // who knows
    fun initialize() { }
}
