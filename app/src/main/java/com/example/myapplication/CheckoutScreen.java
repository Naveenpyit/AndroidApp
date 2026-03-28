package com.example.myapplication;

import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.myapplication.utils.CartManager;

public class CheckoutScreen extends AppCompatActivity {

    private TextView tvSubtotal, tvPay20Today, tvPay20Balance;
    private TextView tvFullTotal, tvFullBalance;
    private TextView tvItemsSubtotal, tvShipping, tvTotal;
    private TextView tvPayableNow, tvBalanceAmount, btnPlaceOrder;
    private ImageView btnBack;
    private LinearLayout option20, optionFull;
    private ImageView ivRadio20, ivRadioFull;

    private boolean isPay20Selected = true;
    private double grandTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_screen);

        setupStatusBar();
        initViews();
        calculateAndDisplay();
        setupPaymentOptions();

        btnBack.setOnClickListener(v -> onBackPressed());

        btnPlaceOrder.setOnClickListener(v -> {
            double payNow = isPay20Selected ? grandTotal * 0.20 : grandTotal;
            Toast.makeText(this,
                    "Order placed! Paying ₹" + String.format("%.0f", payNow),
                    Toast.LENGTH_LONG).show();
            CartManager.getInstance().clearCart();
            finish();
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvPay20Today = findViewById(R.id.tv_pay20_today);
        tvPay20Balance = findViewById(R.id.tv_pay20_balance);
        tvFullTotal = findViewById(R.id.tv_full_total);
        tvFullBalance = findViewById(R.id.tv_full_balance);
        tvItemsSubtotal = findViewById(R.id.tv_items_subtotal);
        tvShipping = findViewById(R.id.tv_shipping);
        tvTotal = findViewById(R.id.tv_total);
        tvPayableNow = findViewById(R.id.tv_payable_now);
        tvBalanceAmount = findViewById(R.id.tv_balance_amount);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        option20 = findViewById(R.id.option_20);
        optionFull = findViewById(R.id.option_full);
        ivRadio20 = findViewById(R.id.iv_radio_full);
    }

    private void calculateAndDisplay() {
        CartManager cart = CartManager.getInstance();

        double subtotal = cart.getSubtotal();
        double gst = cart.getGstTotal();
        double total = cart.getGrandTotal();
        double shipping = 0;
        grandTotal = total;

        double pay20Now = grandTotal * 0.20;
        double pay20Balance = grandTotal * 0.80;

        tvSubtotal.setText("₹" + String.format("%.0f", grandTotal));

        tvPay20Today.setText("₹" + String.format("%.0f", pay20Now));
        tvPay20Balance.setText("₹" + String.format("%.0f", pay20Balance));

        tvFullTotal.setText("₹" + String.format("%.2f", grandTotal));
        tvFullBalance.setText("₹0.00");

        tvItemsSubtotal.setText("₹" + String.format("%.2f", subtotal));
        tvShipping.setText("₹" + String.format("%.2f", shipping));
        tvTotal.setText("₹" + String.format("%.2f", grandTotal));

        updatePayableNow(true);
    }

    private void updatePayableNow(boolean pay20) {
        isPay20Selected = pay20;
        double payNow = pay20 ? grandTotal * 0.20 : grandTotal;
        double balance = pay20 ? grandTotal * 0.80 : 0;

        tvPayableNow.setText("₹" + String.format("%.0f", payNow));
        tvBalanceAmount.setText("₹" + String.format("%.0f", balance));
        btnPlaceOrder.setText("Place Order & Pay ₹" + String.format("%.0f", payNow));
    }

    private void setupPaymentOptions() {
        option20.setOnClickListener(v -> {
            isPay20Selected = true;
            option20.setBackgroundResource(R.drawable.payment_option_selected_bg);
            optionFull.setBackgroundResource(R.drawable.payment_option_bg);
            updatePayableNow(true);
        });

        optionFull.setOnClickListener(v -> {
            isPay20Selected = false;
            optionFull.setBackgroundResource(R.drawable.payment_option_selected_bg);
            option20.setBackgroundResource(R.drawable.payment_option_bg);
            updatePayableNow(false);
        });
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary, getTheme()));
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                    .setAppearanceLightStatusBars(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary));
        }
    }
}