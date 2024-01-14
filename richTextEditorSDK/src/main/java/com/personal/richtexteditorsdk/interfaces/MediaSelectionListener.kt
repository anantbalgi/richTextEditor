package com.personal.richtexteditorsdk.interfaces

import android.net.Uri

interface MediaSelectionListener {
    fun onMediaSelected(uri: Uri)
}