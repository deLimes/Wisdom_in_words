package com.example.delimes.words_teacher;

import static com.example.delimes.words_teacher.MainActivity.frag1;
import static com.example.delimes.words_teacher.MainActivity.voiceModeOn;
import static com.example.delimes.words_teacher.TService.indexOfThePreviousSelectedRow;
import static com.example.delimes.words_teacher.TService.listDictionary;
import static com.example.delimes.words_teacher.TService.listDictionaryCopy;
//import static com.example.delimes.words_teacher.TService.textToSpeechSystem;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

public class Speecher implements TextToSpeech.OnInitListener {

    private static final String TAG = "TextToSpeechController";
    private TextToSpeech myTTS;
    private String textToSpeak;
    private Locale locale;
    private Context context;

    private static Speecher singleton;

    public static Speecher getInstance(Context ctx) {
        if (singleton == null)
            singleton = new Speecher(ctx);
        return singleton;
    }

    private Speecher(Context ctx) {
        context = ctx;
        if (myTTS == null) {
            myTTS = new TextToSpeech(context, this);
            /*
            // currently can\'t change Locale until speech ends
            try {
                // Initialize text-to-speech. This is an asynchronous operation.
                // The OnInitListener (second argument) is called after
                // initialization completes.
                myTTS = new TextToSpeech(context, this);
                //myTTS.setLanguage(locale);
            } catch (Exception e) {
                e.printStackTrace();
            }
             */
        }
        MainActivity.voiceModeOn = false;
        voiceMode();

    }

    public void setLanguage(Locale locale){
        this.locale = locale;
    }

    public void speak(String text) {
        textToSpeak = text;


        if (myTTS == null) {
            // currently can\'t change Locale until speech ends
            try {
                // Initialize text-to-speech. This is an asynchronous operation.
                // The OnInitListener (second argument) is called after
                // initialization completes.
                myTTS = new TextToSpeech(context, this);
                //myTTS.setLanguage(locale);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        sayText();

    }

    public void speakSpeel(Character symbol) {
        //textToSpeak = text;

        //voiceModeOn = false;//i
        //voiceMode();

        myTTS.speak(symbol.toString(), TextToSpeech.QUEUE_ADD, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);

    }

    public boolean isSpeaking() {
        return myTTS.isSpeaking();
    }

    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            //if (myTTS.isLanguageAvailable(locale) == TextToSpeech.LANG_AVAILABLE) {
                //myTTS.setLanguage(locale);
            //}
            //myTTS.setPitch((float) 0.9);
        }
/*
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (initStatus == TextToSpeech.SUCCESS) {
            int result = myTTS.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "TTS missing or not supported (" + result + ")");
                // Language data is missing or the language is not supported.
                // showError(R.string.tts_lang_not_available);

            } else {
                // Initialization failed.
                Log.e(TAG, "Error occured");
            }

        }*/
    }
    private void sayText() {
        HashMap<String, String> myHash = new HashMap<String, String>();
        myHash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "done");
        String[] splitspeech = this.textToSpeak.split("\\\\");
/*

        for (int i = 0; i < splitspeech.length; i++) {

            if (i == 0) { // Use for the first splited text to flush on audio stream
                myTTS.speak(splitspeech[i].toString().trim(), TextToSpeech.QUEUE_FLUSH,     myHash);

            } else { // add the new test on previous then play the TTS

                myTTS.speak(splitspeech[i].toString().trim(), TextToSpeech.QUEUE_ADD,     myHash);
            }
            myTTS.playSilence(100, TextToSpeech.QUEUE_FLUSH,  null);
        }
*/

        myTTS.setLanguage(locale);
        myTTS.speak(this.textToSpeak, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
    }

    public void voiceMode(){

        //((PageFragment) frag1).automatically = false;

        if (MainActivity.voiceModeOn){
            //startListening();
        }else {
            ((PageFragment) frag1).stopListening();
        }


        if (!MainActivity.voiceModeOn){
            myTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                }

                @Override
                public void onDone(String utteranceId) {
                }

                @Override
                public void onError(String utteranceId) {
                }
            });
            return;
        }
        //textToSpeechSystem.speak("Hello world. Привет мир!", TextToSpeech.QUEUE_FLUSH, null, null);
        myTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                Handler mainHandler = new Handler(context.getMainLooper());
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        ((PageFragment) frag1).audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE,0);

                    }
                };
                mainHandler.post(runnable);

            }

            @Override
            public void onDone(String utteranceId) {

                Handler mainHandler = new Handler(context.getMainLooper());
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        //if (TService.count == TService.numberOfRepetitions){
                        //    automatically = true;
                        //}
                        if (!myTTS.isSpeaking() && !((PageFragment) frag1).automatically) {
                            //stopListening();
                            ((PageFragment) frag1).startListening();
                        }
                        if (((PageFragment) frag1).automatically) {
                            //stopListening();
                            ((PageFragment) frag1).onResultsFromMainActivity(null);
                        }
                    }
                };
                mainHandler.postDelayed(runnable, 1000);



            }

            @Override
            public void onError(String utteranceId) {

            }
        });

        //if (TService.count != TService.numberOfRepetitions) {
        final Collocation collocation = listDictionaryCopy.get(indexOfThePreviousSelectedRow);

        if (((PageFragment) frag1).englishLeft) {
            myTTS.setLanguage(Locale.US);
            myTTS.speak(collocation.en, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        } else {
            myTTS.setLanguage(new Locale("ru"));
            myTTS.speak(collocation.ru, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        }
        ;
        // }


    }

    public void stopTTS() {
        if (myTTS != null) {
            myTTS.shutdown();
            myTTS.stop();
            myTTS = null;
        }
    }

}
