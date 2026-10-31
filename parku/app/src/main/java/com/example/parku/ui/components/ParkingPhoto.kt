package com.example.parku.ui.components

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.disk.DiskCache
import coil.request.ImageRequest
import com.example.parku.R

private const val DEFAULT_URL =
    "https://raw.githubusercontent.com/app-ISIS3510/Kotlin/main/" +
        "parku/app/src/main/res/drawable-nodpi/cityu.jpg"

private const val CACHE_DIR = "parking_photos"
private const val CACHE_MAX_BYTES = 30L * 1024 * 1024

@Volatile
private var imageLoader: ImageLoader? = null

private fun parkingImageLoader(context: Context): ImageLoader {
    val app = context.applicationContext
    return imageLoader ?: synchronized(CACHE_DIR) {
        imageLoader ?: ImageLoader.Builder(app)
            .diskCache {
                DiskCache.Builder()
                    .directory(app.cacheDir.resolve(CACHE_DIR))
                    .maxSizeBytes(CACHE_MAX_BYTES)
                    .build()
            }
            .build()
            .also { imageLoader = it }
    }
}

private fun resolve(imageUrl: String?): String {
    val url = imageUrl?.trim().orEmpty()
    val parsed = runCatching { Uri.parse(url) }.getOrNull() ?: return DEFAULT_URL
    val usable = !parsed.host.isNullOrEmpty() &&
        (parsed.scheme == "https" || parsed.scheme == "http")
    return if (usable) url else DEFAULT_URL
}

@Composable
fun ParkingPhoto(imageUrl: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val fallback = painterResource(R.drawable.cityu)

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(resolve(imageUrl))
            .crossfade(150)
            .build(),
        imageLoader = parkingImageLoader(context),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        placeholder = fallback,
        error = fallback,
    )
}
