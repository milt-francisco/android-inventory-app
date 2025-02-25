package com.cs360.inventory;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class InventoryFragment extends Fragment {
    private ItemAdapter mAdapter;

    // App Bar Navigation
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.appbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // Navigate to the SettingsFragment
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_inventoryFragment_to_settings2);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Main functionality
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_inventory, container, false);

        // Add icon
        ImageView addItemButton = rootView.findViewById(R.id.inventory_add_item_button);
        addItemButton.setOnClickListener(v -> Navigation.findNavController(rootView).navigate(R.id.action_inventoryFragment_to_inventoryItemFragment));

        // Recycler View
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<ListData> items = InventoryDatabase.getInstance(requireContext()).getItems();
        mAdapter = new ItemAdapter(items);
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update Recycler to show new items
        List<ListData> items = InventoryDatabase.getInstance(requireContext()).getItems();
        if (mAdapter != null) {
            mAdapter.updateData(items);
        }
    }

    //
    // Adapter Class
    //
    private class ItemAdapter extends RecyclerView.Adapter<ItemHolder> {
        private final List<ListData> mItems;

        public ItemAdapter(List<ListData> items) {
            mItems = items;
        }

        @NonNull
        @Override
        public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ItemHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
            ListData item = mItems.get(position);
            holder.bind(item);
            holder.itemView.setTag(item.getmId());

            // Card onClick arguments/navigation
            holder.itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt(InventoryItemFragment.ARG_ITEM_ID, (int) item.getmId());
                Navigation.findNavController(v).navigate(R.id.action_inventoryFragment_to_inventoryItemFragment, args);
            });
            // Set delete icon's onClick listener inside the card.
            holder.deleteIcon.setOnClickListener(v -> {
                // Prompt user in case of mis-click
                new AlertDialog.Builder(getContext())
                        .setTitle("Are you sure you want to delete this item?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Remove item from database
                            boolean isDeleted = InventoryDatabase.getInstance(holder.itemView.getContext()).deleteItem(item);
                            if (isDeleted) {
                                int pos = holder.getAdapterPosition();
                                mItems.remove(pos);
                                notifyItemRemoved(pos);
                                Toast.makeText(getActivity(), "Item deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Error deleting item..", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", null).show();
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }


        @SuppressLint("NotifyDataSetChanged")
        public void updateData(List<ListData> newItems) {
            mItems.clear();
            mItems.addAll(newItems);
            notifyDataSetChanged();
        }
    }

    //
    // Holder Class
    //
    private static class ItemHolder extends RecyclerView.ViewHolder {
        private final TextView mNameTextView;
        private final TextView mQtyTextView;
        private final ImageView deleteIcon;

        public ItemHolder(LayoutInflater layoutInflater, ViewGroup parent) {
            super(layoutInflater.inflate(R.layout.list_item, parent, false));
            mNameTextView = itemView.findViewById(R.id.list_item_name_label);
            mQtyTextView = itemView.findViewById(R.id.list_item_quantity_value);
            deleteIcon = itemView.findViewById(R.id.list_delete_item);
        }

        public void bind(ListData item) {
            mNameTextView.setText(item.getName());
            mQtyTextView.setText(String.valueOf(item.getQty()));
        }
    }
}