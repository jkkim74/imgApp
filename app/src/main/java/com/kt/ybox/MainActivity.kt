package com.kt.ybox

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : AppCompatActivity() {
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private val REQUEST_SELECT_FILE = 4 // 임의의 숫자 값
    private var filePathCallback: ValueCallback<Array<Uri>>? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val file = File(getExternalFilesDir(null), "example_image.jpg")
        val uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", file)

        val webView = findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.domStorageEnabled = true


        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d("WebView", consoleMessage?.message() ?: "No message")
                return super.onConsoleMessage(consoleMessage)
            }
            // For Android 5.0+
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                // 이전에 열려 있던 파일 선택 콜백을 무효화
                if (this@MainActivity.filePathCallback != null) {
                    this@MainActivity.filePathCallback?.onReceiveValue(null)
                    this@MainActivity.filePathCallback = null
                }

                this@MainActivity.filePathCallback = filePathCallback

                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // 복수 선택 허용

                startActivityForResult(Intent.createChooser(intent, "Select Images"), REQUEST_SELECT_FILE)

                return true
            }
        }
        //webView.loadUrl("http://192.168.1.124:8080/image/test_new");
        //webView.loadUrl("http://192.168.1.124:8080/image/uploadImg");
        webView.loadUrl("http://192.168.1.124:8080/image/write");
        //webView.loadUrl("http://10.0.2.2:8080/image/write")
        //webView.loadUrl("https://dev.ybox.kt.com/yapp4/yspot/main");
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SELECT_FILE && resultCode == Activity.RESULT_OK) {
            val clipData = data?.clipData
            val uris = mutableListOf<Uri>()

            if (clipData != null) { // 복수 파일 선택 시
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    uris.add(uri)
                    // 이미지를 다른 앱으로 열기
                    //openImage(uri)
                }
            } else {
                data?.data?.let { uri ->
                    uris.add(uri)
                    // 이미지를 다른 앱으로 열기
                    //openImage(uri)
                }
            }

            // 선택된 URI들 처리
            filePathCallback?.onReceiveValue(uris.toTypedArray())
            filePathCallback = null // 사용 후 콜백 초기화
        } else {
            filePathCallback?.onReceiveValue(null)
            filePathCallback = null // 사용 후 콜백 초기화
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

}
