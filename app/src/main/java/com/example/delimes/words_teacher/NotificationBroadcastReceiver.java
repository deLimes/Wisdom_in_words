package com.example.delimes.words_teacher;



import static com.example.delimes.words_teacher.MainActivity.frag1;
import static com.example.delimes.words_teacher.MainActivity.item;
import static com.example.delimes.words_teacher.MainActivity.voiceModeOn;
import static com.example.delimes.words_teacher.TService.indexOfThePreviousSelectedRow;
import static com.example.delimes.words_teacher.TService.listDictionary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.util.Half;
import android.util.Log;

import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;

/**
 * Created by User on 19.11.2017.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    public NotificationBroadcastReceiver() {
    }
    final String LOG_TAG = "myLogs";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(LOG_TAG, "onReceive: Receiver");
        Log.d(LOG_TAG, "action = " + intent.getAction());
        Log.d(LOG_TAG, "extra = " + intent.getStringExtra("extra"));

        String action = intent.getAction();
        if (action.equals("notification_cancelled")) {

            indexOfThePreviousSelectedRow++;
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
            if (((PageFragment)  frag1)!=null) {
                //if (!((PageFragment) frag1).swap) {
                    ((PageFragment) frag1).save();
                    ((PageFragment) frag1).isResumeAfterStop = true;
                    ((PageFragment) frag1).swap = true;
                //}
            }

            Intent serviceIntent = new Intent(context, TService.class);
            serviceIntent.setAction("action_NextCollocation");
            //serviceIntent.putExtra("id", Integer.valueOf(intent.getStringExtra("id")));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }

        }

        if (action.equals("start_MainActivity")) {

            /*
            Intent intentRun = new Intent(context, MainActivity.class);
            //intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //intentRun.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentRun.putExtra("id", intent.getStringExtra("id"));
            intentRun.putExtra("isNewIntent", intent.getStringExtra("isNewIntent"));
            context.startActivity(intentRun);
            */
            //

            if (((PageFragment)  frag1)!=null) {
                //if (!((PageFragment) frag1).swap) {
                    ((PageFragment) frag1).save();
                    ((PageFragment) frag1).isResumeAfterStop = true;
                    ((PageFragment) frag1).swap = true;
                //}
            }

            // Создаем интент для службы
            Intent serviceIntent = new Intent(context, TService.class);
            serviceIntent.setAction("action_Speech");
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //serviceIntent.putExtra("id", Integer.valueOf(intent.getStringExtra("id")));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }

            /*
            ((PageFragment)  frag1).indexOfThePreviousSelectedRow = Integer.valueOf(intent.getStringExtra("id"));

            Collocation collocation = ((PageFragment) frag1).listDictionaryCopy.get(((PageFragment) frag1).indexOfThePreviousSelectedRow);
            ((PageFragment)  frag1).textToSpeechSystem.setLanguage(Locale.US);
            ((PageFragment)  frag1).textToSpeechSystem.speak(collocation.en , TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
            */


        }
    }
}
