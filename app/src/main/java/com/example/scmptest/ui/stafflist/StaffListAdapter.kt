package com.example.scmptest.ui.stafflist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.scmptest.data.model.ListItem
import com.example.scmptest.databinding.ViewLoadMoreBinding
import com.example.scmptest.databinding.ViewStaffItemBinding


class StaffListAdapter(
    private val onLoadMoreClick: () -> Unit
) : RecyclerView.Adapter<StaffListAdapter.BaseViewHolder>() {

    companion object {
        private const val VIEW_TYPE_STAFF = 0
        private const val VIEW_TYPE_LOAD_MORE = 1
    }

    private var list: List<ListItem> = emptyList()

    fun updateList(newList: List<ListItem>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        return when (list[position]) {
            is ListItem.Staff -> VIEW_TYPE_STAFF
            else -> VIEW_TYPE_LOAD_MORE
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_STAFF -> createStaffViewHolder(parent)
            else -> createLoadMoreViewHolder(parent)
        }
    }

    private fun createStaffViewHolder(parent: ViewGroup): StaffViewHolder {
        val binding = ViewStaffItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StaffViewHolder(binding)
    }

    private fun createLoadMoreViewHolder(parent: ViewGroup): LoadMoreViewHolder {
        val binding = ViewLoadMoreBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LoadMoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (val item = list[position]) {
            is ListItem.Staff -> {
                (holder as StaffViewHolder).bind(item)
            }

            is ListItem.LoadMore -> {
                (holder as LoadMoreViewHolder).bind()
            }
        }
    }

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class StaffViewHolder(
        private val binding: ViewStaffItemBinding
    ) : BaseViewHolder(binding.root) {

        fun bind(staff: ListItem.Staff) {
            binding.apply {
                staffItemName.text = listOfNotNull(staff.first_name, staff.last_name)
                    .joinToString(separator = " ")
                staffItemEmail.text = staff.email.orEmpty()
                staff.avatar.takeUnless { it.isNullOrBlank() }?.let {
                    Glide.with(binding.root.context)
                        .load(it)
                        .circleCrop()
                        .into(binding.staffItemAvatar)
                }
            }
        }
    }

    inner class LoadMoreViewHolder(
        private val binding: ViewLoadMoreBinding
    ) : BaseViewHolder(binding.root) {

        fun bind() {
            binding.loadMoreBtn.setOnClickListener {
                onLoadMoreClick()
            }
        }
    }
}