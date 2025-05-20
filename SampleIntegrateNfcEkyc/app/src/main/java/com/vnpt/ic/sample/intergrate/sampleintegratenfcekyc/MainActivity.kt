package com.vnpt.ic.sample.intergrate.sampleintegratenfcekyc

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.nfc.NfcManager
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.vnptit.nfc.activity.VnptScanNFCActivity
import com.vnptit.nfc.utils.KeyIntentConstantsNFC
import com.vnptit.nfc.utils.KeyResultConstantsNFC
import com.vnptit.nfc.utils.SDKEnumNFC

class MainActivity : AppCompatActivity() {

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var domain: String
    private lateinit var access_token: String
    private lateinit var token_id: String
    private lateinit var token_key: String
    private lateinit var access_token_ekyc: String
    private lateinit var token_id_ekyc: String
    private lateinit var token_key_ekyc: String

    companion object {
        const val EXTRA_LOG_RESULT = "extra:LOG_RESULT"
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val rootView = findViewById<CoordinatorLayout>(R.id.coordinator)
        domain = ""
        access_token = "<ACCESS_TOKEN> (including bearer)"
        token_id = "<TOKEN_ID>"
        token_key = "<TOKEN_KEY>"
        access_token_ekyc = "<ACCESS_TOKEN_EKYC> (including bearer)"
        token_id_ekyc = "TOKEN_ID_EKYC"
        token_key_ekyc = "TOKEN_KEY_EKYC"

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.let { data ->
                        /**
                         * đường dẫn ảnh mặt trước trong thẻ chip lưu trong cache
                         * [KeyResultConstantsNFC.IMAGE_AVATAR_CARD_NFC]
                         */
                        val avatarPath = data.getStringExtra(KeyResultConstantsNFC.PATH_IMAGE_AVATAR)

                        /**
                         * chuỗi thông tin cua SDK
                         * [KeyResultConstantsNFC.CLIENT_SESSION_RESULT]
                         */
                        val clientSession =
                            data.getStringExtra(KeyResultConstantsNFC.CLIENT_SESSION_RESULT)

                        /**
                         * kết quả NFC
                         * [KeyResultConstantsNFC.LOG_NFC]
                         */
                        val logNFC = data.getStringExtra(KeyResultConstantsNFC.DATA_NFC_RESULT)

                        /**
                         * mã hash avatar
                         * [KeyResultConstantsNFC.HASH_AVATAR]
                         */
                        val hashAvatar = data.getStringExtra(KeyResultConstantsNFC.HASH_IMAGE_AVATAR)

                        /**
                         * chuỗi json string chứa thông tin post code của quê quán
                         * [KeyResultConstantsNFC.POST_CODE_ORIGINAL_LOCATION_RESULT]
                         */
                        val postCodeOriginalLocation =
                            data.getStringExtra(KeyResultConstantsNFC.POST_CODE_ORIGINAL_LOCATION_RESULT)

                        /**
                         * chuỗi json string chứa thông tin post code của nơi thường trú
                         * [KeyResultConstantsNFC.POST_CODE_RECENT_LOCATION_RESULT]
                         */
                        val postCodeRecentLocation =
                            data.getStringExtra(KeyResultConstantsNFC.POST_CODE_RECENT_LOCATION_RESULT)

                        /**
                         * time scan nfc
                         * [KeyResultConstantsNFC.TIME_SCAN_NFC]
                         */
                        val timeScanNfc = data.getStringExtra(KeyResultConstantsNFC.TIME_SCAN_NFC)

                        /**
                         * kết quả check chip căn cước công dân
                         * [KeyResultConstantsNFC.CHECK_AUTH_CHIP_RESULT]
                         */
                        val checkAuthChipResult =
                            data.getStringExtra(KeyResultConstantsNFC.STATUS_CHIP_AUTHENTICATION)

                        /**
                         * kết quả quét QRCode căn cước công dân
                         * [KeyResultConstantsNFC.QR_CODE_RESULT_NFC]
                         */
                        val qrCodeResult = data.getStringExtra(KeyResultConstantsNFC.QR_CODE_RESULT)

                        val results = ArrayList<LogResult>().apply {
                            addNotNullOrEmpty("Avatar NFC", avatarPath)
                            addNotNullOrEmpty("Client session", clientSession)
                            addNotNullOrEmpty("Log NFC", logNFC)
                            addNotNullOrEmpty("Hash avatar", hashAvatar)
                            addNotNullOrEmpty("Postcode original location", postCodeOriginalLocation)
                            addNotNullOrEmpty("Postcode recent location", postCodeRecentLocation)
                            addNotNullOrEmpty("Time scan NFC", timeScanNfc)
                            addNotNullOrEmpty("Check auth chip", checkAuthChipResult)
                            addNotNullOrEmpty("Qrcode", qrCodeResult)
                        }

                        if (results.isNotEmpty()) {
                            Intent(this, LogResultActivity::class.java).also {
                                it.putExtra(EXTRA_LOG_RESULT, results)

                                startActivity(it)
                            }
                        }
                    }
                }
            }

        findViewById<Button>(R.id.nfc_qrcode).setOnClickListener {
            if (!isDeviceSupportedNfc(rootView)) {
                return@setOnClickListener
            }
            navigateToNfcQrCode()
        }
         findViewById<Button>(R.id.scan_mrz_nfc).setOnClickListener {
               if (!isDeviceSupportedNfc(rootView)) {
                  return@setOnClickListener
               }
               navigateToMrzNfc()
         }

        findViewById<Button>(R.id.scan_nfc).setOnClickListener {
            if (!isDeviceSupportedNfc(rootView)) {
                return@setOnClickListener
            }

            val builder = MaterialAlertDialogBuilder(this)
            val view = layoutInflater.inflate(R.layout.dialog_card_info, null)
            builder.setView(view)
            builder.setTitle("Nhập thông tin")
            builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                val cardIdInput = view.findViewById<TextInputLayout>(R.id.card_id_input)
                val cardDobInput = view.findViewById<TextInputLayout>(R.id.card_dob_input)
                val cardExpiredDateInput = view.findViewById<TextInputLayout>(R.id.card_expired_date_input)

                dialog.cancel()
                navigateToScanNfc(
                    id = cardIdInput.editText?.text?.trim().toString(),
                    dob = cardDobInput.editText?.text?.trim().toString(),
                    expiredDate = cardExpiredDateInput.editText?.text?.trim().toString()
                )
            }
            builder.show()
        }
    }

    private fun ArrayList<LogResult>.addNotNullOrEmpty(key: String, value: String?) {
        if (!TextUtils.isEmpty(value)) {
            add(LogResult(key, value))
        }
    }

    private fun isDeviceSupportedNfc(rootView: CoordinatorLayout): Boolean {
        val adapter = (getSystemService(NFC_SERVICE) as? NfcManager)?.defaultAdapter
        if (adapter != null && adapter.isEnabled) {
            return true
        }

        val snackbar =
            Snackbar.make(rootView, "Thiết bị không hỗ trợ NFC", Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction(android.R.string.ok) { snackbar.dismiss() }
        snackbar.show()
        return false
    }

    private fun navigateToNfcQrCode() {

        Intent(this, VnptScanNFCActivity::class.java).also {
            /**
             * Truyền access token chứa bearer
             */
            it.putExtra(KeyIntentConstantsNFC.ACCESS_TOKEN, access_token)
            /**
             * Truyền token id
             */
            it.putExtra(KeyIntentConstantsNFC.TOKEN_ID, token_id)
            /**
             * Truyền token key
             */
            it.putExtra(KeyIntentConstantsNFC.TOKEN_KEY, token_key)

            /**
             * Truyền access token ekyc cho dịch vụ postcode
             */
            it.putExtra(KeyIntentConstantsNFC.ACCESS_TOKEN_EKYC, access_token_ekyc)

            /**
             * Truyền token id ekyc cho dịch vụ postcode
             */
            it.putExtra(KeyIntentConstantsNFC.TOKEN_ID_EKYC, token_id_ekyc)

            /**
             * Truyền token key ekyc cho dịch vụ postcode
             */
            it.putExtra(KeyIntentConstantsNFC.TOKEN_KEY_EKYC, token_key_ekyc)
            /**
             * điều chỉnh ngôn ngữ tiếng việt
             *    - vi: tiếng việt
             *    - en: tiếng anh
             */
            it.putExtra(KeyIntentConstantsNFC.LANGUAGE_SDK, SDKEnumNFC.LanguageEnum.VIETNAMESE.value)
            /**
             * hiển thị màn hình hướng dẫn
             * - mặc định luôn luôn hiển thị màn hình hướng dẫn
             *    - true: hiển thị màn hướng dẫn
             *    - false: ko hiển thị màn hướng dẫn
             */
            it.putExtra(KeyIntentConstantsNFC.IS_SHOW_TUTORIAL, true)

            /**
             * hiển thị nút bỏ qua hướng dẫn
             * - mặc định luôn luôn hiển thị nút bỏ qua
             *    - true: hiển thị nút bỏ qua
             *    - false: ko hiển thị nút bỏ qua
             */
            it.putExtra(KeyIntentConstantsNFC.IS_ENABLE_GOT_IT, true)
            /**
             * bật tính năng upload ảnh
             *    - true: bật tính năng
             *    - false: tắt tính năng
             */
            it.putExtra(KeyIntentConstantsNFC.IS_ENABLE_UPLOAD_IMAGE, true)
            /**
             * bật tính năng get Postcode
             *    - true: bật tính năng
             *    - false: tắt tính năng
             */
            it.putExtra(KeyIntentConstantsNFC.IS_ENABLE_POSTCODE_MATCHING, true)
            /**
             * bật tính năng xác thực chip
             *    - true: bật tính năng
             *    - false: tắt tính năng
             */
//            it.putExtra(KeyIntentConstantsNFC.IS_ENABLE_CHECK_CHIP_CLONE, true)
            /**
             * truyền các giá trị đọc thẻ
             *    - nếu không truyền gì mặc định sẽ đọc tất cả (MRZ,Verify Document,Image Avatar)
             *    - giá trị truyền vào là 1 mảng int: nếu muốn đọc giá trị nào sẽ truyền
             *      giá trị đó vào mảng
             * eg: chỉ đọc thông tin MRZ
             *    intArrayOf(SDKEnumNFC.ReadingNFCTags.MRZInfo.value)
             */
//            it.putExtra(
//                KeyIntentConstantsNFC.READING_TAGS_NFC,
//                intArrayOf(
//                    SDKEnumNFC.ReadingNFCTags.MRZInfo.value,
//                    SDKEnumNFC.ReadingNFCTags.VerifyDocumentInfo.value,
//                    SDKEnumNFC.ReadingNFCTags.ImageAvatarInfo.value,
//                    SDKEnumNFC.ReadingNFCTags.AuthenticationInfo.value,
//                )
//            )
            /**
             * truyền giá trị bật quét read card mode
             * - [SDKEnumNFC.ReaderCardMode.QRCODE.getValue]: quét QR code -> scan NFC
             * - [SDKEnumNFC.ReaderCardMode.MRZ_CODE.getValue]: quét MRZ -> scan NFC
             * - [SDKEnumNFC.ReaderCardMode.NONE.getValue]: scan NFC
             */
            it.putExtra(KeyIntentConstantsNFC.READER_CARD_MODE, SDKEnumNFC.ReaderCardMode.QRCODE.value)
            // set baseDomain="" => sử dụng mặc định là Product
            it.putExtra(KeyIntentConstantsNFC.BASE_URL, domain)
            // truyền giá trị challange code
//            intent.putExtra(KeyIntentConstantsNFC.CHALLENGE_CODE, "INNOVATIONCENTER")

//            /**
//             * Upload thông tin NFC
//             */
//            intent.putExtra(KeyIntentConstantsNFC.TRANSACTION_PARTNER_ID_UPLOAD_NFC, "UPLOAD_NFC")
//
//            /**
//             * Truyền giá trị địa chỉ thường trú
//             */
//            intent.putExtra(KeyIntentConstantsNFC.TRANSACTION_PARTNER_ID_ORIGINAL_LOCATION, "ORIGINAL_LOCATION")
//
//            /**
//             * Truyền giá trị địa chỉ tạm trú
//             */
//            intent.putExtra(KeyIntentConstantsNFC.TRANSACTION_PARTNER_ID_RECENT_LOCATION, "RECENT_LOCATION")

            intent.putExtra(KeyIntentConstantsNFC.IS_SHOW_LOGO, true)

            resultLauncher.launch(it)
        }
    }

    private fun navigateToMrzNfc() {
        Intent(this, VnptScanNFCActivity::class.java).also {
            /**
             * Truyền access token chứa bearer
             */
            it.putExtra(KeyIntentConstantsNFC.ACCESS_TOKEN, access_token)
            /**
             * Truyền token id
             */
            it.putExtra(KeyIntentConstantsNFC.TOKEN_ID, token_id)
            /**
             * Truyền token key
             */
            it.putExtra(KeyIntentConstantsNFC.TOKEN_KEY, token_key)
            /**
             * Truyền access token ekyc cho dịch vụ postcode
             */
            it.putExtra(KeyIntentConstantsNFC.ACCESS_TOKEN_EKYC, access_token_ekyc)

            /**
             * Truyền token id ekyc cho dịch vụ postcode
             */
            it.putExtra(KeyIntentConstantsNFC.TOKEN_ID_EKYC, token_id_ekyc)

            /**
             * Truyền token key ekyc cho dịch vụ postcode
             */
            it.putExtra(KeyIntentConstantsNFC.TOKEN_KEY_EKYC, token_key_ekyc)
            /**
             * điều chỉnh ngôn ngữ tiếng việt
             *    - vi: tiếng việt
             *    - en: tiếng anh
             */
            it.putExtra(KeyIntentConstantsNFC.LANGUAGE_SDK, SDKEnumNFC.LanguageEnum.VIETNAMESE.value)
            /**
             * hiển thị màn hình hướng dẫn + hiển thị nút bỏ qua hướng dẫn
             * - mặc định luôn luôn hiển thị màn hình hướng dẫn
             *    - true: hiển thị nút bỏ qua
             *    - false: ko hiển thị nút bỏ qua
             */
            it.putExtra(KeyIntentConstantsNFC.IS_ENABLE_GOT_IT, true)
            /**
             * bật tính năng upload ảnh
             *    - true: bật tính năng
             *    - false: tắt tính năng
             */
            it.putExtra(KeyIntentConstantsNFC.IS_ENABLE_UPLOAD_IMAGE, true)
            /**
             * bật tính năng get Postcode
             *    - true: bật tính năng
             *    - false: tắt tính năng
             */
            it.putExtra(KeyIntentConstantsNFC.IS_ENABLE_POSTCODE_MATCHING, true)

            it.putExtra(KeyIntentConstantsNFC.READER_CARD_MODE, SDKEnumNFC.ReaderCardMode.MRZ_CODE.value)

            it.putExtra(KeyIntentConstantsNFC.BASE_URL, domain)

            it.putExtra(KeyIntentConstantsNFC.IS_SHOW_LOGO, true)

            resultLauncher.launch(it)
        }
    }

    private fun navigateToScanNfc(id: String, dob: String, expiredDate: String) {
        Intent(this, VnptScanNFCActivity::class.java).also {
//            intent.putExtra(KeyIntentConstantsNFC.URL_UPLOAD_DATA_NFC, "/ai/v1/nfc")
            /**
             * Truyền access token chứa bearer
             */
            it.putExtra(KeyIntentConstantsNFC.ACCESS_TOKEN, access_token)
            /**
             * Truyền token id
             */
            it.putExtra(KeyIntentConstantsNFC.TOKEN_ID, token_id)
            /**
             * Truyền token key
             */
            it.putExtra(KeyIntentConstantsNFC.TOKEN_KEY, token_key)
            /**
                * Truyền access token ekyc cho dịch vụ postcode
            */
            it.putExtra(KeyIntentConstantsNFC.ACCESS_TOKEN_EKYC, access_token_ekyc)

            /**
                * Truyền token id ekyc cho dịch vụ postcode
            */
            it.putExtra(KeyIntentConstantsNFC.TOKEN_ID_EKYC, token_id_ekyc)

            /**
                * Truyền token key ekyc cho dịch vụ postcode
            */
            it.putExtra(KeyIntentConstantsNFC.TOKEN_KEY_EKYC, token_key_ekyc)
            /**
             * điều chỉnh ngôn ngữ tiếng việt
             *    - vi: tiếng việt
             *    - en: tiếng anh
             */
            it.putExtra(KeyIntentConstantsNFC.LANGUAGE_SDK, SDKEnumNFC.LanguageEnum.VIETNAMESE.value)
            /**
             * hiển thị màn hình hướng dẫn + hiển thị nút bỏ qua hướng dẫn
             * - mặc định luôn luôn hiển thị màn hình hướng dẫn
             *    - true: hiển thị nút bỏ qua
             *    - false: ko hiển thị nút bỏ qua
             */
            it.putExtra(KeyIntentConstantsNFC.IS_ENABLE_GOT_IT, true)
            /**
             * bật tính năng upload ảnh
             *    - true: bật tính năng
             *    - false: tắt tính năng
             */
            it.putExtra(KeyIntentConstantsNFC.IS_ENABLE_UPLOAD_IMAGE, true)
            /**
             * bật tính năng get Postcode
             *    - true: bật tính năng
             *    - false: tắt tính năng
             */
            it.putExtra(KeyIntentConstantsNFC.IS_ENABLE_POSTCODE_MATCHING, true)
            /**
             * bật tính năng xác thực chip
             *    - true: bật tính năng
             *    - false: tắt tính năng
             */
//            it.putExtra(KeyIntentConstantsNFC.IS_ENABLE_CHECK_CHIP_CLONE, true)
            /**
             * truyền các giá trị đọc thẻ
             *    - nếu không truyền gì mặc định sẽ đọc tất cả (MRZ,Verify Document,Image Avatar)
             *    - giá trị truyền vào là 1 mảng int: nếu muốn đọc giá trị nào sẽ truyền
             *      giá trị đó vào mảng
             * eg: chỉ đọc thông tin MRZ
             *    intArrayOf(SDKEnumNFC.ReadingNFCTags.MRZInfo.value)
             */
//            it.putExtra(
//                KeyIntentConstantsNFC.READING_TAGS_NFC,
//                intArrayOf(
//                    SDKEnumNFC.ReadingNFCTags.MRZInfo.value,
//                    SDKEnumNFC.ReadingNFCTags.VerifyDocumentInfo.value,
//                    SDKEnumNFC.ReadingNFCTags.ImageAvatarInfo.value,
//                    SDKEnumNFC.ReadingNFCTags.AuthenticationInfo.value,
//                )
//            )
            /**
             * truyền giá trị bật quét QRCode
             *    - true: tắt quét QRCode
             *    - false: bật quét QRCode
             */
//         it.putExtra(KeyIntentConstantsNFC.IS_TURN_OFF_QR_CODE, true)

            /**
             * truyền giá trị bật quét read card mode
             * - [SDKEnumNFC.ReaderCardMode.QRCODE.getValue]: quét QR code -> scan NFC
             * - [SDKEnumNFC.ReaderCardMode.MRZ_CODE.getValue]: quét MRZ -> scan NFC
             * - [SDKEnumNFC.ReaderCardMode.NONE.getValue]: scan NFC
             */
            it.putExtra(KeyIntentConstantsNFC.READER_CARD_MODE, SDKEnumNFC.ReaderCardMode.NONE.value)
            // set baseDomain="" => sử dụng mặc định là Product
            it.putExtra(KeyIntentConstantsNFC.BASE_URL, domain)
            // truyền id định danh căn cước công dân
            it.putExtra(KeyIntentConstantsNFC.ID_NUMBER_CARD, id)
            // truyền ngày sinh ghi trên căn cước công dân
            it.putExtra(KeyIntentConstantsNFC.BIRTHDAY_CARD, dob)
            // truyền ngày hết hạn căn cước công dân
            it.putExtra(KeyIntentConstantsNFC.EXPIRED_DATE_CARD, expiredDate)
//
//
//            // truyền giá trị challange code
//            intent.putExtra(
//                KeyIntentConstantsNFC.CHALLENGE_CODE, "INNOVATIONCENTER"
//            )
//            /**
//             * Base url để upload thông tin
//             */
//            it.putExtra(KeyIntentConstantsNFC.BASE_URL, "https://api.idg.vnpt.vn")
//
//            /**
//             * Upload thông tin NFC
//             */
//            intent.putExtra(KeyIntentConstantsNFC.TRANSACTION_PARTNER_ID_UPLOAD_NFC, "UPLOAD_NFC")
//
//            /**
//             * Truyền giá trị địa chỉ thường trú
//             */
//            intent.putExtra(KeyIntentConstantsNFC.TRANSACTION_PARTNER_ID_ORIGINAL_LOCATION, "ORIGINAL_LOCATION")
//
//            /**
//             * Truyền giá trị địa chỉ tạm trú
//             */
//            intent.putExtra(KeyIntentConstantsNFC.TRANSACTION_PARTNER_ID_RECENT_LOCATION, "RECENT_LOCATION")

            intent.putExtra(KeyIntentConstantsNFC.IS_SHOW_LOGO, true)


            resultLauncher.launch(it)
        }
    }
}