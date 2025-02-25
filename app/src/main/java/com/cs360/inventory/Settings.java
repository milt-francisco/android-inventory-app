package com.cs360.inventory;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Objects;

public class Settings extends Fragment {
    private static final int REQUEST_SEND_SMS = 1;
    SwitchMaterial smsSwitch;
    InventoryDatabase inventoryDatabase;

    // Appbar functions
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Enable the back button in app bar
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Disable the back button when leaving
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
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

    // Main functionality
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        inventoryDatabase = InventoryDatabase.getInstance(getContext());

        //SMS Notifications switch
        smsSwitch = rootView.findViewById(R.id.sms_material_switch);

        if (getSavedPhoneNumber() != null) {
            smsSwitch.setChecked(true);
        }

        smsSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) {
                showPhoneNumberInputDialog();
            } else {
                removePhoneNumber();
            }
        }));

        return rootView;
    }

    // Phone Number Popup Modal
    private void showPhoneNumberInputDialog() {
        Context context = getActivity();

        assert context != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Your Phone Number");

        // Set up input
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

        // Set up buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String phoneNumber = input.getText().toString();
            // Validate and save the phone number
            if (isValidPhoneNumber(phoneNumber)) {
                String currentUser = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getString("currentUser", null);
                setSmsPermission();
                if (currentUser != null) {
                    boolean updated = InventoryDatabase.getInstance(getContext()).updateUserPhoneNumber(currentUser, phoneNumber);
                    if (updated) {
                        Toast.makeText(getActivity(), "Phone number updated.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to update phone number", Toast.LENGTH_SHORT).show();
                    }
                }
                // Proceed with enabling SMS notifications
            } else {
                Toast.makeText(getActivity(), "Phone number must be 10 digits.", Toast.LENGTH_SHORT).show();
                smsSwitch.setChecked(false);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Turn off the switch if the user cancels
            dialog.cancel();
            smsSwitch.setChecked(false);
        });

        builder.show();
    }

    // Input validation
    private boolean isValidPhoneNumber(String phoneNumber) {
        // Ensure 10 digits and not empty
        return phoneNumber != null && phoneNumber.matches("\\d{10}");
    }


    // Retrieve Phone Number for SMS Notifications
    private String getSavedPhoneNumber() {
        String currentUser = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                .getString("currentUser", null);
        if (currentUser != null) {
            String phoneNumber = inventoryDatabase.getUserPhoneNumber(currentUser);
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                return phoneNumber;
            }
        }
        return null;
    }

    // Remove Phone Number
    // (Also helps determine position for switch)
    private void removePhoneNumber() {
        String currentUser = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                .getString("currentUser", null);
        if (currentUser!= null) {
            boolean removedNumber = inventoryDatabase.updateUserPhoneNumber(currentUser, null);
            if (removedNumber) {
                Toast.makeText(getActivity(), "Phone number removed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Request Permission (Android only allows 2 requests before requiring manual enable through Settings)
    private void setSmsPermission() {
        Context context = getActivity();
        if (context != null) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("SMS", "Permissions not yet granted, requesting...");
                // Permission is not granted; request it
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS);
            }
        }
    }

    // Callback for requestPermissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "SMS permission granted.", Toast.LENGTH_SHORT).show();
                // Permission granted, proceed with SMS sending
                smsSwitch.setChecked(true);
            } else {
                AlertDialog.Builder builder = getSMSPermissionsDeniedBuilder();
                builder.show();

                removePhoneNumber();
                smsSwitch.setChecked(false);
            }
        }
    }

    // Alert for if user attempts to enable SMS permissions after denying twice
    // Shows instructions for where to manually enable permissions.
    private AlertDialog.Builder getSMSPermissionsDeniedBuilder() {
        Context context = getActivity();

        // Permission denied, inform the user
        assert context != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("SMS Permissions were Denied");

        final TextView input = new TextView(context);
        input.setPadding(20, 20, 20, 20);
        input.setText("SMS Permissions must be manually enabled from:\n\n Settings > Apps > Inventory+ > Permissions");
        builder.setView(input);
        return builder;
    }
}