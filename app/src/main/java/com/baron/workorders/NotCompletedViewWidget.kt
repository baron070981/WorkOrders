package com.baron.workorders

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.activity.viewModels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.getValue
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import kotlinx.coroutines.SupervisorJob


class NotCompletedViewWidget : AppWidgetProvider() {
    val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate( context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        widgetScope.launch {
            try{
                val dbapp = context.applicationContext as DataBaseApp
                val countNotComplete: Int = dbapp.repo.getCountNotCompleteInt()

                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId, countNotComplete)
                }
            }
            catch (e: Exception){
                e.printStackTrace()
            }
            finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            count: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.view_not_completed_widget)
//            // Установка текста или обработчиков нажатий
            if (count > 0) {
                views.setViewVisibility(R.id.tv_widget_count, View.VISIBLE)
                views.setTextViewText(R.id.tv_widget_text, "В РАБОТЕ")
                views.setTextViewText(R.id.tv_widget_count, count.toString())
            } else {
                views.setViewVisibility(R.id.tv_widget_count, View.GONE)
                views.setTextViewText(R.id.tv_widget_text, "заявок нет")
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(context, appWidgetId,
                intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.ll_widget, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        fun forceUpdate(context: Context) {
            val intent = android.content.Intent(context, NotCompletedViewWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, NotCompletedViewWidget::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}