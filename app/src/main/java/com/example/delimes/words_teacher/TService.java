package com.example.delimes.words_teacher;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.example.delimes.words_teacher.MainActivity.frag1;
import static com.example.delimes.words_teacher.MainActivity.item;
import static com.example.delimes.words_teacher.MainActivity.voiceModeOn;


public class TService extends Service {

    public MainActivity mainActivity;

    public TService(Context context) {
        this.mainActivity = (MainActivity)context;
    }

    public TService() {
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
            Collocation collocation = ((PageFragment) frag1).listDictionaryCopy.get(((PageFragment) frag1).indexOfThePreviousSelectedRow);
            ((PageFragment) frag1).textToSpeechSystem.setLanguage(Locale.US);
            ((PageFragment) frag1).textToSpeechSystem.speak(collocation.en, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        }

        //stopService(intent);

        //если остановить службу. пропадут выведеные сообщения
        //stopService(new Intent(this, UpdateReminders.class));

        return super.onStartCommand(intent, flags, startId);
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
