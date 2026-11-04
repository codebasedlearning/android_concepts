// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
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

/*
 * The manifest declares the camera as not required (uses-feature ... required="false"), so the
 *  app also installs on devices without one (some tablets, Chromebooks, TVs). Check before asking
 *  for a permission to hardware that isn't there.
 */
fun hasCamera(context: Context): Boolean =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

enum class PermissionUiState {
    Granted,
    NotGrantedRequestable,                  // can show dialog
    NotGrantedShowRationale,                // we should explain before asking
    NotGrantedMaybePermanentlyDenied        // offer 'try again' AND settings; best-effort guess
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

    /*
     * Asked before, no rationale: our best-effort "maybe permanently denied / blocked".
     *  'Maybe', because these look exactly the same, but a new request WOULD show the dialog:
     *   - the user dismissed the dialog (Back, tap outside) - since Android 11 not a denial,
     *   - the permission was auto-reset (app unused for a while),
     *   - a one-time grant ("Only this time") has expired.
     *  So the UI offers both: try again (if blocked, it returns at once with 'denied') and Settings.
     */
    return PermissionUiState.NotGrantedMaybePermanentlyDenied
}

