package org.zakky.googlerepositorychecker.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.zakky.googlerepositorychecker.R

class SettingsFragment : Fragment() {
    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

    @Suppress("HasPlatformType")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        @Suppress("UnnecessaryVariable")
        val view = inflater.inflate(R.layout.fragment_group_list, container, false)
        return view
    }
}
