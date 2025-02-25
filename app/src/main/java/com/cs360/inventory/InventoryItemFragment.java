package com.cs360.inventory;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

public class InventoryItemFragment extends Fragment {
    private ListData mItem;
    public static final String ARG_ITEM_ID = "item_id";
    InventoryDatabase inventoryDatabase;
    EditText mItemName;
    EditText mQtyValue;

    Button saveItemButton;
    TextView removeText;

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            saveItemButton.setEnabled(!s.toString().trim().isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    public static InventoryItemFragment newInstance(int itemId) {
        InventoryItemFragment fragment = new InventoryItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ITEM_ID, itemId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        inventoryDatabase = InventoryDatabase.getInstance(getContext());

        // If navigating from item that is currently in database
        if (getArguments() != null) {
            int itemId = getArguments().getInt(ARG_ITEM_ID);
            List<ListData> mItems = inventoryDatabase.getItems();
            for (ListData item : mItems) {
                if (item.getmId() == itemId) {
                    mItem = item;
                }
            }
        }
    }

    // App Bar Navigation
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Enable the back button in the app bar
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Disable the back button when leaving the fragment
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button press
            NavHostFragment.findNavController(this).navigateUp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //
    // Main functionality
    //
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_inventory_item, container, false);

        mItemName = rootView.findViewById(R.id.item_name_input);
        mQtyValue = rootView.findViewById(R.id.item_quantity_input);
        removeText = rootView.findViewById(R.id.item_delete_text);

        // Unable to save or remove item by default
        removeText.setEnabled(false);
        removeText.setTextColor(Color.GRAY);
        saveItemButton = rootView.findViewById(R.id.item_save_button);
        saveItemButton.setEnabled(false);

        // If item is already in database
        if (mItem != null) {
            mItemName.setText(mItem.getName());
            mQtyValue.setText(String.valueOf(mItem.getQty()));

            // Allow saving and removing
            saveItemButton.setEnabled(true);
            removeText.setEnabled(true);
            removeText.setTextColor(Color.RED);
        }

        // Remove Item Button
        removeText.setOnClickListener(v -> {
            // Toast for successful delete
            deleteItem();
        });

        // Save Item Button
        saveItemButton.setOnClickListener(v -> {
            if (saveItem()) {
                // Save to database and return to main inventory page
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
        mItemName.addTextChangedListener(textWatcher);

        // Add Icon
        ImageView addIcon = rootView.findViewById(R.id.item_add_icon);
        addIcon.setOnClickListener(v -> incrementQty());

        // Minus Icon
        ImageView minusIcon = rootView.findViewById(R.id.item_remove_icon);
        minusIcon.setOnClickListener(v -> decrementQty());

        return rootView;
    }

    public void deleteItem() {
        // Prompt user in case of mis-click
        new AlertDialog.Builder(getContext())
                .setTitle("Are you sure you want to delete this item?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    boolean isDeleted = inventoryDatabase.deleteItem(mItem);
                    if (isDeleted) {
                        Toast.makeText(getActivity(), "Deleted item!", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(InventoryItemFragment.this).navigateUp();
                    } else {
                        Toast.makeText(getActivity(), "Error deleting item..", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null).show();
    }

    public void decrementQty() {
        String qtyText = mQtyValue.getText().toString();
        int qty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);
        mQtyValue.setText(String.valueOf(Math.max(0, qty - 1)));
    }

    public void incrementQty() {
        String qtyText = mQtyValue.getText().toString();
        int qty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);
        mQtyValue.setText(String.valueOf(qty + 1));
    }

    public boolean saveItem() {
        boolean itemSaved;
        String name = mItemName.getText().toString();
        int qty;

        // Ensure quantity is valid
        try {
            qty = Integer.parseInt(mQtyValue.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "Invalid quantity entered", Toast.LENGTH_SHORT).show();
            return false;
        }

        // if the item is already in the database update it, else add
        if (mItem != null) {
            mItem.setName(name);
            mItem.updateQty(qty);

            itemSaved = inventoryDatabase.updateItem(mItem);
        } else {
            itemSaved = inventoryDatabase.addItem(name, qty);
        }

        // Send SMS if quantity reaches zero
        if (itemSaved && qty == 0) {
            sendSMSNotification(name);
        }

        return itemSaved;
    }

    private void sendSMSNotification(String itemName) {
        String currentUser = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                .getString("currentUser", null);
        if (currentUser != null) {
            String phoneNumber = inventoryDatabase.getUserPhoneNumber(currentUser);
            if (phoneNumber != null) {
                SmsManager smsManager = SmsManager.getDefault();
                String msg = "ALERT from Inventory+: " + itemName + " is out of stock.";
                smsManager.sendTextMessage(phoneNumber, null, msg, null, null);
                Log.d("sendSMSNotifications", "Sent out of stock SMS for " + itemName );
            } else {
                Log.d("sendSMSNotifications", "Attempted to send out of stock SMS, but user has not enabled SMS notifications");
            }
        }
    }
}