package com.example.videosamplecompose

import android.util.Base64
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.zip.Deflater
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class TrtcUserSig {
    val SDKAPPID: Int = MY_SDKAPPID
    val SECRETKEY: String = MY_SECRETKEY
    val EXPIRETIME = 10 * 60 // seconds

    fun genTestUserSig(userId: String): String {
        return genTLSSignature(
            SDKAPPID.toLong(),
            userId,
            EXPIRETIME.toLong(),
            null,
            SECRETKEY
        )
    }

    private fun genTLSSignature(
        sdkAppId: Long, userId: String, expire: Long, userBuf: ByteArray?, priKeyContent: String
    ): String {
        val currTime = System.currentTimeMillis() / 1000
        val sigDoc = JSONObject()
        try {
            sigDoc.put("TLS.ver", "2.0")
            sigDoc.put("TLS.identifier", userId)
            sigDoc.put("TLS.sdkappid", sdkAppId)
            sigDoc.put("TLS.expire", expire)
            sigDoc.put("TLS.time", currTime)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        var base64UserBuf: String? = null
        if (null != userBuf) {
            base64UserBuf = Base64.encodeToString(userBuf, Base64.NO_WRAP)
            try {
                sigDoc.put("TLS.userbuf", base64UserBuf)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val sig: String = hmacsha256(
            sdkAppId,
            userId,
            currTime,
            expire,
            priKeyContent,
            base64UserBuf
        )
        if (sig.length == 0) {
            return ""
        }
        try {
            sigDoc.put("TLS.sig", sig)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val compressor = Deflater()
        compressor.setInput(sigDoc.toString().toByteArray(Charset.forName("UTF-8")))
        compressor.finish()
        val compressedBytes = ByteArray(2048)
        val compressedBytesLength = compressor.deflate(compressedBytes)
        compressor.end()
        return String(base64EncodeUrl(
            Arrays.copyOfRange(compressedBytes, 0, compressedBytesLength)
        )
        )
    }

    private fun hmacsha256(
        sdkAppId: Long, userId: String, currTime: Long, expire: Long, priKeyContent: String,
        base64UserBuf: String?
    ): String {
        var contentToBeSigned = """
            TLS.identifier:$userId
            TLS.sdkappid:$sdkAppId
            TLS.time:$currTime
            TLS.expire:$expire
            
            """.trimIndent()
        if (null != base64UserBuf) {
            contentToBeSigned += "TLS.userbuf:$base64UserBuf\n"
        }
        return try {
            val byteKey = priKeyContent.toByteArray(charset("UTF-8"))
            val hmac = Mac.getInstance("HmacSHA256")
            val keySpec = SecretKeySpec(byteKey, "HmacSHA256")
            hmac.init(keySpec)
            val byteSig = hmac.doFinal(contentToBeSigned.toByteArray(charset("UTF-8")))
            String(Base64.encode(byteSig, Base64.NO_WRAP))
        } catch (e: UnsupportedEncodingException) {
            return ""
        } catch (e: NoSuchAlgorithmException) {
            return ""
        } catch (e: InvalidKeyException) {
            return ""
        }
    }

    private fun base64EncodeUrl(input: ByteArray): ByteArray {
        val base64 = String(Base64.encode(input, Base64.NO_WRAP)).toByteArray()
        for (i in base64.indices) {
            when (base64[i].toInt().toChar()) {
                '+' -> base64[i] = '*'.code.toByte()
                '/' -> base64[i] = '-'.code.toByte()
                '=' -> base64[i] = '_'.code.toByte()
                else -> {}
            }
        }
        return base64
    }
}