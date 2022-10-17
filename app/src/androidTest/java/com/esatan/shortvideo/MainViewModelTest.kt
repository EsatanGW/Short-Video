package com.esatan.shortvideo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.esatan.shortvideo.model.dao.VideoCommentDao
import com.esatan.shortvideo.model.data.SourceData
import com.esatan.shortvideo.model.data.UserData
import com.esatan.shortvideo.model.data.VideoCommentData
import com.esatan.shortvideo.model.data.VideoData
import com.esatan.shortvideo.model.repository.VideoRepository
import com.esatan.shortvideo.viewmodel.MainViewModel
import io.mockk.*
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

    @MockK(relaxed = true)
    lateinit var videoRepository: VideoRepository

    @MockK
    lateinit var commentDao: VideoCommentDao

    lateinit var mainViewModel: MainViewModel

    @OptIn(DelicateCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(mainThreadSurrogate)
        mainViewModel = MainViewModel(videoRepository, commentDao)
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

    @Test
    fun queryVideoCommentTest() {
        val videoId = "111"

        val mockPagingSource = object : PagingSource<Int, VideoCommentData>() {
            override fun getRefreshKey(state: PagingState<Int, VideoCommentData>): Int? {
                return 0
            }

            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, VideoCommentData> {
                return LoadResult.Page(
                    listOf(),
                    null,
                    null
                )
            }
        }

        coEvery { commentDao.queryComments(videoId) } returns mockPagingSource

        assertEquals(mockPagingSource, mainViewModel.queryVideoComments(videoId))

        coVerify(exactly = 1) { mainViewModel.queryVideoComments(videoId) }
    }

    @Test
    fun addVideoCommentTest() {
        val videoId = "123"
        val message = "This is a test."
        val timestamp = System.currentTimeMillis()

        val mockRowId = 1L

        coEvery { commentDao.addComment(
            VideoCommentData(
                videoId = videoId,
                nickname = "Guest",
                avatarUrl = null,
                comment = message,
                timestamp = timestamp
            )
        ) } returns mockRowId

        val result: Long

        runBlocking {
            result = mainViewModel.addVideoComments(videoId, message, timestamp)
        }

        coVerify(exactly = 1) { mainViewModel.addVideoComments(videoId, message, timestamp) }

        assertEquals(mockRowId, result)
    }

    @After
    fun cleanUp() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }
}