package com.mobile.travelhub.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.ui.components.PlaceListScreenContent
import com.mobile.travelhub.viewmodels.HomeViewModel
import com.mobile.travelhub.viewmodels.PlaceListViewModel

@Composable
fun PlaceListScreen(
    onPlaceClick: (TravelPlaceListItemResponse) -> Unit,
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit = {},
    placeListViewModel: PlaceListViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val placeUiState by placeListViewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()

    PlaceListScreenContent(
        placeUiState = placeUiState,
        homeUiState = homeUiState,
        onPlaceClick = onPlaceClick,
        onMenuClick = onMenuClick,
        onSearchClick = onSearchClick,
        onRetryPlaces = placeListViewModel::refresh,
        onRetryPosts = homeViewModel::refreshPosts,
        onLikeClick = homeViewModel::onLikeClicked,
        onCommentClick = homeViewModel::onCommentClicked,
        onDismissCommentSheet = homeViewModel::onCommentDismissed,
        onCommentInputChanged = homeViewModel::onCommentInputChanged,
        onCommentSubmit = homeViewModel::submitComment
    )
}
