package com.example.delimes.words_teacher;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.delimes.words_teacher.MainActivity.frag1;
import static com.example.delimes.words_teacher.MainActivity.item;
import static com.example.delimes.words_teacher.MainActivity.voiceModeOn;
import static com.example.delimes.words_teacher.PageFragment.adapter;


public class TService extends Service {

    public static MainActivity mainActivity;
    public static Notification notification;
    public static int indexOfThePreviousSelectedRow = -1;
    public static List<Collocation> listDictionary = new ArrayList<Collocation>();
    public static List<Collocation> listDictionaryCopy = new ArrayList<Collocation>();
    public static TextToSpeech textToSpeechSystem;
    public static boolean activityIsStarted = false;
    public static Collocation collocation;
    public static Context mContext;
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
            Toast.makeText(mContext, "onServiceConnected", Toast.LENGTH_LONG).show();

        }
    };
    public TService(Context context) {
        this.mainActivity = (MainActivity)context;
    }

    public TService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

       // if (((PageFragment)  frag1).swap != null) {

        //}


        mContext = getApplicationContext();

        //startForeground(123, notification);

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        String action = intent.getAction();
        if (action.equals("action_NextCollocation")) {


            Intent intentRun = new Intent(getApplicationContext(), MainActivity.class);
            ////////////intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //intentRun.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentRun.putExtra("id", intent.getStringExtra("id"));
            intentRun.putExtra("isNewIntent", "true");
            mContext.startActivity(intentRun);
            //int REQUEST_CODE = 1;
            //startActivityForResult(mainActivity, intentRun, REQUEST_CODE,null);




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
                    MainActivity.sendNotif(collocation.en + "~" + collocation.ru, collocation);
                } else {
                    MainActivity.sendNotif(collocation.ru + "~" + collocation.en, collocation);
                }
            }else {
                if (((PageFragment) frag1).englishLeft) {
                    MainActivity.sendNotif(collocation.en + "~" + collocation.ru, collocation);
                } else {
                    MainActivity.sendNotif(collocation.ru + "~" + collocation.en, collocation);
                }
            }

            if (item != null) {
                item.setTitle(getApplicationContext().getResources().getString(R.string.action_voiceMode));
                voiceModeOn = false;
                ((PageFragment) frag1).stopListening();
                ((PageFragment) frag1).automatically = false;
            }

        }
        //((PageFragment)  frag1).indexOfThePreviousSelectedRow = Integer.parseInt(intent.getStringExtra("id"));

        if (action.equals("action_Speech")) {

            Log.d(LOG_TAG, "action_Speech: TService");
            Intent intentRun = new Intent(getApplicationContext(), MainActivity.class);
            ////////////intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //intentRun.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentRun.putExtra("id", intent.getStringExtra("id"));
            intentRun.putExtra("isNewIntent", "true");
            mContext.startActivity(intentRun);


// Устанавливаем флаги для запуска активити в новом задании и очистки стека
            //intentRun.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
// Запускаем активити
            startActivity(intentRun);
// Останавливаем службу
            //stopSelf();
            //return START_NOT_STICKY;

            Collocation collocation = listDictionaryCopy.get(indexOfThePreviousSelectedRow);
            textToSpeechSystem.setLanguage(Locale.US);
            textToSpeechSystem.speak(collocation.en, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        }


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
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {

        // Показываем уведомление в переднем плане


        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {


        return super.onUnbind(intent);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
