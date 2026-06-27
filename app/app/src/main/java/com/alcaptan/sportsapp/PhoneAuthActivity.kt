package com.alcaptan.sportsapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var phoneNumber: EditText
    private lateinit var otpCode: EditText
    private lateinit var sendCodeBtn: Button
    private lateinit var verifyCodeBtn: Button
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth)

        auth = FirebaseAuth.getInstance()
        phoneNumber = findViewById(R.id.phoneNumber)
        otpCode = findViewById(R.id.otpCode)
        sendCodeBtn = findViewById(R.id.sendCodeBtn)
        verifyCodeBtn = findViewById(R.id.verifyCodeBtn)

        sendCodeBtn.setOnClickListener {
            val phone = phoneNumber.text.toString()
            if (phone.isNotEmpty()) {
                sendVerificationCode(phone)
            } else {
                Toast.makeText(this, "أدخل رقم الجوال", Toast.LENGTH_SHORT).show()
            }
        }

        verifyCodeBtn.setOnClickListener {
            val code = otpCode.text.toString()
            if (code.isNotEmpty() && verificationId != null) {
                verifyCode(code)
            } else {
                Toast.makeText(this, "أدخل رمز التحقق", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendVerificationCode(phone: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Toast.makeText(this@PhoneAuthActivity, "تم التحقق تلقائياً", Toast.LENGTH_SHORT).show()
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: Exception) {
                    Toast.makeText(this@PhoneAuthActivity, "فشل التحقق: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = id
                    Toast.makeText(this@PhoneAuthActivity, "تم إرسال الرمز", Toast.LENGTH_SHORT).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "تم تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "فشل تسجيل الدخول", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
