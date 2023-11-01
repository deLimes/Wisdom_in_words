package com.example.delimes.words_teacher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static android.content.Context.BATTERY_SERVICE;
import static android.os.Environment.MEDIA_MOUNTED;
import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static android.os.Environment.getExternalStorageState;
import static com.example.delimes.words_teacher.MainActivity.frag2;
import static com.example.delimes.words_teacher.MainActivity.frag1;
import static com.example.delimes.words_teacher.MainActivity.isNewIntent;
import static com.example.delimes.words_teacher.MainActivity.item;
import static com.example.delimes.words_teacher.MainActivity.voiceModeOn;
import static com.example.delimes.words_teacher.TService.indexOfThePreviousSelectedRow;
import static com.example.delimes.words_teacher.TService.listDictionary;
import static com.example.delimes.words_teacher.TService.listDictionaryCopy;
import static com.example.delimes.words_teacher.TService.textToSpeechSystemCls;

//import static com.example.delimes.words_teacher.TService.textToSpeechSystem;
public class PageFragment extends Fragment implements RecognitionListener {

    InputMethodManager inputMethodManager;
    ConstraintLayout rootView;
    SearchView searchView;
    ProgressBar tvProgressBar;
    TextView tvTextLearned;
    TextView tvTextOnRepetition;
    TextView tvTextLeft;
    TextView tvTextTotal;

    EditText editTextNumberOfBlocks;
    EditText editTextNumberOfCollocationsInABlock;
    EditText editTexScrollingSpeed;
    EditText editTextHostname, editTextPortname;

    public View buttonSwap;

    private boolean EnglishTextLayout = false;
    char[] ArrayEnglishCharacters = {'h', 'j', 'k', 'l', 'y', 'u', 'i', 'o', 'p', '[', ']', 'n', 'm',
            'g', 'f', 'd', 's', 'a', 'b', 'v', 'c', 'x', 'z', 't', 'r', 'e', 'w', 'q', '`'};
    List engList = Arrays.asList(ArrayEnglishCharacters);

    char[] ArrayRussianCharacters = {'р', 'о', 'л', 'д', 'ж', 'э', 'н', 'г', 'ш', 'щ', 'з', 'х', 'ъ', 'т', 'ь', 'б', 'ю',
            'п', 'а', 'в', 'ы', 'ф', 'и', 'м', 'с', 'ч', 'я', 'е', 'к', 'у', 'ц', 'й', 'ё'};
    List rusList = Arrays.asList(ArrayRussianCharacters);


    //public List <Collocation> listDictionary = new ArrayList<Collocation>();
    //public List <Collocation> listDictionaryCopy = new ArrayList<Collocation>();

    RecyclerView recyclerView;
    MyLinearLayoutManager myLlm;
    int scrollStepY = 0;
    volatile public static RecyclerViewAdapter adapter;
    public Boolean englishLeft = true;
    String TAG = "mylogs";
    boolean answersWereHidden;
    int numberOfBlocks = 1, numberOfCollocationsInABlock = 1;
    float millisecondsPerInch = 1000f;//100f
    int rowBeginIndexOfLearnedWords = 0, rowBeginIndexOfWellLearnedWords = 0, rowBeginIndexOfNativeWords = 0;
    int countOfLearnedWords = 0, countOfDifficultWords = 0;
    boolean textForViewing;
    Receiver rp;
    public List<Receiver> receiverList = new ArrayList<Receiver>();
    public final int COUNT_OF_RECEIVERS = 100;
    int indexOfCurrentReceiver = 0, indexOfPreviousReceiver = 0;
    public boolean swap = false;
    int indexOfTheSelectedRow = 0, indexOfTheFilteredSelectedRow = 0;
    public int indexOfTheTempPreviousSelectedRow = -1;
    volatile public static boolean isStart,  isResumeAfterStop;
    SpannableString text;
    boolean afterPressEnter = false;
    int childPosition;
    boolean collocationRemoved = false;
    boolean playedNextPoint = false;
    //boolean stopListening = false;
    TextView textViewCommands;

    @Override
    public void onReadyForSpeech(Bundle params) {
        textViewCommands.setText("onReadyForSpeech");
        textViewCommands.setBackgroundColor(Color.parseColor("#02FA02"));
    }

    @Override
    public void onBeginningOfSpeech() {
        //textViewCommands.setText("onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        textViewCommands.setText("onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        //searchView.setQuery("onEndOfSpeech", false);

        textViewCommands.setText("onEndOfSpeech");
        textViewCommands.setBackgroundColor(Color.parseColor("#FA1C02"));
        //startListening();

        Handler mainHandler = new Handler(getContext().getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE,0);
            }
        };
        mainHandler.postDelayed(runnable, 2000);

    }

    @Override
    public void onError(int error) {
        String mError = "";
        //mStatus = "Error detected";
        switch (error) {
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                mError = " network timeout";
                startListening();
                Log.e("123", mError);
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                mError = " network" ;
                //toast("Please check data bundle or network settings");
                Log.e("123", mError);
                return;
            case SpeechRecognizer.ERROR_AUDIO:
                mError = " audio";
                Log.e("123", mError);
                break;
            case SpeechRecognizer.ERROR_SERVER:
                mError = " server";
                startListening();
                Log.e("123", mError);
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                mError = " client";
                startListening();
                Log.e("123", mError);
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                mError = " speech time out" ;
                Log.e("123", mError);
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                mError = " no match" ;
                startListening();
                Log.e("123", mError);
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                mError = " recogniser busy" ;
                sr.cancel();
                startListening();
                Log.e("123", mError);
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                mError = " insufficient permissions" ;
                startListening();
                Log.e("123", mError);
                break;

        }
        //Log.i(TAG,  "Error: " +  error + " - " + mError);
       // searchView.setQuery("onError:" + mError, false);
        textViewCommands.setText("onError:" + mError);
//        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, original_volume_level, 0);
//        Toast.makeText(getContext(), "Media volume restored.", Toast.LENGTH_SHORT).show();

    }

    public void onResultsFromMainActivity(Intent data) {
        String word = "";
        if (data == null) {
            if (answerIsSaid){
                word = "AUTOMATICALLY";
            }else {
                word = "ANSWER";
            }
        }else {
            words = data.getExtras().getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
            word = words.get(0);
        }
        //searchView.setQuery(word, false);

        Collocation collocation = listDictionaryCopy.get(indexOfThePreviousSelectedRow);

        if (word.toUpperCase().equals("REPEAT")) {
            if (englishLeft) {
                //textToSpeechSystem.setLanguage(Locale.US);
                TService.sendNotify(collocation.en+"~"+collocation.ru, collocation);
                //textToSpeechSystem.speak(collocation.en
                //        .replace("✓", "")
                //        .replace("⚓", ""), TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                textToSpeechSystemCls.setLanguage(Locale.US);
                textToSpeechSystemCls.speak(collocation.en
                                .replace("✓", "")
                                .replace("⚓", ""));
            } else {
                textToSpeechSystemCls.setLanguage(new Locale("ru"));
                TService.sendNotify(collocation.ru+"~"+collocation.en, collocation);
                textToSpeechSystemCls.speak(collocation.ru);
            }
        } else if (word.toUpperCase().equals("ANSWER")) {
            if (englishLeft) {
                textToSpeechSystemCls.setLanguage(new Locale("ru"));
                //MainActivity.sendNotif(collocation.ru+"~"+collocation.en, collocation);
                textToSpeechSystemCls.speak(collocation.ru);
            } else {
                textToSpeechSystemCls.setLanguage(Locale.US);
                //MainActivity.sendNotif(collocation.en+"~"+collocation.ru, collocation);
                textToSpeechSystemCls.speak(collocation.en
                        .replace("✓", "")
                        .replace("⚓", ""));
            }
            answerIsSaid = true;
        } else if (word.toUpperCase().equals("SPELL")) {
            textToSpeechSystemCls.setLanguage(Locale.US);
            for (int i = 0; i < collocation.en.length(); i++){
                Character symbol = collocation.en
                        .replace("✓", "")
                        .replace("⚓", "").charAt(i);
                textToSpeechSystemCls.speakSpeel(symbol);
            }
        } else if (word.toUpperCase().equals("AUTOMATICALLY")) {
            automatically = true;
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
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(indexOfThePreviousSelectedRow);
            collocation = listDictionaryCopy.get(indexOfThePreviousSelectedRow);
            if (englishLeft) {
                textToSpeechSystemCls.setLanguage(Locale.US);
                TService.sendNotify(collocation.en+"~"+collocation.ru, collocation);
                textToSpeechSystemCls.speak(collocation.en
                        .replace("✓", "")
                        .replace("⚓", ""));
            } else {
                textToSpeechSystemCls.setLanguage(new Locale("ru"));
                 TService.sendNotify(collocation.ru+"~"+collocation.en, collocation);
                textToSpeechSystemCls.speak(collocation.ru);
            }
            answerIsSaid = false;
        } else if (word.toUpperCase().equals("PREVIOUS")) {
            indexOfThePreviousSelectedRow--;
            if (indexOfThePreviousSelectedRow == -1) {
                indexOfThePreviousSelectedRow = listDictionary.size()-1;
            }
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(indexOfThePreviousSelectedRow);
            collocation = listDictionaryCopy.get(indexOfThePreviousSelectedRow);
            if (englishLeft) {
                textToSpeechSystemCls.setLanguage(Locale.US);
                 TService.sendNotify(collocation.en+"~"+collocation.ru, collocation);
                textToSpeechSystemCls.speak(collocation.en
                        .replace("✓", "")
                        .replace("⚓", ""));
            } else {
                textToSpeechSystemCls.setLanguage(new Locale("ru"));
                 TService.sendNotify(collocation.ru+"~"+collocation.en, collocation);
                textToSpeechSystemCls.speak(collocation.ru);
            }

        } else if (word.toUpperCase().equals("STOP")) {
            item.setTitle(getResources().getString(R.string.action_voiceMode));
            MainActivity.voiceModeOn = false;
            stopListening();
            automatically = false;
        } else {
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
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(indexOfThePreviousSelectedRow);
            collocation = listDictionaryCopy.get(indexOfThePreviousSelectedRow);
            if (englishLeft) {
                textToSpeechSystemCls.setLanguage(Locale.US);
                TService.sendNotify(collocation.en+"~"+collocation.ru, collocation);
                textToSpeechSystemCls.speak(collocation.en
                        .replace("✓", "")
                        .replace("⚓", ""));
            } else {
                textToSpeechSystemCls.setLanguage(new Locale("ru"));
                TService.sendNotify(collocation.ru+"~"+collocation.en, collocation);
                textToSpeechSystemCls.speak(collocation.ru);
            }

        }

    }

    public void commandHandler(String word) {

        textForViewing = true;
        searchView.setQuery(word, false);
        textViewCommands.setText(word);

        Collocation collocation = listDictionaryCopy.get(indexOfThePreviousSelectedRow);

        if (word.toUpperCase().equals("REPEAT")) {
            if (englishLeft) {
                textToSpeechSystemCls.setLanguage(Locale.US);
                 //////%%c//TService(collocation.en+"~"+collocation.ru, collocation);
                textToSpeechSystemCls.speak(collocation.en
                        .replace("✓", "")
                        .replace("⚓", ""));
            } else {
                textToSpeechSystemCls.setLanguage(new Locale("ru"));
                 //////%%c//TService(collocation.ru+"~"+collocation.en, collocation);
                textToSpeechSystemCls.speak(collocation.ru);
            }
        } else if (word.toUpperCase().equals("ANSWER")) {
            if (englishLeft) {
                textToSpeechSystemCls.setLanguage(new Locale("ru"));
                //MainActivity.sendNotif(collocation.ru+"~"+collocation.en, collocation);
                textToSpeechSystemCls.speak(collocation.ru);
            } else {
                textToSpeechSystemCls.setLanguage(Locale.US);
                //MainActivity.sendNotif(collocation.en+"~"+collocation.ru, collocation);
                textToSpeechSystemCls.speak(collocation.en
                        .replace("✓", "")
                        .replace("⚓", ""));
            }
            answerIsSaid = true;


        } else if (word.toUpperCase().equals("SPELL")) {
            textToSpeechSystemCls.setLanguage(Locale.US);
            for (int i = 0; i < collocation.en
                    .replace("✓", "")
                    .replace("⚓", "").length(); i++){
                Character symbol = collocation.en
                        .replace("✓", "")
                        .replace("⚓", "").charAt(i);
                textToSpeechSystemCls.speakSpeel(symbol);
            }
        } else if (word.toUpperCase().equals("AUTOMATICALLY")) {
            automatically = true;
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
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(indexOfThePreviousSelectedRow);
            collocation = listDictionaryCopy.get(indexOfThePreviousSelectedRow);
            if (englishLeft) {
                textToSpeechSystemCls.setLanguage(Locale.US);
                TService.sendNotify(collocation.en+"~"+collocation.ru, collocation);
                textToSpeechSystemCls.speak(collocation.en
                        .replace("✓", "")
                        .replace("⚓", ""));
            } else {
                textToSpeechSystemCls.setLanguage(new Locale("ru"));
                TService.sendNotify(collocation.ru+"~"+collocation.en, collocation);
                textToSpeechSystemCls.speak(collocation.ru);
            }
            answerIsSaid = false;
        } else if (word.toUpperCase().equals("PREVIOUS")) {
            indexOfThePreviousSelectedRow--;
            if (indexOfThePreviousSelectedRow == -1) {
                indexOfThePreviousSelectedRow = listDictionary.size()-1;
            }
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(indexOfThePreviousSelectedRow);
            collocation = listDictionaryCopy.get(indexOfThePreviousSelectedRow);
            if (englishLeft) {
                textToSpeechSystemCls.setLanguage(Locale.US);
                TService.sendNotify(collocation.en+"~"+collocation.ru, collocation);
                textToSpeechSystemCls.speak(collocation.en
                        .replace("✓", "")
                        .replace("⚓", ""));
            } else {
                textToSpeechSystemCls.setLanguage(new Locale("ru"));
                TService.sendNotify(collocation.ru+"~"+collocation.en, collocation);
                textToSpeechSystemCls.speak(collocation.ru);
            }

        } else if (word.toUpperCase().equals("STOP")) {
            item.setTitle(getResources().getString(R.string.action_voiceMode));
            MainActivity.voiceModeOn = false;
            stopListening();
            automatically = false;
        } else if (word.toUpperCase().equals("NEXT")) {
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
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(indexOfThePreviousSelectedRow);
            collocation = listDictionaryCopy.get(indexOfThePreviousSelectedRow);
            if (englishLeft) {
                textToSpeechSystemCls.setLanguage(Locale.US);
                TService.sendNotify(collocation.en+"~"+collocation.ru, collocation);
                textToSpeechSystemCls.speak(collocation.en
                        .replace("✓", "")
                        .replace("⚓", ""));
            } else {
                textToSpeechSystemCls.setLanguage(new Locale("ru"));
                TService.sendNotify(collocation.ru+"~"+collocation.en, collocation);
                textToSpeechSystemCls.speak(collocation.ru);
            }

        }else{
            startListening();
        }
    }
    @Override
    public void onResults(Bundle results) {

        //stopListening();

        words = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        //searchView.setQuery(words.get(0), false);

        word = "";
        if (results == null) {
            if (answerIsSaid){
                word = "AUTOMATICALLY";
            }else {
                word = "ANSWER";
            }
        }else {
            //words = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            word = words.get(0);
        }


        Handler mainHandler = new Handler(getContext().getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                commandHandler(word);
            }
        };
        mainHandler.postDelayed(runnable, 1000);


    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }



    public void startListening(){

        //Toast.makeText(getContext(), "STREAM_NOTIFICATION volume muted.", Toast.LENGTH_SHORT).show();

        if (MainActivity.voiceModeOn && !textToSpeechSystemCls.isSpeaking()) {
            //original_volume_level = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            //audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
            //audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
            //audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
            //audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);
            //mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
            sr.startListening(speechRecognizerIntent);
        }
        //Toast.makeText(getContext(), "startListening", Toast.LENGTH_LONG).show();

//        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en_US");
//        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
//        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
//                getContext().getPackageName());
//
//        getActivity().startActivityForResult(intent,300);
        //sr.startListening(intent);//dont work

    }
    public void stopListening(){

        //if (sr!=null){
            sr.stopListening();
            sr.cancel();
        //}
        //audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, original_volume_level, 0);
        //audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0);
        //audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0);
        Handler mainHandler = new Handler(getContext().getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE,0);
            }
        };
        mainHandler.postDelayed(runnable, 2000);

        //audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0);
        //audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0);
        //Toast.makeText(getContext(), "STREAM_NOTIFICATION volume restored.", Toast.LENGTH_SHORT).show();

    }


    public enum ComparisonValue {BEFORE, EQUAL, AFTER};
    boolean isIrregularVerbs = false;
    //public TextToSpeech textToSpeechSystem;
    private SpeechRecognizer sr = null;
    private Intent speechRecognizerIntent;
    public ArrayList<String> words;
    public String word;
    public boolean answerIsSaid = false;
    public boolean automatically = false;
    //Socket socket;
    private Integer original_volume_level;
    public AudioManager audioManager;

    View page, page2;
    private WebView mWebView;

    public void saveListDictionary(boolean isIrregularVerbs) {

        try {

//            if (!getExternalStorageState().equals(
//                    MEDIA_MOUNTED)) {
//                Toast.makeText(getActivity().getApplicationContext(), "SD-карта не доступна: " + getExternalStorageState(), Toast.LENGTH_SHORT).show();
//                return;
//            }

            // получаем путь к SD
            //File sdPath = getExternalStorageDirectory();
            File sdPath = MainActivity.mainActivityContext.getExternalCacheDir();
            // добавляем свой каталог к пути
            sdPath = new File(sdPath.getAbsolutePath());// + "/mytextfile.txt");
            // создаем каталог
            sdPath.mkdirs();
            // формируем объект File, который содержит путь к файлу
            File sdFile = new File(sdPath, "savedListDictionary");
            if (isIrregularVerbs) {
                sdFile = new File(sdPath, "savedListDictionaryIrregularVerbs");
            }
            String jsonStr = new Gson().toJson(listDictionaryCopy);

            try {
                // открываем поток для записи
                BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile, false));
                // пишем данные
                bw.write(Integer.toString(numberOfBlocks)+";"+Integer.toString(numberOfCollocationsInABlock)+"\r\n");
                bw.write(jsonStr);//
                // закрываем поток
                bw.flush();
                bw.close();

                SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
                editPrefs.putBoolean("isIrregularVerbs", !isIrregularVerbs);
                editPrefs.commit();

                Toast.makeText(getActivity().getBaseContext(), "File saved: " + sdFile.getAbsolutePath(),
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getActivity().getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            //e.printStackTrace();
            Toast.makeText(MainActivity.mainActivityContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static PageFragment newInstance(int page) {
        PageFragment fragment = new PageFragment();
        Bundle args=new Bundle();
        args.putInt("num", page);
        fragment.setArguments(args);
        return fragment;
    }

    public PageFragment() {
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    //@SuppressWarnings("unchecked")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        page = inflater.inflate(R.layout.fragment_page, container, false);
        page2 = inflater.inflate(R.layout.fragment_page2, container, false);

        /*
        List<android.support.v4.app.Fragment> fragments = getFragmentManager().getFragments();
        //android.support.v4.app.Fragment frag = fragments.get(0);
        android.support.v4.app.Fragment frag2 = fragments.get(1);
        */

        List<Fragment> fragments = getFragmentManager().getFragments();
        frag1 = fragments.get(0);
        frag2 = fragments.get(1);

        rootView = page.findViewById(R.id.constraintLayout);
        searchView = page.findViewById(R.id.searchView);

        tvProgressBar = (ProgressBar) page.findViewById(R.id.progressBar);
        tvTextLearned = (TextView) page.findViewById(R.id.tvTextLearned);
        tvTextOnRepetition = (TextView) page.findViewById(R.id.tvTextOnRepetition);
        tvTextLeft = (TextView) page.findViewById(R.id.tvTextLeft);
        tvTextTotal = (TextView) page.findViewById(R.id.tvTextTotal);

        editTextNumberOfBlocks = (EditText) page.findViewById(R.id.numberOfBlocks);
        textViewCommands = (TextView) page.findViewById(R.id.textViewCommands);

        editTextHostname = (EditText) page2.findViewById(R.id.hostname);
        editTextPortname = (EditText) page2.findViewById(R.id.portname);



        ////////////////////////////////////////////////////////////////////////
       /* textToSpeechSystem = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                if (status == TextToSpeech.SUCCESS) {
                    if (!textToSpeechSystem.isSpeaking()
                            && TService.count +1000== TService.numberOfRepetitions) {
                        //textToSpeechSystem.speak(TService.textToSpeak, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);

                        ((PageFragment) frag1).automatically = true;
                        ((PageFragment) frag1).answerIsSaid = true;
                        ((PageFragment) frag1).onResultsFromMainActivity(null);

                    }

                }

            }
        });*/
        ////////TService.textToSpeechSystem2 = Speecher.getInstance(getContext());
        /*
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, new String[]{"uk-UA"});
        ////speechRecognizerIntent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{"uk-UA"});

        //speechRecognizerIntent.putExtra("android.speech.extra.DICTATION_MODE", true);
        //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                getContext().getPackageName());
        */
        //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"speech_prompt");
        //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
        sr = SpeechRecognizer.createSpeechRecognizer(this.getContext());
        sr.setRecognitionListener(this);

        TService.textToSpeechSystemCls = Speecher.getInstance(getContext());

        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en_US");//"ja_JP");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getContext().getPackageName());
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        //audioManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        ///startListening();
        /////////////////////////////////////////////////////////////////////
        editTextNumberOfBlocks.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int arg1, KeyEvent event) {
                int backupValue = numberOfBlocks;
                try {
                    numberOfBlocks = Integer.parseInt( ((EditText)v).getText().toString() );
                    if( numberOfBlocks < 1 ) numberOfBlocks = 1;
                } catch( NumberFormatException nfe ){
                    numberOfBlocks = backupValue;
                    return false;
                }
                defineIndexesOfWords();
                adapter.notifyDataSetChanged();

                return false;
            }
        });

        editTextNumberOfCollocationsInABlock = page.findViewById(R.id.numberOfCollocationsInABlock);

        editTextNumberOfCollocationsInABlock.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int arg1, KeyEvent event) {
                int backupValue = numberOfCollocationsInABlock;
                try {
                    numberOfCollocationsInABlock = Integer.parseInt( ((EditText)v).getText().toString() );
                    if( numberOfCollocationsInABlock < 1 ) numberOfCollocationsInABlock = 1;
                } catch( NumberFormatException nfe ){
                    numberOfCollocationsInABlock = backupValue;
                    return false;
                }
                defineIndexesOfWords();
                adapter.notifyDataSetChanged();

                return false;
            }
        });

        editTexScrollingSpeed = page.findViewById(R.id.scrollingSpeed);

        editTexScrollingSpeed.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int arg1, KeyEvent event) {
                float backupValue = millisecondsPerInch;
                try {
                    millisecondsPerInch = Integer.parseInt( ((EditText)v).getText().toString() );
                    if( millisecondsPerInch < 1 ) millisecondsPerInch = 1;
                } catch( NumberFormatException nfe ){
                    millisecondsPerInch = backupValue;
                    return false;
                }

                return false;
            }
        });

        //скрыть ответы
        final View buttonHideAnswers = page.findViewById(R.id.buttonHideAnswers);
        buttonHideAnswers.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                hideAnswers();

                //sr.stopListening();
            }

        });

        //Показать ответы
        final View buttonShowAnswers = page.findViewById(R.id.buttonShowAnswers);
        buttonShowAnswers.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    showAnswers();

//                    try {
//                        sr.startListening(speechRecognizerIntent);
//                    }catch (Exception e){
//                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
//                    }

                    //getContext().bindService(new Intent(getContext(), TService.class), TService.mConnection, Context.BIND_AUTO_CREATE);

//                    Handler mainHandler = new Handler(getContext().getMainLooper());
//                    Runnable runnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            //MainActivity.sr.startListening(speechRecognizerIntent);
//                            getContext().bindService(new Intent(getContext(), TService.class), TService.mConnection, Context.BIND_AUTO_CREATE);
//                        }
//                    };
//                    ((MainActivity)getContext()).runOnUiThread(runnable);

                }
            });

        //сортировать
        final View buttonSort = page.findViewById(R.id.buttonSort);
        buttonSort.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if(listDictionaryCopy.size() != listDictionary.size()) {
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                "Clean the filter!",
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 0);
                        toast.show();
                        return;
                    }

                    Comparator<Collocation> enRuComparator = new Comparator<Collocation>() {
                        @Override
                        public int compare(Collocation o1, Collocation o2) {
                            if(englishLeft){
                                return o1.en.compareTo(o2.en);
                            }else{
                                return o1.ru.compareTo(o2.ru);
                            }
                        }
                    };

                    boolean answersAreHidden = answersWereHidden;
                    if (answersWereHidden){
                        showAnswers();
                    }

                    List<Collocation> listOfStudiedWords = new ArrayList<Collocation>();
                    List<Collocation> listOfFavoriteWords = new ArrayList<Collocation>();
                    List<Collocation> listOfDifficultWords = new ArrayList<Collocation>();
                    List<Collocation> listOfLearnedWords = new ArrayList<Collocation>();

                    for (int i = 0; i < listDictionary.size(); i++) {

                        Collocation collocation = listDictionary.get(i);

                        if(collocation.learnedEn != collocation.learnedRu){
                            if(collocation.isDifficult && swap){
                                listOfFavoriteWords.add(collocation);
                            }else {
                                listOfStudiedWords.add(collocation);
                            }
                            listDictionary.remove(i);
                            i--;
                            continue;
                        }
                        if(collocation.isDifficult){
                            listOfDifficultWords.add(collocation);
                            listDictionary.remove(i);
                            i--;
                            continue;
                        }
                        if(collocation.learnedEn && collocation.learnedRu){
                            listOfLearnedWords.add(collocation);
                            listDictionary.remove(i);
                            i--;
                            continue;
                        }
                    }

                    Collections.sort(listOfStudiedWords, enRuComparator);



                    Collections.sort(listOfFavoriteWords, enRuComparator);
                    Collections.sort(listOfDifficultWords, enRuComparator);
                    Collections.sort(listDictionary, enRuComparator);

                    int j = 0;
                    for (Collocation collocation : listOfFavoriteWords) {
                        listDictionary.add(j, collocation);
                        j++;
                        rowBeginIndexOfLearnedWords = j;
                    }
                    for (Collocation collocation : listOfStudiedWords) {
                        listDictionary.add(j, collocation);
                        j++;
                        rowBeginIndexOfLearnedWords = j;
                    }
                    for (Collocation collocation : listDictionary) {
                        if(collocation.learnedEn == collocation.learnedRu) {
                            j++;
                        }
                        rowBeginIndexOfLearnedWords = j;
                    }
                    int countOfDifficultWords = 0;
                    for (Collocation collocation : listOfDifficultWords) {
                        countOfDifficultWords++;

                        listDictionary.add(collocation);

                        j++;
                        rowBeginIndexOfLearnedWords = j;
                    }
                    countOfLearnedWords = 0;
                    List<Collocation> listOfWellLearnedWords = new ArrayList<Collocation>();
                    for (Collocation collocation : listOfLearnedWords) {
                        countOfLearnedWords++;

                        if(j >= rowBeginIndexOfLearnedWords + numberOfBlocks * numberOfCollocationsInABlock){
                            listOfWellLearnedWords.add(collocation);
                        }else {
                            listDictionary.add(collocation);
                            j++;
                            rowBeginIndexOfWellLearnedWords = j;
                        }
                    }
                    rowBeginIndexOfNativeWords = rowBeginIndexOfWellLearnedWords + numberOfBlocks * numberOfCollocationsInABlock;
                    tvProgressBar.setMax(listDictionary.size());
                    tvProgressBar.setProgress(countOfLearnedWords);
                    for (Collocation collocation : listOfWellLearnedWords) {
                        listDictionary.add(collocation);
                        j++;
                    }

                    int index = 0;
                    listDictionaryCopy.clear();
                    for (Collocation collocation : listDictionary) {
                        collocation.index = index;
                        listDictionaryCopy.add(new Collocation(
                                collocation.learnedEn,
                                collocation.en,
                                collocation.learnedRu,
                                collocation.ru,
                                collocation.isDifficult,
                                index
                        ));
                        index++;
                    }


                    if(answersAreHidden){
                        hideAnswers();
                    }

                    adapter.notifyDataSetChanged();

                    tvTextLearned.setText(Integer.toString(countOfLearnedWords));
                    tvTextOnRepetition.setText(Integer.toString(countOfDifficultWords));
                    tvTextLeft.setText(Integer.toString(listDictionary.size() - countOfLearnedWords));
                    tvTextTotal.setText(Integer.toString(listDictionary.size()));

                    if (answersWereHidden){
                        hideAnswers();
                    }

                    swap = false;
                    buttonSwap.getBackground().clearColorFilter();

                }
            });

        //Del.
        final View buttonDelete = page.findViewById(R.id.buttonDelete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (swap) {
                    //Dialog
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setTitle("Reset progress");  // заголовок
                    alertDialog.setMessage("Clear progress?"); // сообщение
                    alertDialog.setPositiveButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                        }
                    });
                    alertDialog.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {

                            listDictionary.clear();
                            adapter.notifyDataSetChanged();

                            for (Collocation collocationCopy : listDictionaryCopy) {
                                collocationCopy.learnedEn = false;
                                collocationCopy.learnedRu = false;
                                collocationCopy.isDifficult = false;
                                collocationCopy.en = collocationCopy.en
                                        .replace("✓", "")
                                        .replace("⚓", "");

                                listDictionary.add(new Collocation(
                                        collocationCopy.learnedEn,
                                        collocationCopy.en,
                                        collocationCopy.learnedRu,
                                        collocationCopy.ru,
                                        collocationCopy.isDifficult,
                                        collocationCopy.index));
                            }

                            defineIndexesOfWords();
                            answersWereHidden = false;
                            adapter.notifyDataSetChanged();

                            tvTextLearned.setText("0");
                            tvTextOnRepetition.setText("0");
                            tvTextLeft.setText(Integer.toString(listDictionaryCopy.size()));
                            tvTextTotal.setText(Integer.toString(listDictionaryCopy.size()));

                            tvProgressBar.setMax(listDictionaryCopy.size());
                            tvProgressBar.setProgress(countOfLearnedWords);

                            Toast.makeText(getContext(), "Progress is reset", Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                    alertDialog.setCancelable(true);
                    alertDialog.show();

                    swap = false;
                    buttonSwap.getBackground().clearColorFilter();

                    return;
                }

                if (indexOfTheSelectedRow < 0){
                    Toast.makeText(getContext(), "choose the collocation!", Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                final Collocation collocation = listDictionary.get(indexOfTheFilteredSelectedRow);
                final Collocation collocationCopy = listDictionaryCopy.get(collocation.index);
                String strCollocation = collocationCopy.en + "~" + collocationCopy.ru;

                //Dialog
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setTitle("Remove the words?");  // заголовок
                alertDialog.setMessage(strCollocation); // сообщение
                alertDialog.setPositiveButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                    }
                });
                alertDialog.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {

                        listDictionary.remove(collocation);
                        listDictionaryCopy.remove(collocationCopy);

                        collocationRemoved = true;
                        defineIndexesOfWords();
                        adapter.notifyDataSetChanged();

                        tvProgressBar.setMax(listDictionaryCopy.size());
                        tvTextLearned.setText(Integer.toString(countOfLearnedWords));
                        tvTextOnRepetition.setText(Integer.toString(countOfDifficultWords));
                        tvTextLeft.setText(Integer.toString(listDictionaryCopy.size() - countOfLearnedWords));
                        tvTextTotal.setText(Integer.toString(listDictionaryCopy.size()));

                        Toast.makeText(getContext(), "words deleted", Toast.LENGTH_LONG)
                                .show();
                    }
                });
                alertDialog.setCancelable(true);
                alertDialog.show();
            }
        });

        //Поменять
        final View buttonChange = page.findViewById(R.id.buttonChange);
        buttonChange.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    View current = getActivity().getCurrentFocus();
                    if (current != null) current.clearFocus();

                    boolean answersAreHidden = answersWereHidden;
                    if (answersWereHidden){
                        showAnswers();
                    }

                    englishLeft = !englishLeft;
                    adapter.notifyDataSetChanged();

                    if(answersAreHidden){
                        hideAnswers();
                    }


                }
            });

        //перемешать
        final View buttonShuffle = page.findViewById(R.id.buttonShuffle);
        buttonShuffle.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if(listDictionaryCopy.size() != listDictionary.size()) {
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                "Clean the filter!",
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 0);
                        toast.show();
                        return;
                    }

                    //Очищаем фокус
                    rootView.requestFocus();
                    //скрываем клавиатуру
                    //inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                    boolean answersAreHidden = answersWereHidden;
                    if (answersWereHidden){
                        showAnswers();
                    }

                    List<Collocation> listOfStudiedWords = new ArrayList<Collocation>();
                    List<Collocation> listOfFavoriteWords = new ArrayList<Collocation>();
                    List<Collocation> listOfDifficultWords = new ArrayList<Collocation>();
                    List<Collocation> listOfLearnedWords = new ArrayList<Collocation>();

                    for (int i = 0; i < listDictionary.size(); i++) {

                        Collocation collocation = listDictionary.get(i);

                        if(collocation.learnedEn != collocation.learnedRu){
                            if(collocation.isDifficult && swap){
                                listOfFavoriteWords.add(collocation);
                            }else {
                                listOfStudiedWords.add(collocation);
                            }
                            listDictionary.remove(i);
                            i--;
                            continue;
                        }
                        if(collocation.isDifficult){

                            listOfDifficultWords.add(collocation);
                            listDictionary.remove(i);
                            i--;
                            continue;
                        }
                        if(collocation.learnedEn && collocation.learnedRu){
                            listOfLearnedWords.add(collocation);
                            listDictionary.remove(i);
                            i--;
                            continue;
                        }
                    }

                    Collections.shuffle(listOfStudiedWords);
                    Collections.shuffle(listOfFavoriteWords);
                    Collections.shuffle(listDictionary);

                    int j = 0;
                    for (Collocation collocation : listOfFavoriteWords) {
                        listDictionary.add(j, collocation);
                        j++;
                        rowBeginIndexOfLearnedWords = j;
                    }

                    for (Collocation collocation : listOfStudiedWords) {
                        listDictionary.add(j, collocation);
                        j++;
                        rowBeginIndexOfLearnedWords = j;
                    }

                    for (Collocation collocation : listDictionary) {
                        if(collocation.learnedEn == collocation.learnedRu) {
                            j++;
                        }
                        rowBeginIndexOfLearnedWords = j;
                    }

                    int countOfDifficultWords = 0;
                    for (Collocation collocation : listOfDifficultWords) {
                        countOfDifficultWords++;
                        listDictionary.add(collocation);
                        j++;
                        rowBeginIndexOfLearnedWords = j;
                    }
                    countOfLearnedWords = 0;
                    List<Collocation> listOfWellLearnedWords = new ArrayList<Collocation>();
                    for (Collocation collocation : listOfLearnedWords) {
                        countOfLearnedWords++;

                        if(j >= rowBeginIndexOfLearnedWords + numberOfBlocks * numberOfCollocationsInABlock){
                            listOfWellLearnedWords.add(collocation);
                        }else {
                            listDictionary.add(collocation);
                            j++;
                            rowBeginIndexOfWellLearnedWords = j;
                        }
                    }
                    rowBeginIndexOfNativeWords = rowBeginIndexOfWellLearnedWords + numberOfBlocks * numberOfCollocationsInABlock;
                    tvProgressBar.setMax(listDictionary.size());
                    tvProgressBar.setProgress(countOfLearnedWords);
                    Collections.shuffle(listOfWellLearnedWords);
                    for (Collocation collocation : listOfWellLearnedWords) {
                        listDictionary.add(collocation);
                        j++;
                    }

                    listDictionaryCopy.clear();
                    int index = 0;
                    for (Collocation collocation : listDictionary) {
                        collocation.index = index;
                        listDictionaryCopy.add(new Collocation(
                                collocation.learnedEn,
                                collocation.en,
                                collocation.learnedRu,
                                collocation.ru,
                                collocation.isDifficult,
                                index
                        ));
                        index++;
                    }

                    if(answersAreHidden){
                        hideAnswers();
                    }

                    adapter.notifyDataSetChanged();

                    tvTextLearned.setText(Integer.toString(countOfLearnedWords));
                    tvTextOnRepetition.setText(Integer.toString(countOfDifficultWords));
                    tvTextLeft.setText(Integer.toString(listDictionary.size() - countOfLearnedWords));
                    tvTextTotal.setText(Integer.toString(listDictionary.size()));

                    if (answersWereHidden){
                        hideAnswers();
                    }

                    swap = false;
                    buttonSwap.getBackground().clearColorFilter();

                }
            });

        //Вниз
        final View buttonRepeat = page.findViewById(R.id.buttonRepeat);
        buttonRepeat.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if(swap) {
                        recyclerView.scrollToPosition(listDictionary.size() - 1);

                        swap = false;
                        buttonSwap.getBackground().clearColorFilter();
                    }
                    else {
                        int j = 0;
                        for (Collocation i : listDictionary) {
                            if (i.learnedEn && i.learnedRu) {
                                break;
                            }
                            j++;
                        }
                        if (j == listDictionary.size()) j = 0;

                        recyclerView.scrollToPosition(j);
                    }

                }
            });

        //Вверх
        View buttonLearnNew = page.findViewById(R.id.buttonLearnNew);
        buttonLearnNew.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if (swap) {
                        recyclerView.scrollToPosition(0);

                        swap = false;
                        buttonSwap.getBackground().clearColorFilter();
                    }else{
                        int i = 0;
                        int j = 0;
                        for(Collocation colloc: listDictionary){
                            i++;
                            if (colloc.learnedEn != colloc.learnedRu){
                                j = i;
                            }
                        }
                        recyclerView.scrollToPosition(j);
                    }

                }
            });

        buttonSwap = page.findViewById(R.id.buttonSwap);
        buttonSwap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                swap = !swap;
                if(swap){
                    buttonSwap.getBackground().setColorFilter(
                            Color.parseColor("#FA8072"), PorterDuff.Mode.MULTIPLY
                    );
                }else{
                    buttonSwap.getBackground().clearColorFilter();
                }
            }
        });

        recyclerView = (RecyclerView) page.findViewById(R.id.recyclerView);

        myLlm = new MyLinearLayoutManager(getContext());
        myLlm.setAutoMeasureEnabled(false);

        recyclerView.setLayoutManager(myLlm);
        //recyclerView.setDrawingCacheEnabled(false);

        adapter = new RecyclerViewAdapter(listDictionary);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recView, int newState) {
                super.onScrollStateChanged(recView, newState);

                if (newState == 2){
                    if(scrollStepY < 0){
                        recyclerView.smoothScrollToPosition(0);
                    }else if(scrollStepY > 0){
                        recyclerView.smoothScrollToPosition(listDictionary.size());
                    }

                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                scrollStepY = dy;
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        //OnQueryTextListener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                String strCollocation = query;
                if (strCollocation.contains("~")){
                    String[] collocationParts = strCollocation.split("~");
                    if(collocationParts.length != 2){
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                "Use format: [ENword][~][RUword]",
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        return true;
                    }
                    Collocation collocation = new Collocation(false, collocationParts[0].trim(), false, collocationParts[1].trim(), false, 0);
                    Collocation collocationCopy = new Collocation(false, collocationParts[0].trim(), false, collocationParts[1].trim(), false, 0);

                    Character Symbol = collocation.en.charAt(0);
                    boolean EnglishLayout = false;//engList.indexOf(Symbol) != -1;
                    boolean RussianLayout = false;//rusList.indexOf(Symbol) != -1;

                    for (int i = 0; i < ArrayEnglishCharacters.length; i++) {
                        if (ArrayEnglishCharacters[i] == Symbol) {
                            EnglishLayout = true;
                        }
                    }

                    for (int i = 0; i < ArrayRussianCharacters.length; i++) {
                        if (ArrayRussianCharacters[i] == Symbol) {
                            RussianLayout = true;
                        }
                    }

                    if (EnglishLayout != RussianLayout) {
                        EnglishTextLayout = EnglishLayout;
                    }

                    if (!EnglishTextLayout) {
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                "Use format: [ENword][~][RUword]",
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        return true;
                    }

                    listDictionary.add(0, collocation);
                    listDictionaryCopy.add(0, collocationCopy);

                    defineIndexesOfWords();
                    adapter.notifyDataSetChanged();

                    tvProgressBar.setMax(listDictionaryCopy.size());

                    searchView.setQuery("", false);

                    tvTextLeft.setText(Integer.toString(listDictionaryCopy.size() - countOfLearnedWords));
                    tvTextTotal.setText(Integer.toString(listDictionaryCopy.size()));

                    return true;

                }else{
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                            "Use format: [ENword][~][RUword]",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return true;

                }
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!textForViewing) {
                    adapter.filter(newText.toString());
                }
                textForViewing = false;

                return false;
            }
        });

        editTextHostname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable hostname) {

                /*
                receiverList.clear();
                for (int i = 0; i < COUNT_OF_RECEIVERS; i++) {
                    receiverList.add( new Receiver(hostname.toString(),
                            Integer.parseInt(editTextPortname.getText().toString())));
                }
                */

            }
        });

        editTextPortname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable portname) {

                /*
                receiverList.clear();
                for (int i = 0; i < COUNT_OF_RECEIVERS; i++) {
                    receiverList.add( new Receiver(editTextHostname.getText().toString(),
                            Integer.parseInt(portname.toString())));
                }
                */

            }
        });


        recyclerView.scrollToPosition(indexOfTheSelectedRow);

        /*
        rp = new Receiver(editTextHostname.getText().toString(),
                Integer.parseInt(editTextPortname.getText().toString()));

        */

        if (TService.action.equals("action_Speech")
                && TService.count != TService.numberOfRepetitions) {

            if (TService.count == TService.numberOfRepetitions-1) {

                BatteryManager batteryManager = (BatteryManager) getContext().getSystemService(BATTERY_SERVICE);


                Collocation collocationCopy = listDictionaryCopy.get(indexOfThePreviousSelectedRow);

                int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                TService.sendNotify("Battery level: "+batteryLevel+"%",collocationCopy);

            }
            if (TService.count > TService.numberOfRepetitions) {
                int j = 0;
                for (Collocation i : listDictionary) {
                    if (i.learnedEn && i.learnedRu) {
                        break;
                    }
                    j++;
                }
                if (j == listDictionary.size()) j = 0;

                indexOfThePreviousSelectedRow = j;
                Collocation collocationCopy = listDictionaryCopy.get(indexOfThePreviousSelectedRow);

                if(englishLeft) {
                    TService.sendNotify(collocationCopy.en + "~" + collocationCopy.ru, collocationCopy);
                }else {
                    TService.sendNotify(collocationCopy.ru + "~" + collocationCopy.en, collocationCopy);
                }
                SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivityContext).edit();
                editPrefs.putInt("indexOfThePreviousSelectedRow", indexOfThePreviousSelectedRow);
                //editPrefs.putBoolean("englishLeft", englishLeft);
                editPrefs.commit();

                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(indexOfThePreviousSelectedRow);

                //((PageFragment) frag1).automatically = true;
                TService.count = TService.numberOfRepetitions - 1;


            }
            Collocation collocation = TService.listDictionaryCopy.get(indexOfThePreviousSelectedRow);
            // ((PageFragment) frag1).automatically = true;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            englishLeft = prefs.getBoolean("englishLeft", true);

            if (englishLeft) {
                textToSpeechSystemCls.setLanguage(Locale.US);
                textToSpeechSystemCls.speak(collocation.en);
            } else {
                textToSpeechSystemCls.setLanguage(new Locale("ru"));
                textToSpeechSystemCls.speak(collocation.ru);
            }

        }

        if (TService.action.equals("action_Speech")
                && TService.count == TService.numberOfRepetitions
                && !((PageFragment) frag1).automatically
        ) {
            if (item != null) {
                item.setTitle(getResources().getString(R.string.action_textMode));
            }

            ((PageFragment) frag1).automatically = true;
            MainActivity.voiceModeOn = true;
            try {
                ((PageFragment)frag1).voiceMode();
            }catch(Exception e){
                Log.d("onOptionsItemSelected: ", e.getMessage());
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            englishLeft = prefs.getBoolean("englishLeft", true);

            Collocation collocation = TService.listDictionaryCopy.get(indexOfThePreviousSelectedRow);
            ((PageFragment) frag1).automatically = true;
            if (englishLeft) {
                textToSpeechSystemCls.setLanguage(Locale.US);
                textToSpeechSystemCls.speak(collocation.en);
            } else {
                textToSpeechSystemCls.setLanguage(new Locale("ru"));
                textToSpeechSystemCls.speak(collocation.ru);
            }
        }

        Handler mainHandler = new Handler(getContext().getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run(){
            }
        };
        mainHandler.post(runnable);

        fillReceiverList(editTextHostname.getText().toString(), editTextPortname.getText().toString());

        isStart = true;
        isResumeAfterStop = false;

        return page;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!swap) {
            save();
        }
        isResumeAfterStop = true;

    }

    @Override
    public void onStop() {
        super.onStop();

//        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, original_volume_level, 0);
//        Toast.makeText(getContext(), "STREAM_NOTIFICATION volume restored.", Toast.LENGTH_SHORT).show();
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE,0);

        if (!swap) {
            save();
            swap = true;
        }
        isResumeAfterStop = true;


    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        //isStart = true;
        swap = true;

        if (isStart || isResumeAfterStop) {
            isIrregularVerbs = false;

            restore();
            TService.activityIsStarted = true;
        }
        isStart = false;
        isResumeAfterStop = false;

        rootView.requestFocus();


    }

    @Override
    public void onResume() {
        super.onResume();

        if (swap){
            swap = false;
            buttonSwap.getBackground().clearColorFilter();
            return;
        }
        if (isStart || isResumeAfterStop) {
            isIrregularVerbs = false;

            restore();
            TService.activityIsStarted = true;
        }
        isStart = false;
        isResumeAfterStop = false;

        rootView.requestFocus();


    }



    public void restoreListDictionary(boolean isIrregularVerbs){

        this.isIrregularVerbs = isIrregularVerbs;

        // проверяем доступность SD
        //if (!getExternalStorageState().equals(
        //if (!MainActivity.mainActivityContext.getExternalCacheDir().equals(
        //        MEDIA_MOUNTED)) {
        //    Toast.makeText(getActivity().getBaseContext(), "SD-карта не доступна: " + getExternalStorageState(), Toast.LENGTH_SHORT).show();
        //    return;
        //}

        // получаем путь к SD
        //File sdPath = getExternalStorageDirectory();
        File sdPath = MainActivity.mainActivityContext.getExternalCacheDir();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath());// + "/mytextfile.txt");
        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, "savedListDictionary");
        if (isIrregularVerbs) {
            sdFile = new File(sdPath, "savedListDictionaryIrregularVerbs");
        }
        if (!sdFile.exists()){
            try {
                sdFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        listDictionary.clear();
        listDictionaryCopy.clear();

        try {

            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new FileReader(sdFile));
            String str = "";
            JsonParser parser = new JsonParser();
            // читаем содержимое
            //while ((str = br.readLine()) != null) {
            str = br.readLine();
            String[] arrayStr = str.split(";");
            numberOfBlocks = Integer.valueOf(arrayStr[0]);
            numberOfCollocationsInABlock = Integer.valueOf(arrayStr[1]);

            str = br.readLine();
            Gson gson = new Gson();
            JsonArray array = parser.parse(str).getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                Collocation collocation = (gson.fromJson(array.get(i), Collocation.class));
                listDictionary.add(collocation);
                listDictionaryCopy.add(new Collocation(
                        collocation.learnedEn,
                        collocation.en,
                        collocation.learnedRu,
                        collocation.ru,
                        collocation.isDifficult,
                        i
                ));
            }
            //}
            Toast.makeText(getContext(), "File restore successfully!",Toast.LENGTH_SHORT).show();
            Log.d("jkl", "restoreListDictionary: File restore successfully!");
        } catch (FileNotFoundException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("jkl", "restoreListDictionary: "+e.getMessage());
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("jkl", "restoreListDictionary: "+e.getMessage());
        }







        if (listDictionary.size() == 0) {
            Log.d("jkl", "restoreListDictionary: listDictionary.size() == 0");
            resetListDictionary();
        }else{
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

            //this.isIrregularVerbs = prefs.getBoolean("isIrregularVerbs", false);
            englishLeft = prefs.getBoolean("englishLeft", true);
            answersWereHidden = prefs.getBoolean("answersWereHidden", false);
            indexOfTheSelectedRow = prefs.getInt("indexOfTheSelectedRow", 0);
            indexOfThePreviousSelectedRow = prefs.getInt("indexOfThePreviousSelectedRow", 0);
            millisecondsPerInch = prefs.getFloat("millisecondsPerInch", 1000f);

            tvTextLearned.setText(Integer.toString( prefs.getInt("countOfLearnedWords", 0) ));
            tvTextOnRepetition.setText(Integer.toString( prefs.getInt("countOfDifficultWords", 0) ));
            tvTextLeft.setText(Integer.toString( prefs.getInt("countOfLeftWords",  listDictionaryCopy.size()) ));
            tvTextTotal.setText(Integer.toString( prefs.getInt("countOfTotalWords", listDictionaryCopy.size()) ));

            if (isIrregularVerbs){
                englishLeft = prefs.getBoolean("englishLeftIr", true);
                answersWereHidden = prefs.getBoolean("answersWereHiddenIr", false);
                indexOfTheSelectedRow = prefs.getInt("indexOfTheSelectedRowIr", 0);
                indexOfThePreviousSelectedRow = prefs.getInt("indexOfThePreviousSelectedRowIr", 0);
                millisecondsPerInch = prefs.getFloat("millisecondsPerInchIr", 1000f);

                tvTextLearned.setText(Integer.toString( prefs.getInt("countOfLearnedWordsIr", 0) ));
                tvTextOnRepetition.setText(Integer.toString( prefs.getInt("countOfDifficultWordsIr", 0) ));
                tvTextLeft.setText(Integer.toString( prefs.getInt("countOfLeftWordsIr",  listDictionaryCopy.size()) ));
                tvTextTotal.setText(Integer.toString( prefs.getInt("countOfTotalWordsIr", listDictionaryCopy.size()) ));
            }

            editTexScrollingSpeed.setText(Integer.toString( (int) millisecondsPerInch));
            editTextHostname.setText(prefs.getString("hostname", "192.168.0.1"));
            editTextPortname.setText(prefs.getString("portname", "7373"));
        }

        defineIndexesOfWords();
        editTextNumberOfBlocks.setText(Integer.toString(numberOfBlocks));
        editTextNumberOfCollocationsInABlock.setText(Integer.toString(numberOfCollocationsInABlock));

        tvProgressBar.setMax(listDictionary.size());
        tvProgressBar.setProgress(countOfLearnedWords);

        if (answersWereHidden){
            hideAnswers();
        }

        adapter.notifyDataSetChanged();
        if (MainActivity.isNewIntent) {
            MainActivity.isNewIntent = false;
            recyclerView.scrollToPosition(indexOfThePreviousSelectedRow);
        }
    }

    public void resetListDictionary(){//boolean softwareReset

        String[] arrayDictionary = getResources().getStringArray(R.array.dictionary);
        if (isIrregularVerbs) {
            arrayDictionary = getResources().getStringArray(R.array.dictionary_irregular_verbs);
        }
        listDictionary.clear();
        listDictionaryCopy.clear();
        int index = 0;
        for (int i = 0; i < arrayDictionary.length; i += 2) {
            Collocation collocation = new Collocation(false, arrayDictionary[i], false,  arrayDictionary[i + 1], false, index);
            listDictionary.add(collocation);
            listDictionaryCopy.add(new Collocation(
                    collocation.learnedEn,
                    collocation.en,
                    collocation.learnedRu,
                    collocation.ru,
                    collocation.isDifficult,
                    index
            ));
            index++;
        }
        defineIndexesOfWords();
        answersWereHidden = false;
        adapter.notifyDataSetChanged();

        tvTextLearned.setText("0");
        tvTextOnRepetition.setText("0");
        tvTextLeft.setText(Integer.toString(listDictionaryCopy.size()));
        tvTextTotal.setText(Integer.toString(listDictionaryCopy.size()));


        tvProgressBar.setMax(listDictionaryCopy.size());
        tvProgressBar.setProgress(countOfLearnedWords);


    }

    public void restore(){

        restoreListDictionary(false);


        if (answersWereHidden){
            hideAnswers();
        }

    }

   
    public void save(){
        if (!isIrregularVerbs) {
            saveListDictionary(false);
        }else {
            saveListDictionary(true);
        }

        SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivityContext).edit();
        editPrefs.putBoolean("isIrregularVerbs", false);
        if (!isIrregularVerbs) {
            editPrefs.putBoolean("englishLeft", englishLeft);
            editPrefs.putBoolean("answersWereHidden", answersWereHidden);
            if (recyclerView.getChildAdapterPosition(recyclerView.getFocusedChild())==-1){
                recyclerView.getChildAdapterPosition(recyclerView.getFocusedChild());
            }else {
                editPrefs.putInt("indexOfTheSelectedRow", 0);
            }

            editPrefs.putInt("indexOfThePreviousSelectedRow", indexOfThePreviousSelectedRow);

            editPrefs.putInt("countOfLearnedWords", countOfLearnedWords);
            editPrefs.putInt("countOfDifficultWords", countOfDifficultWords);
            editPrefs.putInt("countOfLeftWords", listDictionaryCopy.size() - countOfLearnedWords);
            editPrefs.putInt("countOfTotalWords", listDictionaryCopy.size());

            editPrefs.putFloat("millisecondsPerInch", millisecondsPerInch);
        }else {
            editPrefs.putBoolean("englishLeftIr", englishLeft);
            editPrefs.putBoolean("answersWereHiddenIr", answersWereHidden);
            editPrefs.putInt("indexOfTheSelectedRowIr", recyclerView.getChildAdapterPosition(recyclerView.getFocusedChild()));
            editPrefs.putInt("indexOfThePreviousSelectedRowIr", indexOfThePreviousSelectedRow);

            editPrefs.putInt("countOfLearnedWordsIr", countOfLearnedWords);
            editPrefs.putInt("countOfDifficultWordsIr", countOfDifficultWords);
            editPrefs.putInt("countOfLeftWordsIr", listDictionaryCopy.size() - countOfLearnedWords);
            editPrefs.putInt("countOfTotalWordsIr", listDictionaryCopy.size());

            editPrefs.putFloat("millisecondsPerInchIr", millisecondsPerInch);
        }



        editPrefs.commit();
        TService.mainActivity.finish();

    }

    private void hideAnswers(){

        if (englishLeft) {
            for (Collocation collocation : listDictionary) {
                collocation.ru = "";
            }
        } else {
            for (Collocation collocation : listDictionary) {
                collocation.en = "";
            }
        }
        answersWereHidden = true;
        adapter.notifyDataSetChanged();
    }

    private void showAnswers() {

        for (Collocation collocation : listDictionary) {
            Collocation collocationCopy = listDictionaryCopy.get(collocation.index);

            collocation.ru = collocationCopy.ru;
            collocation.en = collocationCopy.en;
        }
        answersWereHidden = false;
        adapter.notifyDataSetChanged();

    }

    private int defineViewType(int rowIndex){

        if (englishLeft) {

            if (rowIndex == indexOfThePreviousSelectedRow) {
                return 0;
            }else if (listDictionary.size() > 0 && listDictionary.get(rowIndex).isDifficult) {
                return 1;
            } else if  (rowIndex + 1 > rowBeginIndexOfLearnedWords && rowIndex + 1 <= rowBeginIndexOfWellLearnedWords) {
                return 2;
            } else if (rowIndex + 1 > rowBeginIndexOfWellLearnedWords && rowIndex < rowBeginIndexOfNativeWords) {
                return 3;
            }else if (rowIndex + 1 > rowBeginIndexOfNativeWords){
                return 4;
            } else if ((rowIndex + 1) % numberOfCollocationsInABlock == 0) {
                return 5;
            }

            return 6;

        }else{

            if (rowIndex == indexOfThePreviousSelectedRow) {
                return 10;
            }else if (listDictionary.size() > 0 && listDictionary.get(rowIndex).isDifficult) {
                return 11;
            } else if (rowIndex + 1 > rowBeginIndexOfLearnedWords && rowIndex + 1 <= rowBeginIndexOfWellLearnedWords) {
                return 12;
            } else if (rowIndex + 1 > rowBeginIndexOfWellLearnedWords && rowIndex < rowBeginIndexOfNativeWords) {
                return 13;
            }else if (rowIndex + 1 > rowBeginIndexOfNativeWords){
                return 14;
            } else if ((rowIndex + 1) % numberOfCollocationsInABlock == 0) {
                return 15;
            }

            return 16;

        }

    }

    private void colorizeLines(View itemView, int viewType){

        ConstraintLayout linearLayoutCommon = itemView.findViewById(R.id.constraintLayoutCommon);

        if (viewType == 0 || viewType == 10) {
            linearLayoutCommon.setBackgroundColor(Color.parseColor("#FAFAD2"));
        }else if (viewType == 2 || viewType == 12) {
            linearLayoutCommon.setBackgroundColor(Color.parseColor("#F0FFFF"));
        }else if (viewType == 3 || viewType == 13){
            linearLayoutCommon.setBackgroundColor(Color.parseColor("#D3D3D3"));
        } else if (viewType == 4 || viewType == 14) {
            linearLayoutCommon.setBackgroundColor(Color.parseColor("#A0FAA0"));
        } else if (viewType == 5 || viewType == 15) {
            linearLayoutCommon.setBackgroundColor(Color.parseColor("#F0F8FF"));
        } else if (viewType == 1 || viewType == 11) {
            linearLayoutCommon.setBackgroundColor(Color.parseColor("#FFA07A"));
        } else if (viewType == 6 || viewType == 16) {
            linearLayoutCommon.setBackgroundColor(Color.TRANSPARENT);
        }

    }

    private void defineIndexesOfWords(){

        List <Collocation> listDictionaryLocalCopy = new ArrayList<Collocation>();

        int index = 0;
        for (Collocation collocationCopy : listDictionaryCopy) {
            collocationCopy.index = index;
            listDictionaryLocalCopy.add(new Collocation(
                    collocationCopy.learnedEn,
                    collocationCopy.en,
                    collocationCopy.learnedRu,
                    collocationCopy.ru,
                    collocationCopy.isDifficult,
                    collocationCopy.index
            ));
            index++;
        }

        List<Collocation> listOfStudiedWords = new ArrayList<Collocation>();
        List<Collocation> listOfDifficultWords = new ArrayList<Collocation>();
        List<Collocation> listOfLearnedWords = new ArrayList<Collocation>();

        for (int i = 0; i < listDictionaryLocalCopy.size(); i++) {

            Collocation collocation = listDictionaryLocalCopy.get(i);

            if(collocation.learnedEn != collocation.learnedRu){
                listOfStudiedWords.add(collocation);
                listDictionaryLocalCopy.remove(i);
                i--;
                continue;
            }
            if(collocation.isDifficult){

                listOfDifficultWords.add(collocation);
                listDictionaryLocalCopy.remove(i);
                i--;
                continue;
            }
            if(collocation.learnedEn && collocation.learnedRu){
                listOfLearnedWords.add(collocation);
                listDictionaryLocalCopy.remove(i);
                i--;
                continue;
            }
        }


        int j = 0;
        for (Collocation collocation : listOfStudiedWords) {
            listDictionaryLocalCopy.add(j, collocation);
            j++;
            rowBeginIndexOfLearnedWords = j;
        }

        for (Collocation collocation : listDictionaryLocalCopy) {
            if(collocation.learnedEn == collocation.learnedRu) {
                j++;
            }
            rowBeginIndexOfLearnedWords = j;
        }

        countOfDifficultWords = 0;
        for (Collocation collocation : listOfDifficultWords) {
            countOfDifficultWords++;
            listDictionaryLocalCopy.add(collocation);
            j++;
            rowBeginIndexOfLearnedWords = j;
        }
        countOfLearnedWords = 0;
        List<Collocation> listOfWellLearnedWords = new ArrayList<Collocation>();
        for (Collocation collocation : listOfLearnedWords) {
            countOfLearnedWords++;

            if(j >= rowBeginIndexOfLearnedWords + numberOfBlocks * numberOfCollocationsInABlock){
                listOfWellLearnedWords.add(collocation);
            }else {
                listDictionaryLocalCopy.add(collocation);
                j++;
                rowBeginIndexOfWellLearnedWords = j;
            }
        }
        rowBeginIndexOfNativeWords = rowBeginIndexOfWellLearnedWords + numberOfBlocks * numberOfCollocationsInABlock;

        if (rowBeginIndexOfWellLearnedWords == 0 || rowBeginIndexOfLearnedWords == listDictionaryCopy.size()) {
            rowBeginIndexOfWellLearnedWords = listDictionaryCopy.size();
            rowBeginIndexOfLearnedWords  = listDictionaryCopy.size();
            rowBeginIndexOfNativeWords = listDictionaryCopy.size();
        }

    }

    public void sync(){

        if(indexOfCurrentReceiver >= COUNT_OF_RECEIVERS){
            indexOfCurrentReceiver = 0;
        }
        indexOfPreviousReceiver = indexOfCurrentReceiver - 1;
        if(indexOfPreviousReceiver < 0){
            indexOfPreviousReceiver = COUNT_OF_RECEIVERS - 1;
        }

        try {

            /*
            Receiver rps = new Receiver(editTextHostname.getText().toString(),
                    Integer.parseInt(editTextPortname.getText().toString()));
            */
            rp = receiverList.get(indexOfCurrentReceiver);
            receiverList.get(indexOfPreviousReceiver).interrupt();

            rp.start();
            indexOfCurrentReceiver++;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Log.e(TAG, sw.toString());

            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                    "Failed synchronization",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.show();
        }


    }

    public void fillReceiverList(String hostname, String portname){

        receiverList.clear();
        for (int i = 0; i < COUNT_OF_RECEIVERS; i++) {
            receiverList.add( new Receiver(hostname, Integer.parseInt(portname)) );
        }

    }

    private void playNextPoint() {

        playedNextPoint = true;

        MediaPlayer mPlayer = MediaPlayer.create(getActivity(), R.raw.next_point);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.release();
            }
        });
        mPlayer.start();

    }

    public Character[] createArrayOfCharactersByLine(String line){

        Character[] arrayOfCharacters = new Character[line.length()];
        for (int i = 0; i < line.length(); i++) {
            arrayOfCharacters[i] = line.charAt(i);
        }

        return arrayOfCharacters;
    }

    public StateMap[] createStateMap(String original, String answer) {

        Character[] x = createArrayOfCharactersByLine(original);
        Character[] y = createArrayOfCharactersByLine(answer);

        int m = x.length;
        int n = y.length;
        int[][] len = new int[m + 1][n + 1];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (x[i] == y[j]) {
                    len[i + 1][j + 1] = len[i][j] + 1;
                } else {
                    len[i + 1][j + 1] = Math.max(len[i + 1][j], len[i][j + 1]);
                }
            }
        }
        int cnt = len[m][n];
        StateMap[] res = new StateMap[cnt];
        for (int i = m - 1, j = n - 1; i >= 0 && j >= 0;) {
            if (x[i] == y[j]) {
                res[--cnt] = new StateMap(x[i], i, j);
                --i;
                --j;
            } else if (len[i + 1][j] > len[i][j + 1]) {
                --j;
            } else {
                --i;
            }
        }

        return res;
    }

    public void goYouGlish(){

        mWebView.clearCache(true);

        String word = "";

        Collocation collocationCopy = listDictionaryCopy.get(indexOfTheSelectedRow);
        if (englishLeft) {
            word = collocationCopy.ru;
        } else {
            word = collocationCopy.en;
        }
        word = word.replace("✓", "")
                .replace("⚓", "");

        Character Symbol = word.charAt(0);
        boolean EnglishLayout = false;//engList.indexOf(Symbol) != -1;
        boolean RussianLayout = false;//rusList.indexOf(Symbol) != -1;

        for (int i = 0; i < ArrayEnglishCharacters.length; i++) {
            if (ArrayEnglishCharacters[i] == Symbol) {
                EnglishLayout = true;
            }
        }

        for (int i = 0; i < ArrayRussianCharacters.length; i++) {
            if (ArrayRussianCharacters[i] == Symbol) {
                RussianLayout = true;
            }
        }

        if (EnglishLayout != RussianLayout) {
            EnglishTextLayout = EnglishLayout;
        }

        String text = "https://translate.google.com/?hl=ru#en/ru/" + word;
        //String text = "https://context.reverso.net/%D0%BF%D0%B5%D1%80%D0%B5%D0%B2%D0%BE%D0%B4/%D0%B0%D0%BD%D0%B3%D0%BB%D0%B8%D0%B9%D1%81%D0%BA%D0%B8%D0%B9-%D1%80%D1%83%D1%81%D1%81%D0%BA%D0%B8%D0%B9/" + word;
        if (EnglishTextLayout) {
            text = "https://youglish.com/pronounce/"+word+"/english";
            //text = "https://context.reverso.net/%D0%BF%D0%B5%D1%80%D0%B5%D0%B2%D0%BE%D0%B4/%D0%B0%D0%BD%D0%B3%D0%BB%D0%B8%D0%B9%D1%81%D0%BA%D0%B8%D0%B9-%D1%80%D1%83%D1%81%D1%81%D0%BA%D0%B8%D0%B9/" + word;
        } else {
            text = "https://youglish.com/pronounce/"+word+"/russian";
        }


        List<android.support.v4.app.Fragment> fragments = getFragmentManager().getFragments();
        frag1 = fragments.get(0);
        frag2 = fragments.get(1);
        mWebView = frag2.getView().findViewById(R.id.webView);
        mWebView.loadUrl(text);


        Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                "Go YouGlish",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();

        textForViewing = true;
        searchView.setQuery(word, false);

    }
    public void goGoogle(){

        mWebView.clearCache(true);

        String word = "";

        Collocation collocationCopy = listDictionaryCopy.get(indexOfTheSelectedRow);
        if (englishLeft) {
            word = collocationCopy.ru;
        } else {
            word = collocationCopy.en;
        }
        word = word.replace("✓", "")
                .replace("⚓", "");

        Character Symbol = word.charAt(0);
        boolean EnglishLayout = false;//engList.indexOf(Symbol) != -1;
        boolean RussianLayout = false;//rusList.indexOf(Symbol) != -1;

        for (int i = 0; i < ArrayEnglishCharacters.length; i++) {
            if (ArrayEnglishCharacters[i] == Symbol) {
                EnglishLayout = true;
            }
        }

        for (int i = 0; i < ArrayRussianCharacters.length; i++) {
            if (ArrayRussianCharacters[i] == Symbol) {
                RussianLayout = true;
            }
        }

        if (EnglishLayout != RussianLayout) {
            EnglishTextLayout = EnglishLayout;
        }

        String text = "https://translate.google.com/?hl=ru#en/ru/" + word;
        //String text = "https://context.reverso.net/%D0%BF%D0%B5%D1%80%D0%B5%D0%B2%D0%BE%D0%B4/%D0%B0%D0%BD%D0%B3%D0%BB%D0%B8%D0%B9%D1%81%D0%BA%D0%B8%D0%B9-%D1%80%D1%83%D1%81%D1%81%D0%BA%D0%B8%D0%B9/" + word;
        text = "https://www.google.com/search?q="+word;


        List<android.support.v4.app.Fragment> fragments = getFragmentManager().getFragments();
        frag1 = fragments.get(0);
        frag2 = fragments.get(1);
        mWebView = frag2.getView().findViewById(R.id.webView);
        mWebView.loadUrl(text);


        Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                "Go Google",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();

        textForViewing = true;
        searchView.setQuery(word, false);

    }

    public void voiceMode(){
        textToSpeechSystemCls.voiceMode();
    }

    class StateMap {

        Character unit;
        int indexX;
        int indexY;

        public StateMap(Character unit, int indexX, int indexY) {
            this.unit = unit;
            this.indexX = indexX;
            this.indexY = indexY;
        }
    }

    class Receiver extends Thread {

        String host;
        int port;

        public Receiver(String hostname, int portname) {
            host = hostname;
            port = portname;
        }

        public void run() {

            defineIndexesOfWords();

            try {
                InetAddress ipAddr = InetAddress.getByName(host);

                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ipAddr, port), 5000);
                InputStream sin = socket.getInputStream();
                OutputStream sout = socket.getOutputStream();

                DataInputStream in = new DataInputStream(sin);
                DataOutputStream out = new DataOutputStream(sout);

                ObjectOutputStream oos = new ObjectOutputStream(sout);
                ObjectInputStream ois = new ObjectInputStream(sin);

                out.writeBoolean(swap);
                out.writeInt(countOfLearnedWords);
                out.flush(); // заставляем поток закончить передачу данных.

                String line = in.readUTF();
                if ( line.equals("unloading") ){
                    out.writeInt(numberOfBlocks);
                    out.writeInt(numberOfCollocationsInABlock);

                    String jsonStr = new Gson().toJson(listDictionaryCopy);
                    oos.writeObject(jsonStr);
                    oos.flush();

                    // Get a handler that can be used to post to the main thread
                    Handler mainHandler = new Handler(getContext().getMainLooper());
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                    "Synchronization complete",
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 0);
                            toast.show();

                            swap = false;
                            buttonSwap.getBackground().clearColorFilter();
                        }
                    };
                    mainHandler.post(runnable);

                }else if ( line.equals("loading") ){
                    numberOfBlocks = in.readInt();
                    numberOfCollocationsInABlock = in.readInt();

                    String jsonStr  = (String) ois.readObject();//listDictionary = (ArrayList<Collocation>) ois.readObject();

                    JsonParser parser = new JsonParser();
                    Gson gson = new Gson();
                    JsonArray array = parser.parse(jsonStr).getAsJsonArray();
                    listDictionary.clear();
                    listDictionaryCopy.clear();
                    for (int i = 0; i < array.size(); i++) {
                        Collocation collocation = (gson.fromJson(array.get(i), Collocation.class));
                        listDictionary.add(collocation);
                        listDictionaryCopy.add(new Collocation(
                                collocation.learnedEn,
                                collocation.en,
                                collocation.learnedRu,
                                collocation.ru,
                                collocation.isDifficult,
                                i
                        ));
                    }

                    // Get a handler that can be used to post to the main thread
                    Handler mainHandler = new Handler(getContext().getMainLooper());
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {

                            if (answersWereHidden){
                                hideAnswers();
                            }
                            defineIndexesOfWords();
                            adapter.notifyDataSetChanged();

                            tvProgressBar.setMax(listDictionaryCopy.size());
                            tvProgressBar.setProgress(countOfLearnedWords);

                            editTextNumberOfBlocks.setText(Integer.toString(numberOfBlocks));
                            editTextNumberOfCollocationsInABlock.setText(Integer.toString(numberOfCollocationsInABlock));

                            tvTextLearned.setText(Integer.toString(countOfLearnedWords));
                            tvTextOnRepetition.setText(Integer.toString(countOfDifficultWords));
                            tvTextLeft.setText(Integer.toString(listDictionaryCopy.size() - countOfLearnedWords));
                            tvTextTotal.setText(Integer.toString(listDictionaryCopy.size()));

                            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                    "Synchronization complete",
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 0);
                            toast.show();
                        }
                    };
                    mainHandler.post(runnable);

                }

                socket.close();

            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                Log.e(TAG, sw.toString());

                // Get a handler that can be used to post to the main thread
                Handler mainHandler = new Handler(getContext().getMainLooper());
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();

                        Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                "Failed synchronization",
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.BOTTOM, 0, 0);
                        toast.show();
                    }
                };
                mainHandler.post(runnable);



            }



        }
    }



    /**
     * Класс адаптера наследуется от RecyclerView.Adapter с указанием класса,
     * который будет хранить ссылки на виджеты элемента списка, т.е. класса,
     * имплементирующего ViewHolder. В нашем случае класс объявлен внутри класса адаптера.
     */
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>
            implements View.OnFocusChangeListener, View.OnLongClickListener {

        private List <Collocation> listDictionary;
        boolean pairedMark;
        boolean userInput = true;

        public RecyclerViewAdapter(List <Collocation> listDictionary) {
            this.listDictionary = listDictionary;
        }

        public void filter(String text) {

            listDictionary.clear();
            if(text.isEmpty()){
                for (Collocation collocationCopy : listDictionaryCopy) {
                    listDictionary.add(new Collocation(
                            collocationCopy.learnedEn,
                            collocationCopy.en,
                            collocationCopy.learnedRu,
                            collocationCopy.ru,
                            collocationCopy.isDifficult,
                            collocationCopy.index
                    ));
                }
                defineIndexesOfWords();
                if (answersWereHidden){
                    hideAnswers();
                }else {
                    notifyDataSetChanged();
                }
                recyclerView.scrollToPosition(indexOfTheSelectedRow);
            } else{
                text = text.toLowerCase();
                for (Collocation collocationCopy : listDictionaryCopy) {
                    if(collocationCopy.en.toLowerCase().contains(text) || collocationCopy.ru.toLowerCase().contains(text)) {
                        listDictionary.add(new Collocation(
                                collocationCopy.learnedEn,
                                collocationCopy.en,
                                collocationCopy.learnedRu,
                                collocationCopy.ru,
                                collocationCopy.isDifficult,
                                collocationCopy.index
                        ));
                    }
                }
            }
            notifyDataSetChanged();
        }

        /**
         * Создание новых View и ViewHolder элемента списка, которые впоследствии могут переиспользоваться.
         */
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View itemView;

            if(viewType < 10) {
                itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_en_ru, viewGroup, false);
                colorizeLines(itemView, viewType);
            }else{
                itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_ru_en, viewGroup, false);
                colorizeLines(itemView, viewType);
            }

            return new ViewHolder(itemView);
        }


        /**
         * Заполнение виджетов View данными из элемента списка с номером i
         */
        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, int i) {

            final Collocation collocation = listDictionary.get(i);
            final Collocation collocationCopy = listDictionaryCopy.get(collocation.index);

            userInput = false;
            pairedMark = true;
            viewHolder.checkBoxLearnedEn.setChecked(collocation.learnedEn);
            userInput = true;
            pairedMark = false;
            viewHolder.editTextEnWord.setText(collocation.en);
            viewHolder.editTextEnWord.setTag(R.id.Tag1, collocation.index);
            viewHolder.editTextEnWord.setTag(R.id.Tag2, i);

            userInput = false;
            pairedMark = true;
            viewHolder.checkBoxLearnedRu.setChecked(collocation.learnedRu);
            userInput = true;
            pairedMark = false;
            viewHolder.editTextRuWord.setText(collocation.ru);
            viewHolder.editTextRuWord.setTag(R.id.Tag1, collocation.index);
            viewHolder.editTextRuWord.setTag(R.id.Tag2, i);

            viewHolder.checkBoxLearnedEn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (userInput) {
                        collocation.learnedEn = isChecked;
                        collocationCopy.learnedEn = isChecked;

                        if (!pairedMark) {

                            if (!englishLeft && isChecked) {
                                pairedMark = true;
                                viewHolder.checkBoxLearnedRu.setChecked(isChecked);
                            }
                            if (englishLeft && !isChecked) {
                                pairedMark = true;
                                viewHolder.checkBoxLearnedRu.setChecked(isChecked);
                            }

                        }

                        if(collocationCopy.learnedEn && collocationCopy.learnedRu){
                            if (englishLeft) {
                                viewHolder.editTextRuWord.setText(collocationCopy.ru);
                                if (!pairedMark){
                                    tvProgressBar.setProgress(++countOfLearnedWords);

                                    tvTextLearned.setText(Integer.toString(countOfLearnedWords));
                                    tvTextLeft.setText(Integer.toString(listDictionaryCopy.size() - countOfLearnedWords));
                                    if (countOfLearnedWords % 1000 == 0 && !playedNextPoint) {
                                        playNextPoint();
                                    }
                                }
                            } else {
                                viewHolder.editTextEnWord.setText(collocationCopy.en);
                                tvProgressBar.setProgress(++countOfLearnedWords);

                                tvTextLearned.setText(Integer.toString(countOfLearnedWords));
                                tvTextLeft.setText(Integer.toString(listDictionaryCopy.size() - countOfLearnedWords));
                                if (countOfLearnedWords % 1000 == 0 && !playedNextPoint) {
                                    playNextPoint();
                                }
                            }
                        }else if(!collocationCopy.learnedEn && !collocationCopy.learnedRu){
                            if (englishLeft) {
                                if (answersWereHidden) viewHolder.editTextRuWord.setText("");
                                if(!pairedMark){
                                    tvProgressBar.setProgress(--countOfLearnedWords);

                                    tvTextLearned.setText(Integer.toString(countOfLearnedWords));
                                    tvTextLeft.setText(Integer.toString(listDictionaryCopy.size() - countOfLearnedWords));
                                }
                            } else {
                                if (answersWereHidden) viewHolder.editTextEnWord.setText("");
                            }
                        }else {
                            if (englishLeft) {
                                if (answersWereHidden) viewHolder.editTextRuWord.setText("");
                            } else {
                                if (answersWereHidden) viewHolder.editTextEnWord.setText("");
                                tvProgressBar.setProgress(--countOfLearnedWords);

                                tvTextLearned.setText(Integer.toString(countOfLearnedWords));
                                tvTextLeft.setText(Integer.toString(listDictionaryCopy.size() - countOfLearnedWords));
                            }
                        }
                        pairedMark = false;


                        int i = 0;
                        for(Collocation colloc: listDictionary){
                            if (colloc.learnedEn != colloc.learnedRu){
                                i++;
                            }
                        }
                        tvProgressBar.setMax(numberOfCollocationsInABlock);
                        tvProgressBar.setProgress(i);


                    }
                    userInput = true;

                }

            });

            viewHolder.checkBoxLearnedRu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (userInput) {
                        collocation.learnedRu = isChecked;
                        collocationCopy.learnedRu = isChecked;

                        if (!pairedMark) {

                            if (englishLeft && isChecked) {
                                pairedMark = true;
                                viewHolder.checkBoxLearnedEn.setChecked(isChecked);
                            }
                            if (!englishLeft && !isChecked) {
                                pairedMark = true;
                                viewHolder.checkBoxLearnedEn.setChecked(isChecked);
                            }
                        }

                        if(collocationCopy.learnedEn && collocationCopy.learnedRu){
                            if (englishLeft) {
                                viewHolder.editTextRuWord.setText(collocationCopy.ru);
                                tvProgressBar.setProgress(++countOfLearnedWords);

                                tvTextLearned.setText(Integer.toString(countOfLearnedWords));
                                tvTextLeft.setText(Integer.toString(listDictionaryCopy.size() - countOfLearnedWords));
                                if (countOfLearnedWords % 1000 == 0 && !playedNextPoint) {
                                    playNextPoint();
                                }
                            } else {
                                viewHolder.editTextEnWord.setText(collocationCopy.en);
                                if (!pairedMark){
                                    tvProgressBar.setProgress(++countOfLearnedWords);

                                    tvTextLearned.setText(Integer.toString(countOfLearnedWords));
                                    tvTextLeft.setText(Integer.toString(listDictionaryCopy.size() - countOfLearnedWords));
                                    if (countOfLearnedWords % 1000 == 0 && !playedNextPoint) {
                                        playNextPoint();
                                    }
                                }
                            }
                        }else if(!collocationCopy.learnedEn && !collocationCopy.learnedRu){
                            if (englishLeft) {
                                if (answersWereHidden) viewHolder.editTextRuWord.setText("");
                            } else {
                                if (answersWereHidden) viewHolder.editTextEnWord.setText("");
                                if(!pairedMark){
                                    tvProgressBar.setProgress(--countOfLearnedWords);

                                    tvTextLearned.setText(Integer.toString(countOfLearnedWords));
                                    tvTextLeft.setText(Integer.toString(listDictionaryCopy.size() - countOfLearnedWords));
                                }
                            }
                        }else {
                            if (englishLeft) {
                                if (answersWereHidden) viewHolder.editTextRuWord.setText("");
                                tvProgressBar.setProgress(--countOfLearnedWords);

                                tvTextLearned.setText(Integer.toString(countOfLearnedWords));
                                tvTextLeft.setText(Integer.toString(listDictionaryCopy.size() - countOfLearnedWords));
                            } else {
                                if (answersWereHidden) viewHolder.editTextEnWord.setText("");
                            }
                        }
                        pairedMark = false;


                        int i = 0;
                        for(Collocation colloc: listDictionary){
                            if (colloc.learnedEn != colloc.learnedRu){
                                i++;
                            }
                        }
                        tvProgressBar.setMax(numberOfCollocationsInABlock);
                        tvProgressBar.setProgress(i);


                    }
                    userInput = true;
                }
            });


            viewHolder.editTextEnWord.setOnFocusChangeListener(this);
            viewHolder.editTextRuWord.setOnFocusChangeListener(this);

            viewHolder.editTextEnWord.setOnLongClickListener(this);
            viewHolder.editTextRuWord.setOnLongClickListener(this);

        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Override
        public int getItemCount() {
            return listDictionary.size();
        }

        @Override
        public int getItemViewType(int position) {

            childPosition = position;
            return defineViewType(position);
            //super.getItemViewType(position);

        }


        @Override
        public void onFocusChange(View v, boolean hasFocus) {

            indexOfTheSelectedRow = Integer.valueOf(v.getTag(R.id.Tag1).toString());
            indexOfTheFilteredSelectedRow = Integer.valueOf(v.getTag(R.id.Tag2).toString());

            if (!hasFocus) {

                if(!afterPressEnter && indexOfTheSelectedRow != -1 && !collocationRemoved) {

                    Collocation collocationCopy = listDictionaryCopy.get(indexOfTheSelectedRow);

                    String original;
                    if (englishLeft) {
                        original = collocationCopy.ru;
                    } else {
                        original = collocationCopy.en
                                .replace("✓", "")
                                .replace("⚓", "");
                    }
                    String answer = ((EditText) v).getText().toString();
                    String[] arrayAnswer = answer.split("\n");
                    if (arrayAnswer.length > 1){
                        original = arrayAnswer[0];
                    }
                    String resultText = original;

                    if (collocationCopy.isDifficult && !englishLeft) {
                        resultText = original + "⚓";
                    } else if (!englishLeft) {
                        resultText = original + "✓";
                    }

                    if (englishLeft) {
                        collocationCopy.ru = resultText;
                    } else {
                        collocationCopy.en = resultText;
                    }

                    if (answersWereHidden) {
                        ((EditText) v).setText("");
                    } else {
                        ((EditText) v).setText(resultText);
                    }
                }

                collocationRemoved = false;

            }else{

                if(afterPressEnter) {
                    afterPressEnter = false;
                    ((EditText) v).setText(text);
                }
            }

        }



        @Override
        public boolean onLongClick(View v) {

            String word = ((EditText) v). getText().toString();
            //String word = "";

            //Collocation collocationCopy = null;
            indexOfTheSelectedRow = Integer.valueOf(v.getTag(R.id.Tag1).toString());
            Collocation collocationCopy = listDictionaryCopy.get(indexOfTheSelectedRow);
            if (word.isEmpty()) {

                v.requestFocus();

                if (indexOfTheSelectedRow < 0) {
                    return true;
                }

                //collocationCopy = listDictionaryCopy.get(indexOfTheSelectedRow);
                if (englishLeft) {
                    word = collocationCopy.ru;
                } else {
                    word = collocationCopy.en;
                }
            }

            if (word.isEmpty()) {
                return true;
            }

            word = word.replace("✓", "")
                    .replace("⚓", "");

            Character Symbol = word.charAt(0);
            boolean EnglishLayout = false;//engList.indexOf(Symbol) != -1;
            boolean RussianLayout = false;//rusList.indexOf(Symbol) != -1;

            for (int i = 0; i < ArrayEnglishCharacters.length; i++) {
                if (ArrayEnglishCharacters[i] == Symbol) {
                    EnglishLayout = true;
                }
            }

            for (int i = 0; i < ArrayRussianCharacters.length; i++) {
                if (ArrayRussianCharacters[i] == Symbol) {
                    RussianLayout = true;
                }
            }

            if (EnglishLayout != RussianLayout) {
                EnglishTextLayout = EnglishLayout;
            }

            String text = "https://translate.google.com/?hl=ru#en/ru/" + word;
            //String text = "https://context.reverso.net/%D0%BF%D0%B5%D1%80%D0%B5%D0%B2%D0%BE%D0%B4/%D0%B0%D0%BD%D0%B3%D0%BB%D0%B8%D0%B9%D1%81%D0%BA%D0%B8%D0%B9-%D1%80%D1%83%D1%81%D1%81%D0%BA%D0%B8%D0%B9/" + word;
            if (EnglishTextLayout) {
                text = "https://translate.google.com/?hl=ru#en/ru/" + word;
                //text = "https://context.reverso.net/%D0%BF%D0%B5%D1%80%D0%B5%D0%B2%D0%BE%D0%B4/%D0%B0%D0%BD%D0%B3%D0%BB%D0%B8%D0%B9%D1%81%D0%BA%D0%B8%D0%B9-%D1%80%D1%83%D1%81%D1%81%D0%BA%D0%B8%D0%B9/" + word;
            } else {
                text = "https://translate.google.com/?hl=ru#ru/en/" + word;
            }


            if (!swap) {
                List<android.support.v4.app.Fragment> fragments = getFragmentManager().getFragments();
                frag1 = fragments.get(0);
                frag2 = fragments.get(1);
                mWebView = frag2.getView().findViewById(R.id.webView);
                mWebView.loadUrl(text);

                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                        "Попытка перевода",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
            }else {
                swap = false;
                buttonSwap.getBackground().clearColorFilter();
            }

            textForViewing = true;
            searchView.setQuery(word, false);

            if(englishLeft) {
                TService.sendNotify(collocationCopy.en + "~" + collocationCopy.ru, collocationCopy);
            }else {
                TService.sendNotify(collocationCopy.ru + "~" + collocationCopy.en, collocationCopy);
            }

            indexOfThePreviousSelectedRow = indexOfTheSelectedRow;
            ((PageFragment)  frag1).adapter.notifyDataSetChanged();

            return false;
        }




        /**
         * Реализация класса ViewHolder, хранящего ссылки на виджеты.
         */
        class ViewHolder extends RecyclerView.ViewHolder implements TextView.OnEditorActionListener {
            private final CheckBox checkBoxLearnedEn;
            private final EditText editTextEnWord;
            private final CheckBox checkBoxLearnedRu;
            private final EditText editTextRuWord;

            public ViewHolder(View itemView) {
                super(itemView);

                checkBoxLearnedEn = itemView.findViewById(R.id.checkBoxLearnedEn);
                editTextEnWord = itemView.findViewById(R.id.editTextEnWord);
                checkBoxLearnedRu = itemView.findViewById(R.id.checkBoxLearnedRu);
                editTextRuWord = itemView.findViewById(R.id.editTextRuWord);

                editTextEnWord.setOnEditorActionListener(this);
                editTextRuWord.setOnEditorActionListener(this);

                editTextEnWord.setImeOptions(EditorInfo.IME_ACTION_GO);
                //editTextEnWord.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_FILTER | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                //editTextEnWord.setPrivateImeOptions("nm");
                //editTextEnWord.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

                editTextEnWord.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                //editTextEnWord.setRawInputType(editTextEnWord.getInputType());;
//
//                if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M){
//                }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    editTextEnWord.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
//                }

                //editTextEnWord.setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);


                editTextRuWord.setImeOptions(EditorInfo.IME_ACTION_GO);
                editTextRuWord.setRawInputType(InputType.TYPE_CLASS_TEXT);
            }

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_GO) {

                    String original, answer;

                    Collocation collocation =  listDictionary.get(indexOfTheFilteredSelectedRow);
                    Collocation collocationCopy =  listDictionaryCopy.get(indexOfTheSelectedRow);
                    ////
                    Handler mainHandler = new Handler(getContext().getMainLooper());
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {

                            Collocation collocationCopy =  listDictionaryCopy.get(indexOfTheSelectedRow);
                            textToSpeechSystemCls.setLanguage(Locale.US);
                            textToSpeechSystemCls.speak(collocationCopy.en
                                    .replace("✓", "")
                                    .replace("⚓", ""));

                        }
                    };
                    mainHandler.post(runnable);

                    ////
                    if (englishLeft) {
                        original = collocationCopy.ru;
                    } else {
                        original = collocationCopy.en
                                .replace("✓", "")
                                .replace("⚓", "");
                    }
                    answer = ((EditText) v).getText().toString();
                    String[] arrayAnswer = answer.split("\n");
                    if (arrayAnswer.length > 1){
                        answer = arrayAnswer[arrayAnswer.length - 1];
                    }

                    String comparison = original;
                    if(!answer.isEmpty()){
                        comparison = original + "\n"  + answer;
                    }

                    text = new SpannableString(comparison);

                    for (int i = 0; i < comparison.length(); i++) {
                        if (i <= original.length()){
                            ForegroundColorSpan style = new ForegroundColorSpan(Color.BLUE);
                            text.setSpan(style, i, i + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        }else{
                            ForegroundColorSpan style = new ForegroundColorSpan(Color.RED);
                            text.setSpan(style, i, i + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        }

                    }

                    StateMap[] stateMap = createStateMap(original, answer);

                    int s = 0;
                    for (int i = 0; i < comparison.length(); i++) {
                        if(i <= original.length()){
                            for (int j = 0; j < stateMap.length; j++) {
                                if(stateMap[j].unit.equals(comparison.charAt(i)) && stateMap[j].indexX == i){
                                    ForegroundColorSpan style = new ForegroundColorSpan(Color.GRAY);
                                    text.setSpan(style, i, i + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    break;
                                }
                            }
                        }else{
                            for (int j = 0; j < stateMap.length; j++) {
                                if(stateMap[j].unit.equals(comparison.charAt(i)) && stateMap[j].indexY == s){
                                    ForegroundColorSpan style = new ForegroundColorSpan(Color.rgb(0, 128, 0));
                                    text.setSpan(style, i, i + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    break;
                                }
                            }
                            s++;
                        }

                    }

                    boolean isDifficultTemp = collocationCopy.isDifficult;

                    if(!englishLeft) {
                        if (answer.equals(original)) {
                            collocation.isDifficult = false;
                            collocationCopy.isDifficult = false;
                            if (isDifficultTemp){
                                tvTextOnRepetition.setText(Integer.toString(--countOfDifficultWords));
                            }
                        } else {
                            collocation.isDifficult = true;
                            collocationCopy.isDifficult = true;
                            if (!isDifficultTemp){
                                tvTextOnRepetition.setText(Integer.toString(++countOfDifficultWords));
                            }
                        }
                    }

                    //afterPressEnter = true;
                    //adapter.notifyItemChanged(indexOfTheSelectedRow);

                    if(indexOfTheSelectedRow == indexOfThePreviousSelectedRow
                            || isDifficultTemp != collocation.isDifficult) {//text is set to onFocusChange
                        afterPressEnter = true;
                        if(listDictionaryCopy.size() == listDictionary.size()){
                            adapter.notifyItemChanged(indexOfTheSelectedRow);
                        }else{
                            adapter.notifyItemChanged(indexOfTheFilteredSelectedRow);
                        }
                    }else{
                        ((EditText) v).setText(text);
                    }

                    if(indexOfTheSelectedRow != indexOfTheTempPreviousSelectedRow) {
                        indexOfThePreviousSelectedRow = indexOfTheTempPreviousSelectedRow;
                    }
                    indexOfTheTempPreviousSelectedRow = indexOfTheSelectedRow;

                    //скрываем клавиатуру по окончании ввода шилдт ООП.интуит.ру
//                    Activity activity = getActivity();
//                    View view = new View(activity);
                    inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);


                    return true;


                }
                return false;
            }


        }


    }

    class MyLinearLayoutManager extends LinearLayoutManager{

        public MyLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public int computeVerticalScrollExtent(RecyclerView.State state) {
            return super.computeVerticalScrollExtent(state);
        }

        @Override
        public int computeVerticalScrollOffset(RecyclerView.State state) {
            return (int) (super.computeVerticalScrollOffset(state) / 50);//23.5f);
        }

        @Override
        public int computeVerticalScrollRange(RecyclerView.State state) {
            return (int) (super.computeVerticalScrollRange(state) / 50);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            final LinearSmoothScroller linearSmoothScroller =
                    new LinearSmoothScroller(recyclerView.getContext()) {
                        //private static final float MILLISECONDS_PER_INCH = 10000f;//100f

                        @Override
                        public PointF computeScrollVectorForPosition(int targetPosition) {
                            return MyLinearLayoutManager.this
                                    .computeScrollVectorForPosition(targetPosition);
                        }

                        @Override
                        protected float calculateSpeedPerPixel
                                (DisplayMetrics displayMetrics) {
                            return millisecondsPerInch / displayMetrics.densityDpi;
                        }
                    };
            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }


    }
}