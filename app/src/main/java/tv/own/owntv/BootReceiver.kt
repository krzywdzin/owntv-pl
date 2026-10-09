package tv.own.owntv

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Best-effort product-box autostart.
 *
 * Some OEM Android TV builds exempt launcher/TV apps from the Android 10+ background-activity
 * restriction and open the app after BOOT_COMPLETED. On stricter boxes this safely becomes a no-op;
 * the device provisioning procedure can then set TV Leśnik as launcher/kiosk instead.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        runCatching { context.startActivity(launch) }
    }
}
