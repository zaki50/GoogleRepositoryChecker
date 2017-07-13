package org.zakky.googlerepositorychecker.ui

import android.os.Bundle
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.zakky.googlerepositorychecker.MyApplication
import org.zakky.googlerepositorychecker.R
import org.zakky.googlerepositorychecker.model.Artifact
import org.zakky.googlerepositorychecker.retrofit2.service.GoogleRepositoryService
import retrofit2.Retrofit
import toothpick.Toothpick
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.reflect.KFunction


class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_favorite -> {
                pager.setCurrentItem(0, true)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_list -> {
                pager.setCurrentItem(1, true)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                pager.setCurrentItem(2, true)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private val pagerAdapter: PagerAdapter by lazy {
        object : FragmentStatePagerAdapter(supportFragmentManager) {
            val fragmentsList = listOf<Pair<KClass<out Fragment>, KFunction<Fragment>>>(
                    FavoritesFragment::class to FavoritesFragment.Companion::newInstance,
                    AllGroupsFragment::class to AllGroupsFragment.Companion::newInstance,
                    SettingsFragment::class to SettingsFragment.Companion::newInstance
            )

            override fun getItem(position: Int): Fragment = fragmentsList[position].second.call()

            override fun getCount() = fragmentsList.size
        }
    }

    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var realm: Realm

    private var refreshing: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val scope = Toothpick.openScope(MyApplication.APP_SCOPE_NAME)
        Toothpick.inject(this, scope)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pager.apply {
            adapter = pagerAdapter
            addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    navigation.selectedItemId = when (position) {
                        0 -> R.id.navigation_favorite
                        1 -> R.id.navigation_list
                        2 -> R.id.navigation_settings
                        else -> 0
                    }
                }
            })
        }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val service = retrofit.create(GoogleRepositoryService::class.java)

        service.listGroups()
                .flatMapPublisher { groupNames -> Single.merge(groupNames.map { service.listArtifact(GoogleRepositoryService.toPath(it)) }) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<List<Artifact>> {
                    override fun onSubscribe(s: Subscription) {
                        refreshing = s
                        s.request(Long.MAX_VALUE)
                    }

                    override fun onNext(t: List<Artifact>) {
                        Toast.makeText(this@MainActivity, t.joinToString(), Toast.LENGTH_SHORT).show()
                    }

                    override fun onComplete() {
                        refreshing = null
                    }

                    override fun onError(t: Throwable) {
                        Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_SHORT).show()
                        refreshing = null
                    }
                })
    }

    override fun onPause() {
        super.onPause()

        refreshing?.cancel()
        refreshing = null
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }
}
