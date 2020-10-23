package com.ssoftwares.userapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ssoftwares.userapp.R;
import com.ssoftwares.userapp.api.ApiClient;
import com.ssoftwares.userapp.api.ApiInterface;
import com.ssoftwares.userapp.api.RestResponse;
import com.ssoftwares.userapp.model.RegistrationModel;
import com.ssoftwares.userapp.utils.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpActivity extends AppCompatActivity {

    EditText numOne, numTwo, numThree, numFour, numFive, numSix;
    Button confirmButton;
    AlertDialog dialog;
    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private FirebaseAuth fbAuth;
    String verify;
    String phoneNumber;
    private HashMap<String, String> userdata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_code);

        fbAuth = FirebaseAuth.getInstance();
        numOne = findViewById(R.id.numone);
        numTwo = findViewById(R.id.numtwo);
        numThree = findViewById(R.id.numthree);
        numFour = findViewById(R.id.numfour);
        numFive = findViewById(R.id.numfive);
        numSix = findViewById(R.id.numsix);
        findViewById(R.id.go_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        confirmButton = findViewById(R.id.buttonconfirm);
        confirmButton.setEnabled(false);
        codenumber();

//        String phone = getIntent().getStringExtra("phone");
        userdata = (HashMap<String, String>) getIntent().getSerializableExtra("data");
        if (userdata == null)
            finish();
        String phone = userdata.get("mobile");
        if (phone == null){
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
        phoneNumber = "+91" + phone;
        if (phoneNumber.length() != 13){
            Toast.makeText(this, "Phone number should be of 10 digits", Toast.LENGTH_SHORT).show();
        } else {
            Send_Number_tofirebase(phoneNumber);
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = "" + numOne.getText().toString() + numTwo.getText().toString() + numThree.getText().toString() + numFour.getText().toString() + numFive.getText().toString() + numSix.getText().toString();
                if (!code.equals("")) {
                    Common.INSTANCE.showLoadingProgress(OtpActivity.this);
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneVerificationId, code);
                    signInWithPhoneAuthCredential(credential);
                } else {
                    Toast.makeText(OtpActivity.this, "verification code cant be empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void Send_Number_tofirebase(String phoneNumber) {
        setUpVerificatonCallbacks();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                120,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks);
    }


    public void codenumber() {

        numOne.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (numOne.getText().toString().length() == 0) {
                    numTwo.requestFocus();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        numTwo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (numTwo.getText().toString().length() == 0) {
                    numThree.requestFocus();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        numThree.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (numThree.getText().toString().length() == 0) {
                    numFour.requestFocus();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        numFour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (numFour.getText().toString().length() == 0) {
                    numFive.requestFocus();
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        numFive.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (numFive.getText().toString().length() == 0) {
                    numSix.requestFocus();
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        fbAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            verify = "true";
                            onSignInClick(userdata);
                        } else {
                            Common.INSTANCE.dismissLoadingProgress();

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(OtpActivity.this, "Wrong Code", Toast.LENGTH_SHORT).show();
                            } else if (task.getException() instanceof FirebaseTooManyRequestsException) {
                                Toast.makeText(OtpActivity.this, "Too Many Requests, please try with other number", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(OtpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            finish();
                        }
                    }
                });
    }

    public void resendCode(View view) {

        setUpVerificatonCallbacks();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                120,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks,
                resendToken);
    }

    private void setUpVerificatonCallbacks() {
        verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d("respon", e.toString());
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(OtpActivity.this, "Wrong Code", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(OtpActivity.this, "please try with other phone number!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OtpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                phoneVerificationId = verificationId;
                resendToken = token;
                confirmButton.setEnabled(true);
                Toast.makeText(OtpActivity.this, "Code Sent To " + phoneNumber, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void onSignInClick(HashMap<String , String> map) {

        Call<RestResponse<RegistrationModel>> client = ApiClient.INSTANCE.getGetClient().setRegistration(map);
        client.enqueue(new Callback<RestResponse<RegistrationModel>>() {
            @Override
            public void onResponse(Call<RestResponse<RegistrationModel>> call, Response<RestResponse<RegistrationModel>> response) {
                if (response.code() == 200){
                    Toast.makeText(OtpActivity.this, "Registration Success", Toast.LENGTH_SHORT).show();
                    Common.INSTANCE.dismissLoadingProgress();
                    RestResponse<RegistrationModel> registrationResponse = response.body();
                    if (registrationResponse.getStatus().equals("1")) {
                        successfullDialog(registrationResponse.getMessage());
                    } else if (registrationResponse.getStatus().equals("0")) {
                        Toast.makeText(OtpActivity.this, "Failed: " +  registrationResponse.getMessage(), Toast.LENGTH_LONG).show();
                        Common.INSTANCE.dismissLoadingProgress();

                        finish();
                    }
                } else {
                    try {
                       JSONObject object = new JSONObject(response.errorBody().string()) ;
                       Toast.makeText(OtpActivity.this, "Error: " + object.getString("message") , Toast.LENGTH_LONG).show();
                        Common.INSTANCE.dismissLoadingProgress();

                        finish();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<RestResponse<RegistrationModel>> call, Throwable t) {
                Toast.makeText(OtpActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    Dialog successDialog;
    private void successfullDialog(String msg) {
        successDialog = new Dialog(this , R.style.AppCompatAlertDialogStyleBig);
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successDialog.getWindow().setLayout( WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this ,android.R.color.transparent)));
        successDialog.setCancelable(false);
        View view = LayoutInflater.from(this).inflate(R.layout.dlg_validation , null , false);
        TextView message = view.findViewById(R.id.tvMessage);
        TextView okay = view.findViewById(R.id.tvOk);
        message.setText(msg);
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successDialog.dismiss();
                startActivity(new Intent(OtpActivity.this ,LoginActivity.class));
                finish();
            }
        });
        successDialog.setContentView(view);
        successDialog.show();
    }


}