package com.ansh.wishhub.dto;

import java.util.List;

public class WishListResponse {

    private List<WishResponse> wishes;
    private int count;

    public WishListResponse() {
    }

    public WishListResponse(List<WishResponse> wishes) {
        this.wishes = wishes;
        this.count = wishes != null ? wishes.size() : 0;
    }

    public List<WishResponse> getWishes() {
        return wishes;
    }

    public void setWishes(List<WishResponse> wishes) {
        this.wishes = wishes;
        this.count = wishes != null ? wishes.size() : 0;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
