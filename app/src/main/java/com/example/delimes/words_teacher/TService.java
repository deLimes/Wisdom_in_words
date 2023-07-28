package com.example.delimes.words_teacher;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.delimes.words_teacher.MainActivity.frag1;
import static com.example.delimes.words_teacher.MainActivity.item;
import static com.example.delimes.words_teacher.MainActivity.voiceModeOn;
import static com.example.delimes.words_teacher.PageFragment.adapter;

//JobIntentService
public class TService extends Service {

    volatile public static MainActivity mainActivity;

    volatile public static int indexOfThePreviousSelectedRow = -1;
    volatile public static List<Collocation> listDictionary = new ArrayList<Collocation>();
    volatile public static List<Collocation> listDictionaryCopy = new ArrayList<Collocation>();
    //volatile public static TextToSpeech textToSpeechSystem;
    volatile public static Speecher textToSpeechSystemCls;
    volatile public static String textToSpeak = "";
    volatile public static boolean activityIsStarted = false;
    volatile public static Collocation collocation;
    volatile public static String action = "action_fromMainActivity";
    volatile public static Notification notification;
    //volatile public static boolean ServiceIsStaeted = false;
    volatile public static Context mContext;
    //private final IBinder binder = new LocalBinder();
    volatile public static int count = 0;
    volatile public static int numberOfRepetitions = 5;

    final String LOG_TAG = "myLogs";
    public static ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            Toast.makeText(mContext, "onServiceConnected", Toast.LENGTH_LONG).show();

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            Toast.makeText(mContext, "onServiceDisconnected", Toast.LENGTH_LONG).show();

        }
    };
    public TService(Context context) {
        this.mainActivity = (MainActivity)context;
        //this.binder = new LocalBinder();
    }

    public TService() {
    }

    // Внутренний класс LocalBinder
//    public class LocalBinder extends Binder {
//        // Метод, который возвращает экземпляр службы
//        public TService getService() {
//            return TService.this;
//        }
//    }

    public static void sendNotify(String content, Collocation collocation) {

        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = getNotification(content, collocation, mContext);
        //notifyId = Integer.valueOf(notificationIntent.getStringExtra("id"));
        int notifyId = 123;
        //notification.defaults |= Notification.DEFAULT_VIBRATE;//doesn't work
        notificationManager.notify(notifyId, notification);


    }
    public static Notification getNotification(String content, Collocation collocation, Context context) {

        //%%C - del
        Intent notificationIntent = new Intent(context, NotificationBroadcastReceiver.class);
        notificationIntent.setAction("notification_opened");
        notificationIntent.putExtra("id", Integer.toString(collocation.index));
        notificationIntent.putExtra("content", content);
        notificationIntent.putExtra("isNewIntent", "true");

        Uri data = Uri.parse(notificationIntent.toUri(Intent.URI_INTENT_SCHEME));
        notificationIntent.setData(data);
        //
        //notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        Log.d("myLogs", "sendNotif: notificationIntent.extra: " + notificationIntent.getStringExtra("extra"));
//        try {
//            int i = 1/0;
//        }catch (Exception e){
//            e.printStackTrace();
//        }
        PendingIntent pIntent = PendingIntent.getBroadcast(mContext,
                Integer.valueOf(notificationIntent.getStringExtra("id")), notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);//PendingIntent.FLAG_UPDATE_CURRENT);//PendingIntent.FLAG_CANCEL_CURRENT);

        //pIntent = PendingIntent.getBroadcast(context, Integer.valueOf(notificationIntent.getStringExtra("id")), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Intent fullScreenIntent = new Intent(mainActivityContext, MainActivity.class);
        //PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(mainActivityContext, 0,
        //        fullScreenIntent, PendingIntent.FLAG_MUTABLE);

        Resources res = mContext.getResources();
        //Notification.Builder builder = new Notification.Builder(context);

        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getPackageName() + "/" + R.raw.next_point);
        String channelId = "1234";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, channelId);

        //%%C - del builder.setContentIntent(contentIntent)
        builder.setContentIntent(pIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                // большая картинка
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                .setTicker("Пора!")
                .setWhen(System.currentTimeMillis())
                //.setAutoCancel(true)
                //////////////.setTimeoutAfter(dateDoomsday)
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

        NotificationManager notificationManager = (NotificationManager) mContext
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
            NotificationCompat.Builder builderCompat = new NotificationCompat.Builder(mContext, channelId);
            builderCompat.setContentTitle("Напоминание");                            // required
            //builderCompat.setDefaults(Notification.DEFAULT_ALL);
            builderCompat.setSmallIcon(android.R.drawable.ic_popup_reminder);   // required
            builderCompat.setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher));
            builderCompat.setContentText(content); // required
            //builderCompat.setDefaults(Notification.DEFAULT_ALL);
            builderCompat.setAutoCancel(false);
            /////////builderCompat.setTimeoutAfter(dateDoomsday);
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
            ////////TService.notification = notification;
            TService.collocation = collocation;
            SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
            editPrefs.putString("content", content);
            editPrefs.commit();

        }
        //////////////////////////////

        //notifyId = Integer.valueOf(notificationIntent.getStringExtra("id"));
        //notifyId = 123;
        //notification.defaults |= Notification.DEFAULT_VIBRATE;//doesn't work
        //////////notificationManager.notify(notifyId, notification);
        return notification;


    }

    public static PendingIntent getDeleteIntent()
    {
        Intent intent = new Intent(mContext, NotificationBroadcastReceiver.class);
        intent.setAction("notification_cancelled");
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_MUTABLE);
    }

    @Override
    public void onCreate() {
        super.onCreate();

       // if (((PageFragment)  frag1).swap != null) {

        //}


        mContext = getApplicationContext();
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
//        String content = prefs.getString("content", "Background process");

        Collocation collocation2 = new Collocation(false,"",false,"",false,0);
        Notification notification = getNotification("Background process", collocation2, getApplicationContext());

        startForeground(123, notification);

        //mContext.getIntent



    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //ServiceIsStaeted = true;
        startMainActivity(intent);

        //this.intent = intent;
        /*try {
            getApplicationContext().unbindService(TService.mConnection);
        } catch (IllegalStateException e) {
            Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }*/

        //stopService(intent);

        //если остановить службу. пропадут выведеные сообщения
        //stopService(new Intent(this, UpdateReminders.class));

        //stopSelf();
        //return super.onStartCommand(intent, flags, startId);
        //return START_NOT_STICKY;
        //return START_REDELIVER_INTENT;
        return START_STICKY;
    }

    public void startMainActivity(Intent intent){

        if (intent == null) return;


        String action = intent.getAction();

        if (action.equals("action_fromMainActivity")) {

            /*Toast toast = Toast.makeText(getApplicationContext(),
                    "action_fromMainActivity",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();*/
        }
        if (action.equals("action_NextCollocation")) {


            TService.action = action;

            count = 0;
            Intent intentRun = new Intent(getApplicationContext(), MainActivity.class);
            //intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //intentRun.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentRun.putExtra("id", intent.getStringExtra("id"));
            intentRun.putExtra("isNewIntent", "true");
            startActivity(intentRun);
            //int REQUEST_CODE = 1;
            //startActivityForResult(mainActivity, intentRun, REQUEST_CODE,null);

            ////////////////////////// mainActivity.unbindService(mConnection);


            //((PageFragment)  frag1).indexOfThePreviousSelectedRow++;
            //indexOfThePreviousSelectedRow++;
            if (indexOfThePreviousSelectedRow == listDictionary.size()) {
                int j = 0;
                for (Collocation i : listDictionary) {
                    if (i.learnedEn && i.learnedRu) {
                        break;
                    }
                    j++;
                }
                if (j == listDictionary.size()) j = 0;

                indexOfThePreviousSelectedRow = j;
            }

            if (adapter != null) {
                adapter.notifyDataSetChanged();
                ((PageFragment) frag1).recyclerView.scrollToPosition(indexOfThePreviousSelectedRow);
            }


            if (activityIsStarted) {
                activityIsStarted = false;
                collocation = listDictionaryCopy.get(indexOfThePreviousSelectedRow);
                if (((PageFragment) frag1).englishLeft) {
                    TService.sendNotify(collocation.en + "~" + collocation.ru, collocation);
                    //Notification notification = getNotification(collocation.en + "~" + collocation.ru, collocation, getApplicationContext());
                    //startForeground(123, notification);
                } else {
                    TService.sendNotify(collocation.ru + "~" + collocation.en, collocation);
                    //Notification notification = getNotification(collocation.ru + "~" + collocation.en, collocation, getApplicationContext());
                    //startForeground(123, notification);
                }
            }else {
                TService.sendNotify(collocation.ru + "~" + collocation.en, collocation);
                //Notification notification = getNotification("Background process", collocation, getApplicationContext());
                //startForeground(123, notification);
            }

            //выключить режим automatically
            if (item != null) {
                item.setTitle(getApplicationContext().getResources().getString(R.string.action_voiceMode));
                //((PageFragment) frag1).stopListening();
            }
            ((PageFragment) frag1).automatically = true;
            voiceModeOn = false;
            try {
                ((PageFragment)frag1).voiceMode();
            }catch(Exception e){
                Log.d("onOptionsItemSelected: ", e.getMessage());
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            //((MainActivity)mainActivity).unbindService(mConnection);
        }
        //((PageFragment)  frag1).indexOfThePreviousSelectedRow = Integer.parseInt(intent.getStringExtra("id"));

        if (action.equals("action_Speech")) {

            Log.d(LOG_TAG, "action_Speech: TService");
            TService.action = action;

            Intent intentRun = new Intent(getApplicationContext(), MainActivity.class);
            //intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //intentRun.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentRun.putExtra("id", intent.getStringExtra("id"));
            intentRun.putExtra("isNewIntent", "true");
            ///////mContext.startActivity(intentRun);

            //startActivity(intentRun);
            startActivityForResult(mainActivity, intentRun, 21, null);
            //stopSelf();
            //return START_NOT_STICKY;


            count++;


//            /*if (TService.count != TService.numberOfRepetitions) {
//                Collocation collocation = listDictionaryCopy.get(indexOfThePreviousSelectedRow);
//                //textToSpeechSystem.setLanguage(Locale.US);
//                //textToSpeechSystem.speak(collocation.en, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
//
//                textToSpeechSystem2.setLanguage(Locale.US);
//                textToSpeechSystem2.speak(collocation.en);
////            if (((PageFragment) frag1).englishLeft) {
////                    textToSpeechSystem.setLanguage(Locale.US);
////                    textToSpeechSystem.speak(collocation.en , TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
////            }else {
////                    textToSpeechSystem.setLanguage(new Locale("ru"));
////                    textToSpeechSystem.speak(collocation.ru , TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
////            }
//
//            }*/

                //((MainActivity)mainActivity).unbindService(mConnection);



        }



    }
    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
    }


    // Метод, который вызывается при привязке к службе
    @Override
    public IBinder onBind(Intent intent) {
        //return binder;
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        int i = 1;

        //return super.onUnbind(intent);
        return true;
    }


    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        //startMainActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //ServiceIsStaeted = false;
    }




}
