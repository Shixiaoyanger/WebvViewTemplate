package com.yanger.suwayomi

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.HttpAuthHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.yanger.suwayomi.databinding.ActivityWebViewBinding
import androidx.core.content.edit

class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 开发环境下允许 WebView 调试
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        // 支持刘海屏（Android P 及以上）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val mode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes.layoutInDisplayCutoutMode = mode
        }

        // 沉浸式状态栏：内容延伸到状态栏下方
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 设置状态栏图标为白色（适配深色背景）
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false

        // 视图绑定
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // WebView 基本设置
        val settings = binding.webView.settings
        settings.javaScriptEnabled = true // 启用 JS
        settings.domStorageEnabled = true // 启用 DOM Storage

        // 启用 Cookie 支持
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(binding.webView, true)

        // 初始时隐藏加载图片
        binding.loaderImage.visibility = View.GONE

        // 设置自定义 WebViewClient 处理认证等
        binding.webView.webViewClient = AuthWebViewClient()
        // 从 BuildConfig 读取 URL（支持不同环境配置）
        val url = BuildConfig.WEBVIEW_URL
        binding.webView.loadUrl(url)
    }

    // Activity 获得焦点时可再次设置沉浸式（如用户下拉后）
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setImmersiveStatusBar()
        }
    }

    // 沉浸式状态栏设置
    private fun setImmersiveStatusBar() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
        // 如需隐藏状态栏可添加：
        controller.hide(WindowInsetsCompat.Type.statusBars())
        // 如需隐藏导航栏可添加：
        controller.hide(WindowInsetsCompat.Type.navigationBars())
    }

    override fun onPause() {
        super.onPause()
        // 同步 Cookie 到持久化存储
        CookieManager.getInstance().flush()
    }

    override fun onDestroy() {
        super.onDestroy()
        CookieManager.getInstance().flush()
    }

    override fun onResume() {
        super.onResume()
        // 可以在这里添加恢复时的逻辑，如刷新页面等
    }

    // 处理 WebView 返回键
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.canGoBack()) {
            binding.webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // 自定义 WebViewClient 处理 HTTP 认证、错误等
    private inner class AuthWebViewClient : WebViewClient() {
        private var triedAuthOnce = false

        // 页面开始加载时显示加载图片
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            binding.loaderImage.visibility = View.VISIBLE
        }

        // 页面加载完成时隐藏加载图片
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            binding.loaderImage.visibility = View.GONE
            url?.let {
                val cookies = CookieManager.getInstance().getCookie(it)
                // 可在此处处理 cookies
            }
        }

        // 处理 HTTP Basic Auth
        override fun onReceivedHttpAuthRequest(
            view: WebView?,
            handler: HttpAuthHandler,
            host: String,
            realm: String
        ) {

            val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
            val username = prefs.getString("username", null)
            val password = prefs.getString("password", null)
            if (!username.isNullOrEmpty() && !password.isNullOrEmpty() && !triedAuthOnce) {
                triedAuthOnce = true
                handler.proceed(username, password)
            } else {
                processLogin(host, handler)
            }
        }

        // 处理 HTTP 错误
        override fun onReceivedHttpError(
            view: WebView,
            request: WebResourceRequest,
            errorResponse: WebResourceResponse
        ) {
            if (errorResponse.statusCode == 401) {
                deleteSharedPreferences("auth")
                Toast.makeText(this@WebViewActivity, "delete auth info", Toast.LENGTH_SHORT).show()
            } else if (errorResponse.statusCode != 200) {
                Toast.makeText(
                    this@WebViewActivity,
                    "error code${errorResponse.statusCode}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            Log.d("debug", "error code${errorResponse.statusCode}")
        }

        // 保证所有跳转都在 WebView 内部处理
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return false
        }
    }

    // 弹出认证对话框，处理 HTTP Basic Auth
    fun processLogin(host: String, handler: HttpAuthHandler) {
        val builder = AlertDialog.Builder(this@WebViewActivity)
        val dialogView = layoutInflater.inflate(R.layout.dialog_auth, null)
        val usernameEditText = dialogView.findViewById<EditText>(R.id.username_edit_text)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.password_edit_text)
        builder.setTitle("需要认证")
        builder.setMessage("网站 $host 需要你的凭证")
        builder.setView(dialogView)
        builder.setPositiveButton("登录") { _, _ ->
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
            prefs.edit { putString("username", username) }
            prefs.edit { putString("password", password) }
            handler.proceed(username, password)
        }
        builder.setNegativeButton("取消") { _, _ ->
            handler.cancel()
        }
        builder.setOnCancelListener {
            handler.cancel()
        }
        builder.create().show()
    }
}