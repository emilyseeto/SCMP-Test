package com.example.scmptest.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.scmptest.ext.orFalse
import com.example.scmptest.ext.orZero
import java.util.regex.Pattern

class LoginViewModel : ViewModel() {

    private var _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private var _pwd = MutableLiveData<String>()
    val pwd: LiveData<String> = _pwd

    private var _emailError = MutableLiveData<Boolean>()
    val emailError: LiveData<Boolean> = _emailError

    private var _pwdError = MutableLiveData<Boolean>()
    val pwdError: LiveData<Boolean> = _pwdError

    private var _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        _email.value = ""
        _pwd.value = ""
        clearEmailError()
        clearPwdError()
        _isLoading.value = false
    }

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setPwd(pwd: String) {
        _pwd.value = pwd
    }

    fun clearEmailError() {
        _emailError.value = false
    }

    fun clearPwdError() {
        _pwdError.value = false
    }

    fun login() {
        _isLoading.value = true

        validateInputs()

        if (emailError.value.orFalse() || pwdError.value.orFalse()) {
            _isLoading.value = false
            return
        }

        // TODO call api
    }

    private fun validateInputs() {
        // Email validation
        val emailRegex = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")
        _emailError.value =
            email.value.isNullOrEmpty() || !emailRegex.matcher(email.value.orEmpty()).matches()

        // Password validation
        _pwdError.value = pwd.value?.length.orZero() < 6 || pwd.value?.length.orZero() > 10
    }
}
