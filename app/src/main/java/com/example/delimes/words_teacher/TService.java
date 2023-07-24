package com.example.delimes.words_teacher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;


import java.util.Locale;

import static com.example.delimes.words_teacher.MainActivity.frag1;
import static com.example.delimes.words_teacher.MainActivity.item;
import static com.example.delimes.words_teacher.MainActivity.mainActivityContext;
import static com.example.delimes.words_teacher.MainActivity.voiceModeOn;


public class TService extends Service {

    public MainActivity mainActivity;
    public static Notification notification;

    public TService(Context context) {
        this.mainActivity = (MainActivity)context;
    }

    public TService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Показываем уведомление в переднем плане
        startForeground(123, notification);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        String action = intent.getAction();
        if (action.equals("action_NextCollocation")) {

            ((PageFragment)  frag1).indexOfThePreviousSelectedRow++;
            if (((PageFragment)  frag1).indexOfThePreviousSelectedRow == ((PageFragment)  frag1).listDictionary.size()) {
                int j = 0;
                for (Collocation i : ((PageFragment)  frag1).listDictionary) {
                    if (i.learnedEn && i.learnedRu) {
                        break;
                    }
                    j++;
                }
                if (j == ((PageFragment)  frag1).listDictionary.size()) j = 0;

                ((PageFragment)  frag1).indexOfThePreviousSelectedRow = j;
            }

            ((PageFragment)  frag1).adapter.notifyDataSetChanged();
            ((PageFragment)  frag1).recyclerView.scrollToPosition(((PageFragment)  frag1).indexOfThePreviousSelectedRow);


            Collocation collocation = ((PageFragment) frag1).listDictionaryCopy.get(((PageFragment) frag1).indexOfThePreviousSelectedRow);
            if (((PageFragment)frag1).englishLeft) {
                MainActivity.sendNotif(collocation.en + "~" + collocation.ru, collocation);
            } else {
                MainActivity.sendNotif(collocation.ru + "~" + collocation.en, collocation);
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

            Intent intentRun = new Intent(getApplicationContext(), MainActivity.class);
            ////////////intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //intentRun.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentRun.putExtra("id", intent.getStringExtra("id"));
            intentRun.putExtra("isNewIntent", "true");
            //mainActivityContext.startActivity(intentRun);


// Устанавливаем флаги для запуска активити в новом задании и очистки стека
            intentRun.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
// Запускаем активити
            startActivity(intentRun);
// Останавливаем службу
            //stopSelf();
            //return START_NOT_STICKY;

            Collocation collocation = ((PageFragment) frag1).listDictionaryCopy.get(((PageFragment) frag1).indexOfThePreviousSelectedRow);
            ((PageFragment) frag1).textToSpeechSystem.setLanguage(Locale.US);
            ((PageFragment) frag1).textToSpeechSystem.speak(collocation.en, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        }

        //stopService(intent);

        //если остановить службу. пропадут выведеные сообщения
        //stopService(new Intent(this, UpdateReminders.class));

        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
