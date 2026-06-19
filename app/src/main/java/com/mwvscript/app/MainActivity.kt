package com.mwvscript.app

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : Activity() {

    // ── タブID ──────────────────────────────────────────────
    companion object {
        const val TAB_BUILDER  = 0
        const val TAB_TERMINAL = 1
        const val TAB_WEB      = 2
        const val TAB_LAUNCHER = 3

        const val RECORDER_CHANNEL_ID  = "mwv_recorder"
        const val RECORDER_NOTIF_ID    = 1001
        const val ACTION_RECORDER_STOP = "com.mwvscript.app.RECORDER_STOP"
    }

    private var currentTab = TAB_BUILDER

    // ── ビルダータブの状態 ────────────────────────────────────
    private var selectedCategory: String? = null

    // ── テンプレートカテゴリ定義 ──────────────────────────────
    private val categories = listOf("UI", "System", "Data", "Media", "Advanced")

    private val templates: Map<String, List<Pair<String, String>>> = mapOf(
        "UI" to listOf(
            "Button"              to "Widgets/Button.txt",
            "TextView"            to "Widgets/TextView.txt",
            "ImageView"           to "Widgets/ImageView.txt",
            "ListView"            to "Widgets/ListView.txt",
            "Custom ListView"     to "Widgets/Custom ListView.txt",
            "Expandable ListView" to "Widgets/Expandable Listview.txt",
            "Widgets（全部入り）"  to "Widgets/Widgets.txt",
            "Color Drawable"      to "Drawables/ColorDrawable.txt",
            "Paint Drawable"      to "Drawables/PaintDrawable.txt",
            "Ripple Drawable"     to "Drawables/RippleDrawable.txt",
            "Shape Drawable"      to "Drawables/ShapeDrawable.txt",
            "Alpha Animation"     to "Animations/Alpha.txt",
            "Scale Animation"     to "Animations/Scale.txt",
            "Rotate Animation"    to "Animations/Rotate.txt",
            "Translate Animation" to "Animations/Translate.txt"
        ),
        "System" to listOf(
            "Accelerometer"       to "Sensors/Accerelometer.txt",
            "Proximity Sensor"    to "Sensors/Proximity.txt",
            "Light Sensor"        to "Sensors/Light.txt",
            "Notification"        to "Notification/Basic Notification.txt",
            "Background Service"  to "Service/Background Service.txt",
            "Start Service"       to "Service/Start Service.txt",
            "Stop Service"        to "Service/Stop Service.txt",
            "Request Permissions" to "Others/Request Permissions.txt",
            "Activity Lifecycle"  to "Others/Activity lifecycle.txt",
            "Battery Info"        to "Others/Battery Info.txt",
            "Device Details"      to "Others/Device Details.txt",
            "Vibrate"             to "Others/Vibrate.txt",
            "Flashlight"          to "Others/Flashlight.txt",
            "Immersive Mode"      to "Others/Immersive mode.txt",
            "setTheme"            to "Others/setTheme.txt"
        ),
        "Data" to listOf(
            "Read & Write File"   to "Files/Read and Write File.txt",
            "SQLite Database"     to "Database/Create Database.txt",
            "SharedPreferences"   to "Others/Load and Save Text.txt",
            "Preference Activity" to "Others/Preference Activity.txt",
            "Fetch from URL"      to "Others/Fetch data from url.txt",
            "Enable WiFi"         to "Network/Enable Wifi.txt",
            "Enable Bluetooth"    to "Network/Enable Bluetooth.txt",
            "File Chooser"        to "Others/File Chooser.txt",
            "File List"           to "Others/File List.txt"
        ),
        "Media" to listOf(
            "Audio Player"        to "Media/Media Player.txt",
            "Video Player"        to "Media/Video Player.txt",
            "Media Provider"      to "Media/MediaProvider.txt",
            "TextToSpeech"        to "Others/TextToSpeech.txt",
            "Speech Recognition"  to "Others/Speech Recognition.txt"
        ),
        "Advanced" to listOf(
            "WebView Browser"     to "Webview/Simple Web Browser.txt",
            "Canvas Draw"         to "Others/Canvas.txt",
            "3D Cube (jPCT-AE)"  to "Others/3D Cube.txt",
            "App Launcher"        to "Others/List installed apps.txt",
            "Broadcast Receiver"  to "Others/Broadcast Receiver.txt",
            "AlertDialog"         to "Dialogs/AlertDialog.txt",
            "Options Menu"        to "Menu/Options Menu.txt",
            "Popup Menu"          to "Menu/Popup Menu.txt",
            "Action Bar"          to "ActionBar/Action Bar Background.txt",
            "Action Mode"         to "ActionBar/Action Mode.txt",
            "Thread"              to "Threads/Thread.txt",
            "Handler"             to "Threads/Handler.txt",
            "Async Task"          to "Threads/Async Task.txt",
            "Font Icons"          to "Others/Font Icons.txt",
            "PopupWindow"         to "Others/PopupWindow.txt",
            "Activity for Result" to "Others/Activity For Result.txt",
            "Detect KeyPress"     to "Others/Detect keyPress.txt",
            "Task Description"    to "Others/TaskDescription.txt"
        )
    )

    // ── ライフサイクル ────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupRecorderNotification()
        setContentView(buildRootLayout())
    }

    // ── ルートレイアウト構築 ──────────────────────────────────
    private fun buildRootLayout(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val tabBar   = buildTabBar()
        val content  = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
            id = android.R.id.content
        }

        root.addView(content)
        root.addView(tabBar)   // タブバーは下部

        showTab(content, TAB_BUILDER)
        return root
    }

    // ── 下部タブバー ──────────────────────────────────────────
    private fun buildTabBar(): LinearLayout {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFF1A1A2E.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val labels = listOf("ビルダー", "ターミナル", "ウェブ", "ランチャー")
        labels.forEachIndexed { index, label ->
            val btn = TextView(this).apply {
                text = label
                gravity = Gravity.CENTER
                setPadding(0, 24, 0, 24)
                setTextColor(0xFFCCCCCC.toInt())
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    val contentFrame = findViewById<FrameLayout>(android.R.id.content)
                    showTab(contentFrame, index)
                    // タブバーの色更新
                    (parent as LinearLayout).forEachChild { child ->
                        (child as? TextView)?.setTextColor(0xFFCCCCCC.toInt())
                    }
                    setTextColor(0xFF4FC3F7.toInt())
                }
                if (index == TAB_BUILDER) setTextColor(0xFF4FC3F7.toInt())
            }
            bar.addView(btn)
        }
        return bar
    }

    // ── タブ切り替え ──────────────────────────────────────────
    private fun showTab(container: FrameLayout, tab: Int) {
        currentTab = tab
        container.removeAllViews()
        val view = when (tab) {
            TAB_BUILDER  -> buildBuilderTab()
            TAB_TERMINAL -> buildTerminalTab()
            TAB_WEB      -> buildWebTab()
            TAB_LAUNCHER -> buildLauncherTab()
            else         -> buildBuilderTab()
        }
        container.addView(view)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ビルダータブ
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    private fun buildBuilderTab(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0F0F1A.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // カテゴリ横スクロール
        val categoryScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }
        val categoryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(12, 12, 12, 12)
        }
        categories.forEach { cat ->
            val chip = buildCategoryChip(cat, cat == (selectedCategory ?: "UI")) {
                selectedCategory = cat
                // ビルダータブを再描画
                val contentFrame = findViewById<FrameLayout>(android.R.id.content)
                showTab(contentFrame, TAB_BUILDER)
            }
            categoryRow.addView(chip)
        }
        categoryScroll.addView(categoryRow)
        root.addView(categoryScroll)

        // 区切り線
        root.addView(divider())

        // テンプレート一覧
        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 8, 16, 8)
        }

        val cat = selectedCategory ?: "UI"
        templates[cat]?.forEach { (name, path) ->
            list.addView(buildTemplateCard(name, path))
        }

        scroll.addView(list)
        root.addView(scroll)
        return root
    }

    private fun buildCategoryChip(label: String, selected: Boolean, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            setPadding(28, 14, 28, 14)
            textSize = 13f
            gravity = Gravity.CENTER
            setOnClickListener { onClick() }
            val margin = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(4, 0, 4, 0) }
            layoutParams = margin
            if (selected) {
                setTextColor(0xFF0F0F1A.toInt())
                setBackgroundColor(0xFF4FC3F7.toInt())
            } else {
                setTextColor(0xFF4FC3F7.toInt())
                setBackgroundColor(0xFF1E1E35.toInt())
            }
        }
    }

    private fun buildTemplateCard(name: String, relativePath: String): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFF1E1E35.toInt())
            setPadding(20, 16, 20, 16)
            gravity = Gravity.CENTER_VERTICAL
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(0, 6, 0, 6) }
            layoutParams = lp
        }

        val nameView = TextView(this).apply {
            text = name
            setTextColor(0xFFEEEEEE.toInt())
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val btnEdit = buildSmallButton("編集") {
            openInJasEdit(relativePath)
        }
        val btnRun = buildSmallButton("実行") {
            runTemplate(relativePath)
        }

        card.addView(nameView)
        card.addView(btnEdit)
        card.addView(btnRun)
        return card
    }

    private fun buildSmallButton(label: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            setTextColor(0xFF0F0F1A.toInt())
            setBackgroundColor(0xFF4FC3F7.toInt())
            setPadding(20, 10, 20, 10)
            textSize = 12f
            gravity = Gravity.CENTER
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(8, 0, 0, 0) }
            layoutParams = lp
            setOnClickListener { onClick() }
        }
    }

    // ── テンプレート操作 ──────────────────────────────────────
    private fun templatePath(relativePath: String): String {
        return android.os.Environment.getExternalStorageDirectory().absolutePath +
            "/MWV-Script/Templates/RhinoJS/$relativePath"
    }

    private fun openInJasEdit(relativePath: String) {
        val file = java.io.File(templatePath(relativePath))
        if (!file.exists()) {
            Toast.makeText(this, "テンプレートが見つかりません", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = androidx.core.content.FileProvider.getUriForFile(
            this, "$packageName.provider", file
        )
        val intent = Intent(Intent.ACTION_EDIT).apply {
            setDataAndType(uri, "text/plain")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            setPackage("com.completeapps.jascriptapp")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "JASが見つかりません", Toast.LENGTH_SHORT).show()
        }
    }

    private fun runTemplate(relativePath: String) {
        val path = templatePath(relativePath)
        // SecurityGate → MWVEngineService経由で実行
        val intent = Intent(this, MWVEngineService::class.java).apply {
            action = MWVEngineService.ACTION_RUN_SCRIPT
            putExtra(MWVEngineService.EXTRA_SCRIPT_PATH, path)
        }
        startService(intent)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ターミナルタブ（スタブ）
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    private fun buildTerminalTab(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0F0F1A.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            val output = EditText(this@MainActivity).apply {
                setText("> MWV-Script Terminal\n> ")
                setTextColor(0xFF00FF88.toInt())
                setBackgroundColor(0xFF0F0F1A.toInt())
                textSize = 13f
                typeface = android.graphics.Typeface.MONOSPACE
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
                )
                setPadding(16, 16, 16, 16)
            }

            val inputRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(0xFF1E1E35.toInt())
                setPadding(8, 8, 8, 8)
            }
            val input = EditText(this@MainActivity).apply {
                hint = "スクリプトパスまたはコマンド"
                setTextColor(0xFFEEEEEE.toInt())
                setHintTextColor(0xFF666688.toInt())
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                textSize = 13f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val runBtn = buildSmallButton("▶ 実行") {
                val path = input.text.toString().trim()
                if (path.isNotEmpty()) runTemplate(path)
            }

            inputRow.addView(input)
            inputRow.addView(runBtn)

            addView(output)
            addView(divider())
            addView(inputRow)
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ウェブタブ（スタブ）
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    private fun buildWebTab(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0F0F1A.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            val urlBar = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(0xFF1E1E35.toInt())
                setPadding(8, 8, 8, 8)
            }
            val urlInput = EditText(this@MainActivity).apply {
                setText("https://")
                setTextColor(0xFFEEEEEE.toInt())
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                textSize = 13f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val goBtn = buildSmallButton("移動") { /* WebView実装時に接続 */ }
            urlBar.addView(urlInput)
            urlBar.addView(goBtn)

            val webView = android.webkit.WebView(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
                )
                settings.javaScriptEnabled = true
                loadUrl("https://www.google.com")
            }

            addView(urlBar)
            addView(webView)
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ランチャータブ（スタブ）
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    private fun buildLauncherTab(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0F0F1A.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            val header = TextView(this@MainActivity).apply {
                text = "インストール済みアプリ"
                setTextColor(0xFF4FC3F7.toInt())
                textSize = 15f
                setPadding(20, 20, 20, 12)
            }
            // GridViewはMWVLauncher.ktで実装予定
            val placeholder = TextView(this@MainActivity).apply {
                text = "JPKアプリ一覧をここに表示\n(MWVLauncher実装後に接続)"
                setTextColor(0xFF666688.toInt())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
                )
            }
            addView(header)
            addView(divider())
            addView(placeholder)
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // マクロレコーダー通知
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    private fun setupRecorderNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                RECORDER_CHANNEL_ID,
                "マクロレコーダー",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "録画開始ボタン常駐" }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
        showRecorderReadyNotification()
    }

    private fun showRecorderReadyNotification() {
        // タップ → 通知パネルを閉じる → a11yがACTION_CLOSE_SYSTEM_DIALOGSを検知 → 録画開始
        // PendingIntentは「何もしない」で良い。閉じる動作自体がトリガー
        val closePi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = NotificationCompat.Builder(this, RECORDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("MWV マクロレコーダー")
            .setContentText("タップして通知欄を閉じると録画開始")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(closePi)
            .build()

        NotificationManagerCompat.from(this).notify(RECORDER_NOTIF_ID, notif)
    }

    fun showRecorderActiveNotification() {
        val stopIntent = Intent(ACTION_RECORDER_STOP)
        val stopPi = PendingIntent.getBroadcast(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = NotificationCompat.Builder(this, RECORDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_rew)
            .setContentTitle("⏺ 録画中...")
            .setContentText("タップして録画停止")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(stopPi)
            .build()

        NotificationManagerCompat.from(this).notify(RECORDER_NOTIF_ID, notif)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ユーティリティ
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    private fun divider(): View {
        return View(this).apply {
            setBackgroundColor(0xFF2A2A45.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            )
        }
    }

    private fun LinearLayout.forEachChild(action: (View) -> Unit) {
        for (i in 0 until childCount) action(getChildAt(i))
    }
}
