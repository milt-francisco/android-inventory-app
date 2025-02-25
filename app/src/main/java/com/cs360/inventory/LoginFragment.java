package com.cs360.inventory;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class LoginFragment extends Fragment {
    InventoryDatabase inventoryDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        EditText username = rootView.findViewById(R.id.username_input);
        EditText password = rootView.findViewById(R.id.password_input);

        inventoryDatabase = InventoryDatabase.getInstance(getContext());

        // Register link
        TextView createAccount = rootView.findViewById(R.id.create_account_link);
        createAccount.setOnClickListener(l -> Navigation.findNavController(rootView).navigate(R.id.action_loginFragment2_to_registerFragment3));

        //Login link - Verify credentials onClick
        Button loginButton = rootView.findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> {
            String usernameInput = username.getText().toString().trim().toLowerCase(Locale.US);
            String passwordInput = password.getText().toString().trim();

            // Ensure fields are populated
            if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean verifiedLogin = inventoryDatabase.verifyLogin(usernameInput, passwordInput);
            if (verifiedLogin) {
                // Save username for later notification use.
                SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                prefs.edit().putString("currentUser", usernameInput).apply();

                Navigation.findNavController(rootView).navigate(R.id.action_loginFragment2_to_inventoryFragment);
            } else {
                Toast.makeText(getContext(), "Invalid login credentials", Toast.LENGTH_SHORT).show();
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

}