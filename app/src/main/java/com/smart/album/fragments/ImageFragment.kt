package com.smart.album.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.smart.album.R
import com.smart.album.utils.BlurBuilder
import com.smart.album.events.ImageDisplayEvent
import com.smart.album.utils.PreferencesHelper
import com.smart.album.views.PanningImageView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.max

class ImageFragment : Fragment() {

    private var imageUrl: String? = null
    private lateinit var panImageView: PanningImageView
    private lateinit var scaleImageView: ImageView
    private lateinit var cropImageView: ImageView
    private lateinit var zoomImageView: ImageView

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
        zoomImageView = view.findViewById<ImageView>(R.id.zoom_img)

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
                        .asBitmap()
                        .load(it)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                // 虚化背景
                                val blurredBackground = BlurBuilder.blur(requireActivity(), resource)
                                // 将虚化的 Bitmap 设置为 ImageView 的背景
                                scaleImageView.background = BitmapDrawable(resources, blurredBackground)
                                // 设置图片到 ImageView
                                scaleImageView.setImageBitmap(resource)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                // 图片加载失败时的处理
                            }
                        })
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
            3,4->{
                panImageView.visibility = View.GONE
                scaleImageView.visibility = View.GONE
                cropImageView.visibility = View.GONE
                zoomImageView.visibility = View.VISIBLE
                imageUrl?.let {
                    Glide.with(this)
                        .asBitmap()
                        .load(it)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                // 虚化背景
                                val blurredBackground = BlurBuilder.blur(requireActivity(), resource)
                                // 将虚化的 Bitmap 设置为 ImageView 的背景
                                zoomImageView.background = BitmapDrawable(resources, blurredBackground)
                                // 设置图片到 ImageView
                                zoomImageView.setImageBitmap(resource)
                                val viewWidth = resources.displayMetrics.widthPixels
                                val viewHeight = resources.displayMetrics.heightPixels
                                // 计算图片初始尺寸
                                val imageWidth = resource.width
                                val imageHeight = resource.height

                                // 计算初始缩放比例
                                var initialScale = 1.0f
                                var finalScale = if(viewWidth > imageWidth && viewHeight > imageHeight){
                                    max(viewWidth.toFloat() / imageWidth, viewHeight.toFloat() / imageHeight)
                                } else {
                                    max( imageWidth / viewWidth.toFloat(), imageHeight / viewHeight.toFloat())
                                }
                                if(finalScale < 3.0f){
                                    finalScale = 3.0f
                                }
                                zoomImageView.scaleX = initialScale
                                zoomImageView.scaleY = initialScale
                                val scaleXAnimator = ObjectAnimator.ofFloat(zoomImageView, "scaleX", finalScale,initialScale)
                                val scaleYAnimator = ObjectAnimator.ofFloat(zoomImageView, "scaleY", finalScale,initialScale)

                                val animatorSet = AnimatorSet()
                                // 将动画添加到AnimatorSet中
                                animatorSet.playTogether(scaleXAnimator, scaleYAnimator)
                                animatorSet.duration = 3000  // 动画持续时间2秒
                                // 开始动画
                                animatorSet.start()
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                // 图片加载失败时的处理
                            }
                        })
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
        when(displayEffect){
            0->{
                panImageView.requestLayout()
                panImageView.invalidate()
            }
            3,4->{
                imageUrl?.let {
                    Glide.with(this)
                        .asBitmap()
                        .load(it)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                // 虚化背景
                                val blurredBackground = BlurBuilder.blur(requireActivity(), resource)
                                // 将虚化的 Bitmap 设置为 ImageView 的背景
                                zoomImageView.background = BitmapDrawable(resources, blurredBackground)
                                // 设置图片到 ImageView
                                zoomImageView.setImageBitmap(resource)
                                val viewWidth = resources.displayMetrics.widthPixels
                                val viewHeight = resources.displayMetrics.heightPixels
                                // 计算图片初始尺寸
                                val imageWidth = resource.width
                                val imageHeight = resource.height

                                // 计算初始缩放比例
                                var initialScale = 1.0f
                                var finalScale = if(viewWidth > imageWidth && viewHeight > imageHeight){
                                    max(viewWidth.toFloat() / imageWidth, viewHeight.toFloat() / imageHeight)
                                } else {
                                    max( imageWidth / viewWidth.toFloat(), imageHeight / viewHeight.toFloat())
                                }
                                if(finalScale < 3.0f){
                                    finalScale = 3.0f
                                }
                                zoomImageView.scaleX = initialScale
                                zoomImageView.scaleY = initialScale
                                val scaleXAnimator = ObjectAnimator.ofFloat(zoomImageView, "scaleX", finalScale,initialScale)
                                val scaleYAnimator = ObjectAnimator.ofFloat(zoomImageView, "scaleY", finalScale,initialScale)

                                val animatorSet = AnimatorSet()
                                // 将动画添加到AnimatorSet中
                                animatorSet.playTogether(scaleXAnimator, scaleYAnimator)
                                animatorSet.duration = 2000  // 动画持续时间2秒
                                // 开始动画
                                animatorSet.start()
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                // 图片加载失败时的处理
                            }
                        })
                }
            }
        }

        super.onResume()
    }
}