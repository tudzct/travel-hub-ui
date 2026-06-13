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
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import com.mobile.travelhub.ui.components.BankPicker
import com.mobile.travelhub.ui.components.EditProfileLoadingSkeleton
import com.mobile.travelhub.ui.components.TravelHubAvatar
import com.mobile.travelhub.viewmodels.ProfileViewModel
import com.mobile.travelhub.viewmodels.UiState
import java.io.ByteArrayOutputStream
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val provincePickerState by viewModel.provincePickerState.collectAsState()
    val bankPickerState by viewModel.bankPickerState.collectAsState()
    val context = LocalContext.current
    val imageUploadFailedMessage = stringResource(R.string.image_upload_failed)
    val selectedImageReadFailedMessage = stringResource(R.string.selected_image_read_failed)
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

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        android.util.Log.d("AVATAR_FLOW", "Photo picker result uri=$uri")
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            try {
                val avatar = withContext(Dispatchers.IO) {
                    buildAvatarFromUri(context, uri)
                }
                android.util.Log.d(
                    "AVATAR_FLOW",
                    "Avatar prepared bytes=${avatar.bytes.size}, file=${avatar.fileName}"
                )
                pendingAvatar = avatar
            } catch (e: Exception) {
                android.util.Log.e("AVATAR_FLOW", "Avatar prepare failed", e)
                Toast.makeText(
                    context,
                    e.userMessage(selectedImageReadFailedMessage),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun saveProfile() {
        if (isSaving) return
        coroutineScope.launch {
            isSaving = true
            try {
                val currentAvatarUrl = (profileState as? UiState.Success)
                    ?.data
                    ?.avatarUrl
                    ?.takeIf { it.isNotBlank() }

                val finalAvatarUrl = pendingAvatar?.let { avatar ->
                    android.util.Log.d(
                        "AVATAR_FLOW",
                        "Uploading cropped avatar bytes=${avatar.bytes.size}, file=${avatar.fileName}"
                    )
                    val uploadedUrl = viewModel.uploadAvatar(
                        imageBytes = avatar.bytes,
                        mimeType = avatar.mimeType,
                        fileName = avatar.fileName
                    )
                    android.util.Log.d("AVATAR_FLOW", "Avatar upload success url=$uploadedUrl")
                    uploadedUrl
                } ?: currentAvatarUrl

                val updatedProfile = viewModel.updateProfile(
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
                    avatarUrl = finalAvatarUrl
                )
                android.util.Log.d(
                    "AVATAR_FLOW",
                    "Profile update success avatarUrl=${updatedProfile.avatarUrl}"
                )

                pendingAvatar = null
                onSaveSuccess()

            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()

                android.util.Log.e(
                    "UPLOAD_AVATAR",
                    "HTTP ${e.code()} - $errorBody",
                    e
                )

                Toast.makeText(
                    context,
                    "Upload lỗi HTTP ${e.code()}: $errorBody",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                android.util.Log.e("UPLOAD_AVATAR", "Save profile failed", e)

                Toast.makeText(
                    context,
                    e.userMessage(imageUploadFailedMessage),
                    Toast.LENGTH_LONG
                ).show()

            } finally {
                isSaving = false
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
        viewModel.loadBanks()
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
    val selectedBank = bankPickerState.banks.firstOrNull {
        it.code.equals(bankCode, ignoreCase = true)
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
                TextButton(
                    onClick = { saveProfile() },
                    enabled = !isSaving,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(
                        text = "Lưu",
                        color = if (isSaving) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSaving) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

            Spacer(modifier = Modifier.height(30.dp))

            EditProfileAvatarPicker(
                avatarUrl = (profileState as? UiState.Success)
                    ?.data
                    ?.avatarUrl
                    ?.takeIf { it.isNotBlank() },
                fallbackName = (profileState as? UiState.Success)
                    ?.data
                    ?.let { profile -> profile.name.ifBlank { profile.username } },
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
                onValueChange = { name = it },
                iconInCircle = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            EditProfileInputCard(
                icon = Icons.Outlined.AlternateEmail,
                label = "Tên người dùng",
                value = handle,
                onValueChange = { handle = it },
                iconInCircle = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            EditProfileInputCard(
                icon = Icons.Outlined.Edit,
                label = "Giới thiệu",
                value = bio,
                onValueChange = { bio = it.take(120) },
                placeholder = "Giới thiệu về bạn...",
                maxLength = 120,
                minHeight = 120.dp,
                iconInCircle = true
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

            BankPicker(
                selectedBank = selectedBank,
                currentBankCode = bankCode,
                currentBankName = bankName,
                banks = bankPickerState.banks,
                isLoading = bankPickerState.isLoading,
                errorMessage = bankPickerState.errorMessage,
                enabled = !isSaving && !bankPickerState.isLoading,
                borderColor = EditProfileBorder,
                onBankSelected = { bank ->
                    bankCode = bank.code
                    bankName = bank.shortName.ifBlank { bank.name }
                },
                onRetry = viewModel::retryLoadBanks
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
    fallbackName: String?,
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
                TravelHubAvatar(
                    avatarUrl = avatarUrl,
                    contentDescription = stringResource(R.string.ui_4dbfd0986a),
                    fallbackName = fallbackName,
                    modifier = Modifier
                        .fillMaxSize()
                )
            } else {
                TravelHubAvatar(
                    avatarUrl = null,
                    contentDescription = stringResource(R.string.ui_4dbfd0986a),
                    fallbackName = fallbackName,
                    modifier = Modifier
                        .fillMaxSize()
                )
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
                        color = if (enabled) EditProfileInk else EditProfileMuted,
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

private val EditProfileBackground: Color
    @Composable
    get() = MaterialTheme.colorScheme.surface

private val EditProfileInk: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurface

private val EditProfileMuted: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

private val EditProfileIcon: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

private val EditProfileBorder: Color
    @Composable
    get() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)

private val EditProfileSoftBlue: Color
    @Composable
    get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

private const val AVATAR_OUTPUT_SIZE = 500
private const val AVATAR_JPEG_QUALITY = 85

private fun loadEditableBitmapFromUri(context: Context, uri: Uri): Bitmap {
    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = false
        }
    } else {
        context.contentResolver.openInputStream(uri).use { input ->
            BitmapFactory.decodeStream(input)
                ?: throw IllegalStateException("Không thể giải mã ảnh đã chọn")
        }
    }
    return if (bitmap.config == Bitmap.Config.ARGB_8888 && !bitmap.isRecycled) {
        bitmap
    } else {
        bitmap.copy(Bitmap.Config.ARGB_8888, false)
    }
}

private fun buildAvatarFromUri(context: Context, uri: Uri): PendingAvatar {
    val source = loadEditableBitmapFromUri(context, uri)

    // Center-crop to a square so the avatar fits the circular frame, then downscale.
    val squareSize = min(source.width, source.height).coerceAtLeast(1)
    val left = (source.width - squareSize) / 2
    val top = (source.height - squareSize) / 2
    val squared = Bitmap.createBitmap(source, left, top, squareSize, squareSize)
    val outputBitmap = if (squareSize != AVATAR_OUTPUT_SIZE) {
        Bitmap.createScaledBitmap(squared, AVATAR_OUTPUT_SIZE, AVATAR_OUTPUT_SIZE, true)
    } else {
        squared
    }

    val outputStream = ByteArrayOutputStream()
    outputBitmap.compress(Bitmap.CompressFormat.JPEG, AVATAR_JPEG_QUALITY, outputStream)
    val bytes = outputStream.toByteArray()
    val previewBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: throw IllegalStateException("Không thể tạo preview ảnh đã chọn")

    if (outputBitmap != squared) {
        outputBitmap.recycle()
    }
    if (squared != source) {
        squared.recycle()
    }
    source.recycle()

    val fileName = (uri.lastPathSegment
        ?.substringAfterLast('/')
        ?.takeIf { it.isNotBlank() }
        ?: "avatar")
        .substringBeforeLast('.') + ".jpg"

    return PendingAvatar(
        bytes = bytes,
        mimeType = "image/jpeg",
        fileName = fileName,
        previewBitmap = previewBitmap
    )
}
