package com.example.musicplayer.activity;

import android.os.Bundle;
import android.view.MotionEvent;
import androidx.fragment.app.FragmentTransaction;
import com.example.musicplayer.R;
import com.example.musicplayer.fragment.PlayBarFragment;

public abstract class PlayBarBaseActivity extends BaseActivity {

    private PlayBarFragment playBarFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        show();
    }

    private void show() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (playBarFragment == null) {
            playBarFragment = PlayBarFragment.newInstance();
            ft.add(R.id.fragment_playbar, playBarFragment).commit();
        } else {
            ft.show(playBarFragment).commit();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }
}
