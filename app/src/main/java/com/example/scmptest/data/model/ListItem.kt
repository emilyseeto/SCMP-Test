package com.example.scmptest.data.model

sealed class ListItem {
    data class Staff(
        val id: Int?,
        val email: String?,
        val first_name: String?,
        val last_name: String?,
        val avatar: String?
    ) : ListItem()

    object LoadMore : ListItem()
}
