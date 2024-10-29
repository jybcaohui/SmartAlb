package com.smart.album.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.api.services.drive.model.File
import com.smart.album.R
import kotlinx.coroutines.withContext

class DriveFileAdapter(var context: Context, private var files: List<File>) :
    RecyclerView.Adapter<DriveFileAdapter.FileViewHolder>() {

    class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.fileImageView)
        val nameText: TextView = view.findViewById(R.id.fileNameText)
        val typeText: TextView = view.findViewById(R.id.fileTypeText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drive_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.nameText.text = file.name
        holder.typeText.text = file.mimeType

        // 检查是否是图片文件
        if (isImageFile(file.mimeType)) {
            // 构建图片URL
            val imageUrl = "https://drive.google.com/uc?export=download&id=${file.id}"
            Log.d("albs===","imgurl==${imageUrl}==${file.name}")

            // 使用Glide加载图片
            Glide.with(holder.imageView.context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(holder.imageView)

            holder.imageView.setOnClickListener{
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl))
                context.startActivity(intent)
            }
        } else {
            // 如果不是图片，显示默认图标
            holder.imageView.setImageResource(R.drawable.placeholder_image)
        }
    }

    override fun getItemCount() = files.size

    fun updateFiles(newFiles: List<File>) {
        files = newFiles
        notifyDataSetChanged()
    }

    private fun isImageFile(mimeType: String?): Boolean {
        return mimeType?.startsWith("image/") == true
    }
}
