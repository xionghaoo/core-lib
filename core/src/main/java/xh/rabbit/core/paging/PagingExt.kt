package xh.rabbit.core.paging

import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource

fun <T: Any> ViewModel.dataSourceFlow(pageSize: Int, pagingSourceFactory: () -> PagingSource<Int, T>) =
    Pager(
        PagingConfig(
            pageSize = pageSize,
            enablePlaceholders = false,
            prefetchDistance = 1
        ),
        pagingSourceFactory = pagingSourceFactory
    ).flow