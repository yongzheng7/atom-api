package com.atom.api.core.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;

import androidx.fragment.app.Fragment;

import com.atom.api.ApiImplContext;


public interface ActivityApi {

    /**
     * Get api implemnet context
     *
     * @return api implemnet context
     */
    ApiImplContext apiImplContext();

    /**
     * Get Actitity
     *
     * @return activity
     */
    Activity getActivity();


    /**
     * Load a {@link Fragment} to {@link Activity}
     *
     */
    void loadFragment(Fragment fragment, boolean addToBackStack);

    interface OnNewIntentListener {

        /**
         * Called at the . The default implementation check intent extra and load fragment, but you can
         * implement this interface on your fragment to do whatever you want.
         *
         * @param intent The new intent that was started for the activity.
         * @return true if you handled this event, false if you want activity handled this event.
         */
        boolean onNewIntent(Intent intent);
    }

    interface OnBackPressedListener {

        /**
         * Called when the activity has detected the user's press of the back key. The default implementation simply finishes the current activity, but you can
         * implement this interface on your fragment to do whatever you want.
         *
         * @return true if you handled this event, false if you want activity handled this event.
         */
        boolean onBackPressed();
    }

    interface OnFinishListener {

        /**
         * call at activity finish,implement this interface on your fragment to do whatever you want.
         *
         * @return true continue call {@link Activity#finish()}. return false break activity finish
         */
        boolean onFinish();
    }

    interface OnKeyPressedListener extends KeyEvent.Callback {

    }

    interface OnMenuPressedListener {

        /**
         * Called when the activity has detected the user's press of the menu key. You can implement this interface on your fragment to do whatever you want.
         */
        void onMenuPressed();
    }
}
