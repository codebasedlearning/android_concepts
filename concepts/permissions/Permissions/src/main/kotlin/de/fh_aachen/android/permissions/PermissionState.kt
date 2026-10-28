// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.permissions

import android.app.Activity
import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import de.fh_aachen.android.ui_tools.isPermissionGranted

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

/*
 * "Have we asked for this permission before?" - Android does not tell us, but we need it
 *  to tell 'never asked' from 'blocked' (both: denied, no rationale). It must survive an app
 *  restart: kept in rememberSaveable only, a permanently denied permission looked 'requestable'
 *  again after the next start, the tap gave an instant, silent 'no', and only then the hint
 *  to open the settings appeared. SharedPreferences are enough for one flag per permission
 *  (and keyed by permission, all screens asking for the same one share it).
 */
private fun Context.permissionPrefs() =
    getSharedPreferences("permission_requests", Context.MODE_PRIVATE)

fun wasPermissionRequested(context: Context, permission: String): Boolean =
    context.permissionPrefs().getBoolean(permission, false)

fun markPermissionRequested(context: Context, permission: String) =
    context.permissionPrefs().edit { putBoolean(permission, true) }

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

