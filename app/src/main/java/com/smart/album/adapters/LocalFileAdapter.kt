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
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.api.services.drive.model.File
import com.smart.album.R
import kotlinx.coroutines.withContext

class LocalFileAdapter(var context: Context, private var files: List<DocumentFile>) :
    RecyclerView.Adapter<LocalFileAdapter.FileViewHolder>() {

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
        holder.nameText.text = ""
        holder.typeText.text = ""
        // 使用Glide加载图片
        Glide.with(holder.imageView.context)
            .load(file.uri)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .centerCrop()
            .into(holder.imageView)
    }

    override fun getItemCount() = files.size

    fun updateFiles(newFiles: List<DocumentFile>) {
        files = newFiles
        notifyDataSetChanged()
    }

}
