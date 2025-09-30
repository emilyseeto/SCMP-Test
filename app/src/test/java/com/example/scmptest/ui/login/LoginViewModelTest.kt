package com.example.scmptest.ui.login

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.scmptest.R
import com.example.scmptest.data.api.ApiService
import com.example.scmptest.data.model.LoginRequest
import com.example.scmptest.data.model.LoginResponse
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val application = mockk<Application>(relaxed = true)

    private lateinit var viewModel: LoginViewModel
    private lateinit var emailObserver: Observer<String>
    private lateinit var pwdObserver: Observer<String>
    private lateinit var emailErrorObserver: Observer<Boolean>
    private lateinit var pwdErrorObserver: Observer<Boolean>
    private lateinit var isLoadingObserver: Observer<Boolean>
    private lateinit var loginTokenObserver: Observer<String?>
    private lateinit var loginErrorObserver: Observer<String?>

    private val validEmail = "test@example.com.hk"
    private val invalidEmail1 = "test"
    private val invalidEmail2 = "test@"
    private val invalidEmail3 = "test@example"
    private val invalidEmail4 = "@example"
    private val invalidEmail5 = "@example.com"
    private val invalidEmail6 = "example.com"

    private val validPassword = "password12"
    private val invalidPassword1 = "123"
    private val invalidPassword2 = "password1234"

    private val token = "token"
    private val errorMsg = "Api failed"
    private val genericErrorMsg = "Generic error"

    private val loginSuccessResponse = Response.success(LoginResponse(token))
    private val loginSuccessResponseBlankToken = Response.success(LoginResponse(" "))
    private val loginSuccessResponseNullToken = Response.success(LoginResponse(null))
    private val loginFailedResponse =
        Response.error<LoginResponse>(401, "{ \"error\": \"${errorMsg}\" }".toResponseBody())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { application.getString(R.string.common_generic_error_msg) } returns genericErrorMsg

        mockkObject(ApiService)

        emailObserver = mockk<Observer<String>>(relaxed = true)
        pwdObserver = mockk<Observer<String>>(relaxed = true)
        emailErrorObserver = mockk<Observer<Boolean>>(relaxed = true)
        pwdErrorObserver = mockk<Observer<Boolean>>(relaxed = true)
        isLoadingObserver = mockk<Observer<Boolean>>(relaxed = true)
        loginTokenObserver = mockk<Observer<String?>>(relaxed = true)
        loginErrorObserver = mockk<Observer<String?>>(relaxed = true)

        viewModel = LoginViewModel(application).apply {
            email.observeForever(emailObserver)
            pwd.observeForever(pwdObserver)
            emailError.observeForever(emailErrorObserver)
            pwdError.observeForever(pwdErrorObserver)
            isLoading.observeForever(isLoadingObserver)
            loginToken.observeForever(loginTokenObserver)
            loginError.observeForever(loginErrorObserver)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `setEmail should update email value`() {
        viewModel.apply {
            setEmail(validEmail)

            assertEquals(validEmail, email.value)
        }


    }

    @Test
    fun `setPwd should update password value`() {
        viewModel.apply {
            setPwd(validPassword)

            assertEquals(validPassword, pwd.value)
        }
    }

    @Test
    fun `clearEmailError should set email error to false`() {
        viewModel.apply {
            clearEmailError()

            assertFalse(emailError.value!!)
        }
    }

    @Test
    fun `clearPwdError should set password error to false`() {
        viewModel.apply {
            clearPwdError()

            assertFalse(pwdError.value!!)
        }
    }

    @Test
    fun `validateInputs should not set email error for valid email`() {
        viewModel.apply {
            setEmail(validEmail)
            login()

            assertFalse(emailError.value!!)
        }
    }

    @Test
    fun `validateInputs should set email error for invalid email 1`() {
        viewModel.apply {
            setEmail(invalidEmail1)
            login()

            assertTrue(emailError.value!!)
        }
    }

    @Test
    fun `validateInputs should set email error for invalid email 2`() {
        viewModel.apply {
            setEmail(invalidEmail2)
            login()

            assertTrue(emailError.value!!)
        }
    }

    @Test
    fun `validateInputs should set email error for invalid email 3`() {
        viewModel.apply {
            setEmail(invalidEmail3)
            login()

            assertTrue(emailError.value!!)
        }
    }

    @Test
    fun `validateInputs should set email error for invalid email 4`() {
        viewModel.apply {
            setEmail(invalidEmail4)
            login()

            assertTrue(emailError.value!!)
        }
    }

    @Test
    fun `validateInputs should set email error for invalid email 5`() {
        viewModel.apply {
            setEmail(invalidEmail5)
            login()

            assertTrue(emailError.value!!)
        }
    }

    @Test
    fun `validateInputs should set email error for invalid email 6`() {
        viewModel.apply {
            setEmail(invalidEmail6)
            login()

            assertTrue(emailError.value!!)
        }
    }

    @Test
    fun `validateInputs should not set password error for valid password`() {
        viewModel.apply {
            setPwd(validPassword)
            login()

            assertFalse(pwdError.value!!)
        }
    }

    @Test
    fun `validateInputs should set password error for short password`() {
        viewModel.apply {
            setPwd(invalidPassword1)
            login()

            assertTrue(pwdError.value!!)
        }
    }

    @Test
    fun `validateInputs should set password error for long password`() {
        viewModel.apply {
            setPwd(invalidPassword2)
            login()

            assertTrue(pwdError.value!!)
        }
    }

    @Test
    fun `login should not proceed when email validation fails`() {
        viewModel.apply {
            setEmail(invalidEmail1)
            setPwd(validPassword)
            login()
        }

        coVerify(exactly = 0) {
            ApiService.scmpApi.login(any())
        }
    }

    @Test
    fun `login should not proceed when password validation fails`() {
        viewModel.apply {
            setEmail(validEmail)
            setPwd(invalidPassword1)
            login()
        }

        coVerify(exactly = 0) {
            ApiService.scmpApi.login(any())
        }
    }

    @Test
    fun `login should not proceed when both email and password validation fails`() {
        viewModel.apply {
            setEmail(invalidEmail1)
            setPwd(invalidPassword1)
            login()
        }

        coVerify(exactly = 0) {
            ApiService.scmpApi.login(any())
        }
    }

    @Test
    fun `login should proceed when both email and password validation success`() {
        coEvery { ApiService.scmpApi.login(any()) } returns loginSuccessResponse

        viewModel.apply {
            setEmail(validEmail)
            setPwd(validPassword)
            login()
        }

        coVerify {
            ApiService.scmpApi.login(
                LoginRequest(
                    email = validEmail,
                    password = validPassword
                )
            )
        }
    }

    @Test
    fun `login should call API with valid credentials`() {
        coEvery { ApiService.scmpApi.login(any()) } returns loginSuccessResponse

        viewModel.apply {
            setEmail(validEmail)
            setPwd(validPassword)
            login()

            assertEquals(token, loginToken.value)
            assertNull(loginError.value)
        }
    }

    @Test
    fun `login should handle error when API returned blank token`() {
        coEvery { ApiService.scmpApi.login(any()) } returns loginSuccessResponseBlankToken

        viewModel.apply {
            setEmail(validEmail)
            setPwd(validPassword)
            login()

            assertNull(loginToken.value)
            assertEquals(genericErrorMsg, loginError.value)
        }
    }

    @Test
    fun `login should handle error when API returned null token`() {
        coEvery { ApiService.scmpApi.login(any()) } returns loginSuccessResponseNullToken

        viewModel.apply {
            setEmail(validEmail)
            setPwd(validPassword)
            login()

            assertNull(loginToken.value)
            assertEquals(genericErrorMsg, loginError.value)
        }
    }

    @Test
    fun `login should handle error when API returned null body`() {
        coEvery { ApiService.scmpApi.login(any()) } returns Response.success(null)

        viewModel.apply {
            setEmail(validEmail)
            setPwd(validPassword)
            login()

            assertNull(loginToken.value)
            assertEquals(genericErrorMsg, loginError.value)
        }
    }

    @Test
    fun `login should handle API error response`() {
        coEvery { ApiService.scmpApi.login(any()) } returns loginFailedResponse

        viewModel.apply {
            setEmail(validEmail)
            setPwd(validPassword)
            login()

            assertNull(loginToken.value)
            assertEquals(errorMsg, loginError.value)
        }
    }

    @Test
    fun `login should handle exception`() {
        coEvery { ApiService.scmpApi.login(any()) } throws Exception(errorMsg)

        viewModel.apply {
            setEmail(validEmail)
            setPwd(validPassword)
            login()

            assertNull(loginToken.value)
            assertEquals(errorMsg, loginError.value)
        }
    }

    @Test
    fun `clearLoginStatus should reset token and error after login success`() {
        coEvery { ApiService.scmpApi.login(any()) } returns loginSuccessResponse

        viewModel.apply {
            setEmail(validEmail)
            setPwd(validPassword)
            login()

            assertEquals(token, loginToken.value)

            clearLoginStatus()

            assertNull(loginToken.value)
            assertNull(loginError.value)
        }
    }

    @Test
    fun `clearLoginStatus should reset token and error after login failed`() {
        coEvery { ApiService.scmpApi.login(any()) } returns loginFailedResponse

        viewModel.apply {
            setEmail(validEmail)
            setPwd(validPassword)
            login()

            assertEquals(errorMsg, loginError.value)

            clearLoginStatus()

            assertNull(loginToken.value)
            assertNull(loginError.value)
        }
    }
}
