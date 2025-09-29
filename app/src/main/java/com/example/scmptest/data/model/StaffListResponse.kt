package com.example.scmptest.data.model

data class StaffListResponse(
    val page: Int?,
    val per_page: Int?,
    val total: Int?,
    val total_pages: Int?,
    val data: List<ListItem.Staff>?
)
