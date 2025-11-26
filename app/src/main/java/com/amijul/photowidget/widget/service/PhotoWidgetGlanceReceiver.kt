package com.amijul.photowidget.widget.service

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.amijul.photowidget.widget.data.PhotoWidgetGlance

class PhotoWidgetGlanceReceiver: GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget
        get() = PhotoWidgetGlance()
}