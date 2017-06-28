package edu.txstate.mobile.tracs.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import edu.txstate.mobile.tracs.*

class MenuController {
    companion object {
        private val DUBLABS_APP = "edu.txstate.mobileapp"

        @JvmStatic
        fun handleMenuClick(menuId: Int, context: Context): Boolean {
            when (menuId) {
                R.id.menu_notifications -> launchIntent(NotificationsActivity::class.java, context)
                R.id.menu_notification_settings -> launchIntent(NotificationSettingsActivity::class.java, context)
                R.id.menu_about_us -> launchIntent(AboutActivity::class.java, context)
                R.id.menu_dublabs -> {
                    val dubLabsIntent: Intent? = context.packageManager.getLaunchIntentForPackage(DUBLABS_APP)
                    if (dubLabsIntent != null) { //Dublabs app is installed
                        context.startActivity(dubLabsIntent)
                    } else { //Dublabs app was not installed
                        try { //to go to the play store and get it
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$DUBLABS_APP")))
                        } catch (e: ActivityNotFoundException) { //the play store isn't installed
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$DUBLABS_APP")))
                        }
                    }
                }
                R.id.menu_feedback -> launchIntent(FeedbackActivity::class.java, context)
                R.id.menu_tracs_support -> launchIntent(SupportActivity::class.java, context)
                android.R.id.home -> launchIntent(MainActivity::class.java, context)
                else -> return false
            }
            return true
        }

        private fun launchIntent(clazz: Class<*>, context: Context) {
            val intent = Intent(context, clazz)
            context.startActivity(intent)
        }
    }
}