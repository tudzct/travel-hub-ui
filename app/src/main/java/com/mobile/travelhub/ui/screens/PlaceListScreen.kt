package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.ui.components.PlaceListScreenContent
import com.mobile.travelhub.viewmodels.HomeViewModel
import com.mobile.travelhub.viewmodels.PlaceListViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun PlaceListScreen(
    reloadSignal: Int = 0,
    onPlaceClick: (TravelPlaceListItemResponse) -> Unit,
    onSearchClick: () -> Unit,
    onAuthorClick: (Long) -> Unit,
    onMenuClick: () -> Unit = {},
    placeListViewModel: PlaceListViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val placeUiState by placeListViewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(reloadSignal) {
        if (reloadSignal > 0) {
            listState.animateScrollToItem(0)
            placeListViewModel.refresh()
            homeViewModel.refreshPosts()
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisibleIndex to layoutInfo.totalItemsCount
        }
            .distinctUntilChanged()
            .collect { (lastVisibleIndex, totalItemsCount) ->
                if (totalItemsCount > 0 && lastVisibleIndex >= totalItemsCount - 3) {
                    homeViewModel.loadMorePosts()
                }
            }
    }

    PlaceListScreenContent(
        placeUiState = placeUiState,
        homeUiState = homeUiState,
        listState = listState,
        onPlaceClick = onPlaceClick,
        onMenuClick = onMenuClick,
        onSearchClick = onSearchClick,
        onRetryPlaces = placeListViewModel::refresh,
        onRetryPosts = homeViewModel::refreshPosts,
        onLoadMorePosts = homeViewModel::loadMorePosts,
        onLikeClick = homeViewModel::onLikeClicked,
        onSaveClick = homeViewModel::onSaveClicked,
        onCommentClick = homeViewModel::onCommentClicked,
        onAuthorClick = onAuthorClick,
        onDismissCommentSheet = homeViewModel::onCommentDismissed,
        onCommentInputChanged = homeViewModel::onCommentInputChanged,
        onCommentSubmit = homeViewModel::submitComment
    )
}
