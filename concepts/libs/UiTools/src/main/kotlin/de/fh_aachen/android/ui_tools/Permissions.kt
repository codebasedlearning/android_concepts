// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.ui_tools

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/*
 *  On modern Android (especially 11+):
 *    - There’s one-time permission (dialog: “Only this time”).
 *    - There’s Ask every time (in system settings).
 *    - OS may auto-revoke permissions after some time.
 *    - OEMs can tweak behavior subtly.
 *
 * You cannot reliably distinguish: 'first time', 'denied, can ask again',
 * 'denied, don’t ask again / blocked' or 'one-time / auto-revoked / ask-every-time'
 * on all versions & OEMs...
 * So, this approach is guidance, not a robust state machine.
 */

fun isPermissionGranted(permission: String, context: Context)
        = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

enum class PermissionUiState {
    Granted,
    NotGrantedRequestable,                  // can show dialog
    NotGrantedShowRationale,                // we should explain before asking
    NotGrantedMaybePermanentlyDenied        // suggest settings; best-effort guess
}

fun computePermissionUiState(
    permission: String,
    context: Context,
    activity: Activity,
    hasEverRequested: Boolean
): PermissionUiState {
    val isGranted = isPermissionGranted(permission, context)
    if (isGranted)
        return PermissionUiState.Granted

    if (!hasEverRequested)
        return PermissionUiState.NotGrantedRequestable

    val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    if (shouldShowRationale)
        return PermissionUiState.NotGrantedShowRationale

    // This is our best-effort "maybe permanently denied / don't ask again / blocked".
    return PermissionUiState.NotGrantedMaybePermanentlyDenied
}

fun gotoSettingsActivity(
    context: Context,
) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}
