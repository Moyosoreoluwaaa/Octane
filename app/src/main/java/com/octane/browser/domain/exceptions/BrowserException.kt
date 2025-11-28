package com.octane.browser.domain.exceptions

sealed class BrowserException(message: String) : Exception(message) {
    class InvalidUrlException(url: String) : BrowserException("Invalid URL: $url")
    class TabNotFoundException(tabId: String) : BrowserException("Tab not found: $tabId")
    class ConnectionFailedException(domain: String) : BrowserException("Connection failed: $domain")
    class BookmarkAlreadyExistsException(url: String) : BrowserException("Bookmark already exists: $url")
}