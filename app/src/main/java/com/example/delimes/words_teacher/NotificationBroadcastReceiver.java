package com.example.delimes.words_teacher;



import static com.example.delimes.words_teacher.MainActivity.frag1;
import static com.example.delimes.words_teacher.MainActivity.item;
import static com.example.delimes.words_teacher.MainActivity.mainActivityContext;
import static com.example.delimes.words_teacher.MainActivity.voiceModeOn;
import static com.example.delimes.words_teacher.TService.indexOfThePreviousSelectedRow;
import static com.example.delimes.words_teacher.TService.listDictionary;
import static com.example.delimes.words_teacher.TService.mConnection;
import static com.example.delimes.words_teacher.TService.mContext;
import static com.example.delimes.words_teacher.TService.mainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
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
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //serviceIntent.putExtra("id", Integer.valueOf(intent.getStringExtra("id")));

            context.startService(serviceIntent);
            //((MainActivity)mainActivity).bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
            //context.bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);

        }

        if (action.equals("notification_opened")) {

            Intent serviceIntent = new Intent(context, TService.class);
            serviceIntent.setAction("action_Speech");
            //serviceIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //serviceIntent.putExtra("id", Integer.valueOf(intent.getStringExtra("id")));


            //context.bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
            // Получаем IBinder из запущенной службы
//            IBinder binder = peekService(context, serviceIntent);
//// Проверяем, не null ли он
//            if (binder != null) {
//                TService myService = ((TService.LocalBinder) binder).getService();
//                myService.startMainActivity(serviceIntent);
//            }
            //

            if (((PageFragment)  frag1)!=null) {
                //if (!((PageFragment) frag1).swap) {
                    ((PageFragment) frag1).save();
                    ((PageFragment) frag1).isResumeAfterStop = true;
                    ((PageFragment) frag1).swap = true;
                //}
            }

            context.startService(serviceIntent);
            //TService.intent = serviceIntent;
            //((MainActivity)mainActivity).bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
            //context.bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);


            /*
            ((PageFragment)  frag1).indexOfThePreviousSelectedRow = Integer.valueOf(intent.getStringExtra("id"));

            Collocation collocation = ((PageFragment) frag1).listDictionaryCopy.get(((PageFragment) frag1).indexOfThePreviousSelectedRow);
            ((PageFragment)  frag1).textToSpeechSystem.setLanguage(Locale.US);
            ((PageFragment)  frag1).textToSpeechSystem.speak(collocation.en , TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
            */


        }
    }
}
