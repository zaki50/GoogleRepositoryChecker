package org.zakky.googlerepositorychecker.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.zakky.googlerepositorychecker.MyApplication
import org.zakky.googlerepositorychecker.R
import org.zakky.googlerepositorychecker.realm.opContainsAnyArtifacts
import org.zakky.googlerepositorychecker.realm.opDeleteAllGroupsAndArtifacts
import org.zakky.googlerepositorychecker.realm.opImportArtifacts
import org.zakky.googlerepositorychecker.retrofit2.service.GoogleRepositoryService
import retrofit2.Retrofit
import toothpick.Scope
import toothpick.Toothpick
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KFunction0


class MainActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        const val STATE_CURRENT_FRAGMENT = "current_fragment"

        val fragmentsFactories: Array<KFunction0<Fragment>> = arrayOf(
                FavoritesFragment.Companion::newInstance,
                AllGroupsFragment.Companion::newInstance,
                SettingsFragment.Companion::newInstance
        )

    }

    private val job = Job()

    override val coroutineContext: CoroutineContext
            get() = job + Dispatchers.Main

    private lateinit var scope: Scope

    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var realm: Realm

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

    override fun onDestroy(): Unit {
        super.onDestroy()

        job.cancel()

        realm.close()
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

        launch {
            var isFirstSave = true
            refreshMenu.isEnabled = false
            try {
                for (groupName in service.listGroups()) {
                    val artifacts = service.listArtifact(GoogleRepositoryService.toPath(groupName))
                    scope.getInstance(Realm::class.java).use { realm ->
                        realm.executeTransaction {
                            if (isFirstSave) {
                                isFirstSave = false
                                realm.opDeleteAllGroupsAndArtifacts()
                            }
                            it.opImportArtifacts(artifacts)
                        }
                    }
                }
            } catch (t: Throwable) {
                Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_SHORT).show()
            } finally {
                refreshMenu.isEnabled = true
            }
        }
    }
}
