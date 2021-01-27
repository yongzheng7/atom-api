package com.atom.app.base;

import android.app.Activity;

import androidx.fragment.app.Fragment;

import com.atom.api.core.ui.ActivityApi;
import com.atom.core.AtomApi;

public abstract class AbstractFragment extends Fragment implements ActivityApi.OnBackPressedListener {
    private String TAG = getClass().getName();
    protected boolean mHasModify = false;
    private AtomApi atomApi;
    /**
     * Get the singleton ApiImplContext object.
     */
    public AtomApi apiImplContext() {
        if (atomApi == null) {
            Activity activity = this.getActivity();
            if (activity instanceof ActivityApi) {
                atomApi = ((ActivityApi)activity).apiImplContext();
            } else {
                throw new RuntimeException("Your Activity can't instanceof ActivityApi!");
            }
        }
        return atomApi;
    }

    protected void reportException(Exception ex) {

    }

    @Override
    public boolean onBackPressed() {
        if (mHasModify) {
            return true;
        }
        return false;
    }

    protected void loadFragment(Fragment fragment, boolean addToBackStack) {
        Activity activity = getActivity();
        if (activity instanceof ActivityApi) {
            ((ActivityApi) activity).loadFragment(fragment, addToBackStack);
        }
    }

}
