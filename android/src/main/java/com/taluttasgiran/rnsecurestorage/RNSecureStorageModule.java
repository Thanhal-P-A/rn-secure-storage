package com.taluttasgiran.rnsecurestorage;

import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.securepreferences.SecurePreferences;

import java.io.FileNotFoundException;

public class RNSecureStorageModule extends ReactContextBaseJavaModule {
    private SharedPreferences prefs;
    private RNKeyStore rnKeyStore;

    RNSecureStorageModule(ReactApplicationContext reactContext) {
        super(reactContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rnKeyStore = new RNKeyStore();
        } else {
            prefs = new SecurePreferences(getReactApplicationContext(), (String) null, "e4b001df9a082298dd090bb7455c45d92fbd5ddd.xml");
        }
    }

    private boolean useKeystore() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    @ReactMethod
    public void set(String key, String value, Promise promise) {
        if (useKeystore()) {
            try {
                rnKeyStore.setCipherText(getReactApplicationContext(), key, value);
                promise.resolve("{\"status\":" + true + "}")
            } catch (Exception e) {
                e.printStackTrace();
                promise.reject("{\"status\":" + false + "}")
            }
        } else {
            try {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(key, value);
                editor.apply();
                promise.resolve("{\"status\":" + true + "}")
            } catch (Exception e) {
                promise.reject("{\"status\":" + false + "}")
            }
        }
    }

    @ReactMethod
    public void get(String key, Promise promise) {
        if (useKeystore()) {
            try {
                promise.resolve(rnKeyStore.getPlainText(getReactApplicationContext(), key));
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
                promise.reject("{\"status\":" + false + "}")
            } catch (Exception e) {
                e.printStackTrace();
                promise.reject("{\"status\":" + false + "}")
            }
        } else {
            try {
                promise.resolve(prefs.getString(key, null));
            } catch (IllegalViewOperationException e) {
                promise.reject("{\"status\":" + false + "}")
            }
        }
    }


    @ReactMethod
    public void clear(String key, Promise promise) {
        if (useKeystore()) {
            try {
                Storage.resetValues(getReactApplicationContext(), new String[]{
                        Constants.SKS_DATA_FILENAME + key,
                        Constants.SKS_KEY_FILENAME + key,
                });
                promise.resolve("{\"status\":" + true + "}")
            } catch (Exception e) {
                promise.reject("{\"status\":" + false + "}")
            }
        } else {
            try {
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove(key).apply();
                promise.resolve("{\"status\":" + true + "}")
            } catch () {
                promise.reject("{\"status\":" + false + "}")
            }
        }
    }

    @Override
    public String getName() {
        return "RNSecureStorageModule";
    }
}