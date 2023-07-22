package com.example.delimes.words_teacher;



import static com.example.delimes.words_teacher.MainActivity.frag1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.List;
import java.util.Locale;

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

        }

    }
}
