package hcmute.edu.vn.tlcn.attendanceapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class ConfirmOTPActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    PhoneAuthProvider.ForceResendingToken token;
    Button btn_confirmOtp;
    TextView nofi3,btn_resendCode;
    EditText editTextInput1,editTextInput2,editTextInput3,
            editTextInput4,editTextInput5,editTextInput6;

    SharedPreferences sharedPreferences;
    String mPhone, mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_otp);

        sharedPreferences = getSharedPreferences("isVerifyOtp", Context.MODE_MULTI_PROCESS);

        mapping();
        firebaseAuth = FirebaseAuth.getInstance();

        mPhone = getIntent().getStringExtra("phone");
        mVerificationId = getIntent().getStringExtra("verificationId");

        nofi3.setText("We have sent you an SMS with the code to "+mPhone);

        editTextInput1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(editTextInput1.getText().toString().length()==1) {
                    editTextInput2.requestFocus();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editTextInput2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(editTextInput2.getText().toString().length()==1) {
                    editTextInput3.requestFocus();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editTextInput3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(editTextInput3.getText().toString().length()==1) {
                    editTextInput4.requestFocus();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editTextInput4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(editTextInput4.getText().toString().length()==1){
                    editTextInput5.requestFocus();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editTextInput5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(editTextInput5.getText().toString().length()==1){
                    editTextInput6.requestFocus();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_confirmOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextInput1.getText().toString().isEmpty()){
                    editTextInput1.setError("Required");
                    return;
                }
                if(editTextInput2.getText().toString().isEmpty()){
                    editTextInput2.setError("Required");
                    return;
                }
                if(editTextInput3.getText().toString().isEmpty()){
                    editTextInput3.setError("Required");
                    return;
                }
                if(editTextInput4.getText().toString().isEmpty()){
                    editTextInput4.setError("Required");
                    return;
                }
                if(editTextInput5.getText().toString().isEmpty()){
                    editTextInput5.setError("Required");
                    return;
                }
                if(editTextInput6.getText().toString().isEmpty()){
                    editTextInput6.setError("Required");
                    return;
                }

                String code=
                        editTextInput1.getText().toString()+
                                editTextInput2.getText().toString()+
                                editTextInput3.getText().toString()+
                                editTextInput4.getText().toString()+
                                editTextInput5.getText().toString()+
                                editTextInput6.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                signInWithPhoneAuthCredential(credential);
            }
        });
        
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ConfirmOTPActivity.this,
                                    "signInWithCredential:success", Toast.LENGTH_SHORT).show();

                            FirebaseUser user = task.getResult().getUser();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("otp",true);
                            editor.apply();

                            finish();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(ConfirmOTPActivity.this, "failure", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void mapping() {
        btn_confirmOtp = (Button) findViewById(R.id.btn_confirmOtp);
        btn_resendCode = (TextView) findViewById(R.id.btn_resendCode);
        nofi3 = (TextView) findViewById(R.id.nofi3);
        editTextInput1 = (EditText) findViewById(R.id.editTextInput1);
        editTextInput2 = (EditText) findViewById(R.id.editTextInput2);
        editTextInput3 = (EditText) findViewById(R.id.editTextInput3);
        editTextInput4 = (EditText) findViewById(R.id.editTextInput4);
        editTextInput5 = (EditText) findViewById(R.id.editTextInput5);
        editTextInput6 = (EditText) findViewById(R.id.editTextInput6);
    }
}