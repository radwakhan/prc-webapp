package com.example.app

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.PRINT_SERVICE
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.example.app.ui.theme.AppTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.content.ContextCompat.getString
import androidx.core.content.ContextCompat.getSystemService
import com.example.app.webview.MyWebView
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.app.splashscreen.SplashScreen
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {


    // Permissions to request at runtime
    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_MEDIA_IMAGES,   // For image files
            Manifest.permission.READ_MEDIA_VIDEO,    // For video files
            Manifest.permission.READ_MEDIA_AUDIO     // For audio files
        )
    } else {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE // For Android 12 and below
        )
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (!allGranted) {
                Toast.makeText(this, "All permissions are required to run the app.", Toast.LENGTH_LONG).show()
                finish()  // Close the app if permissions are denied
            }
        }

    // Activity-level WebView reference
    private var webView: WebView? = null
    private var canGoBack = false  // Used to track WebView's back navigation state

    // FileChooser callback reference (for handling file uploads)
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null

    private var cameraImageUri: Uri? = null

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name with a timestamp
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionsIfNeeded()
        // File picker launcher
        val photoPickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val resultUri = data?.data ?: cameraImageUri  // Use camera URI if data is null (camera capture)

                resultUri?.let {
                    fileChooserCallback?.onReceiveValue(arrayOf(it))
                } ?: run {
                    fileChooserCallback?.onReceiveValue(null)
                }
            } else {
                fileChooserCallback?.onReceiveValue(null)
            }

            fileChooserCallback = null
        }

        enableEdgeToEdge()
        setContent {
            AppTheme {
                var isSplashScreenVisible by remember { mutableStateOf(true)}
                    // Splash screen logic
                    LaunchedEffect(key1 = Unit) {
                        kotlinx.coroutines.delay(6000)  // Splash screen duration (3 seconds)
                        isSplashScreenVisible = false  // Switch to main content
                    }
                if (isSplashScreenVisible) {
                    SplashScreen()
                } else {
                    Scaffold { paddingValues ->

                        MyWebView(
                            url = "https://digiedu.co.in/",
                            modifier = Modifier.padding(paddingValues),
                            onWebViewCreated = { createdWebView ->
                                // Assign WebView to the Activity-level reference
                                webView = createdWebView

                                webView?.webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        canGoBack = view?.canGoBack() ?: false
                                    }
                                }

                                webView?.webChromeClient = object : WebChromeClient() {

                                    // Handling file uploads
                                    override fun onShowFileChooser(
                                        webView: WebView?,
                                        filePathCallback: ValueCallback<Array<Uri>>?,
                                        fileChooserParams: FileChooserParams?
                                    ): Boolean {
                                        fileChooserCallback = filePathCallback

                                        // Create intent for capturing image from the camera
                                        val cameraIntent =
                                            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                                                resolveActivity(packageManager)?.let {
                                                    val photoFile = try {
                                                        createImageFile()  // Create a file to save the image
                                                    } catch (ex: IOException) {
                                                        null
                                                    }
                                                    photoFile?.also {
                                                        cameraImageUri = FileProvider.getUriForFile(
                                                            this@MainActivity,
                                                            "com.example.app.provider",
                                                            it
                                                        )
                                                        putExtra(
                                                            MediaStore.EXTRA_OUTPUT,
                                                            cameraImageUri
                                                        )
                                                    }
                                                }
                                            }

                                        // Create intent for picking an image from the gallery
                                        val galleryIntent =
                                            Intent(Intent.ACTION_GET_CONTENT).apply {
                                                type = "image/*"
                                            }

                                        // Combine intents into a chooser
                                        val chooserIntent = Intent.createChooser(
                                            galleryIntent,
                                            "Select or capture image"
                                        ).apply {
                                            if (cameraIntent.resolveActivity(packageManager) != null) {
                                                putExtra(
                                                    Intent.EXTRA_INITIAL_INTENTS,
                                                    arrayOf(cameraIntent)
                                                )
                                            }
                                        }

                                        // Launch the chooser intent
                                        try {
                                            photoPickerLauncher.launch(chooserIntent)
                                        } catch (e: ActivityNotFoundException) {
                                            fileChooserCallback?.onReceiveValue(null)
                                            return false
                                        }

                                        return true
                                    }


                                    // Handling pop-up windows
                                    override fun onCreateWindow(
                                        view: WebView?,
                                        isDialog: Boolean,
                                        isUserGesture: Boolean,
                                        resultMsg: Message?
                                    ): Boolean {
                                        val newWebView = WebView(this@MainActivity).apply {
                                            webViewClient = WebViewClient()
                                        }
                                        val dialog = android.app.Dialog(this@MainActivity).apply {
                                            setContentView(newWebView)
                                            show()
                                        }
                                        val transport = resultMsg?.obj as WebView.WebViewTransport
                                        transport.webView = newWebView
                                        resultMsg.sendToTarget()
                                        return true
                                    }

                                    // Grant notification permission
                                    override fun onPermissionRequest(request: PermissionRequest) {
                                        request.grant(request.resources)
                                    }
                                }
                            }
                        )

                        // Handle back press inside Compose (in-app back handling)
                        BackHandler(enabled = canGoBack) {
                            webView?.goBack()
                        }
                    }
                }
            }
        }
    }

    // Function to check and request permissions at runtime
    private fun requestPermissionsIfNeeded() {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // Handle device back button outside Compose
    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()  // Go back in WebView if it can
        } else {
            super.onBackPressed()  // Follow default behavior
        }
    }

    // Function to handle printing
    private fun printWebPage(context: Context, webView: WebView) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        val printAdapter: PrintDocumentAdapter = webView.createPrintDocumentAdapter("MyDocument")
        val jobName = "${context.getString(R.string.app_name)} Document"
        printManager?.print(jobName, printAdapter, PrintAttributes.Builder().build())
    }

}



