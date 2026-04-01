package com.mobile.travelhub.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.components.EditProfileField
import com.mobile.travelhub.ui.viewmodels.ProfileViewModel
import com.mobile.travelhub.ui.viewmodels.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var originalName by remember { mutableStateOf("") }
    var originalHandle by remember { mutableStateOf("") }
    var originalBio by remember { mutableStateOf("") }
    var originalDob by remember { mutableStateOf("") }
    var originalLocation by remember { mutableStateOf("") }
    var originalGender by remember { mutableStateOf("") }

    var showConfirmDialog by remember { mutableStateOf(false) }

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
            phoneNumber = data.phoneNumber ?: ""
            
            originalName = data.name
            originalHandle = data.username
            originalBio = data.bio ?: ""
            originalDob = data.dateOfBirth ?: ""
            originalLocation = data.location ?: ""
            originalGender = data.gender ?: ""
        }
    }
    
    LaunchedEffect(updateStatus) {
        when (updateStatus) {
            is UiState.Success -> {
                viewModel.resetUpdateStatus()
                Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                viewModel.loadUserProfile() 
                onSaveSuccess()
            }
            is UiState.Error -> {
                val errorMsg = (updateStatus as UiState.Error).message
                Toast.makeText(context, "Lỗi: $errorMsg", Toast.LENGTH_LONG).show()
                viewModel.resetUpdateStatus()
            }
            else -> {}
        }
    }

    val hasChanges = name != originalName || handle != originalHandle || bio != originalBio || 
                     dob != originalDob || location != originalLocation || gender != originalGender

    val handleBack = {
        if (hasChanges) {
            showConfirmDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(onBack = handleBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "EDIT PROFILE", 
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (updateStatus is UiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(end = 16.dp))
                    } else {
                        TextButton(onClick = {
                            viewModel.updateProfile(name, handle, bio, dob, gender, location)
                        }) {
                            Text(
                                "SAVE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            if (profileState is UiState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(100.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Change",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            EditProfileField(
                label = "Full Name",
                value = name,
                onValueChange = { name = it }
            )
            EditProfileField(
                label = "Username",
                value = handle,
                onValueChange = { handle = it }
            )
            EditProfileField(
                label = "Bio",
                value = bio,
                onValueChange = { bio = it }
            )
            EditProfileField(
                label = "Date of Birth (YYYY-MM-DD)",
                value = dob,
                onValueChange = { dob = it }
            )
            EditProfileField(
                label = "Location",
                value = location,
                onValueChange = { location = it }
            )

            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "PRIVATE INFORMATION",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            EditProfileField(
                label = "Email Address",
                value = email,
                onValueChange = { email = it },
                enabled = false
            )
            EditProfileField(
                label = "Phone Number",
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                enabled = false
            )
            EditProfileField(
                label = "Gender",
                value = gender,
                onValueChange = { gender = it },
                enabled = true
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { 
                    Text(
                        text = "Unsaved Changes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = { 
                    Text(
                        text = "You have unsaved changes. Do you want to save them before leaving?",
                        style = MaterialTheme.typography.bodyLarge
                    ) 
                },
                confirmButton = {
                    TextButton(onClick = {
                        showConfirmDialog = false
                        viewModel.updateProfile(name, handle, bio, dob, gender, location)
                    }) {
                        Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showConfirmDialog = false
                        onBack()
                    }) {
                        Text("Discard", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        }
    }
}
