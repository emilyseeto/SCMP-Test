package com.example.scmptest.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.scmptest.R
import com.example.scmptest.data.api.ApiService
import com.example.scmptest.data.model.LoginRequest
import com.example.scmptest.ext.getErrorMsg
import com.example.scmptest.ext.orFalse
import com.example.scmptest.ext.orZero
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class LoginViewModel(application: Application) : AndroidViewModel(application) {

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

    private var _loginToken = MutableLiveData<String?>()
    val loginToken: LiveData<String?> = _loginToken

    private var _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> = _loginError

    init {
        _email.value = ""
        _pwd.value = ""
        clearEmailError()
        clearPwdError()
        _isLoading.value = false
        _loginToken.value = null
        _loginError.value = null
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
        _loginError.value = null

        validateInputs()

        if (emailError.value.orFalse() || pwdError.value.orFalse()) {
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            try {
                val request = LoginRequest(
                    email = email.value.orEmpty(),
                    password = pwd.value.orEmpty()
                )

                val response = ApiService.scmpApi.login(request)
                val genericError =
                    getApplication<Application>().getString(R.string.common_generic_error_msg)

                if (response.isSuccessful) {
                    response.body()?.token.takeUnless { it.isNullOrEmpty() }?.let { token ->
                        _loginToken.value = token
                    } ?: run {
                        _loginError.value = genericError
                    }
                } else {
                    _loginError.value = response.errorBody().getErrorMsg(genericError)
                }
            } catch (e: Exception) {
                _loginError.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearLoginStatus() {
        _loginToken.value = null
        _loginError.value = null
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
