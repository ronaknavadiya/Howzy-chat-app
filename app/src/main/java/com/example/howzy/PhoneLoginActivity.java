package com.example.howzy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationCodeBtn, verifyBtn;
    private EditText inputVerificationCode, inputPhoneNumber;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();
        initializeFields();
        loadingBar = new ProgressDialog(this);

        sendVerificationCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String phoneNumber = inputPhoneNumber.getText().toString();

                if(TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this, "please enter the Phone number first..", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("please wait...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,60, TimeUnit.SECONDS,PhoneLoginActivity.this, callbacks);

                }
            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                inputPhoneNumber.setVisibility(View.INVISIBLE);
                sendVerificationCodeBtn.setVisibility(View.INVISIBLE);

                String verificationCode = inputVerificationCode.getText().toString();
                if(TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this, "please write verification code first..", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("please wait, while we verify code..");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e)
            {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid, please enter the correct phone number or verification code", Toast.LENGTH_SHORT).show();

                inputPhoneNumber.setVisibility(View.VISIBLE);
                sendVerificationCodeBtn.setVisibility(View.VISIBLE);

                inputVerificationCode.setVisibility(View.INVISIBLE);
                verifyBtn.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken)
            {
               mVerificationId = verificationId;
               mResendToken = forceResendingToken;

                Toast.makeText(PhoneLoginActivity.this, "Verification code has been Sent..", Toast.LENGTH_SHORT).show();

                loadingBar.dismiss();
                inputPhoneNumber.setVisibility(View.INVISIBLE);
                sendVerificationCodeBtn.setVisibility(View.INVISIBLE);

                inputVerificationCode.setVisibility(View.VISIBLE);
                verifyBtn.setVisibility(View.VISIBLE);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if(task.isSuccessful())
                {
                    loadingBar.dismiss();
                    Toast.makeText(PhoneLoginActivity.this, "Congratulations, You are login successfully", Toast.LENGTH_SHORT).show();
                    SendUserToMainActivity();
                }
                else
                {
                    Toast.makeText(PhoneLoginActivity.this, "Error:"+ task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void initializeFields()
    {
        sendVerificationCodeBtn = findViewById(R.id.send_verification_code_btn);
        verifyBtn = findViewById(R.id.verify_btn);
        inputVerificationCode = findViewById(R.id.verification_code_input);
        inputPhoneNumber = findViewById(R.id.phone_number_login_input);
    }

    private void SendUserToMainActivity()
    {
        Intent intent = new Intent(PhoneLoginActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
