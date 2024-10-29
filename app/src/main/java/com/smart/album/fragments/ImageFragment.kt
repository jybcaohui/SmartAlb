package com.smart.album.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.smart.album.R
import com.smart.album.views.PanningImageView

class ImageFragment : Fragment() {

    private var imageUrl: String? = null
    private lateinit var imageView: PanningImageView

    companion object {
        const val IMAGE_URL = "IMAGE_URL"

        fun newInstance(imageUrl: String): ImageFragment {
            val fragment = ImageFragment()
            val args = Bundle()
            args.putString(IMAGE_URL, imageUrl)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageUrl = it.getString(IMAGE_URL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView = view.findViewById<PanningImageView>(R.id.imageView)
        Log.d("albs===","imageUrl==="+imageUrl)
        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .into(imageView)
        }
    }

    override fun onResume() {
        imageView.requestLayout()
        imageView.invalidate()
        super.onResume()
    }
}