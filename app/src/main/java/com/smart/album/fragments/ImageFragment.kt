package com.smart.album.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.smart.album.R
import com.smart.album.utils.ImageDisplayEvent
import com.smart.album.utils.PreferencesHelper
import com.smart.album.views.PanningImageView
import com.smart.album.views.ZoomableImageView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ImageFragment : Fragment() {

    private var imageUrl: String? = null
    private lateinit var panImageView: PanningImageView
    private lateinit var scaleImageView: ImageView
    private lateinit var cropImageView: ImageView
    private lateinit var zoomImageView: ZoomableImageView

    private var displayEffect:Int = 0


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
        Log.d("albs===","imgurl==${imageUrl}")
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        panImageView = view.findViewById<PanningImageView>(R.id.pan_img)
        scaleImageView = view.findViewById<ImageView>(R.id.scale_center_img)
        cropImageView = view.findViewById<ImageView>(R.id.crop_center_img)
        zoomImageView = view.findViewById<ZoomableImageView>(R.id.zoom_img)

        initImageView()
    }

    fun initImageView(){
        displayEffect =  PreferencesHelper.getInstance(requireActivity()).getInt(PreferencesHelper.DISPLAY_EFFECT,0)
        when(displayEffect){
            0->{
                panImageView.visibility = View.VISIBLE
                scaleImageView.visibility = View.GONE
                cropImageView.visibility = View.GONE
                zoomImageView.visibility = View.GONE
                imageUrl?.let {
                    Glide.with(this)
                        .load(it)
                        .into(panImageView)
                }
            }
            1->{
                panImageView.visibility = View.GONE
                scaleImageView.visibility = View.VISIBLE
                cropImageView.visibility = View.GONE
                zoomImageView.visibility = View.GONE
                imageUrl?.let {
                    Glide.with(this)
                        .load(it)
                        .into(scaleImageView)
                }
            }
            2->{
                panImageView.visibility = View.GONE
                scaleImageView.visibility = View.GONE
                cropImageView.visibility = View.VISIBLE
                zoomImageView.visibility = View.GONE
                imageUrl?.let {
                    Glide.with(this)
                        .load(it)
                        .into(cropImageView)
                }
            }
            3->{
                panImageView.visibility = View.GONE
                scaleImageView.visibility = View.GONE
                cropImageView.visibility = View.GONE
                zoomImageView.visibility = View.VISIBLE
                imageUrl?.let {
                    Glide.with(this)
                        .load(it)
                        .apply(RequestOptions.centerCropTransform())
                        .into(zoomImageView)
                }
                // 图片加载完成后开始动画
                zoomImageView.post {
                    zoomImageView.startZoomAnimation()
                }
            }
            4->{
                panImageView.visibility = View.GONE
                scaleImageView.visibility = View.GONE
                cropImageView.visibility = View.GONE
                zoomImageView.visibility = View.VISIBLE
                imageUrl?.let {
                    Glide.with(this)
                        .load(it)
                        .apply(RequestOptions.centerCropTransform())
                        .into(zoomImageView)
                }
                // 图片加载完成后开始动画
                zoomImageView.post {
                    zoomImageView.startZoomAnimation()
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ImageDisplayEvent) {
        initImageView()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        panImageView.requestLayout()
        panImageView.invalidate()
        super.onResume()
    }
}