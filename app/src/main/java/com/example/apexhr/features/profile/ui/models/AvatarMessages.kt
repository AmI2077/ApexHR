package com.example.apexhr.features.profile.ui.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.ui.model.messageRes
import com.example.apexhr.features.profile.domain.AvatarChangeError

@StringRes
fun AvatarChangeError.messageRes(): Int = when (this) {
    is AvatarChangeError.Data -> error.messageRes()
    AvatarChangeError.SessionMismatch -> R.string.profile_avatar_error_session
    AvatarChangeError.ImageUnreadable -> R.string.profile_avatar_error_image
    AvatarChangeError.ImageTooLarge -> R.string.profile_avatar_error_too_large
    AvatarChangeError.UploadRejected -> R.string.profile_avatar_error_rejected
}
