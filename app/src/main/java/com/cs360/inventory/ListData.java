package com.cs360.inventory;

public class ListData {
    private String mName;
    private int mQty;
    private long mId;

    public ListData(long id, String name, int qty) {
        this.mId = id;
        this.mName = name;
        this.mQty = qty;
    }

    public long getmId() {
        return this.mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) { this.mName = mName; }

    public int getQty() {
        return mQty;
    }

    public void updateQty(int qty) {
        this.mQty = qty;
    }
    public void incrementQty() {
        this.mQty++;
    }

    // Ensure quantity cannot be negative
    public void decrementQty() {
        this.mQty = Math.max(0, this.mQty--);
    }
}
