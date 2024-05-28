package children.kotlin.readqrcode

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import children.kotlin.readqrcode.buy.PayActivity
import children.kotlin.readqrcode.buy.SharedPreferencesManager
import children.kotlin.readqrcode.databinding.ActivityMainBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val PAYMENT_REQUEST_CODE = 123 // Giá trị có thể được thay đổi tùy ý
        private const val CAMERA_PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferencesManager = SharedPreferencesManager.getInstance(baseContext)
        binding.sl.text = sharedPreferencesManager.getLives().toString()

        binding.scanButton.setOnClickListener {
            if(check()){
                Toast.makeText(this, sharedPreferencesManager.getLives().toString(), Toast.LENGTH_SHORT).show()

                if(sharedPreferencesManager.isPremium==true){
                    updateLivesDisplay()
                } else {
                    Toast.makeText(this, sharedPreferencesManager.getLives().toString(), Toast.LENGTH_SHORT).show()
                    sharedPreferencesManager.removeLife()
                    updateLivesDisplay()
                }

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    startQRScanner()
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
                }

            } else {
                val intent = Intent(this, PayActivity::class.java)
                startActivity(intent)
            }

        }
        binding.buy.setOnClickListener {
            val intent = Intent(this, PayActivity::class.java)
            startActivityForResult(intent, PAYMENT_REQUEST_CODE)        }

        // Update lives display on startup
        updateLivesDisplay()
    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan a QR code")
        integrator.setCameraId(0) // Use a specific camera of the device
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(true)
        integrator.captureActivity = CustomCaptureActivity::class.java
        integrator.initiateScan()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRScanner()
            } else {
                binding.resultTextView.text = "Camera permission is required to scan QR codes"
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PAYMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            updateLivesDisplay()
        } else {
            val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    binding.resultTextView.text = "Cancelled"
                } else {
                    // Kiểm tra xem nội dung có phải là URL không
                    if (Patterns.WEB_URL.matcher(result.contents).matches()) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(result.contents))
                        startActivity(browserIntent)
                    } else {
                        binding.resultTextView.text = "Scanned: " + result.contents
                    }
                }
            }
        }
    }

    private fun updateLivesDisplay() {
        val newLives = sharedPreferencesManager.getLives()
        binding.sl.text = newLives.toString()
    }

    fun check(): Boolean {
        return sharedPreferencesManager.getLives() > 0 || sharedPreferencesManager.isPremium == true
    }
}
