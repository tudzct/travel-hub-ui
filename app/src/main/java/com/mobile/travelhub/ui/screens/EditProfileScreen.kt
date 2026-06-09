package com.mobile.travelhub.ui.screens

import androidx.compose.ui.res.stringResource
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.R
import com.mobile.travelhub.data.userMessage
import com.mobile.travelhub.ui.components.DestinationPlacePicker
import com.mobile.travelhub.ui.components.InlineLoadingSkeleton
import com.mobile.travelhub.ui.components.EditProfileLoadingSkeleton
import com.mobile.travelhub.viewmodels.ProfileViewModel
import com.mobile.travelhub.viewmodels.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val provincePickerState by viewModel.provincePickerState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    
    var email by remember { mutableStateOf("") }
    var bankCode by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }

    var originalName by remember { mutableStateOf("") }
    var originalHandle by remember { mutableStateOf("") }
    var originalBio by remember { mutableStateOf("") }
    var originalDob by remember { mutableStateOf("") }
    var originalLocation by remember { mutableStateOf("") }
    var originalGender by remember { mutableStateOf("") }
    var originalBankCode by remember { mutableStateOf("") }
    var originalBankName by remember { mutableStateOf("") }
    var originalAccountNumber by remember { mutableStateOf("") }
    var originalAccountName by remember { mutableStateOf("") }

    var showConfirmDialog by remember { mutableStateOf(false) }

    var pendingAvatar by remember { mutableStateOf<PendingAvatar?>(null) }

    fun saveProfile() {
        if (isSaving) return
        coroutineScope.launch {
            isSaving = true
            try {
                val uploadedUrl = pendingAvatar?.let { avatar ->
                    viewModel.uploadAvatar(
                        imageBytes = avatar.bytes,
                        mimeType = avatar.mimeType,
                        fileName = avatar.fileName
                    )
                }
                viewModel.updateProfile(
                    name = name,
                    username = handle,
                    bio = bio,
                    dob = dob,
                    gender = gender,
                    location = location,
                    bankCode = bankCode,
                    bankName = bankName,
                    accountNumber = accountNumber,
                    accountName = accountName,
                    avatarUrl = uploadedUrl
                )
                when (val result = viewModel.updateStatus.value) {
                    is UiState.Success -> {
                        pendingAvatar = null
                        onSaveSuccess()
                    }
                    is UiState.Error -> {
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.profile_save_failed_with_reason,
                                result.message
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        Toast.makeText(
                            context,
                            context.getString(R.string.profile_save_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    e.userMessage(context.getString(R.string.image_upload_failed)),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                isSaving = false
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    LaunchedEffect(profileState) {
        if (profileState is UiState.Success) {
            val data = (profileState as UiState.Success).data
            name = data.name
            handle = data.username
            bio = data.bio ?: ""
            dob = data.dateOfBirth ?: ""
            gender = data.gender ?: ""
            location = data.location ?: ""
            email = data.email ?: ""
            bankCode = data.bankCode.orEmpty()
            bankName = data.bankName.orEmpty()
            accountNumber = data.accountNumber.orEmpty()
            accountName = data.accountName.orEmpty()
            
            originalName = data.name
            originalHandle = data.username
            originalBio = data.bio ?: ""
            originalDob = data.dateOfBirth ?: ""
            originalLocation = data.location ?: ""
            originalGender = data.gender ?: ""
            originalBankCode = data.bankCode.orEmpty()
            originalBankName = data.bankName.orEmpty()
            originalAccountNumber = data.accountNumber.orEmpty()
            originalAccountName = data.accountName.orEmpty()
        }
    }

    LaunchedEffect(profileState, provincePickerState.provinces) {
        val data = (profileState as? UiState.Success)?.data ?: return@LaunchedEffect
        val matchedProvince = provincePickerState.provinces.firstOrNull { province ->
            province.name.equals(data.location?.trim(), ignoreCase = true)
        }
        if (matchedProvince != null) {
            viewModel.selectProfileProvince(matchedProvince.id)
            location = matchedProvince.name
        } else if (data.location.isNullOrBlank()) {
            viewModel.clearProfileProvinceSelection()
            location = ""
        }
    }

    val selectedProvince = provincePickerState.provinces.firstOrNull {
        it.id == provincePickerState.selectedProvinceId
    }

    val hasChanges = name != originalName || handle != originalHandle || bio != originalBio ||
        location != originalLocation || gender != originalGender ||
        bankCode != originalBankCode || bankName != originalBankName ||
        accountNumber != originalAccountNumber || accountName != originalAccountName ||
        pendingAvatar != null

    val handleBack = {
        if (hasChanges) {
            showConfirmDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(onBack = handleBack)

    Scaffold(
        containerColor = EditProfileBackground,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(EditProfileBackground)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = handleBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.ui_b52b36b726),
                        tint = EditProfileInk,
                        modifier = Modifier.size(31.dp)
                    )
                }
                Text(
                    text = "Chỉnh sửa hồ sơ",
                    color = EditProfileInk,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isSaving) {
                    InlineLoadingSkeleton(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(24.dp)
                    )
                } else {
                    Text(
                        text = "Lưu",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable { saveProfile() }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            if (profileState is UiState.Loading) {
                EditProfileLoadingSkeleton()
                return@Scaffold
            }
            
            val avatarPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                if (uri != null) {
                    try {
                        val originalBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: throw IllegalStateException("Không thể đọc ảnh đã chọn")
                        val originalBitmap = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size)
                            ?: throw IllegalStateException("Không thể giải mã ảnh đã chọn")

                        val maxDimension = 500
                        val width = originalBitmap.width
                        val height = originalBitmap.height
                        val (newWidth, newHeight) = if (width > height) {
                            if (width > maxDimension) {
                                Pair(maxDimension, (height * (maxDimension.toFloat() / width)).toInt())
                            } else {
                                Pair(width, height)
                            }
                        } else {
                            if (height > maxDimension) {
                                Pair((width * (maxDimension.toFloat() / height)).toInt(), maxDimension)
                            } else {
                                Pair(width, height)
                            }
                        }

                        val scaledBitmap = if (newWidth != width || newHeight != height) {
                            Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
                        } else {
                            originalBitmap
                        }

                        val outputStream = java.io.ByteArrayOutputStream()
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                        val compressedBytes = outputStream.toByteArray()

                        if (scaledBitmap != originalBitmap) {
                            scaledBitmap.recycle()
                        }
                        originalBitmap.recycle()

                        val previewBitmap = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
                            ?: throw IllegalStateException("Không thể tạo preview ảnh đã chọn")

                        pendingAvatar = PendingAvatar(
                            bytes = compressedBytes,
                            mimeType = "image/jpeg",
                            fileName = (uri.lastPathSegment
                                ?.substringAfterLast('/')
                                ?.takeIf { it.isNotBlank() }
                                ?: "avatar")
                                .substringBeforeLast('.') + ".jpg",
                            previewBitmap = previewBitmap
                        )
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            e.userMessage(
                                context.getString(R.string.selected_image_read_failed)
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            EditProfileAvatarPicker(
                avatarUrl = (profileState as? UiState.Success)
                    ?.data
                    ?.avatarUrl
                    ?.takeIf { it.isNotBlank() },
                pendingAvatar = pendingAvatar,
                onClick = {
                    avatarPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )

            Spacer(modifier = Modifier.height(48.dp))

            EditProfileSectionTitle("Thông tin cơ bản")

            Spacer(modifier = Modifier.height(16.dp))

            EditProfileInputCard(
                icon = Icons.Outlined.Person,
                label = "Họ và tên",
                value = name,
                onValueChange = { name = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            EditProfileInputCard(
                icon = Icons.Outlined.AlternateEmail,
                label = "Tên người dùng",
                value = handle,
                onValueChange = { handle = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            EditProfileInputCard(
                icon = Icons.Outlined.Edit,
                label = "Giới thiệu",
                value = bio,
                onValueChange = { bio = it.take(120) },
                placeholder = "Giới thiệu về bạn...",
                maxLength = 120,
                minHeight = 82.dp
            )

            Spacer(modifier = Modifier.height(34.dp))

            EditProfileSectionTitle("Địa điểm")

            Spacer(modifier = Modifier.height(16.dp))

            DestinationPlacePicker(
                label = "",
                selectedProvince = selectedProvince,
                selectedPlace = null,
                provinces = provincePickerState.provinces,
                places = emptyList(),
                isLoading = provincePickerState.isLoading,
                enabled = !isSaving &&
                    (provincePickerState.provinces.isNotEmpty() || provincePickerState.errorMessage != null),
                placeholder = "Địa điểm",
                allowPlaceSelection = false,
                compactAnchor = true,
                anchorContainerColor = MaterialTheme.colorScheme.surface,
                anchorBorderColor = EditProfileBorder,
                anchorTitleOverride = "Địa điểm",
                compactSupportingText = selectedProvince?.name
                    ?: "Chọn nơi bạn đang sống hoặc thường xuyên ghé thăm",
                anchorTrailingIcon = Icons.Outlined.ChevronRight,
                onProvinceSelected = { provinceId ->
                    viewModel.selectProfileProvince(provinceId)
                    location = provincePickerState.provinces
                        .firstOrNull { it.id == provinceId }
                        ?.name
                        .orEmpty()
                },
                provinceErrorMessage = provincePickerState.errorMessage,
                onRetryProvinces = viewModel::retryLoadProfileProvinces
            )

            Spacer(modifier = Modifier.height(34.dp))

            EditProfileSectionTitle("Thông tin liên hệ")

            Spacer(modifier = Modifier.height(16.dp))

            EditProfileInputCard(
                icon = Icons.Outlined.Email,
                label = "Địa chỉ email",
                value = email,
                onValueChange = { email = it },
                enabled = false,
                iconInCircle = true
            )

            Spacer(modifier = Modifier.height(34.dp))

            EditProfileSectionTitle("Thông tin ngân hàng")

            Spacer(modifier = Modifier.height(16.dp))

            EditProfileInputCard(
                icon = Icons.Outlined.AccountBalance,
                label = "Mã ngân hàng",
                value = bankCode,
                onValueChange = { bankCode = it.uppercase().take(30) },
                placeholder = "VD: VCB, BIDV, MBBANK",
                iconInCircle = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            EditProfileInputCard(
                icon = Icons.Outlined.AccountBalance,
                label = "Tên ngân hàng",
                value = bankName,
                onValueChange = { bankName = it },
                placeholder = "VD: Vietcombank",
                iconInCircle = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            EditProfileInputCard(
                icon = Icons.Outlined.CreditCard,
                label = "Số tài khoản",
                value = accountNumber,
                onValueChange = { accountNumber = it.filter { char -> char.isDigit() }.take(50) },
                placeholder = "Nhập số tài khoản",
                iconInCircle = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            EditProfileInputCard(
                icon = Icons.Outlined.Badge,
                label = "Tên chủ tài khoản",
                value = accountName,
                onValueChange = { accountName = it },
                placeholder = "Tên trên tài khoản ngân hàng",
                iconInCircle = true
            )

            Spacer(modifier = Modifier.height(56.dp))
        }

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { 
                    Text(
                        text = stringResource(R.string.ui_9563b6bfa0),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = { 
                    Text(
                        text = stringResource(R.string.ui_3fc627b036),
                        style = MaterialTheme.typography.bodyLarge
                    ) 
                },
                confirmButton = {
                    TextButton(onClick = {
                        showConfirmDialog = false
                        saveProfile()
                    }) {
                        Text(stringResource(R.string.ui_efc007a393), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showConfirmDialog = false
                        onBack()
                    }) {
                        Text(stringResource(R.string.ui_36fff63ccb), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

private data class PendingAvatar(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String,
    val previewBitmap: Bitmap
)

@Composable
private fun EditProfileAvatarPicker(
    avatarUrl: String?,
    pendingAvatar: PendingAvatar?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(124.dp)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (pendingAvatar != null) {
                Image(
                    bitmap = pendingAvatar.previewBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.ui_4dbfd0986a),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(EditProfileSoftBlue),
                    contentScale = ContentScale.Crop
                )
            } else if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = stringResource(R.string.ui_4dbfd0986a),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(EditProfileSoftBlue),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(EditProfileSoftBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .shadow(8.dp, CircleShape)
                    .size(45.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditProfileSectionTitle(text: String) {
    Text(
        text = text,
        color = EditProfileInk,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun EditProfileInputCard(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "",
    maxLength: Int? = null,
    minHeight: Dp = 70.dp,
    iconInCircle: Boolean = false
) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(minHeight)
                .border(1.dp, EditProfileBorder, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EditProfileFieldIcon(icon = icon, inCircle = iconInCircle)

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    color = EditProfileMuted,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(5.dp))
                BasicTextField(
                    value = value,
                    onValueChange = { nextValue ->
                        onValueChange(maxLength?.let { nextValue.take(it) } ?: nextValue)
                    },
                    enabled = enabled,
                    singleLine = maxLength == null,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = if (enabled) Color.Black else Color.Black,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (value.isBlank() && placeholder.isNotBlank()) {
                            Text(
                                text = placeholder,
                                color = EditProfileMuted,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        innerTextField()
                    }
                )
            }

            if (maxLength != null) {
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${value.length}/$maxLength",
                    color = EditProfileMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Bottom)
                )
            }
        }
    }
}

@Composable
private fun EditProfileFieldIcon(
    icon: ImageVector,
    inCircle: Boolean
) {
    if (inCircle) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(EditProfileSoftBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EditProfileIcon,
                modifier = Modifier.size(28.dp)
            )
        }
    } else {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = EditProfileIcon,
            modifier = Modifier.size(31.dp)
        )
    }
}

private val EditProfileBackground = Color(0xFFFFFFFF)
private val EditProfileInk = Color(0xFF111827)
private val EditProfileMuted = Color(0xFF5F6B7A)
private val EditProfileIcon = Color(0xFF3F4A59)
private val EditProfileBorder = Color(0xFFE8ECF2)
private val EditProfileSoftBlue = Color(0xFFEAF3FF)
