package org.zakky.googlerepositorychecker.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.zakky.googlerepositorychecker.MyApplication
import org.zakky.googlerepositorychecker.R
import org.zakky.googlerepositorychecker.model.Artifact
import org.zakky.googlerepositorychecker.model.Group
import org.zakky.googlerepositorychecker.realm.*
import org.zakky.googlerepositorychecker.retrofit2.service.GoogleRepositoryService
import retrofit2.Retrofit
import toothpick.Scope
import toothpick.Toothpick
import javax.inject.Inject
import kotlin.reflect.KFunction0


class MainActivity : AppCompatActivity() {

    companion object {
        val STATE_CURRENT_FRAGMENT = "current_fragment"

        val fragmentsFactories: Array<KFunction0<Fragment>> = arrayOf(
                FavoritesFragment.Companion::newInstance,
                AllGroupsFragment.Companion::newInstance,
                SettingsFragment.Companion::newInstance
        )

    }

    private lateinit var scope: Scope

    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var realm: Realm

    private var refreshing: Subscription? = null

    private lateinit var refreshMenu: MenuItem

    private lateinit var navigationMenuIds: IntArray

    private var currentFragmentIndex = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        scope = Toothpick.openScope(MyApplication.APP_SCOPE_NAME)
        Toothpick.inject(this, scope)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            replaceFragment(currentFragmentIndex)
        } else {
            currentFragmentIndex = savedInstanceState.getInt(STATE_CURRENT_FRAGMENT)
        }

        navigationMenuIds = IntArray(navigation.menu.size()) { index -> navigation.menu.getItem(index).itemId }
        navigation.selectedItemId = navigationMenuIds[currentFragmentIndex]

        navigation.setOnNavigationItemSelectedListener {
            val index = navigationMenuIds.indexOf(it.itemId)
            if (index < 0) {
                return@setOnNavigationItemSelectedListener false
            }

            replaceFragment(index)

            return@setOnNavigationItemSelectedListener true
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }

    override fun onPause() {
        super.onPause()

        refreshing?.cancel()
        refreshing = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        refreshMenu = menu.findItem(R.id.refresh)

        // FIXME we are calling refreshData() here since it must be called after initialization of `refreshMenu`
        if (!realm.opContainsAnyArtifacts()) {
            refreshData()
        }

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(STATE_CURRENT_FRAGMENT, currentFragmentIndex)
    }

    private fun replaceFragment(fragmentIndex: Int) {
        val fragment = fragmentsFactories[fragmentIndex].invoke()
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, fragment)
        ft.commit()
        currentFragmentIndex = fragmentIndex
    }

    private fun refreshData() {
        val service = retrofit.create(GoogleRepositoryService::class.java)
        service.listGroups()
                .flatMapPublisher { groupNames -> Single.merge(groupNames.map { service.listArtifact(GoogleRepositoryService.toPath(it)) }) }
                .subscribeOn(Schedulers.io())
                .subscribe(object : Subscriber<List<Artifact>> {
                    private var isFirstSave = true

                    override fun onSubscribe(s: Subscription) {
                        runOnUiThread {
                            refreshing = s
                            refreshMenu.setEnabled(false)
                        }
                        s.request(Long.MAX_VALUE)
                    }

                    override fun onNext(artifacts: List<Artifact>) {
                        scope.getInstance(Realm::class.java).use { realm ->
                            realm.executeTransaction {
                                if (isFirstSave) {
                                    realm.opDeleteAllGroupsAndArtifacts()
                                    isFirstSave = false
                                }
                                it.opImportArtifacts(artifacts)
                            }
                        }
                    }

                    override fun onComplete() {
                        runOnUiThread {
                            refreshing = null
                            refreshMenu.isEnabled = true
                        }
                    }

                    override fun onError(t: Throwable) {
                        runOnUiThread {
                            refreshing = null
                            refreshMenu.isEnabled = true
                            Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                })
    }
}
