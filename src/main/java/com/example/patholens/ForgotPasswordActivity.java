package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.patholens.api.RetrofitClient;
import com.example.patholens.modules.OtpResponse;
import com.example.patholens.modules.SendOtpRequest;
import com.example.patholens.modules.VerifyOtpRequest;
import com.example.patholens.modules.VerifyOtpResponse;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final int OTP_LENGTH = 6;
    private static final int SUCCESS_MESSAGE_DELAY = 2000;

    private TextInputEditText etEmail;
    private Button btnResetPassword;
    private TextView tvBackToLogin;
    private TextView tvErrorMessage;
    private TextView tvDescription;

    private LinearLayout otpSection;
    private TextInputEditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private Button btnVerifyOtp;
    private TextView tvOtpError;
    private TextView tvResendOtp;

    private String userEmail;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initializeViews();
        setupOtpInputs();
        setupListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvDescription = findViewById(R.id.tvDescription);

        otpSection = findViewById(R.id.otpSection);
        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);
        etOtp5 = findViewById(R.id.etOtp5);
        etOtp6 = findViewById(R.id.etOtp6);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvOtpError = findViewById(R.id.tvOtpError);
        tvResendOtp = findViewById(R.id.tvResendOtp);
    }

    private void setupOtpInputs() {
        etOtp1.addTextChangedListener(new OtpTextWatcher(etOtp1, etOtp2));
        etOtp2.addTextChangedListener(new OtpTextWatcher(etOtp2, etOtp3));
        etOtp3.addTextChangedListener(new OtpTextWatcher(etOtp3, etOtp4));
        etOtp4.addTextChangedListener(new OtpTextWatcher(etOtp4, etOtp5));
        etOtp5.addTextChangedListener(new OtpTextWatcher(etOtp5, etOtp6));
        etOtp6.addTextChangedListener(new OtpTextWatcher(etOtp6, null));
    }

    private void setupListeners() {
        btnResetPassword.setOnClickListener(v -> {
            tvErrorMessage.setVisibility(View.GONE);
            String email = etEmail.getText().toString().trim();
            if (validateEmail(email)) {
                sendOtpRequest(email);
            }
        });

        btnVerifyOtp.setOnClickListener(v -> {
            tvOtpError.setVisibility(View.GONE);
            String otp = getEnteredOtp();
            if (validateOtp(otp)) {
                verifyOtpRequest(otp);
            }
        });

        tvResendOtp.setOnClickListener(v -> {
            tvOtpError.setVisibility(View.GONE);
            resendOtpRequest();
        });

        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            showError("Email is required");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address");
            return false;
        }

        return true;
    }

    private boolean validateOtp(String otp) {
        if (otp.length() != OTP_LENGTH) {
            showOtpError("Please enter complete 6-digit OTP");
            return false;
        }
        return true;
    }

    private void sendOtpRequest(String email) {
        if (isLoading) {
            return;
        }

        setOtpSendingState(true);

        SendOtpRequest request = new SendOtpRequest(email);
        Call<OtpResponse> call = RetrofitClient.getInstance().getApiService().sendOtp(request);

        call.enqueue(new Callback<OtpResponse>() {
            @Override
            public void onResponse(Call<OtpResponse> call, Response<OtpResponse> response) {
                setOtpSendingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleSendOtpResponse(response.body(), email);
                } else {
                    showError("Failed to send OTP. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<OtpResponse> call, Throwable t) {
                setOtpSendingState(false);
                showError("Network error. Please check your connection.");
            }
        });
    }

    private void handleSendOtpResponse(OtpResponse otpResponse, String email) {
        if (otpResponse.isSuccess()) {
            userEmail = email;
            showOtpSection();
            Toast.makeText(this, otpResponse.getMessage(), Toast.LENGTH_SHORT).show();
        } else {
            showError(otpResponse.getMessage());
        }
    }

    private void verifyOtpRequest(String otp) {
        if (isLoading) {
            return;
        }

        setOtpVerifyingState(true);

        VerifyOtpRequest request = new VerifyOtpRequest(userEmail, otp);
        Call<VerifyOtpResponse> call = RetrofitClient.getInstance().getApiService().verifyOtp(request);

        call.enqueue(new Callback<VerifyOtpResponse>() {
            @Override
            public void onResponse(Call<VerifyOtpResponse> call, Response<VerifyOtpResponse> response) {
                setOtpVerifyingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleVerifyOtpResponse(response.body());
                } else {
                    showOtpError("Invalid or expired OTP. Please try again.");
                    clearOtpInputs();
                }
            }

            @Override
            public void onFailure(Call<VerifyOtpResponse> call, Throwable t) {
                setOtpVerifyingState(false);
                showOtpError("Network error. Please check your connection.");
            }
        });
    }

    private void handleVerifyOtpResponse(VerifyOtpResponse verifyResponse) {
        if (verifyResponse.isSuccess()) {
            navigateToResetPassword();
        } else {
            showOtpError(verifyResponse.getMessage());
            clearOtpInputs();
        }
    }

    private void resendOtpRequest() {
        if (isLoading || userEmail == null) {
            return;
        }

        setResendingState(true);

        SendOtpRequest request = new SendOtpRequest(userEmail);
        Call<OtpResponse> call = RetrofitClient.getInstance().getApiService().resendOtp(request);

        call.enqueue(new Callback<OtpResponse>() {
            @Override
            public void onResponse(Call<OtpResponse> call, Response<OtpResponse> response) {
                setResendingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleResendOtpResponse(response.body());
                } else {
                    showOtpError("Failed to resend OTP. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<OtpResponse> call, Throwable t) {
                setResendingState(false);
                showOtpError("Network error. Please check your connection.");
            }
        });
    }

    private void handleResendOtpResponse(OtpResponse otpResponse) {
        if (otpResponse.isSuccess()) {
            clearOtpInputs();
            showSuccessMessage("OTP resent successfully");
        } else {
            showOtpError(otpResponse.getMessage());
        }
    }

    private void showOtpSection() {
        tvDescription.setText("Enter the 6-digit OTP sent to " + userEmail);
        etEmail.setEnabled(false);
        btnResetPassword.setVisibility(View.GONE);
        otpSection.setVisibility(View.VISIBLE);
        etOtp1.requestFocus();
    }

    private String getEnteredOtp() {
        return etOtp1.getText().toString() +
                etOtp2.getText().toString() +
                etOtp3.getText().toString() +
                etOtp4.getText().toString() +
                etOtp5.getText().toString() +
                etOtp6.getText().toString();
    }

    private void clearOtpInputs() {
        etOtp1.setText("");
        etOtp2.setText("");
        etOtp3.setText("");
        etOtp4.setText("");
        etOtp5.setText("");
        etOtp6.setText("");
        etOtp1.requestFocus();
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void showOtpError(String message) {
        tvOtpError.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        tvOtpError.setText(message);
        tvOtpError.setVisibility(View.VISIBLE);
    }

    private void showSuccessMessage(String message) {
        tvOtpError.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        tvOtpError.setText(message);
        tvOtpError.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> tvOtpError.setVisibility(View.GONE), SUCCESS_MESSAGE_DELAY);
    }

    private void setOtpSendingState(boolean sending) {
        isLoading = sending;
        btnResetPassword.setEnabled(!sending);
        btnResetPassword.setText(sending ? "Sending..." : "Send OTP");
    }

    private void setOtpVerifyingState(boolean verifying) {
        isLoading = verifying;
        btnVerifyOtp.setEnabled(!verifying);
        btnVerifyOtp.setText(verifying ? "Verifying..." : "Verify OTP");
    }

    private void setResendingState(boolean resending) {
        isLoading = resending;
        tvResendOtp.setEnabled(!resending);
    }

    private void navigateToResetPassword() {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        intent.putExtra("email", userEmail);
        startActivity(intent);
        finish();
    }

    private class OtpTextWatcher implements TextWatcher {
        private final TextInputEditText currentView;
        private final TextInputEditText nextView;

        OtpTextWatcher(TextInputEditText currentView, TextInputEditText nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }
}