package expo.modules.applist

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.Promise
import expo.modules.kotlin.records.Record
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AppInfoRecord : Record {
    val name: String = ""
    val packageName: String = ""
    val iconPath: String? = null
    val isSystemApp: Boolean = false
}

class ExpoApplistModule : Module() {
    override fun definition() = ModuleDefinition {
        Name("ExpoApplist")

        // 获取完整应用信息列表 - 异步版本
        AsyncFunction("getApplistAsync") { promise: Promise ->
            try {
                val appInfoList = getInstalledApps()
                promise.resolve(appInfoList)
            } catch (e: SecurityException) {
                promise.reject("PERMISSION_DENIED", "缺少必要权限获取应用列表", e)
            } catch (e: Exception) {
                promise.reject("GET_APP_LIST_ERROR", "无法获取应用列表: ${e.message}", e)
            }
        }

        // 获取用户应用列表（排除系统应用） - 异步版本
        AsyncFunction("getUserAppsAsync") { promise: Promise ->
            try {
                val userApps = getUserInstalledApps()
                promise.resolve(userApps)
            } catch (e: SecurityException) {
                promise.reject("PERMISSION_DENIED", "缺少必要权限获取应用列表", e)
            } catch (e: Exception) {
                promise.reject("GET_USER_APPS_ERROR", "无法获取用户应用列表: ${e.message}", e)
            }
        }

        // 获取完整应用信息列表 - 同步版本
        Function("getApplist") {
            return@Function try {
                getInstalledApps()
            } catch (e: SecurityException) {
                throw Exception("缺少必要权限获取应用列表")
            } catch (e: Exception) {
                throw Exception("无法获取应用列表: ${e.message}")
            }
        }

        // 获取用户应用列表（排除系统应用） - 同步版本
        Function("getUserApps") {
            return@Function try {
                getUserInstalledApps()
            } catch (e: SecurityException) {
                throw Exception("缺少必要权限获取应用列表")
            } catch (e: Exception) {
                throw Exception("无法获取用户应用列表: ${e.message}")
            }
        }

        // 检查是否有使用统计权限
        Function("hasUsageStatsPermission") {
            return@Function try {
                hasUsageStatsPermission()
            } catch (e: Exception) {
                false
            }
        }

        // 获取正在运行的任务（需要权限）
        Function("getRunningTasks") {
            return@Function try {
                getRunningTasks()
            } catch (e: Exception) {
                emptyList<Map<String, Any?>>()
            }
        }

        // 获取最近的应用启动记录
        Function("getRecentAppLaunches") {
            return@Function try {
                getRecentAppLaunches()
            } catch (e: Exception) {
                emptyList<Map<String, Any?>>()
            }
        }
    }

    private val context: Context
        get() = requireNotNull(appContext.reactContext)

    // 获取完整的应用信息列表 - 增强版本，获取所有应用
    private fun getInstalledApps(): List<Map<String, Any?>> {
        val packageManager = context.packageManager
        val allAppsMap = mutableMapOf<String, Map<String, Any?>>() // 使用Map避免重复
        
        // 策略1: 使用不同的标志获取应用
        val flags = listOf(
            PackageManager.GET_META_DATA,
            PackageManager.GET_META_DATA or PackageManager.MATCH_UNINSTALLED_PACKAGES,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                PackageManager.GET_META_DATA or PackageManager.MATCH_ALL
            } else {
                PackageManager.GET_META_DATA
            },
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                PackageManager.GET_META_DATA or PackageManager.MATCH_DISABLED_COMPONENTS
            } else {
                PackageManager.GET_META_DATA
            }
        )
        
        flags.forEach { flag ->
            try {
                val applications = packageManager.getInstalledApplications(flag)
                applications.forEach { appInfo ->
                    try {
                        if (!allAppsMap.containsKey(appInfo.packageName)) {
                            val appName = packageManager.getApplicationLabel(appInfo).toString()
                            val packageName = appInfo.packageName
                            val isSystemApp = isSystemApp(appInfo)
                            val iconPath = saveAppIcon(appInfo, packageManager)
                            
                            allAppsMap[packageName] = mapOf(
                                "name" to appName,
                                "packageName" to packageName,
                                "iconPath" to iconPath,
                                "isSystemApp" to isSystemApp
                            )
                        }
                    } catch (e: Exception) {
                        // 跳过有问题的应用
                    }
                }
            } catch (e: Exception) {
                // 继续尝试下一个标志
            }
        }
        
        // 策略2: 通过PackageInfo获取
        try {
            val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            packages.forEach { packageInfo ->
                try {
                    val appInfo = packageInfo.applicationInfo
                    if (appInfo != null && !allAppsMap.containsKey(packageInfo.packageName)) {
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val packageName = packageInfo.packageName
                        val isSystemApp = isSystemApp(appInfo)
                        val iconPath = saveAppIcon(appInfo, packageManager)
                        
                        allAppsMap[packageName] = mapOf(
                            "name" to appName,
                            "packageName" to packageName,
                            "iconPath" to iconPath,
                            "isSystemApp" to isSystemApp
                        )
                    }
                } catch (e: Exception) {
                    // 跳过有问题的应用
                }
            }
        } catch (e: Exception) {
            // 继续
        }
        
        return allAppsMap.values.sortedBy { (it["name"] as String).lowercase() }
    }

    // 获取用户安装的应用列表（排除系统应用）- 增强版本，获取所有应用
    private fun getUserInstalledApps(): List<Map<String, Any?>> {
        val packageManager = context.packageManager
        val userAppsMap = mutableMapOf<String, Map<String, Any?>>() // 使用Map避免重复
        
        // 策略1: 使用不同的标志获取应用
        val flags = listOf(
            PackageManager.GET_META_DATA,
            PackageManager.GET_META_DATA or PackageManager.MATCH_UNINSTALLED_PACKAGES,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                PackageManager.GET_META_DATA or PackageManager.MATCH_ALL
            } else {
                PackageManager.GET_META_DATA
            },
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                PackageManager.GET_META_DATA or PackageManager.MATCH_DISABLED_COMPONENTS
            } else {
                PackageManager.GET_META_DATA
            }
        )
        
        flags.forEach { flag ->
            try {
                val applications = packageManager.getInstalledApplications(flag)
                applications.forEach { appInfo ->
                    try {
                        if (!isSystemApp(appInfo) && !userAppsMap.containsKey(appInfo.packageName)) {
                            val appName = packageManager.getApplicationLabel(appInfo).toString()
                            val packageName = appInfo.packageName
                            val iconPath = saveAppIcon(appInfo, packageManager)
                            
                            userAppsMap[packageName] = mapOf(
                                "name" to appName,
                                "packageName" to packageName,
                                "iconPath" to iconPath,
                                "isSystemApp" to false
                            )
                        }
                    } catch (e: Exception) {
                        // 跳过有问题的应用
                    }
                }
            } catch (e: Exception) {
                // 继续尝试下一个标志
            }
        }
        
        // 策略2: 通过Intent查询可启动的应用
        try {
            val mainIntent = android.content.Intent(android.content.Intent.ACTION_MAIN, null)
            mainIntent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            val launchableApps = packageManager.queryIntentActivities(mainIntent, 0)
            
            launchableApps.forEach { resolveInfo ->
                try {
                    val packageName = resolveInfo.activityInfo.packageName
                    if (!userAppsMap.containsKey(packageName)) {
                        val appInfo = packageManager.getApplicationInfo(packageName, 0)
                        
                        if (!isSystemApp(appInfo)) {
                            val appName = packageManager.getApplicationLabel(appInfo).toString()
                            val iconPath = saveAppIcon(appInfo, packageManager)
                            
                            userAppsMap[packageName] = mapOf(
                                "name" to appName,
                                "packageName" to packageName,
                                "iconPath" to iconPath,
                                "isSystemApp" to false
                            )
                        }
                    }
                } catch (e: Exception) {
                    // 跳过有问题的应用
                }
            }
        } catch (e: Exception) {
            // 继续
        }
        
        // 策略3: 通过PackageInfo获取
        try {
            val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            packages.forEach { packageInfo ->
                try {
                    val appInfo = packageInfo.applicationInfo
                    if (appInfo != null && !isSystemApp(appInfo) && !userAppsMap.containsKey(packageInfo.packageName)) {
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val packageName = packageInfo.packageName
                        val iconPath = saveAppIcon(appInfo, packageManager)
                        
                        userAppsMap[packageName] = mapOf(
                            "name" to appName,
                            "packageName" to packageName,
                            "iconPath" to iconPath,
                            "isSystemApp" to false
                        )
                    }
                } catch (e: Exception) {
                    // 跳过有问题的应用
                }
            }
        } catch (e: Exception) {
            // 继续
        }
        
        // 策略4: 对于Android 11+，尝试使用更多标志
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            try {
                // 尝试使用MATCH_ALL标志
                val allApps = packageManager.getInstalledApplications(
                    PackageManager.GET_META_DATA or PackageManager.MATCH_ALL
                )
                allApps.forEach { appInfo ->
                    try {
                        if (!isSystemApp(appInfo) && !userAppsMap.containsKey(appInfo.packageName)) {
                            val appName = packageManager.getApplicationLabel(appInfo).toString()
                            val packageName = appInfo.packageName
                            val iconPath = saveAppIcon(appInfo, packageManager)
                            
                            userAppsMap[packageName] = mapOf(
                                "name" to appName,
                                "packageName" to packageName,
                                "iconPath" to iconPath,
                                "isSystemApp" to false
                            )
                        }
                    } catch (e: Exception) {
                        // 跳过有问题的应用
                    }
                }
            } catch (e: Exception) {
                // 继续
            }
        }
        
        return userAppsMap.values.sortedBy { (it["name"] as String).lowercase() }
    }

    private fun isSystemApp(appInfo: ApplicationInfo): Boolean {
        return (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
               (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }

    private fun saveAppIcon(appInfo: ApplicationInfo, packageManager: PackageManager): String? {
        return try {
            val drawable = packageManager.getApplicationIcon(appInfo)
            val iconDir = File(context.cacheDir, "app_icons")
            if (!iconDir.exists()) {
                iconDir.mkdirs()
            }
            
            val iconFile = File(iconDir, "${appInfo.packageName}.png")
            if (!iconFile.exists()) {
                saveDrawableToFile(drawable, iconFile)
            }
            
            iconFile.absolutePath
        } catch (e: Exception) {
            // 如果无法保存图标，返回null但不影响其他信息
            null
        }
    }

    private fun saveDrawableToFile(drawable: Drawable, file: File) {
        try {
            val bitmap = android.graphics.Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            
            FileOutputStream(file).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: IOException) {
            throw IOException("无法保存应用图标: ${e.message}")
        }
    }

    // 检查是否有使用统计权限
    private fun hasUsageStatsPermission(): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
                val mode = appOps.checkOpNoThrow(
                    android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
                mode == android.app.AppOpsManager.MODE_ALLOWED
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // 获取正在运行的任务
    private fun getRunningTasks(): List<Map<String, Any?>> {
        return try {
            // 注意：从Android 5.0开始，getRunningTasks只能获取自己的任务
            // 这里返回空列表，因为获取其他应用的运行任务需要特殊权限
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 获取最近的应用启动记录
    private fun getRecentAppLaunches(): List<Map<String, Any?>> {
        return try {
            // 这个功能需要使用UsageStatsManager，需要特殊权限
            // 目前返回空列表，实际实现需要用户授权使用统计权限
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}