package com.example.delimes.words_teacher;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(new MyFragmentAdapter(getSupportFragmentManager()));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        final android.support.v4.app.Fragment frag1 = fragments.get(0);
        android.support.v4.app.Fragment frag2 = fragments.get(1);

        // Операции для выбранного пункта меню
        switch (id) {


            case R.id.action_sync:
                ((PageFragment)frag1).sync();

                return true;

            case R.id.action_save:
                ((PageFragment)frag1).save();

                return true;

            case R.id.action_reset:

                //Dialog
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Reset progress");  // заголовок
                alertDialog.setMessage("Back to the reference dictionary?"); // сообщение
                alertDialog.setPositiveButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                    }
                });
                alertDialog.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        Toast.makeText(MainActivity.this, "Progress is reset", Toast.LENGTH_LONG)
                                .show();
                        ((PageFragment)frag1).resetListDictionary();
                    }
                });
                alertDialog.setCancelable(true);
                alertDialog.show();
                //****************************((PageFragment)frag1).fillDictionary();
                //*****************************((PageFragment)frag1).updateAdapter();

                return true;


            case R.id.action_help:
                WebView mWebView = frag2.getView().findViewById(R.id.webView);
                mWebView.loadUrl("https://www.youtube.com/watch?v=sFWIiEP4V9w");
                pager.setCurrentItem(1);

                return true;

            default:
                //return true;
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onResume() {
        super.onResume();

    }


    public class MyFragmentAdapter extends FragmentPagerAdapter {

        public MyFragmentAdapter(FragmentManager mgr) {
            super(mgr);
        }

        @Override
        public int getCount() {
            return(2);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {

            if(position == 0) {
                return (PageFragment.newInstance(position));
            }else{
                return (PageFragment2.newInstance(position));

            }
        }
    }


}
