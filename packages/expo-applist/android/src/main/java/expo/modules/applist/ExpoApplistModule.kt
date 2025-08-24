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
    }

    private val context: Context
        get() = requireNotNull(appContext.reactContext)

    // 获取完整的应用信息列表
    private fun getInstalledApps(): List<Map<String, Any?>> {
        val packageManager = context.packageManager
        
        // 尝试多种方法获取应用列表，以获得最完整的结果
        val allApps = mutableSetOf<String>()
        val appInfoMap = mutableMapOf<String, Map<String, Any?>>()
        
        try {
            // 方法1: 使用 getInstalledPackages
            val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            packages.forEach { packageInfo ->
                allApps.add(packageInfo.packageName)
            }
            
            // 方法2: 使用 getInstalledApplications 作为补充
            val applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            applications.forEach { appInfo ->
                allApps.add(appInfo.packageName)
            }
            
            // 为所有发现的应用创建信息映射
            allApps.forEach { packageName ->
                try {
                    val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    val isSystemApp = isSystemApp(appInfo)
                    val iconPath = saveAppIcon(appInfo, packageManager)
                    
                    appInfoMap[packageName] = mapOf(
                        "name" to appName,
                        "packageName" to packageName,
                        "iconPath" to iconPath,
                        "isSystemApp" to isSystemApp
                    )
                } catch (e: Exception) {
                    // 如果获取某个应用信息失败，跳过该应用但不影响其他应用
                }
            }
            
        } catch (e: Exception) {
            // 如果上述方法失败，回退到原始方法
            val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            return packages
                .mapNotNull { packageInfo ->
                    try {
                        val appInfo = packageInfo.applicationInfo ?: return@mapNotNull null
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val packageName = packageInfo.packageName
                        val isSystemApp = isSystemApp(appInfo)
                        val iconPath = saveAppIcon(appInfo, packageManager)
                        
                        mapOf(
                            "name" to appName,
                            "packageName" to packageName,
                            "iconPath" to iconPath,
                            "isSystemApp" to isSystemApp
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                .sortedBy { (it["name"] as String).lowercase() }
        }
        
        return appInfoMap.values.sortedBy { (it["name"] as String).lowercase() }
    }

    // 获取用户安装的应用列表（排除系统应用）- 强力版本
    private fun getUserInstalledApps(): List<Map<String, Any?>> {
        val packageManager = context.packageManager
        val allUserApps = mutableMapOf<String, Map<String, Any?>>()
        
        // 方法1: 使用 getInstalledPackages 获取所有包
        try {
            val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            packages.forEach { packageInfo ->
                try {
                    val appInfo = packageInfo.applicationInfo
                    if (appInfo != null && !isSystemApp(appInfo)) {
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val packageName = packageInfo.packageName
                        val iconPath = saveAppIcon(appInfo, packageManager)
                        
                        allUserApps[packageName] = mapOf(
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
            // 如果方法1失败，继续尝试其他方法
        }
        
        // 方法2: 使用 getInstalledApplications 作为补充
        try {
            val applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            applications.forEach { appInfo ->
                try {
                    if (!isSystemApp(appInfo) && !allUserApps.containsKey(appInfo.packageName)) {
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val packageName = appInfo.packageName
                        val iconPath = saveAppIcon(appInfo, packageManager)
                        
                        allUserApps[packageName] = mapOf(
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
            // 如果方法2失败，继续尝试其他方法
        }
        
        // 方法3: 使用 MATCH_UNINSTALLED_PACKAGES 标志尝试获取更多应用
        try {
            val packagesWithUninstalled = packageManager.getInstalledPackages(
                PackageManager.GET_META_DATA or PackageManager.MATCH_UNINSTALLED_PACKAGES
            )
            packagesWithUninstalled.forEach { packageInfo ->
                try {
                    val appInfo = packageInfo.applicationInfo
                    if (appInfo != null && !isSystemApp(appInfo) && !allUserApps.containsKey(packageInfo.packageName)) {
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val packageName = packageInfo.packageName
                        val iconPath = saveAppIcon(appInfo, packageManager)
                        
                        allUserApps[packageName] = mapOf(
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
            // 如果方法3失败，继续
        }
        
        // 方法4: 使用 MATCH_ALL 标志（API 23+）
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val allPackages = packageManager.getInstalledPackages(
                    PackageManager.GET_META_DATA or PackageManager.MATCH_ALL
                )
                allPackages.forEach { packageInfo ->
                    try {
                        val appInfo = packageInfo.applicationInfo
                        if (appInfo != null && !isSystemApp(appInfo) && !allUserApps.containsKey(packageInfo.packageName)) {
                            val appName = packageManager.getApplicationLabel(appInfo).toString()
                            val packageName = packageInfo.packageName
                            val iconPath = saveAppIcon(appInfo, packageManager)
                            
                            allUserApps[packageName] = mapOf(
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
            }
        } catch (e: Exception) {
            // 如果方法4失败，继续
        }
        
        // 方法5: 尝试通过Intent查询可启动的应用
        try {
            val mainIntent = android.content.Intent(android.content.Intent.ACTION_MAIN, null)
            mainIntent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            val launchableApps = packageManager.queryIntentActivities(mainIntent, 0)
            
            launchableApps.forEach { resolveInfo ->
                try {
                    val packageName = resolveInfo.activityInfo.packageName
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    
                    if (!isSystemApp(appInfo) && !allUserApps.containsKey(packageName)) {
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val iconPath = saveAppIcon(appInfo, packageManager)
                        
                        allUserApps[packageName] = mapOf(
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
            // 如果方法5失败，继续
        }
        
        return allUserApps.values.sortedBy { (it["name"] as String).lowercase() }
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
}