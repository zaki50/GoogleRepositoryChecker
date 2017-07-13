package org.zakky.googlerepositorychecker.toothpick

import io.realm.Realm
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.zakky.googlerepositorychecker.BuildConfig
import org.zakky.googlerepositorychecker.MyApplication
import org.zakky.googlerepositorychecker.retrofit2.converter.GoogleRepositoryXmlConverterFactory
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import toothpick.config.Module

class ApplicationModule(@Suppress("UNUSED_PARAMETER") app: MyApplication) : Module() {
    init {
        bindRetrofitAsSingleton()

        bindRealm()
    }

    private fun bindRetrofitAsSingleton() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val retrofit = Retrofit.Builder().baseUrl("https://dl.google.com/dl/android/maven2/")
                .client(client)
                .addConverterFactory(GoogleRepositoryXmlConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        bind(Retrofit::class.java).toInstance(retrofit)
    }

    private fun bindRealm() {
        bind(Realm::class.java).toProviderInstance(
                RealmProvider(BuildConfig.BUILD_TYPE == "release"))
    }
}