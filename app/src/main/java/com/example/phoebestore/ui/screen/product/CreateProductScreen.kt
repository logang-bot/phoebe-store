package com.example.phoebestore.ui.screen.product

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.phoebestore.R
import com.example.phoebestore.ui.common.CameraPermissionTextProvider
import com.example.phoebestore.ui.common.PermissionDialog
import com.example.phoebestore.ui.common.openAppSettings
import com.example.phoebestore.ui.theme.PhoebeStoreTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CreateProductScreen(
    storeId: Long,
    productId: Long?,
    onProductSaved: () -> Unit,
    viewModel: CreateProductViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val formState = viewModel.formState
    val dialogQueue = viewModel.visiblePermissionDialogQueue

    val scope = rememberCoroutineScope()
    var cameraFile by remember { mutableStateOf<File?>(null) }
    var cameraReadyToLaunch by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CreateProductEvent.ProductSaved -> onProductSaved()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(Manifest.permission.CAMERA, isGranted)
        if (isGranted) cameraReadyToLaunch = true
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraFile?.let { viewModel.onImageCaptured(Uri.fromFile(it).toString()) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { sourceUri ->
            scope.launch(Dispatchers.IO) {
                val dest = File(context.filesDir, "product_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(sourceUri)?.use { it.copyTo(dest.outputStream()) }
                viewModel.onImageCaptured(Uri.fromFile(dest).toString())
            }
        }
    }

    LaunchedEffect(cameraReadyToLaunch) {
        if (!cameraReadyToLaunch) return@LaunchedEffect
        cameraReadyToLaunch = false
        val file = File(context.filesDir, "product_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraFile = file
        cameraLauncher.launch(uri)
    }

    fun onTakePhotoClick() {
        val isGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (isGranted) {
            cameraReadyToLaunch = true
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    dialogQueue.reversed().forEach { permission ->
        PermissionDialog(
            permissionTextProvider = when (permission) {
                Manifest.permission.CAMERA -> CameraPermissionTextProvider()
                else -> return@forEach
            },
            isPermanentlyDeclined = !activity.shouldShowRequestPermissionRationale(permission),
            onDismiss = viewModel::dismissDialog,
            onOkClick = {
                viewModel.dismissDialog()
                cameraPermissionLauncher.launch(permission)
            },
            onGoToAppSettingsClick = {
                viewModel.dismissDialog()
                activity.openAppSettings()
            }
        )
    }

    CreateProductScreenContent(
        productId = productId,
        formState = formState,
        onNameChange = viewModel::onNameChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onPriceChange = viewModel::onPriceChange,
        onCostPriceChange = viewModel::onCostPriceChange,
        onStockChange = viewModel::onStockChange,
        onTakePhoto = ::onTakePhotoClick,
        onChooseFromGallery = {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onSave = viewModel::saveProduct
    )
}

@Composable
private fun CreateProductScreenContent(
    productId: Long?,
    formState: CreateProductFormState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onCostPriceChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onTakePhoto: () -> Unit,
    onChooseFromGallery: () -> Unit,
    onSave: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                text = stringResource(
                    if (productId == null) R.string.create_product_title_create
                    else R.string.create_product_title_edit
                ),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Product image
            Text(
                text = stringResource(R.string.create_product_image_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(1f)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (formState.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = formState.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onTakePhoto,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        stringResource(
                            if (formState.imageUrl.isBlank()) R.string.create_store_take_photo
                            else R.string.create_store_retake_photo
                        )
                    )
                }
                OutlinedButton(
                    onClick = onChooseFromGallery,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.create_store_choose_from_gallery))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Name
            OutlinedTextField(
                value = formState.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.create_product_name_label)) },
                placeholder = { Text(stringResource(R.string.create_product_name_placeholder)) },
                isError = formState.nameError,
                supportingText = if (formState.nameError) {
                    { Text(stringResource(R.string.create_product_name_required_error)) }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            OutlinedTextField(
                value = formState.description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.create_product_description_label)) },
                placeholder = { Text(stringResource(R.string.create_product_description_placeholder)) },
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Selling price + Cost price side by side
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = formState.price,
                    onValueChange = onPriceChange,
                    label = { Text(stringResource(R.string.create_product_price_label)) },
                    placeholder = { Text(stringResource(R.string.create_product_price_placeholder)) },
                    prefix = { Text(formState.currency.name) },
                    isError = formState.priceError,
                    supportingText = if (formState.priceError) {
                        { Text(stringResource(R.string.create_product_price_error)) }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { if (!it.isFocused && formState.price.isNotEmpty()) formState.price.toDoubleOrNull()?.let { v -> onPriceChange("%.2f".format(v)) } }
                )
                OutlinedTextField(
                    value = formState.costPrice,
                    onValueChange = onCostPriceChange,
                    label = { Text(stringResource(R.string.create_product_cost_price_label)) },
                    placeholder = { Text(stringResource(R.string.create_product_cost_price_placeholder)) },
                    prefix = { Text(formState.currency.name) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { if (!it.isFocused && formState.costPrice.isNotEmpty()) formState.costPrice.toDoubleOrNull()?.let { v -> onCostPriceChange("%.2f".format(v)) } }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stock
            OutlinedTextField(
                value = formState.stock,
                onValueChange = onStockChange,
                label = { Text(stringResource(R.string.create_product_stock_label)) },
                placeholder = { Text(stringResource(R.string.create_product_stock_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused && formState.stock == "0") onStockChange("") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (formState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = onSave,
                enabled = !formState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.create_product_save))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateProductScreenLightPreview() {
    PhoebeStoreTheme {
        CreateProductScreenContent(
            productId = null,
            formState = CreateProductFormState(),
            onNameChange = {},
            onDescriptionChange = {},
            onPriceChange = {},
            onCostPriceChange = {},
            onStockChange = {},
            onTakePhoto = {},
            onChooseFromGallery = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateProductScreenDarkPreview() {
    PhoebeStoreTheme {
        CreateProductScreenContent(
            productId = null,
            formState = CreateProductFormState(),
            onNameChange = {},
            onDescriptionChange = {},
            onPriceChange = {},
            onCostPriceChange = {},
            onStockChange = {},
            onTakePhoto = {},
            onChooseFromGallery = {},
            onSave = {}
        )
    }
}
