package children.kotlin.readqrcode.buy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import children.kotlin.readqrcode.databinding.ActivityPayBinding
import com.amazon.device.drm.LicensingService
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.FulfillmentResult
import com.amazon.device.iap.model.ProductDataResponse
import com.amazon.device.iap.model.PurchaseResponse
import com.amazon.device.iap.model.PurchaseUpdatesResponse
import com.amazon.device.iap.model.UserDataResponse


class PayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPayBinding
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var currentUserId: String
    private lateinit var currentMarketplace: String

    companion object {
        const val subA = "children.kotlin.readqrcode.buy5scans"
        const val subB = "children.kotlin.readqrcode.buy10scans"
        const val subC = "children.kotlin.readqrcode.buy15scans"
        const val skuWeek = "children.kotlin.readqrcode.buysubpremium"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this)
        setupIAPOnCreate()
        binding.btSubA.setOnClickListener {
            PurchasingService.purchase(subA)

        }
        binding.btSubB.setOnClickListener {
            PurchasingService.purchase(subB)

        }
        binding.btSubC.setOnClickListener {
            PurchasingService.purchase(subC)
        }
        binding.btWeek.setOnClickListener {
            PurchasingService.purchase(skuWeek)

        }
        binding.btExit.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupIAPOnCreate() {
        val purchasingListener: PurchasingListener = object : PurchasingListener {
            override fun onUserDataResponse(response: UserDataResponse) {
                when (response.requestStatus!!) {

                    UserDataResponse.RequestStatus.SUCCESSFUL -> {

                        currentUserId = response.userData.userId
                        currentMarketplace = response.userData.marketplace
                        sharedPreferencesManager.currentUserId(currentUserId)

                    }

                    UserDataResponse.RequestStatus.FAILED, UserDataResponse.RequestStatus.NOT_SUPPORTED ->
                        Log.v("IAP SDK", "loading failed")
                }
            }

            override fun onProductDataResponse(productDataResponse: ProductDataResponse) {
                Log.v("productDataResponse", productDataResponse.requestStatus.toString())
                when (productDataResponse.requestStatus) {
                    ProductDataResponse.RequestStatus.SUCCESSFUL -> {

                        val products = productDataResponse.productData
                        for (key in products.keys) {
                            val product = products[key]
                            //
                            Log.v(
                                "Product:", String.format(
                                    "Product: %s\n Type: %s\n SKU: %s\n Price: %s\n Description: %s\n",
                                    product!!.title,
                                    product.productType,
                                    product.sku,
                                    product.price,
                                    product.description
                                )
                            )

                        }
                        //get all unavailable SKUs
                        for (s in productDataResponse.unavailableSkus) {
                            Log.v("Unavailable SKU:$s", "Unavailable SKU:$s")
                        }
                    }

                    ProductDataResponse.RequestStatus.FAILED -> Log.v("FAILED", "FAILED")
                    else -> {}
                }
            }

            override fun onPurchaseResponse(purchaseResponse: PurchaseResponse) {
                Log.v("purchaseResponse", purchaseResponse.requestStatus.toString())
                when (purchaseResponse.requestStatus) {

                    PurchaseResponse.RequestStatus.SUCCESSFUL -> {
                        if (purchaseResponse.receipt.sku == subA) {
                            sharedPreferencesManager.addLives(5)
                            val intent = Intent()
                            intent.putExtra("result", "success")
                            setResult(Activity.RESULT_OK, intent)
                            finish()

                        }
                        if (purchaseResponse.receipt.sku == subB) {
                            sharedPreferencesManager.addLives(10)
                            val intent = Intent()
                            intent.putExtra("result", "success")
                            setResult(Activity.RESULT_OK, intent)
                            finish()

                        }
                        if (purchaseResponse.receipt.sku == subC) {
                            sharedPreferencesManager.addLives(15)
                            val intent = Intent()
                            intent.putExtra("result", "success")
                            setResult(Activity.RESULT_OK, intent)
                            finish()

                        }
                        if(purchaseResponse.receipt.sku == skuWeek){
                            sharedPreferencesManager.isPremium = true
                            val intent = Intent()
                            intent.putExtra("result", "success")
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }


                        PurchasingService.notifyFulfillment(
                            purchaseResponse.receipt.receiptId,
                            FulfillmentResult.FULFILLED
                        )
//                        sharedPreferencesManager.isPremium = !purchaseResponse.receipt.isCanceled

                        Log.v("FAILED", "FAILED")
                    }

                    PurchaseResponse.RequestStatus.FAILED -> {
                        Log.v("FAILED", "FAILED")
                    }

                    else -> {

                    }
                }
            }

            override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse) {
//                Log.v("purchaseResponse", response.requestStatus.toString())
                when (response.requestStatus) {
                    PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL -> {
                        for (receipt in response.receipts) {
                            sharedPreferencesManager.isPremium = !receipt.isCanceled

                        }
                        if (response.hasMore()) {
                            PurchasingService.getPurchaseUpdates(false)
                        }


                    }

                    PurchaseUpdatesResponse.RequestStatus.FAILED -> Log.d("FAILED", "FAILED")
                    else -> {}
                }
            }
        }
        PurchasingService.registerListener(this, purchasingListener)
        Log.d(
            "DetailBuyAct",
            "Appstore SDK Mode: " + LicensingService.getAppstoreSDKMode()
        )
    }


    override fun onResume() {
        super.onResume()
        PurchasingService.getUserData()

        val productSkus: MutableSet<String> = HashSet()
        productSkus.add(skuWeek)
        productSkus.add(subA)
        productSkus.add(subB)
        productSkus.add(subC)
        PurchasingService.getProductData(productSkus)
        PurchasingService.getPurchaseUpdates(false)
    }
}