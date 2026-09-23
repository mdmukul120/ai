package com.example.service

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.data.model.AppInfo

class DeviceController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val packageManager = context.packageManager

    // --- Volume Control ---
    fun volumeUp(): Pair<Int, String> {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val step = (max / 10).coerceAtLeast(1)
        val newVol = (current + step).coerceAtMost(max)

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, AudioManager.FLAG_SHOW_UI)
        val percent = ((newVol.toFloat() / max.toFloat()) * 100).toInt()
        return Pair(percent, "সাউন্ড বাড়িয়ে $percent% করা হয়েছে, বস।")
    }

    fun volumeDown(): Pair<Int, String> {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val step = (max / 10).coerceAtLeast(1)
        val newVol = (current - step).coerceAtLeast(0)

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, AudioManager.FLAG_SHOW_UI)
        val percent = ((newVol.toFloat() / max.toFloat()) * 100).toInt()
        return Pair(percent, "সাউন্ড কমিয়ে $percent% করা হয়েছে, বস।")
    }

    fun setVolumePercent(percent: Int): Pair<Int, String> {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val target = ((percent.coerceIn(0, 100) / 100f) * max).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
        val actualPercent = ((target.toFloat() / max.toFloat()) * 100).toInt()
        return Pair(actualPercent, "সাউন্ড $actualPercent% সেট করেছি, বস।")
    }

    fun muteVolume(): Pair<Int, String> {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)
        return Pair(0, "সাউন্ড সম্পূর্ণ মিউট করে দিয়েছি, বস।")
    }

    fun maxVolume(): Pair<Int, String> {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, AudioManager.FLAG_SHOW_UI)
        return Pair(100, "সাউন্ড সর্বোচ্চ ১০০% করে দিয়েছি, বস।")
    }

    fun getCurrentVolumePercent(): Int {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (max == 0) return 0
        return ((current.toFloat() / max.toFloat()) * 100).toInt()
    }

    // --- Installed Apps Management ---
    fun getInstalledApps(includeSystemApps: Boolean = false): List<AppInfo> {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(mainIntent, 0)
        }

        val appList = mutableListOf<AppInfo>()
        val seenPackages = mutableSetOf<String>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (seenPackages.contains(pkg)) continue
            seenPackages.add(pkg)

            val appInfo = resolveInfo.activityInfo.applicationInfo
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (isSystem && !includeSystemApps && !isImportantSystemApp(pkg)) {
                continue
            }

            val appName = resolveInfo.loadLabel(packageManager).toString()
            val icon = resolveInfo.loadIcon(packageManager)
            val description = getAppPurposeDescription(pkg, appName)
            val category = detectAppCategory(pkg, appName)

            appList.add(
                AppInfo(
                    appName = appName,
                    packageName = pkg,
                    icon = icon,
                    description = description,
                    category = category,
                    isSystemApp = isSystem,
                    canLaunch = true
                )
            )
        }

        return appList.sortedBy { it.appName.lowercase() }
    }

    private fun isImportantSystemApp(packageName: String): Boolean {
        return packageName.contains("camera", ignoreCase = true) ||
                packageName.contains("gallery", ignoreCase = true) ||
                packageName.contains("settings", ignoreCase = true) ||
                packageName.contains("dialer", ignoreCase = true) ||
                packageName.contains("calculator", ignoreCase = true) ||
                packageName.contains("chrome", ignoreCase = true) ||
                packageName.contains("youtube", ignoreCase = true)
    }

    // --- App Launching ---
    fun launchApp(packageName: String): Boolean {
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun launchAppByName(nameQuery: String): Pair<Boolean, String> {
        val query = nameQuery.trim().lowercase()
        val apps = getInstalledApps(includeSystemApps = true)

        // Exact or fuzzy match
        val matched = apps.firstOrNull {
            it.appName.lowercase() == query ||
                    it.packageName.lowercase() == query ||
                    it.appName.lowercase().contains(query) ||
                    query.contains(it.appName.lowercase())
        }

        return if (matched != null) {
            val launched = launchApp(matched.packageName)
            if (launched) {
                Pair(true, "${matched.appName} চালু করা হয়েছে, বস।")
            } else {
                Pair(false, "${matched.appName} চালু করতে ব্যর্থ হয়েছে, বস।")
            }
        } else {
            // Check specific known apps like facebook lite
            if (query.contains("facebook lite") || query.contains("ফেসবুক লাইট") || query.contains("fb lite")) {
                openFacebookLiteOrFallback(null)
            } else {
                Pair(false, "বস, '$nameQuery' নামের কোনো অ্যাপ ডিভাইসে খুঁজে পাওয়া যায়নি।")
            }
        }
    }

    fun openFacebookLiteOrFallback(messageText: String? = null): Pair<Boolean, String> {
        val fbLitePackage = "com.facebook.lite"
        val fbMainPackage = "com.facebook.katana"

        // Queue Accessibility Service Automation to click message tab
        CharuAccessibilityService.queueFacebookLiteMessageAutomation(messageText)

        val launched = launchApp(fbLitePackage)
        if (launched) {
            return Pair(true, "বস, ফেসবুক লাইট ওপেন করা হয়েছে এবং মেসেজ অপশনে প্রবেশ করানো হচ্ছে।")
        }

        // Try standard Facebook if Lite isn't installed
        val launchedMain = launchApp(fbMainPackage)
        if (launchedMain) {
            return Pair(true, "বস, ফেসবুক লাইট না থাকায় ফেসবুক অ্যাপ ওপেন করে মেসেজে যাওয়ার চেষ্টা করছি।")
        }

        // Fallback: Open Facebook in browser
        return try {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://m.facebook.com/messages/")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
            Pair(true, "বস, ফেসবুক লাইট অ্যাপ ইনস্টল না থাকায় ব্রাউজারে ফেসবুক মেসেজ ওপেন করেছি।")
        } catch (e: Exception) {
            Pair(false, "বস, ফেসবুক লাইট বা মেসেজ ওপেন করা সম্ভব হয়নি।")
        }
    }

    // --- App Purpose Explanations ---
    fun getAppPurposeDescription(packageName: String, appName: String): String {
        val lowerPkg = packageName.lowercase()
        val lowerName = appName.lowercase()

        return when {
            lowerPkg.contains("facebook.lite") || lowerName.contains("facebook lite") ->
                "ফেসবুক লাইট হলো মেটা-র হালকা ও কম ডেটা খরচের সামাজিক যোগাযোগ ও তাৎক্ষণিক মেসেজিং অ্যাপ। এর মাধ্যমে বন্ধুদের বার্তা পাঠানো, ছবি ও স্ট্যাটাস শেয়ার করা যায়।"

            lowerPkg.contains("facebook") || lowerName.contains("facebook") ->
                "ফেসবুক একটি বৈশ্বিক সামাজিক যোগাযোগ মাধ্যম, যার মাধ্যমে বন্ধুদের সাথে চ্যাট করা, পোস্ট শেয়ার করা এবং বিভিন্ন পেজ ও গ্রুপ ফলো করা যায়।"

            lowerPkg.contains("whatsapp") || lowerName.contains("whatsapp") ->
                "হোয়াটসঅ্যাপ একটি নিরাপদ এন্ড-টু-এন্ড এনক্রিপ্টেড মেসেজিং ও ভয়েস/ভিডিও কলিং অ্যাপ।"

            lowerPkg.contains("youtube") || lowerName.contains("youtube") ->
                "ইউটিউব হলো গুগল-এর বিশ্বের বৃহত্তম ভিডিও স্ট্রিমিং ও শেয়ারিং প্ল্যাটফর্ম। গান, নাটক, সংবাদ ও শিক্ষামূলক ভিডিও দেখা যায়।"

            lowerPkg.contains("chrome") || lowerName.contains("chrome") ->
                "গুগল ক্রোম হলো একটি দ্রুতগতির ও নিরাপদ ইন্টারনেট ব্রাউজার।"

            lowerPkg.contains("camera") || lowerName.contains("ক্যামেরা") || lowerName.contains("camera") ->
                "ক্যামেরা অ্যাপটি ডিভাইসের লেন্স ব্যবহার করে উচ্চমানের ছবি ও ভিডিও ধারণ করতে ব্যবহৃত হয়।"

            lowerPkg.contains("gallery") || lowerPkg.contains("photos") || lowerName.contains("ছবি") || lowerName.contains("photos") ->
                "গ্যালারি অ্যাপে ডিভাইসে সংরক্ষিত সকল ছবি, ভিডিও ও অ্যালবাম সুন্দরভাবে দেখা ও পরিচালনা করা যায়।"

            lowerPkg.contains("settings") || lowerName.contains("সেটিংস") || lowerName.contains("settings") ->
                "ডিভাইস সেটিংস হলো ফোনের নিয়ন্ত্রণ কেন্দ্র, যেখান থেকে ওয়াইফাই, ব্লুটুথ, ডিসপ্লে ও নিরাপত্তা নিয়ন্ত্রণ করা যায়।"

            lowerPkg.contains("calculator") || lowerName.contains("ক্যালকুলেটর") || lowerName.contains("calculator") ->
                "দৈনন্দিন হিসাব-নিকাশ ও বৈজ্ঞানিক গণিত সমাধানের জন্য ক্যালকুলেটর অ্যাপ।"

            lowerPkg.contains("dialer") || lowerPkg.contains("phone") || lowerName.contains("ফোন") || lowerName.contains("phone") ->
                "সরাসরি যেকোনো নম্বরে ফোন কল করা ও কল হিস্ট্রি দেখার অ্যাপ্লিকেশন।"

            lowerPkg.contains("messages") || lowerPkg.contains("mms") || lowerName.contains("বার্তা") || lowerName.contains("messages") ->
                "মোবাইল সিমের মাধ্যমে সাধারণ এসএমএস (SMS) পাঠানো ও গ্রহণ করার মেসেজিং অ্যাপ।"

            lowerPkg.contains("vending") || lowerPkg.contains("play.store") ->
                "গুগল প্লে স্টোর যেখান থেকে নতুন নতুন অ্যান্ড্রয়েড অ্যাপস ও গেমস ইনস্টল করা যায়।"

            lowerPkg.contains("maps") || lowerName.contains("maps") || lowerName.contains("ম্যাপ") ->
                "গুগল ম্যাপস দিয়ে রাস্তাঘাট খোঁজা, জিপিএস নেভিগেশন এবং ট্রাফিক তথ্য দেখা যায়।"

            lowerPkg.contains("gmail") || lowerName.contains("gmail") || lowerName.contains("ইমেইল") ->
                "অফিসিয়াল ও ব্যক্তিগত ইমেইল আদান-প্রদান করার গুগল জিমেইল সেবা।"

            else ->
                "এটি আপনার ডিভাইসের একটি সক্রিয় অ্যাপ্লিকেশন। বস, আপনি নির্দেশ দিলে চারু তাৎক্ষণিক এটি ওপেন করে প্রয়োজনীয় কাজ পরিচালনা করবে।"
        }
    }

    private fun detectAppCategory(packageName: String, appName: String): String {
        val lowerPkg = packageName.lowercase()
        return when {
            lowerPkg.contains("facebook") || lowerPkg.contains("whatsapp") || lowerPkg.contains("instagram") || lowerPkg.contains("twitter") || lowerPkg.contains("social") -> "সোশ্যাল মিডিয়া"
            lowerPkg.contains("youtube") || lowerPkg.contains("music") || lowerPkg.contains("video") || lowerPkg.contains("media") -> "বিনোদন ও মিডিয়া"
            lowerPkg.contains("chrome") || lowerPkg.contains("browser") -> "ইন্টারনেট ব্রাউজার"
            lowerPkg.contains("dialer") || lowerPkg.contains("phone") || lowerPkg.contains("messages") || lowerPkg.contains("contacts") -> "যোগাযোগ"
            lowerPkg.contains("camera") || lowerPkg.contains("gallery") || lowerPkg.contains("photos") -> "ছবি ও ভিডিও"
            lowerPkg.contains("settings") || lowerPkg.contains("system") -> "সিস্টেম ও সেটিংস"
            lowerPkg.contains("calculator") || lowerPkg.contains("clock") || lowerPkg.contains("calendar") || lowerPkg.contains("files") -> "দৈনন্দিন টুলস"
            else -> "ইউটিলিটি অ্যাপ"
        }
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
