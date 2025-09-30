package com.example.scmptest.ui.stafflist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.scmptest.R
import com.example.scmptest.data.api.ApiService
import com.example.scmptest.data.model.ListItem
import com.example.scmptest.data.model.StaffListResponse
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
class StaffListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val application = mockk<Application>(relaxed = true)

    private lateinit var viewModel: StaffListViewModel
    private lateinit var displayListObserver: Observer<List<ListItem>>
    private lateinit var isLoadingObserver: Observer<Boolean>
    private lateinit var errorObserver: Observer<String?>

    private val staffs = listOf(
        ListItem.Staff(
            id = 1,
            email = "test1@example.com",
            first_name = "John",
            last_name = "Doe",
            avatar = "avatar1.jpg"
        ),
        ListItem.Staff(
            id = 2,
            email = "test2@example.com",
            first_name = "Jane",
            last_name = "Smith",
            avatar = "avatar2.jpg"
        )
    )

    private val staffListWithLoadMore = staffs + listOf(ListItem.LoadMore)

    private val staffListResponse = StaffListResponse(
        page = 1,
        per_page = 10,
        total = 2,
        total_pages = 1,
        data = staffs
    )

    private val errorMsg = "Api failed"
    private val genericErrorMsg = "Generic error"

    private val retrieveListFailedResponse =
        Response.error<StaffListResponse>(401, "{ \"error\": \"${errorMsg}\" }".toResponseBody())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { application.getString(R.string.common_generic_error_msg) } returns genericErrorMsg

        mockkObject(ApiService)

        displayListObserver = mockk<Observer<List<ListItem>>>(relaxed = true)
        isLoadingObserver = mockk<Observer<Boolean>>(relaxed = true)
        errorObserver = mockk<Observer<String?>>(relaxed = true)

        viewModel = StaffListViewModel(application).apply {
            displayList.observeForever(displayListObserver)
            isLoading.observeForever(isLoadingObserver)
            error.observeForever(errorObserver)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `initial state should not loading and no error`() {
        viewModel.apply {
            assertFalse(isLoading.value!!)
            assertNull(error.value)
        }
    }

    @Test
    fun `retrieveStaffList should call API (cannot load more)`() {
        coEvery { ApiService.scmpApi.retrieveStaffList(any()) } returns Response.success(
            staffListResponse
        )

        viewModel.apply {
            retrieveStaffList()
            coVerify {
                ApiService.scmpApi.retrieveStaffList(1)
            }

            assertEquals(staffs, staffList)
            assertEquals(staffs, displayList.value)
            assertEquals(2, pageCount)
            assertNull(error.value)
        }
    }

    @Test
    fun `retrieveStaffList should call API (can load more)`() {
        coEvery { ApiService.scmpApi.retrieveStaffList(any()) }.returnsMany(
            Response.success(staffListResponse.copy(total_pages = 2)),
            Response.success(staffListResponse.copy(page = 2, total_pages = 2))
        )

        viewModel.apply {
            retrieveStaffList()

            coVerify {
                ApiService.scmpApi.retrieveStaffList(1)
            }

            assertEquals(staffListWithLoadMore, displayList.value)
            assertEquals(staffs, staffList)
            assertEquals(2, pageCount)
            assertNull(error.value)

            retrieveStaffList()

            coVerify {
                ApiService.scmpApi.retrieveStaffList(2)
            }

            assertEquals(staffs + staffs, displayList.value)
            assertEquals(staffs + staffs, staffList)
            assertEquals(3, pageCount)
            assertNull(error.value)
        }
    }

    @Test
    fun `retrieveStaffList should handle error when API returned null body`() {
        coEvery { ApiService.scmpApi.retrieveStaffList(any()) } returns Response.success(null)

        viewModel.apply {
            retrieveStaffList()

            coVerify {
                ApiService.scmpApi.retrieveStaffList(1)
            }

            assertEquals(null, displayList.value)
            assertEquals(emptyList<ListItem.Staff>(), staffList)
            assertEquals(1, pageCount)
            assertEquals(genericErrorMsg, error.value)
        }
    }

    @Test
    fun `retrieveStaffList should handle error response`() {
        coEvery { ApiService.scmpApi.retrieveStaffList(any()) } returns retrieveListFailedResponse

        viewModel.apply {
            retrieveStaffList()

            coVerify {
                ApiService.scmpApi.retrieveStaffList(1)
            }

            assertEquals(null, displayList.value)
            assertEquals(emptyList<ListItem.Staff>(), staffList)
            assertEquals(1, pageCount)
            assertEquals(errorMsg, error.value)
        }
    }

    @Test
    fun `retrieveStaffList should handle exception`() {
        coEvery { ApiService.scmpApi.retrieveStaffList(any()) } throws Exception(errorMsg)

        viewModel.apply {
            retrieveStaffList()

            coVerify {
                ApiService.scmpApi.retrieveStaffList(1)
            }

            assertEquals(null, displayList.value)
            assertEquals(emptyList<ListItem.Staff>(), staffList)
            assertEquals(1, pageCount)
            assertEquals(errorMsg, error.value)
        }
    }
}
