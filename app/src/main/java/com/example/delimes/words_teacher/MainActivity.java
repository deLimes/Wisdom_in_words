package com.example.delimes.words_teacher;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static com.example.delimes.words_teacher.TService.indexOfThePreviousSelectedRow;
import static com.example.delimes.words_teacher.TService.mConnection;
import static com.example.delimes.words_teacher.TService.mainActivity;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    volatile public static ViewPager pager;
    volatile public static boolean voiceModeOn = false;
    volatile public static MenuItem item;
    volatile public static  SpeechRecognizer sr = null;
    ///////////////////////////////////////////
    volatile public static long dateDoomsday = 95617497600000L;//(4999, 11, 31);
    volatile public static int notifyId = 101;
    volatile public static Context mainActivityContext = null;
    volatile public static android.support.v4.app.Fragment frag1;
    volatile public static android.support.v4.app.Fragment frag2;
    volatile public static boolean isNewIntent;
    //public volatile static Notification notification;
    //////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    Button playBtn, recordBtn, stopBtn;
    //public static MainActivity mainActivity = this;



    volatile public static EditText editText;
    /////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
        //
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //getApplicationContext().bindService(new Intent(getApplicationContext(), TService.class), TService.mConnection, Context.BIND_AUTO_CREATE);

        mainActivityContext = getApplicationContext();

        pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(new MyFragmentAdapter(getSupportFragmentManager()));


        Intent notificationIntent = getIntent();
        isNewIntent = notificationIntent != null
                && notificationIntent.getStringExtra("isNewIntent") != null
                && notificationIntent.getStringExtra("isNewIntent") .equals("true");

        String strCollocationIndex = notificationIntent.getStringExtra("id");
        if (strCollocationIndex != null) {
            int collocationIndex = Integer.valueOf(strCollocationIndex);
            indexOfThePreviousSelectedRow = collocationIndex;
        }

        TService.mainActivity = this;

        Intent serviceIntent = new Intent(getApplicationContext(), TService.class);
        serviceIntent.setAction("action_fromMainActivity");
        //serviceIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //serviceIntent.putExtra("id", Integer.valueOf(intent.getStringExtra("id")));
        /*
        if (!TService.ServiceIsStaeted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }*/
        startService(serviceIntent);
        //((MainActivity)mainActivity).bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        //context.bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);


        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(this);

        editText = findViewById(R.id.editText);

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//                Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);//"ja_JP");
//                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak");
//                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
//                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
//
//                sr.startListening(speechRecognizerIntent);
//            }
//        });

////////////////////////////////////////////////////////////////////////////////////
     /*   recordBtn = findViewById(R.id.record_btn);
        stopBtn = findViewById(R.id.stop_btn);
        editText = findViewById(R.id.editText);

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                try{
//                    mediaRecorder.prepare();
//                    mediaRecorder.start();
//                    Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
//                    System.out.println("IT TRIED RECORDING");
//                } catch (IllegalStateException ise){
//                    System.out.println("FAIL RECORDING 1");
//                } catch (IOException ioe){
//                    System.out.println("FAIL RECORDING 2");
//                }
                getApplicationContext().bindService(new Intent(getApplicationContext(), TService.class), TService.mConnection, Context.BIND_AUTO_CREATE);

                System.out.println("ITS JUST PRESSING RECORD BUT NOTHING");

            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), "Stop Recording", Toast.LENGTH_LONG).show();
                TService.isStopBtn = true;
                try {
                    getApplicationContext().unbindService(TService.mConnection);
                } catch (IllegalStateException e) {
                    Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        editText.setText(TService.text);
                    }
                };
                Handler handler = new Handler();

                handler.postDelayed(r, 5000);
              //  AudioManager audioManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
//                audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
//                audioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
//                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
//                audioManager.setStreamMute(AudioManager.STREAM_RING, false);
                //audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
                //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                //getApplicationContext().sendBroadcast(new Intent("zxc"));//set activity_main invisible

            }
        });
*/
//////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);



        if (item != null) {
            item.setTitle(getResources().getString(R.string.action_voiceMode));
            voiceModeOn = false;
            ((PageFragment) frag1).stopListening();
            ((PageFragment) frag1).automatically = false;
        }

        /*
        String strCollocationIndex = intent.getStringExtra("id");
        if (strCollocationIndex != null) {
            int collocationIndex = Integer.valueOf(strCollocationIndex);

            ((PageFragment)  frag1).indexOfThePreviousSelectedRow = collocationIndex;
            ((PageFragment)  frag1).adapter.notifyDataSetChanged();
            ((PageFragment)  frag1).recyclerView.scrollToPosition(((PageFragment)  frag1).indexOfThePreviousSelectedRow);

            isNewIntent = true;
        }*/
        }

public void finish2(){
        //finish();
}
    @Override
    protected void onDestroy() {
        super.onDestroy();

       // mainActivity.unbindService(mConnection);
        /*
        Intent intentRun = new Intent(mainActivityContext, MainActivity.class);
        //intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //intentRun.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intentRun.putExtra("id",  ((PageFragment)  frag1).indexOfThePreviousSelectedRow);
        intentRun.putExtra("isNewIntent", "true");
        mainActivityContext.startActivity(intentRun);
        */
    }







    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == 1){
            pager.setCurrentItem(0);
        }else {
            super.onBackPressed();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        frag1 = fragments.get(0);
        frag2 = fragments.get(1);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        boolean isIrregularVerbs = prefs.getBoolean("isIrregularVerbs", false);
        // Операции для выбранного пункта меню
        switch (id) {

            case R.id.action_irregular_verbs:
                if (!isIrregularVerbs) {
                    ((PageFragment) frag1).saveListDictionary(false);
                    ((PageFragment)frag1).restoreListDictionary(true);
                }


                return true;

            case R.id.action_main_dictionary:
                if (isIrregularVerbs) {
                    ((PageFragment) frag1).saveListDictionary(true);
                    ((PageFragment)frag1).restoreListDictionary(false);
                }


                return true;

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
            case R.id.action_youglish:

                try {
                    ((PageFragment)frag1).goYouGlish();
                }catch(Exception e){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

                return true;
            case R.id.action_googleSearch:


                try {
                    ((PageFragment)frag1).goGoogle();
                }catch(Exception e){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

                return true;
            case R.id.action_voiceMode:

                MainActivity.item = item;
                if (!voiceModeOn){
                    item.setTitle(getResources().getString(R.string.action_textMode));
                    voiceModeOn = true;
                }else {
                    item.setTitle(getResources().getString(R.string.action_voiceMode));
                    voiceModeOn = false;
                }
                //((PageFragment)frag1).voiceMode();
                try {
                    ((PageFragment)frag1).voiceMode();
                }catch(Exception e){
                    Log.d("onOptionsItemSelected: ", e.getMessage());
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        final android.support.v4.app.Fragment frag1 = fragments.get(0);
        android.support.v4.app.Fragment frag2 = fragments.get(1);

        ((PageFragment)frag1).words=data.getExtras().getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
        ((PageFragment)frag1).onResultsFromMainActivity(data);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {

    }

    @Override
    public void onResults(Bundle results) {

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

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
