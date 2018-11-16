package org.zakky.googlerepositorychecker.toothpick

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import io.realm.Realm
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.zakky.googlerepositorychecker.BuildConfig
import org.zakky.googlerepositorychecker.MyApplication
import org.zakky.googlerepositorychecker.retrofit2.converter.GoogleRepositoryXmlConverterFactory
import retrofit2.Retrofit
import toothpick.config.Module

class ApplicationModule(@Suppress("UNUSED_PARAMETER") app: MyApplication) : Module() {
    init {
        bindRetrofitAsSingleton()

        bindRealm()
    }

    private fun bindRetrofitAsSingleton() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val retrofit = Retrofit.Builder().baseUrl("https://dl.google.com/dl/android/maven2/")
                .client(client)
                .addConverterFactory(GoogleRepositoryXmlConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()

        bind(Retrofit::class.java).toInstance(retrofit)
    }

    private fun bindRealm() {
        bind(Realm::class.java).toProviderInstance(
                RealmProvider(BuildConfig.BUILD_TYPE == "release"))
    }
}