package com.example.androdfood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androdfood.adapter.PlaceYourOrderAdapter;
import com.example.androdfood.model.Menu;
import com.example.androdfood.model.OrderSucceessActivity;
import com.example.androdfood.model.RestaurantModel;

public class PlaceYourOrderActivity extends AppCompatActivity {
    private EditText inputName, inputAddress, inputCity, inputState, inputZip, inputCardNumber, inputCardExpiry, inputCardPin;
    private RecyclerView cartItemsRecyclerView;
    private TextView tvSubtotalAmount, tvDeliveryChargeAmount, tvDeliveryCharge, tvTotalAmount, buttonPlaceYourOrder;
    private SwitchCompat switchDelivery;
    private boolean isDeliveryOn;
    private PlaceYourOrderAdapter placeYourOrderAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_your_order);
        RestaurantModel restaurantModel = getIntent().getParcelableExtra("RestaurantModel");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(restaurantModel.getName());
        actionBar.setSubtitle(restaurantModel.getAddress());
        actionBar.setDisplayHomeAsUpEnabled(true);
        inputName = findViewById(R.id.inputName);
        inputAddress = findViewById(R.id.inputAddress);
        inputCity = findViewById(R.id.inputCity);
        inputState = findViewById(R.id.inputState);
        inputZip = findViewById(R.id.inputZip);
        inputCardNumber = findViewById(R.id.inputCardNumber);
        inputCardExpiry = findViewById(R.id.inputCardExpiry);
        inputCardPin = findViewById(R.id.inputCardPin);
        tvSubtotalAmount = findViewById(R.id.tvSubtotalAmount);
        tvDeliveryChargeAmount = findViewById(R.id.tvDeliveryChargeAmount);
        tvDeliveryCharge = findViewById(R.id.tvDeliveryCharge);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        buttonPlaceYourOrder = findViewById(R.id.buttonPlaceYourOrder);
        switchDelivery = findViewById(R.id.switchDelivery);
        cartItemsRecyclerView = findViewById(R.id.cartItemsRecyclerView);


        buttonPlaceYourOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlaceOrderButtonClick(restaurantModel);


            }


        });


        switchDelivery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    inputAddress.setVisibility(View.VISIBLE);
                    inputCity.setVisibility(View.VISIBLE);
                    inputState.setVisibility(View.VISIBLE);
                    inputZip.setVisibility(View.VISIBLE);
                    tvDeliveryChargeAmount.setVisibility(View.VISIBLE);
                    tvDeliveryCharge.setVisibility(View.VISIBLE);
                    isDeliveryOn = true;
                    calculateTotalAmount(restaurantModel);
                } else {
                    inputAddress.setVisibility(View.GONE);
                    inputCity.setVisibility(View.GONE);
                    inputState.setVisibility(View.GONE);
                    inputZip.setVisibility(View.GONE);
                    tvDeliveryChargeAmount.setVisibility(View.GONE);
                    tvDeliveryCharge.setVisibility(View.GONE);
                    isDeliveryOn = false;
                    calculateTotalAmount(restaurantModel);
                }
            }
        });
        maskCard();
        maskValidity();
        maskCvv();
        maskZip();
        initRecyclerView(restaurantModel);
        calculateTotalAmount(restaurantModel);
    }

    private void maskCard() {
        inputCardNumber.addTextChangedListener(
                Mask.insert(Mask.CARD_MASK, inputCardNumber));
    }

    private void maskValidity() {
        inputCardExpiry.addTextChangedListener(
                Mask.insert(Mask.VALIDITY_MASK, inputCardExpiry));
    }

    private void maskCvv() {
        inputCardPin.addTextChangedListener(
                Mask.insert(Mask.CVV_MASK, inputCardPin));
    }

    private void maskZip() {
        inputZip.addTextChangedListener(
                Mask.insert(Mask.ZIP_MASK, inputZip));
    }


    private void calculateTotalAmount(RestaurantModel restaurantModel) {
        float subTotalAmount = 0f;

        for (Menu m : restaurantModel.getMenus()) {
            subTotalAmount += m.getPrice() * m.getTotalInCart();
        }

        tvSubtotalAmount.setText("R$" + String.format("%.2f", subTotalAmount));
        if (isDeliveryOn) {
            tvDeliveryChargeAmount.setText("R$" + String.format("%.2f", restaurantModel.getDelivery_charge()));
            subTotalAmount += restaurantModel.getDelivery_charge();
        }
        tvTotalAmount.setText("R$" + String.format("%.2f", subTotalAmount));
    }

    private void onPlaceOrderButtonClick(RestaurantModel restaurantModel) {
        if (TextUtils.isEmpty(inputName.getText().toString())) {
            inputName.setError("Por favor, digite o nome ");
            return;
        } else if (isDeliveryOn && TextUtils.isEmpty(inputAddress.getText().toString())) {
            inputAddress.setError("Por favor, digite o endereço");
            return;
        } else if (isDeliveryOn && TextUtils.isEmpty(inputCity.getText().toString())) {
            inputCity.setError("Por favor, insira a cidade ");
            return;
        } else if (isDeliveryOn && TextUtils.isEmpty(inputState.getText().toString())) {
            inputState.setError("Por favor, insira o CEP ");
            return;
        } else if (TextUtils.isEmpty(inputCardNumber.getText().toString())) {
            inputCardNumber.setError("Insira o número do cartão ");
            return;
        } else if (TextUtils.isEmpty(inputCardExpiry.getText().toString())) {
            inputCardExpiry.setError("Insira a validade do cartão ");
            return;
        } else if (TextUtils.isEmpty(inputCardPin.getText().toString())) {
            inputCardPin.setError("Por favor, insira o pin/cvv do cartão ");
            return;
        }
        //start success activity..
        Intent i = new Intent(PlaceYourOrderActivity.this, OrderSucceessActivity.class);
        i.putExtra("RestaurantModel", restaurantModel);
        startActivityForResult(i, 1000);
    }

    private void initRecyclerView(RestaurantModel restaurantModel) {
        cartItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        placeYourOrderAdapter = new PlaceYourOrderAdapter(restaurantModel.getMenus());
        cartItemsRecyclerView.setAdapter(placeYourOrderAdapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 1000) {
            setResult(Activity.RESULT_OK);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                //do nothing
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

}