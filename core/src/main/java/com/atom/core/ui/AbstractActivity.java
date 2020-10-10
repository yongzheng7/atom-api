package com.atom.core.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.atom.api.ApiImplContext;
import com.atom.api.ApiImplContextApplication;
import com.atom.api.core.ui.ActivityApi;


/**
 * Created by HYW on 2017/3/14.
 */
@SuppressWarnings("unused")
public abstract class AbstractActivity extends FragmentActivity implements ActivityApi {
    public static final String EXTRA_FRAGMENT_CLASS_NAME = "activity.fragment.classname";
    public static final String EXTRA_REQUEST_PERMISSIONS = "activity.request.permissions";
    public static final int RC_REQUEST_PERMISSIONS = 101;

    private static final int ID_FRAGMENT = android.R.id.widget_frame;
    private static final int ID_LAYOUT = android.R.id.background;
    protected static final String SPKEY_KEEP_SCREEN_ON = "KeepScreenOn";
    protected static final String SPKEY_APP_VERSION_CODE = "AppVersion";

    private String TAG = getClass().getName();

    public AbstractActivity() {
    }

    protected abstract String getAppPackageName();

    /**
     * Get the singleton ApiImplContext object.
     *
     * @return object of ApiImplContext, else null.
     */
    @Override
    public ApiImplContext apiImplContext() {
        Context context = this.getApplicationContext();
        if (context instanceof ApiImplContextApplication) {
            return ((ApiImplContextApplication) context).getApiImplContext();
        }
        throw new RuntimeException("Your Application can't instanceof AapiImplContext!");
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }

    protected void reportException(Exception ex) {
        try {
            apiImplContext().reportException(TAG, ex);
        } catch (Exception e) {
            ex.printStackTrace();
        }
    }


    /**
     * Load a {@link Fragment} to {@link Activity}
     */
    @Override
    public void loadFragment(Fragment fragment, boolean addToBackStack) {
        // Log.e("AbstractActivity.loadFragment", fragment.getClass().getName());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(ID_FRAGMENT, fragment, fragment.getClass().getName());
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.getClass().getName());
        }
        fragmentTransaction.commit();
    }


    /**
     * Initial Setting
     *
     * @param newVersion app version
     * @return true if succeed, else false.
     */
    @SuppressWarnings("unused")
    protected boolean onInitialSetting(long newVersion) {
        return true;
    }

    /**
     * Upgrade Setting
     *
     * @param oldVersion oldVersion
     * @param newVersion newVersion
     * @return true if succeed, else false.
     */
    @SuppressWarnings("unused")
    protected boolean onUpgradeSetting(long oldVersion, long newVersion) {
        return true;
    }

    /**
     * Downgrade Setting
     *
     * @param oldVersion oldVersion
     * @param newVersion newVersion
     * @return true if succeed, else false.
     */
    @SuppressWarnings("unused")
    protected boolean onDowngradeSetting(long oldVersion, long newVersion) {
        return true;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(ID_FRAGMENT);
        if (fragment instanceof OnBackPressedListener) {
            OnBackPressedListener listener = (OnBackPressedListener) fragment;
            if (listener.onBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
        fragment = fragmentManager.findFragmentById(ID_FRAGMENT);
        if (fragment == null) {
            finish();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(ID_FRAGMENT);
        if (fragment instanceof OnKeyPressedListener) {
            OnKeyPressedListener listener = (OnKeyPressedListener) fragment;
            if (listener.onKeyDown(keyCode, event)) {
                return true;
            }
        }
        if (fragment instanceof OnMenuPressedListener) {
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                if (getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.ECLAIR) {
                    event.startTracking();
                } else {
                    ((OnMenuPressedListener) fragment).onMenuPressed();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(ID_FRAGMENT);
        if (fragment instanceof OnKeyPressedListener) {
            OnKeyPressedListener listener = (OnKeyPressedListener) fragment;
            if (listener.onKeyLongPress(keyCode, event)) {
                return true;
            }
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(ID_FRAGMENT);
        if (fragment instanceof OnKeyPressedListener) {
            OnKeyPressedListener listener = (OnKeyPressedListener) fragment;
            if (listener.onKeyMultiple(keyCode, count, event)) {
                return true;
            }
        }
        return super.onKeyMultiple(keyCode, count, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(ID_FRAGMENT);
        if (fragment instanceof OnKeyPressedListener) {
            OnKeyPressedListener listener = (OnKeyPressedListener) fragment;
            if (listener.onKeyUp(keyCode, event)) {
                return true;
            }
        }
        if (fragment instanceof OnMenuPressedListener) {
            if (getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.ECLAIR) {
                if (keyCode == KeyEvent.KEYCODE_MENU && event.isTracking() && !event.isCanceled()) {
                    ((OnMenuPressedListener) fragment).onMenuPressed();
                    return true;
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(ID_FRAGMENT);
        if (fragment instanceof OnNewIntentListener) {
            OnNewIntentListener listener = (OnNewIntentListener) fragment;
            if (listener.onNewIntent(intent)) {
                return;
            }
        }
        String[] extra = intent.getStringArrayExtra(EXTRA_REQUEST_PERMISSIONS);
        String classname = intent.getStringExtra(EXTRA_FRAGMENT_CLASS_NAME);
        if (classname != null) {
            try {
                Class<?> clazz = getClassLoader().loadClass(classname);
                fragment = (Fragment) clazz.newInstance();
                fragment.setArguments(intent.getExtras());
                if (fragment instanceof OnNewIntentListener) {
                    OnNewIntentListener listener = (OnNewIntentListener) fragment;
                    listener.onNewIntent(intent);
                }
                loadFragment(fragment, true);
            } catch (Exception e) {
                reportException(e);
            }
        }
    }

    @Override
    public void finish() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(ID_FRAGMENT);
        if (fragment instanceof OnFinishListener) {
            OnFinishListener listener = (OnFinishListener) fragment;
            if (!listener.onFinish()) {
                return;
            }
        }
        super.finish();
    }

    private void throwRuntimeException() {
        throw new RuntimeException();
    }

    /**
     * exit app, force finish, don't show exit dialog
     */
    public void exit() {
        super.finish();
    }


    private void checkAppPackageName(RuntimeException ex) {
        Context context = this;
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        if (!applicationInfo.packageName.equals(getAppPackageName())) {
            throw ex;
        }
    }
}
