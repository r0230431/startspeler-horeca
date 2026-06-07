package com.startspeler.horeca.service

import com.startspeler.horeca.dto.common.ServiceResult
import java.io.File
import java.nio.file.Files
import java.util.UUID

class ProductImageStorage(
    private val uploadDirectory: File = File("uploads/products"),
    private val publicPathPrefix: String = "/uploads/products",
) {
    companion object {
        const val MAX_IMAGE_BYTES: Long = 2L * 1024L * 1024L
        private val allowedExtensions = setOf("jpg", "jpeg", "png", "webp")
        private val allowedMimeTypes = setOf(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
        )
    }

    init {
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs()
        }
    }

    fun storeImage(
        bytes: ByteArray,
        originalFileName: String?,
        contentType: String?,
    ): ServiceResult<String> {
        if (bytes.isEmpty()) {
            return ServiceResult.Error("Er werd geen afbeelding geselecteerd.")
        }

        if (bytes.size.toLong() > MAX_IMAGE_BYTES) {
            return ServiceResult.Error("De afbeelding is te groot. Maximum 2 MB toegestaan.")
        }

        val sanitizedOriginalName = originalFileName
            ?.substringAfterLast('/')
            ?.substringAfterLast('\\')
            ?.trim()
            .orEmpty()

        val extension = sanitizedOriginalName
            .substringAfterLast('.', "")
            .lowercase()

        if (extension !in allowedExtensions) {
            return ServiceResult.Error("Ongeldig bestandstype. Gebruik JPG, JPEG, PNG of WEBP.")
        }

        val normalizedContentType = contentType?.lowercase()
        if (normalizedContentType != null && normalizedContentType !in allowedMimeTypes) {
            return ServiceResult.Error("Ongeldig bestandstype. Gebruik JPG, JPEG, PNG of WEBP.")
        }

        val uniqueFileName = "${UUID.randomUUID()}.$extension"
        val destination = uploadDirectory.resolve(uniqueFileName)

        Files.write(destination.toPath(), bytes)

        return ServiceResult.Success("$publicPathPrefix/$uniqueFileName")
    }

    fun deleteByPublicPath(imageUrl: String?) {
        val normalizedPath = imageUrl?.trim()?.takeIf { it.startsWith(publicPathPrefix) } ?: return
        val fileName = normalizedPath.removePrefix("$publicPathPrefix/").trim()
        if (fileName.isBlank() || fileName.contains("/") || fileName.contains("\\")) {
            return
        }

        uploadDirectory.resolve(fileName).takeIf { it.exists() }?.delete()
    }
}
