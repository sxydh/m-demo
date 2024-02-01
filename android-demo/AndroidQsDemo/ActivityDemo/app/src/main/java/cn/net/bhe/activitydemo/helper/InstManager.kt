package cn.net.bhe.activitydemo.helper

import android.app.Activity

object InstManager {

    private val activityList: MutableList<Activity> = ArrayList()

    @Synchronized
    fun addActivity(activity: Activity) {
        activityList.add(activity)
    }

    fun getActivityList(): List<Activity> {
        return activityList
    }

}