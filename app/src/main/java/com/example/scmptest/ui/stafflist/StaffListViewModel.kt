package com.example.scmptest.ui.stafflist

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.scmptest.R
import com.example.scmptest.data.api.ApiService
import com.example.scmptest.data.model.ListItem
import com.example.scmptest.ext.getErrorMsg
import com.example.scmptest.ext.orZero
import kotlinx.coroutines.launch

class StaffListViewModel(application: Application) : AndroidViewModel(application) {


    private var _displayList = MutableLiveData<List<ListItem>>()
    val displayList: LiveData<List<ListItem>> = _displayList

    private var _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    @VisibleForTesting
    var pageCount: Int = 1
    @VisibleForTesting
    val staffList = mutableListOf<ListItem.Staff>()

    init {
        _isLoading.value = false
        _error.value = null
    }

    fun retrieveStaffList() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = ApiService.scmpApi.retrieveStaffList(pageCount)
                val genericError =
                    getApplication<Application>().getString(R.string.common_generic_error_msg)

                if (response.isSuccessful) {
                    response.body()?.let { res ->
                        if (!res.data.isNullOrEmpty()) {
                            staffList.addAll(res.data)
                            val list = mutableListOf<ListItem>()
                            list.addAll(staffList)
                            if (res.page.orZero() < res.total_pages.orZero()) {
                                list.add(ListItem.LoadMore)
                            }
                            _displayList.value = list
                        }
                        pageCount++
                    } ?: run {
                        _error.value = genericError
                    }
                } else {
                    _error.value = response.errorBody().getErrorMsg(genericError)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
