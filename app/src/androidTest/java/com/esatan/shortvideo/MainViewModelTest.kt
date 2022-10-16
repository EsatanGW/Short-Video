package com.esatan.shortvideo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.esatan.shortvideo.model.data.SourceData
import com.esatan.shortvideo.model.data.UserData
import com.esatan.shortvideo.model.data.VideoData
import com.esatan.shortvideo.model.repository.VideoRepository
import com.esatan.shortvideo.viewmodel.MainViewModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    lateinit var videoRepository: VideoRepository

    lateinit var mainViewModel: MainViewModel

    @OptIn(DelicateCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        println("Test123 1")
        MockKAnnotations.init(this)
        println("Test123 2")
        Dispatchers.setMain(mainThreadSurrogate)
        println("Test123 3")
        mainViewModel = MainViewModel(videoRepository)
        println("Test123 4")
    }

    @Test
    fun getKeyboardMaskVisibleTest() {
        mainViewModel.setKeyboardMaskVisible(true)
        assertEquals(true, mainViewModel.keyboardMaskVisibleLivedata.value)
    }

    @Test
    fun getVideoListTest() {
        val mockData = listOf(
            VideoData(
                "",
                SourceData(
                    postId = "",
                    createdAt = 0,
                    user = UserData(
                        userId = "",
                        uid = "",
                        name = "",
                        isFollowing = false
                    ),
                    isFeature = false,
                    canReuse = false
                )
            )
        )

        coEvery { videoRepository.queryVideoArray("") } returns mockData

        val result: List<VideoData>?

        runBlocking {
            result = mainViewModel.queryVideoArray("")
        }

        coVerify(exactly = 1) { videoRepository.queryVideoArray("") }

        assertEquals(mockData, result)
    }

    @After
    fun cleanUp() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }
}