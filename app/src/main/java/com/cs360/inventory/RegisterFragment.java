package com.cs360.inventory;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

public class RegisterFragment extends Fragment {
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_register, container, false);

        inventoryDatabase = InventoryDatabase.getInstance(getContext());
        EditText usernameField = rootView.findViewById(R.id.register_email_input);
        EditText passwordField = rootView.findViewById(R.id.register_password_input);
        EditText confirmPasswordField = rootView.findViewById(R.id.register_confirm_password_input);

        //Register Button
        Button registerButton = rootView.findViewById(R.id.register_button);
        registerButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString();
            String confirmPassword = confirmPasswordField.getText().toString();

            // Ensure no fields empty
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate passwords match
            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean userAdded = inventoryDatabase.addUser(username, password);
            if (userAdded) {
                Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).navigateUp();
            } else {
                Toast.makeText(getContext(), "Registration failed. Username may already exist.", Toast.LENGTH_LONG).show();
            }
        });

        return rootView;
    }
}