package com.rncrop.common;

public interface Callback<T> {
    void onSuccess(T result);
    void onFailure(Throwable throwable);
}