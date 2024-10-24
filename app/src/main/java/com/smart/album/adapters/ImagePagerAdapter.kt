package com.smart.album.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.smart.album.fragments.ImageFragment

class ImagePagerAdapter(fragmentActivity: FragmentActivity, private val imageUrls: List<String>) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = imageUrls.size

    override fun createFragment(position: Int): Fragment {
        return ImageFragment.newInstance(imageUrls[position])
    }
}