package com.example.screensense.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.screensense.R
import com.example.screensense.model.AppInfo
import java.util.concurrent.TimeUnit

class AppListAdapter(
    private val apps: List<AppInfo>,
    private val onItemClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_card, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.bind(app)
        holder.itemView.setOnClickListener { onItemClick(app) }
    }

    override fun getItemCount(): Int = apps.size

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_app_icon)
        private val tvName: TextView = itemView.findViewById(R.id.tv_app_name)
        private val tvLimit: TextView = itemView.findViewById(R.id.tv_limit)
        private val tvUsedToday: TextView = itemView.findViewById(R.id.tv_used_today)

        fun bind(app: AppInfo) {
            ivIcon.setImageDrawable(app.appIcon)
            tvName.text = app.appName

            // Formatear el tiempo
            tvLimit.text = "Límite: ${formatMillis(app.limitTimeMillis)}"
            tvUsedToday.text = "Usado hoy: ${formatMillis(app.usageTodayMillis)}"
        }

        private fun formatMillis(millis: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(millis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
            return "${hours}h ${minutes}min"
        }
    }
}
