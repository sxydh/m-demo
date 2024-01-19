package cn.net.bhe.zxingdemo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions


class MainActivity : ComponentActivity() {

    private var textView: TextView? = null
    private val launcher: ActivityResultLauncher<ScanOptions> = registerForActivityResult(
        ScanContract()
    ) {
        textView?.text = it.contents ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            val scanOptions = ScanOptions()
            scanOptions.setBeepEnabled(true)
            launcher.launch(scanOptions)
        }
        textView = findViewById(R.id.textView)
    }

}
