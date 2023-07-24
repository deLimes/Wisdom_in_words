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

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    public static ViewPager pager;
    public static boolean voiceModeOn = false;
    public static MenuItem item;
    public static  SpeechRecognizer sr = null;
    ///////////////////////////////////////////
    public static long dateDoomsday = 95617497600000L;//(4999, 11, 31);
    public static int notifyId = 101;
    public static Context mainActivityContext = null;
    public static android.support.v4.app.Fragment frag1;
    public static android.support.v4.app.Fragment frag2;
    public static boolean isNewIntent;
    //////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    Button playBtn, recordBtn, stopBtn;
    //public static MainActivity mainActivity = this;



    public static EditText editText;
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
            ((PageFragment)  frag1).indexOfThePreviousSelectedRow = collocationIndex;
        }



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

        String strCollocationIndex = intent.getStringExtra("id");
        if (strCollocationIndex != null) {
            int collocationIndex = Integer.valueOf(strCollocationIndex);

            ((PageFragment)  frag1).indexOfThePreviousSelectedRow = collocationIndex;
            ((PageFragment)  frag1).adapter.notifyDataSetChanged();
            ((PageFragment)  frag1).recyclerView.scrollToPosition(((PageFragment)  frag1).indexOfThePreviousSelectedRow);

            isNewIntent = true;
        }
        }


    @Override
    protected void onDestroy() {
        super.onDestroy();

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

    public static void sendNotif(String content, Collocation collocation) {

        //%%C - del
        Intent notificationIntent = new Intent(mainActivityContext, NotificationBroadcastReceiver.class);
        notificationIntent.setAction("start_MainActivity");
        notificationIntent.putExtra("id", Integer.toString(collocation.index));
        notificationIntent.putExtra("content", content);
        notificationIntent.putExtra("isNewIntent", "true");

        Uri data = Uri.parse(notificationIntent.toUri(Intent.URI_INTENT_SCHEME));
        notificationIntent.setData(data);
        //
        //notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        Log.d("myLogs", "sendNotif: notificationIntent.extra: " + notificationIntent.getStringExtra("extra"));
        try {
            int i = 1/0;
        }catch (Exception e){
            e.printStackTrace();
        }
        PendingIntent pIntent = PendingIntent.getBroadcast(mainActivityContext,
                Integer.valueOf(notificationIntent.getStringExtra("id")), notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);//PendingIntent.FLAG_UPDATE_CURRENT);//PendingIntent.FLAG_CANCEL_CURRENT);

        //pIntent = PendingIntent.getBroadcast(context, Integer.valueOf(notificationIntent.getStringExtra("id")), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Intent fullScreenIntent = new Intent(mainActivityContext, MainActivity.class);
        //PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(mainActivityContext, 0,
        //        fullScreenIntent, PendingIntent.FLAG_MUTABLE);

        Resources res = mainActivityContext.getResources();
        //Notification.Builder builder = new Notification.Builder(context);

        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mainActivityContext.getPackageName() + "/" + R.raw.next_point);
        String channelId = "1234";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mainActivityContext, channelId);

        //%%C - del builder.setContentIntent(contentIntent)
        builder.setContentIntent(pIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                // большая картинка
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                .setTicker("Пора!")
                .setWhen(System.currentTimeMillis())
                //.setAutoCancel(true)
                .setTimeoutAfter(dateDoomsday)
                .setOngoing(false)
                .setColorized(true)
                //.setDefaults(Notification.DEFAULT_SOUND)
                .setVibrate(new long[] { 10,10 })
                //.setSound(Uri.parse("android.resource://com.example.delimes.flux/" + R.raw.next_point))
                .setSound(soundUri)
                /////////.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                //.setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                .setContentTitle("Напоминание2")
                //.setContentText(res.getString(R.string.notifytext))
                //%%C - del.setContentText(notificationIntent.getStringExtra("content")); // Текст уведомления
                .setContentText(notificationIntent.getStringExtra("content")); // Текст уведомления


        // Notification notification = builder.getNotification(); // до API 16
        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) mainActivityContext
                .getSystemService(Context.NOTIFICATION_SERVICE);

        //////////////////////////////
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //String channelId = "default_channel_id";

            //String channelDescription = "Default Channel";
            String channelDescription = "Channel";
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);
            if (notificationChannel  == null) {
                notificationChannel  = new NotificationChannel(channelId, channelDescription, NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.enableLights(true);//doesn't work
                notificationChannel.setLightColor(Color.BLUE);//doesn't work
                notificationChannel.enableVibration(true);//doesn't work
                notificationChannel.setVibrationPattern(new long[]{50});//doesn't work
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                notificationChannel.setSound(soundUri, audioAttributes);


                if (notificationManager != null) {
                    notificationManager.createNotificationChannel( notificationChannel );
                }
            }
            NotificationCompat.Builder builderCompat = new NotificationCompat.Builder(mainActivityContext, channelId);
            builderCompat.setContentTitle("Напоминание");                            // required
            //builderCompat.setDefaults(Notification.DEFAULT_ALL);
            builderCompat.setSmallIcon(android.R.drawable.ic_popup_reminder);   // required
            builderCompat.setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher));
            builderCompat.setContentText(content); // required
            //builderCompat.setDefaults(Notification.DEFAULT_ALL);
            builderCompat.setAutoCancel(false);
            builderCompat.setTimeoutAfter(dateDoomsday);
            builderCompat.setColorized(true);
            builderCompat.setContentIntent(pIntent);
            builderCompat.setTicker("Пора!");
            builderCompat.setWhen(System.currentTimeMillis());
            builderCompat.setOngoing(false);
            builderCompat.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            //builderCompat.setSound(Uri.parse("android.resource://com.example.delimes.flux/" + R.raw.next_point));//doesn't work
            builderCompat.setPriority(NotificationCompat.PRIORITY_HIGH);
            builderCompat.setLights(0xff0000ff, 300, 1000);// blue color//doesn't work
            builderCompat.setVibrate(new long[]{10,10});//doesn't work
            builderCompat.setSound(soundUri);
            //builderCompat.setDeleteIntent(pIntent);
            builderCompat.setDeleteIntent(getDeleteIntent());
            //builderCompat.setFullScreenIntent(pIntent, true);



            notification = builderCompat.build();
        }
        //////////////////////////////

        //notifyId = Integer.valueOf(notificationIntent.getStringExtra("id"));
        notifyId = 123;
        //notification.defaults |= Notification.DEFAULT_VIBRATE;//doesn't work
        notificationManager.notify(notifyId, notification);


    }

    public static PendingIntent getDeleteIntent()
    {
        Intent intent = new Intent(mainActivityContext, NotificationBroadcastReceiver.class);
        intent.setAction("notification_cancelled");
        return PendingIntent.getBroadcast(mainActivityContext, 0, intent, PendingIntent.FLAG_MUTABLE);
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
