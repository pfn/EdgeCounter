package com.hanhuy.edge.counter

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.RemoteViews
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailProvider
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max


class EdgeSinglePlusProvider : SlookCocktailProvider() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_INC -> {
                vibrate(context)
                updateViews(context, increment(context))
            }
            ACTION_DEC -> {
                vibrate(context)
                updateViews(context, decrement(context))
            }
            ACTION_RST -> {
                vibrate(context)
                updateViews(context, setCount(context, 0))
            }
            else -> super.onReceive(context, intent)
        }
    }

    override fun onUpdate(
        context: Context,
        cocktailManager: SlookCocktailManager,
        cocktailIds: IntArray?
    ) {
        super.onUpdate(context, cocktailManager, cocktailIds)
        cocktailIds?.forEach {
            updateView(context, cocktailManager, it, count(context))
        }
    }

    private fun updateViews(context: Context, count: Int) {
        val cocktailManager = SlookCocktailManager.getInstance(context)
        cocktailManager.getCocktailIds(component(context)).forEach {
            updateView(context, cocktailManager, it, count)
        }
    }
    private fun updateView(
        context: Context,
        cocktailManager: SlookCocktailManager,
        id: Int,
        count: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.panel)

        views.setTextViewText(R.id.count, count.toString())

        val zeroVisibility = if (count == 0) View.INVISIBLE else View.VISIBLE
        views.setViewVisibility(R.id.reset, zeroVisibility)
        views.setViewVisibility(R.id.last_time, zeroVisibility)
        views.setViewVisibility(R.id.last_container, zeroVisibility)

        val fmt = SimpleDateFormat(context.getString(R.string.ts_fmt), Locale.US)
        views.setTextViewText(R.id.last_time, fmt.format(Date(last(context))))

        views.setOnClickPendingIntent(
            R.id.add,
            PendingIntent.getBroadcast(
                context,
                99,
                Intent(ACTION_INC).setComponent(component(context)),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        views.setOnClickPendingIntent(
            R.id.del,
            PendingIntent.getBroadcast(
                context,
                200,
                Intent(ACTION_DEC).setComponent(component(context)),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        views.setOnClickPendingIntent(
            R.id.reset,
            PendingIntent.getBroadcast(
                context,
                32,
                Intent(ACTION_RST).setComponent(component(context)),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        cocktailManager.updateCocktail(id, views)
    }

    private companion object {
        const val PREF_NAME = "data"
        const val PREF_KEY = "count"
        const val PREF_TIMESTAMP = "last_count"

        const val ACTION_RST = "com.hanhuy.counter.action.RESET"
        const val ACTION_INC = "com.hanhuy.counter.action.INC"
        const val ACTION_DEC = "com.hanhuy.counter.action.DEC"

        fun component(context: Context): ComponentName =
            ComponentName(context, EdgeSinglePlusProvider::class.java)

        fun count(context: Context): Int =
            prefs(context).getInt(PREF_KEY, 0)

        fun increment(context: Context): Int = setCount(context, count(context) + 1)

        fun decrement(context: Context): Int = setCount(context, max(0, count(context) - 1))

        fun setCount(context: Context, value: Int): Int {
            val editor = prefs(context).edit()
            editor.putInt(PREF_KEY, value).apply()
            editor.putLong(PREF_TIMESTAMP, System.currentTimeMillis()).apply()
            return value
        }

        fun prefs(context: Context): SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        fun last(context: Context): Long =
            prefs(context).getLong(PREF_TIMESTAMP, 0L)

        fun vibrate(context: Context) {
            val vibe = context.getSystemService(Vibrator::class.java)
            val aa = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            vibe?.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK), aa
            )
        }
    }
}