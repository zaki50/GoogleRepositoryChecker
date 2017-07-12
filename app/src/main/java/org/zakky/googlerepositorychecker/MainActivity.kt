package org.zakky.googlerepositorychecker

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.zakky.googlerepositorychecker.retrofit2.converter.GoogleRepositoryXmlConverterFactory
import org.zakky.googlerepositorychecker.retrofit2.service.GoogleRepositoryService
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory


class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val retrofit = Retrofit.Builder().baseUrl("https://dl.google.com/dl/android/maven2/")
                .addConverterFactory(GoogleRepositoryXmlConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        val service = retrofit.create(GoogleRepositoryService::class.java);


        val listGroups = service.listGroups()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        listGroups.subscribe(object : SingleObserver<List<String>> {
            var disposable : Disposable? = null

            override fun onSubscribe(d: Disposable) {
                disposable = d
            }

            override fun onSuccess(body: List<String>) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, body.joinToString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(e: Throwable) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

}
