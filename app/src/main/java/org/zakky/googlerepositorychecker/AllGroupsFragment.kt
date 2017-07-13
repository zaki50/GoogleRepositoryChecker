package org.zakky.googlerepositorychecker

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup

class AllGroupsFragment : Fragment() {
    companion object {
        fun newInstance(): AllGroupsFragment {
            return AllGroupsFragment()
        }
    }

    @Suppress("HasPlatformType")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater.inflate(R.layout.fragment_group_list, container, false)
}