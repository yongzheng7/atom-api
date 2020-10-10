package com.atom.core.ui;

import android.app.Activity;

import androidx.fragment.app.Fragment;

import com.atom.api.ApiImplContext;
import com.atom.api.core.ui.ActivityApi;

public abstract class AbstractFragment extends Fragment implements ActivityApi.OnBackPressedListener {
    private String TAG = getClass().getName();
    protected boolean mHasModify = false;
    private ApiImplContext mApiImplContext;

    public AbstractFragment() {
    }

    /**
     * Get the singleton ApiImplContext object.
     */
    public ApiImplContext apiImplContext() {
        if (mApiImplContext == null) {
            Activity activity = this.getActivity();
            if (activity instanceof ActivityApi) {
                mApiImplContext = ((ActivityApi) activity).apiImplContext();
            } else {
                throw new RuntimeException("Your Activity can't instanceof ActivityApi!");
            }
        }
        return mApiImplContext;
    }

    protected void reportException(Exception ex) {
        ApiImplContext apiImplContext = apiImplContext();
        if (apiImplContext != null && apiImplContext.isDebugEnabled()) {
            apiImplContext.reportException(TAG, ex);
        }
    }

    /**
     * Called when the activity has detected the user's press of the back key. The default implementation simply finishes the current activity, but you can
     * implement this interface on your fragment to do whatever you want.
     *
     * @return true if you handled this event, false if you want activity handled this event.
     */
    @Override
    public boolean onBackPressed() {
        if (mHasModify) {
            return true;
        }
        return false;
    }

    /**
     * Load a {@link Fragment} to {@link Activity}
     */
    protected void loadFragment(Fragment fragment, boolean addToBackStack) {
        Activity activity = getActivity();
        if (activity instanceof ActivityApi) {
            ((ActivityApi) activity).loadFragment(fragment, addToBackStack);
        }
    }

}