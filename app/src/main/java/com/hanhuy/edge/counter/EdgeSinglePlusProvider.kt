package com.hanhuy.edge.counter

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailProvider
import kotlin.math.max

class EdgeSinglePlusProvider : SlookCocktailProvider() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_INC -> {
                updateViews(context, increment(context))
            }
            ACTION_DEC -> {
                updateViews(context, decrement(context))
            }
            ACTION_RST -> {
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
    private fun updateView(context: Context, cocktailManager: SlookCocktailManager, id: Int, count: Int) {
        val views = RemoteViews(context.packageName, R.layout.panel)
        views.setTextViewText(R.id.count, count.toString())
        views.setViewVisibility(R.id.reset, if (count == 0) View.INVISIBLE else View.VISIBLE)

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

        const val ACTION_RST = "com.hanhuy.counter.action.RESET"
        const val ACTION_INC = "com.hanhuy.counter.action.INC"
        const val ACTION_DEC = "com.hanhuy.counter.action.DEC"

        fun component(context: Context): ComponentName = ComponentName(context, EdgeSinglePlusProvider::class.java)

        fun count(context: Context): Int =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(PREF_KEY, 0)

        fun increment(context: Context): Int = setCount(context, count(context) + 1)

        fun decrement(context: Context): Int = setCount(context, max(0, count(context) - 1))

        fun setCount(context: Context, value: Int): Int {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(PREF_KEY, value).apply()
            return value
        }
    }
}