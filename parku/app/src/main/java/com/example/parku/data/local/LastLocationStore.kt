package com.example.parku.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.util.AtomicFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Ultima ubicacion del telefono que se pudo leer, y cuando se leyo. */
data class SavedLocation(
    val latitude: Double,
    val longitude: Double,
    val savedAt: Long,
)

/**
 * Correccion del Sprint 3 (Nearest Parking / Smart Parking): guarda la ultima
 * ubicacion conocida para ordenar por cercania aunque el GPS falle al abrir la app.
 *
 * Almacenamiento: un archivo binario en el almacenamiento interno de la app
 * (filesDir), cifrado con AES-GCM y una llave que vive en el Android Keystore. No es
 * JSON ni DataStore: son 24 bytes (latitud, longitud, hora) escritos con
 * DataOutputStream. La ubicacion es un dato personal, asi que no se deja en texto
 * plano, y la llave no se puede extraer del telefono. Se escribe con AtomicFile para
 * que un cierre a mitad de la escritura no deje el archivo corrupto.
 */
class LastLocationStore(context: Context) {

    private val file = AtomicFile(File(context.applicationContext.filesDir, FILE_NAME))

    suspend fun read(): SavedLocation? = withContext(Dispatchers.IO) {
        runCatching {
            val stored = file.readFully()
            DataInputStream(ByteArrayInputStream(stored)).use { input ->
                if (input.readByte() != FORMAT_VERSION) return@runCatching null
                val iv = ByteArray(input.readUnsignedByte()).also { input.readFully(it) }
                val sealed = ByteArray(input.readInt()).also { input.readFully(it) }

                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, iv))
                val plain = cipher.doFinal(sealed)

                DataInputStream(ByteArrayInputStream(plain)).use {
                    SavedLocation(
                        latitude = it.readDouble(),
                        longitude = it.readDouble(),
                        savedAt = it.readLong(),
                    )
                }
            }
        }.getOrNull()
    }

    suspend fun save(latitude: Double, longitude: Double) = withContext(Dispatchers.IO) {
        runCatching {
            val plain = ByteArrayOutputStream().also { bytes ->
                DataOutputStream(bytes).use {
                    it.writeDouble(latitude)
                    it.writeDouble(longitude)
                    it.writeLong(System.currentTimeMillis())
                }
            }.toByteArray()

            // El Keystore genera un IV nuevo en cada cifrado; se guarda junto al dato.
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key())
            val sealed = cipher.doFinal(plain)

            val stream = file.startWrite()
            try {
                DataOutputStream(stream).apply {
                    writeByte(FORMAT_VERSION.toInt())
                    writeByte(cipher.iv.size)
                    write(cipher.iv)
                    writeInt(sealed.size)
                    write(sealed)
                    flush()
                }
                file.finishWrite(stream)
            } catch (e: Exception) {
                file.failWrite(stream)
                throw e
            }
        }.isSuccess
    }

    /** Al cerrar sesion la ubicacion no se queda en el telefono. */
    suspend fun clear() = withContext(Dispatchers.IO) { file.delete() }

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build(),
                )
            }
            .generateKey()
    }

    private companion object {
        const val FILE_NAME = "last_location.bin"
        const val FORMAT_VERSION: Byte = 1
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "parku_last_location"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_BITS = 128
    }
}
