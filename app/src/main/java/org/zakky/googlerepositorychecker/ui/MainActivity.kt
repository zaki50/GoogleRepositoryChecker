package org.zakky.googlerepositorychecker.ui

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.zakky.googlerepositorychecker.MyApplication
import org.zakky.googlerepositorychecker.R
import org.zakky.googlerepositorychecker.model.Artifact
import org.zakky.googlerepositorychecker.retrofit2.service.GoogleRepositoryService
import retrofit2.Retrofit
import toothpick.Scope
import toothpick.Toothpick
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.reflect.KFunction


class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = OnNavigationItemSelectedListener { item ->
        val index = pagerAdapter.indexForMenuId(item.itemId)
        if (index < 0) {
            return@OnNavigationItemSelectedListener false
        }

        pager.setCurrentItem(index, true)
        return@OnNavigationItemSelectedListener true
    }

    private val pagerAdapter: MainFragmentStatePagerAdapter by lazy { MainFragmentStatePagerAdapter(supportFragmentManager) }

    private lateinit var scope: Scope

    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var realm: Realm

    private var refreshing: Subscription? = null

    private lateinit var allGroups: RealmResults<Artifact>

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                refreshData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        scope = Toothpick.openScope(MyApplication.APP_SCOPE_NAME)
        Toothpick.inject(this, scope)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pager.apply {
            adapter = pagerAdapter
            addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    navigation.selectedItemId = pagerAdapter.menuIdForPageIndex(position)
                }
            })
        }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        allGroups = realm.where(Artifact::class.java).findAll()
        allGroups.addChangeListener(RealmChangeListener {
            Toast.makeText(this, it.joinToString(), Toast.LENGTH_SHORT).show()
        })

        if (allGroups.isEmpty()) {
            refreshData()
        }
    }

    private fun refreshData() {
        val service = retrofit.create(GoogleRepositoryService::class.java)
        service.listGroups()
                .flatMapPublisher { groupNames -> Single.merge(groupNames.map { service.listArtifact(GoogleRepositoryService.toPath(it)) }) }
                .subscribeOn(Schedulers.io())
                .subscribe(object : Subscriber<List<Artifact>> {
                    private var isFirstSave = true

                    override fun onSubscribe(s: Subscription) {
                        refreshing = s
                        s.request(Long.MAX_VALUE)
                    }

                    override fun onNext(artifacts: List<Artifact>) {
                        scope.getInstance(Realm::class.java).use {
                            it.executeTransaction {
                                if (isFirstSave) {
                                    it.delete(Artifact::class.java)
                                    isFirstSave = false
                                }
                                it.copyToRealmOrUpdate(artifacts)
                            }
                        }
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

class MainFragmentStatePagerAdapter(supportFragmentManager: FragmentManager) : FragmentStatePagerAdapter(supportFragmentManager) {
    val fragmentsList = listOf<Pair<KClass<out Fragment>, KFunction<Fragment>>>(
            FavoritesFragment::class to FavoritesFragment.Companion::newInstance,
            AllGroupsFragment::class to AllGroupsFragment.Companion::newInstance,
            SettingsFragment::class to SettingsFragment.Companion::newInstance
    )

    val navigationMenuIds = listOf<Int>(
            R.id.navigation_favorite,
            R.id.navigation_list,
            R.id.navigation_settings
    )

    override fun getItem(position: Int): Fragment = fragmentsList[position].second.call()

    override fun getCount() = fragmentsList.size

    @IdRes
    fun menuIdForPageIndex(index: Int): Int {
        return navigationMenuIds[index]
    }
    fun indexForMenuId(@IdRes menuItemId: Int): Int {
        return navigationMenuIds.indexOf(menuItemId)
    }
}
