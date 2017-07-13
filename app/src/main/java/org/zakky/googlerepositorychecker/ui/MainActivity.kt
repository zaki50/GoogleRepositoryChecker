package org.zakky.googlerepositorychecker.ui

import android.os.Bundle
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
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




//        val listGroups = service.listGroups()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//
//        listGroups.subscribe(object : SingleObserver<List<String>> {
//            var disposable : Disposable? = null
//
//            override fun onSubscribe(d: Disposable) {
//                disposable = d
//            }
//
//            override fun onSuccess(body: List<String>) {
//                runOnUiThread {
//                    Toast.makeText(this@MainActivity, body.joinToString(), Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onError(e: Throwable) {
//                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
//            }
//        })


        val artifact = service.listArtifact(GoogleRepositoryService.toPath("com.android.support.constraint"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        artifact.subscribe(object : SingleObserver<List<Artifact>> {
            var disposable: Disposable? = null

            override fun onSubscribe(d: Disposable) {
                disposable = d
            }

            override fun onSuccess(body: List<Artifact>) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, body.joinToString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(e: Throwable) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }
}
