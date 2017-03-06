package com.amiejais.nougatvoipapp.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.amiejais.nougatvoipapp.R;
import com.amiejais.nougatvoipapp.app.Engine;
import com.amiejais.nougatvoipapp.ui.fragment.AboutUsFragment;
import com.amiejais.nougatvoipapp.ui.fragment.ContactsFragment;
import com.amiejais.nougatvoipapp.ui.fragment.DialorFragment;
import com.amiejais.nougatvoipapp.ui.fragment.HistoryFragment;
import com.amiejais.nougatvoipapp.ui.fragment.HomeFragment;

import org.doubango.ngn.services.INgnSipService;

public class DashBoardActivity extends AppCompatActivity {

    private static final String SELECTED_ITEM = "arg_selected_item";

    private BottomNavigationView mBottomNav;
    private int mSelectedItem;
    private INgnSipService mSipService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_dash_board);

        mSipService = Engine.getInstance().getSipService();
        mBottomNav = (BottomNavigationView) findViewById(R.id.navigation);
        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectFragment(item);
                return true;
            }
        });

        MenuItem selectedItem;
        if (savedInstanceState != null) {
            mSelectedItem = savedInstanceState.getInt(SELECTED_ITEM, 0);
            selectedItem = mBottomNav.getMenu().findItem(mSelectedItem);
        } else {
            selectedItem = mBottomNav.getMenu().getItem(0);
        }
        selectFragment(selectedItem);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_ITEM, mSelectedItem);
        super.onSaveInstanceState(outState);
    }

    /*@Override
    public void onBackPressed() {
        MenuItem homeItem = mBottomNav.getMenu().getItem(0);
        if (mSelectedItem != homeItem.getItemId()) {
            selectFragment(homeItem);
        } else {
            super.onBackPressed();
        }
    }*/

    public void selectFragment(MenuItem item) {

        Fragment frag = null;
        // init corresponding fragment
        switch (item.getItemId()) {
            case R.id.menu_home:
                frag = new HomeFragment();
                break;
            case R.id.menu_contact:
                frag = new ContactsFragment();
                break;
            case R.id.menu_dialor:
                frag = new DialorFragment();
                break;
            case R.id.menu_calls:
                frag = new HistoryFragment();
                break;
            case R.id.menu_about_us:
                frag = new AboutUsFragment();
                break;
            default:
                break;
        }

        // update selected item
        mSelectedItem = item.getItemId();

        // uncheck the other items.
        for (int i = 0; i < mBottomNav.getMenu().size(); i++) {
            MenuItem menuItem = mBottomNav.getMenu().getItem(i);
            menuItem.setChecked(menuItem.getItemId() == item.getItemId());
        }

        updateToolbarText(item.getTitle());

        changeFragment(item, frag);
    }

    private void updateToolbarText(CharSequence text) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(text);
        }
    }

    public void getMenuAndUpdate(int pos, Fragment pFragment) {
        MenuItem homeItem = mBottomNav.getMenu().getItem(pos);
        for (int i = 0; i < mBottomNav.getMenu().size(); i++) {
            MenuItem menuItem = mBottomNav.getMenu().getItem(i);
            menuItem.setChecked(menuItem.getItemId() == homeItem.getItemId());
        }
        updateToolbarText(homeItem.getTitle());
        changeFragment(homeItem, pFragment);
    }

    private void changeFragment(MenuItem item, Fragment frag) {
        if (frag != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, frag, frag.getTag());
            if (item.getItemId() != R.id.menu_home) {
                ft.addToBackStack(frag.getTag());
            }
            ft.commit();
        }
    }
}