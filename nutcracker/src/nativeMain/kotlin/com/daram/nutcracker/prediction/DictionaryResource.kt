package com.daram.nutcracker.prediction

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

@OptIn(ExperimentalForeignApi::class)
internal actual fun loadDictionaryText(): String {
    val path = NSBundle.mainBundle.pathForResource("dictionary", ofType = "tsv")
        ?: error("dictionary.tsv not found in bundle")
    return NSString.stringWithContentsOfFile(path, encoding = NSUTF8StringEncoding, error = null)
        ?: error("Failed to read dictionary.tsv")
}
