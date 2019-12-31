package com.foreverrafs.downloader

class DownloadException(override val message: String, val type: ExceptionType) : Exception() {


    enum class ExceptionType {
        NETWORK_ERROR,
        SERVER_ERROR
    }
}