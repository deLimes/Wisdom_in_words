package com.example.delimes.words_teacher;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
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

import static android.os.Environment.MEDIA_MOUNTED;
import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;

public class PageFragment extends android.support.v4.app.Fragment {

    InputMethodManager inputMethodManager;
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

    View buttonSwap;

    private boolean EnglishTextLayout = false;
    char[] ArrayEnglishCharacters = {'h', 'j', 'k', 'l', 'y', 'u', 'i', 'o', 'p', '[', ']', 'n', 'm',
            'g', 'f', 'd', 's', 'a', 'b', 'v', 'c', 'x', 'z', 't', 'r', 'e', 'w', 'q', '`'};
    List engList = Arrays.asList(ArrayEnglishCharacters);

    char[] ArrayRussianCharacters = {'р', 'о', 'л', 'д', 'ж', 'э', 'н', 'г', 'ш', 'щ', 'з', 'х', 'ъ', 'т', 'ь', 'б', 'ю',
            'п', 'а', 'в', 'ы', 'ф', 'и', 'м', 'с', 'ч', 'я', 'е', 'к', 'у', 'ц', 'й', 'ё'};
    List rusList = Arrays.asList(ArrayRussianCharacters);


    List <Collocation> listDictionary = new ArrayList<Collocation>();
    List <Collocation> listDictionaryCopy = new ArrayList<Collocation>();

    RecyclerView recyclerView;
    MyLinearLayoutManager myLlm;
    int scrollStepY = 0;
    RecyclerViewAdapter adapter;
    private Boolean englishLeft = true;
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
    boolean swap = false;
    int indexOfTheSelectedRow = 0, indexOfTheFilteredSelectedRow = 0;
    int indexOfThePreviousSelectedRow = -1, indexOfTheTempPreviousSelectedRow = -1;
    boolean isStart,  isResumeAfterStop;
    SpannableString text;
    boolean afterPressEnter = false;
    int childPosition;
    boolean collocationRemoved = false;
    //Socket socket;

    View page, page2;
    private WebView mWebView;

    public void saveListDictionary() {

        try {

            if (!getExternalStorageState().equals(
                    MEDIA_MOUNTED)) {
                Toast.makeText(getActivity(), "SD-карта не доступна: " + getExternalStorageState(), Toast.LENGTH_SHORT).show();
                return;
            }
            // получаем путь к SD
            File sdPath = getExternalStorageDirectory();
            // добавляем свой каталог к пути
            sdPath = new File(sdPath.getAbsolutePath());// + "/mytextfile.txt");
            // создаем каталог
            sdPath.mkdirs();
            // формируем объект File, который содержит путь к файлу
            File sdFile = new File(sdPath, "savedListDictionary");

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
                Toast.makeText(getActivity().getBaseContext(), "File saved: " + sdFile.getAbsolutePath(),
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getActivity().getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            //e.printStackTrace();
            Toast.makeText(getActivity().getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
        android.support.v4.app.Fragment frag2 = fragments.get(1);

        searchView = page.findViewById(R.id.searchView);

        tvProgressBar = (ProgressBar) page.findViewById(R.id.progressBar);
        tvTextLearned = (TextView) page.findViewById(R.id.tvTextLearned);
        tvTextOnRepetition = (TextView) page.findViewById(R.id.tvTextOnRepetition);
        tvTextLeft = (TextView) page.findViewById(R.id.tvTextLeft);
        tvTextTotal = (TextView) page.findViewById(R.id.tvTextTotal);

        editTextNumberOfBlocks = (EditText) page.findViewById(R.id.numberOfBlocks);

        editTextHostname = (EditText) page2.findViewById(R.id.hostname);
        editTextPortname = (EditText) page2.findViewById(R.id.portname);

        searchView.setIconified (false);

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
                    if( millisecondsPerInch < 50 ) numberOfCollocationsInABlock = 50;
                } catch( NumberFormatException nfe ){
                    millisecondsPerInch = backupValue;
                    return true;
                }
                SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
                editPrefs.putFloat("millisecondsPerInch", millisecondsPerInch);
                editPrefs.commit();

                return false;
            }
        });

        //скрыть ответы
        final View buttonHideAnswers = page.findViewById(R.id.buttonHideAnswers);
        buttonHideAnswers.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                hideAnswers();

            }

        });

        //Показать ответы
        final View buttonShowAnswers = page.findViewById(R.id.buttonShowAnswers);
        buttonShowAnswers.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    showAnswers();

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

                    Comparator enRuComparator = new Comparator<Collocation>() {
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

                    SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
                    editPrefs.putInt("countOfLearnedWords", countOfLearnedWords);
                    editPrefs.putInt("countOfDifficultWords", countOfDifficultWords);
                    editPrefs.putInt("countOfLeftWords", listDictionary.size() - countOfLearnedWords);
                    editPrefs.putInt("countOfTotalWords", listDictionary.size());
                    editPrefs.commit();


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
                            tvTextLeft.setText(Integer.toString(listDictionary.size()));
                            tvTextTotal.setText(Integer.toString(listDictionary.size()));

                            tvProgressBar.setMax(listDictionary.size());
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

                        tvProgressBar.setMax(listDictionary.size());

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

                    SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
                    editPrefs.putInt("countOfLearnedWords", countOfLearnedWords);
                    editPrefs.putInt("countOfDifficultWords", countOfDifficultWords);
                    editPrefs.putInt("countOfLeftWords", listDictionary.size() - countOfLearnedWords);
                    editPrefs.putInt("countOfTotalWords", listDictionary.size());
                    editPrefs.commit();

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


                    if(listDictionaryCopy.size() == listDictionary.size()){
                        defineIndexesOfWords();
                    }
                    adapter.notifyDataSetChanged();


                    tvProgressBar.setMax(listDictionary.size());

                    searchView.setQuery("", false);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    int countOfLeftWords = prefs.getInt("countOfLeftWords", 0);

                    countOfLeftWords++;
                    tvTextLeft.setText(Integer.toString(countOfLeftWords));
                    tvTextTotal.setText(Integer.toString(listDictionary.size()));

                    SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();

                    editPrefs.putInt("countOfLeftWords", countOfLeftWords);
                    editPrefs.putInt("countOfTotalWords", listDictionary.size());
                    editPrefs.commit();


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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        englishLeft = prefs.getBoolean("englishLeft", false);
        answersWereHidden = prefs.getBoolean("answersWereHidden", false);
        indexOfTheSelectedRow = prefs.getInt("indexOfTheSelectedRow", 0);

        editTextHostname.setText(prefs.getString("hostname", "192.168.0.1"));
        editTextPortname.setText(prefs.getString("portname", "7373"));

        recyclerView.scrollToPosition(indexOfTheSelectedRow);

        /*
        rp = new Receiver(editTextHostname.getText().toString(),
                Integer.parseInt(editTextPortname.getText().toString()));

        */
        fillReceiverList(editTextHostname.getText().toString(), editTextPortname.getText().toString());
        searchView.clearFocus();
        buttonHideAnswers.requestFocus();

        isStart = true;
        isResumeAfterStop = false;

        return page;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (!swap) {
            save();
        }
        isResumeAfterStop = true;

    }

    @Override
    public void onResume() {
        super.onResume();

        if (isStart || isResumeAfterStop) {
            restore();
        }
        isStart = false;
        isResumeAfterStop = false;
    }

    private void restoreListDictionary(){

        // проверяем доступность SD
        if (!getExternalStorageState().equals(
                MEDIA_MOUNTED)) {
            Toast.makeText(getActivity().getBaseContext(), "SD-карта не доступна: " + getExternalStorageState(), Toast.LENGTH_SHORT).show();
            return;
        }

        // получаем путь к SD
        File sdPath = getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath());// + "/mytextfile.txt");
        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, "savedListDictionary");
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

            //int k = 1/0;//////////del

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
        }

        defineIndexesOfWords();
        tvProgressBar.setMax(listDictionary.size());
        tvProgressBar.setProgress(countOfLearnedWords);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        millisecondsPerInch = prefs.getFloat("millisecondsPerInch", 1000f);
        editTexScrollingSpeed.setText(Integer.toString( (int) millisecondsPerInch));
        /*
        editTextHostname.setText(prefs.getString("hostname", "192.168.0.1"));
        editTextPortname.setText(prefs.getString("portname", "7373"));
        */

        tvTextLearned.setText(Integer.toString( prefs.getInt("countOfLearnedWords", 0) ));
        tvTextOnRepetition.setText(Integer.toString( prefs.getInt("countOfDifficultWords", 0) ));
        tvTextLeft.setText(Integer.toString( prefs.getInt("countOfLeftWords",  listDictionary.size()) ));
        tvTextTotal.setText(Integer.toString( prefs.getInt("countOfTotalWords", listDictionary.size()) ));

    }

    public void resetListDictionary(){//boolean softwareReset

        String[] arrayDictionary = getResources().getStringArray(R.array.dictionary);

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
        tvTextLeft.setText(Integer.toString(listDictionary.size()));
        tvTextTotal.setText(Integer.toString(listDictionary.size()));

        tvProgressBar.setMax(listDictionary.size());
        tvProgressBar.setProgress(countOfLearnedWords);


    }

    public void restore(){

        restoreListDictionary();

        editTextNumberOfBlocks.setText(Integer.toString(numberOfBlocks));
        editTextNumberOfCollocationsInABlock.setText(Integer.toString(numberOfCollocationsInABlock));

        if (answersWereHidden){
            hideAnswers();
        }
    }

   
    public void save(){

        saveListDictionary();

        SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
        editPrefs.putBoolean("englishLeft", englishLeft);
        editPrefs.putBoolean("answersWereHidden", answersWereHidden);
        editPrefs.putInt("indexOfTheSelectedRow", recyclerView.getChildAdapterPosition(recyclerView.getFocusedChild()));
        editPrefs.commit();

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

        /*
        if(listDictionaryCopy.size() == listDictionary.size()) {

            for (int i = 0; i < listDictionaryCopy.size(); i++) {
                Collocation collocationCopy = listDictionaryCopy.get(i);
                Collocation collocation = listDictionary.get(collocationCopy.filtredIndex);

                collocation.ru = collocationCopy.ru;
                collocation.en = collocationCopy.en;

            }
            answersWereHidden = false;
            adapter.notifyDataSetChanged();


        }else{
            /*
            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                    "Clean the filter!",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
            /*
        }
        */

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
        for (Collocation collocation : listDictionary) {
            collocation.index = index;
            listDictionaryCopy.get(index).index = index;;
            listDictionaryLocalCopy.add(new Collocation(
                    collocation.learnedEn,
                    collocation.en,
                    collocation.learnedRu,
                    collocation.ru,
                    collocation.isDifficult,
                    collocation.index
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
                            defineIndexesOfWords();
                            adapter.notifyDataSetChanged();

                            tvProgressBar.setMax(listDictionary.size());
                            tvProgressBar.setProgress(countOfLearnedWords);

                            editTextNumberOfBlocks.setText(Integer.toString(numberOfBlocks));
                            editTextNumberOfCollocationsInABlock.setText(Integer.toString(numberOfCollocationsInABlock));

                            tvTextLearned.setText(Integer.toString(countOfLearnedWords));
                            tvTextOnRepetition.setText(Integer.toString(countOfDifficultWords));
                            tvTextLeft.setText(Integer.toString(listDictionary.size() - countOfLearnedWords));
                            tvTextTotal.setText(Integer.toString(listDictionary.size()));

                            SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
                            editPrefs.putInt("countOfLearnedWords", countOfLearnedWords);
                            editPrefs.putInt("countOfDifficultWords", countOfDifficultWords);
                            editPrefs.putInt("countOfLeftWords", listDictionary.size() - countOfLearnedWords);
                            editPrefs.putInt("countOfTotalWords", listDictionary.size());
                            editPrefs.commit();

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
                                if (!pairedMark) tvProgressBar.setProgress(++countOfLearnedWords);
                            } else {
                                viewHolder.editTextEnWord.setText(collocationCopy.en);
                                tvProgressBar.setProgress(++countOfLearnedWords);
                            }
                        }else if(!collocationCopy.learnedEn && !collocationCopy.learnedRu){
                            if (englishLeft) {
                                if (answersWereHidden) viewHolder.editTextRuWord.setText("");
                                if(!pairedMark) tvProgressBar.setProgress(--countOfLearnedWords);
                            } else {
                                if (answersWereHidden) viewHolder.editTextEnWord.setText("");
                            }
                        }else {
                            if (englishLeft) {
                                if (answersWereHidden) viewHolder.editTextRuWord.setText("");
                            } else {
                                if (answersWereHidden) viewHolder.editTextEnWord.setText("");
                                tvProgressBar.setProgress(--countOfLearnedWords);
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
                            } else {
                                viewHolder.editTextEnWord.setText(collocationCopy.en);
                                if (!pairedMark) tvProgressBar.setProgress(++countOfLearnedWords);
                            }
                        }else if(!collocationCopy.learnedEn && !collocationCopy.learnedRu){
                            if (englishLeft) {
                                if (answersWereHidden) viewHolder.editTextRuWord.setText("");
                            } else {
                                if (answersWereHidden) viewHolder.editTextEnWord.setText("");
                                if(!pairedMark) tvProgressBar.setProgress(--countOfLearnedWords);
                            }
                        }else {
                            if (englishLeft) {
                                if (answersWereHidden) viewHolder.editTextRuWord.setText("");
                                tvProgressBar.setProgress(--countOfLearnedWords);
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

            /*
            if(listDictionaryCopy.size() == listDictionary.size()) {
                indexOfTheSelectedRow = recyclerView.getChildAdapterPosition(recyclerView.getFocusedChild());
            }else{
                return;
            }
            */
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

            String word = ((EditText) v).getText().toString();

            if (word.isEmpty() && listDictionaryCopy.size() == listDictionary.size()) {

                v.requestFocus();

                if (indexOfTheSelectedRow < 0) {
                    return true;
                }

                Collocation collocationCopy = listDictionaryCopy.get(indexOfTheSelectedRow);
                if (englishLeft) {
                    word = collocationCopy.ru;
                } else {
                    word = collocationCopy.en;
                }
            }

            if (word.isEmpty()){
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
            if (EnglishTextLayout) {
                text = "https://translate.google.com/?hl=ru#en/ru/" + word;
            } else {
                text = "https://translate.google.com/?hl=ru#ru/en/" + word;
            }


            List<android.support.v4.app.Fragment> fragments = getFragmentManager().getFragments();
            android.support.v4.app.Fragment frag2 = fragments.get(1);
            mWebView = frag2.getView().findViewById(R.id.webView);
            mWebView.loadUrl(text);

            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                    "Попытка перевода",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();

            textForViewing = true;
            searchView.setQuery(word, false);

            return false;
        }




        /**
         * Реализация класса ViewHolder, хранящего ссылки на виджеты.
         */
        class ViewHolder extends RecyclerView.ViewHolder implements View.OnKeyListener {
            private CheckBox checkBoxLearnedEn;
            private EditText editTextEnWord;
            private CheckBox checkBoxLearnedRu;
            private EditText editTextRuWord;

            public ViewHolder(View itemView) {
                super(itemView);

                checkBoxLearnedEn = itemView.findViewById(R.id.checkBoxLearnedEn);
                editTextEnWord = itemView.findViewById(R.id.editTextEnWord);
                checkBoxLearnedRu = itemView.findViewById(R.id.checkBoxLearnedRu);
                editTextRuWord = itemView.findViewById(R.id.editTextRuWord);

                editTextEnWord.setOnKeyListener(this);
                editTextRuWord.setOnKeyListener(this);
            }

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {

                        /*
                        if(listDictionaryCopy.size() != listDictionary.size()) {
                            return true;
                        }
                        */
                        String original, answer;

                        Collocation collocationCopy =  listDictionaryCopy.get(indexOfTheSelectedRow);
                        Collocation collocation = collocationCopy;

                        if(listDictionaryCopy.size() == listDictionary.size()) {
                            collocation =  listDictionary.get(indexOfTheSelectedRow);
                        }

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
                            comparison = original + "\n" + answer;
                        }
                        text = new SpannableString(comparison);
                        //
                        char falseDoubletLeftCharacter = '⚓';//any unique character
                        char falseDoubletRightCharacter = '⚓';//any unique character
                        char lastLeftCorrectCharacter = '⚓';//any unique character
                        char lastRightCorrectCharacter = '⚓';//any unique character
                        char lastRightOriginalCharacter = '⚓';//any unique character
                        char lastRightPreviousOriginalCharacter = '✓';
                        //

                        for (int i = 0; i < original.length(); i++) {
                            ForegroundColorSpan style = new ForegroundColorSpan(Color.BLUE);
                            text.setSpan(style, i, i + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        }

                        int j = 0;
                        for (int i = original.length() + 1; i < comparison.length(); i++) {

                            ForegroundColorSpan style = new ForegroundColorSpan(Color.GRAY);
                            if(j < original.length()) {
                                text.setSpan(style, j, j + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                            }

                            if (j >= original.length() || ( original.charAt(j) != answer.charAt(j))) {//if (j >= original.length() || ( original.charAt(j) != answerCharAtJ)) {

                                style = new ForegroundColorSpan(Color.RED);
                                text.setSpan(style, i, i + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                //
                                if (falseDoubletLeftCharacter == '⚓') {
                                    falseDoubletLeftCharacter = answer.charAt(j);
                                }
                                //

                                if (j <= original.length()) {

                                    style = new ForegroundColorSpan(Color.BLUE);
                                    text.setSpan(style, j, j + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                }

                            } else {

                                style = new ForegroundColorSpan(Color.rgb(0, 128, 0));
                                text.setSpan(style, i, i + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                //
                                falseDoubletLeftCharacter = '⚓';
                                lastLeftCorrectCharacter = answer.charAt(j);
                                //
                            }
                            j++;
                        }


                        j = answer.length() - 1;
                        for (int i = comparison.length(); i >= 0; i--) {

                            boolean charactersEqual;

                            if (j >= answer.length() - original.length() && answer.length() >= original.length()) {
                                charactersEqual = original.charAt(j - (answer.length() - original.length())) == answer.charAt(j);

                                int pos = original.length() + 1 + j;
                                ForegroundColorSpan[] spans = text.getSpans(pos, pos + 1,  ForegroundColorSpan.class);
                                if(spans[0].getForegroundColor() == Color.RED && charactersEqual){
                                    text.removeSpan(spans[0]);

                                    ForegroundColorSpan style = new ForegroundColorSpan(Color.rgb(0, 128, 0));
                                    text.setSpan(style, pos, pos + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    //
                                    lastRightCorrectCharacter = answer.charAt(j);
                                    //
                                    ///////////////
                                    falseDoubletRightCharacter = '⚓';
                                }else if(j >= 0){
                                    if (falseDoubletRightCharacter == '⚓') {
                                        falseDoubletRightCharacter = answer.charAt(j);
                                    }
                                }
                                ///////////////

                            } else if (i < original.length() && answer.length() < original.length() && i >= original.length() - answer.length()) {
                                charactersEqual = original.charAt(i) == answer.charAt(i - (original.length() - answer.length()));

                                int pos = original.length() + 1 + (i - (original.length() - answer.length()));
                                ForegroundColorSpan[] spans = text.getSpans(pos, pos + 1,  ForegroundColorSpan.class);
                                if(spans[0].getForegroundColor() == Color.RED && charactersEqual){
                                    text.removeSpan(spans[0]);

                                    ForegroundColorSpan style = new ForegroundColorSpan(Color.rgb(0, 128, 0));
                                    text.setSpan(style, pos, pos + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    //
                                    lastRightCorrectCharacter = answer.charAt(i - (original.length() - answer.length()));
                                    //
                                    ///////////////
                                     falseDoubletRightCharacter = '⚓';
                                }else if(j >= 0){
                                    if (falseDoubletRightCharacter == '⚓') {
                                        falseDoubletRightCharacter = answer.charAt(j);
                                    }
                                }
                                ///////////////
                            }

                            //
                            if (j >= 0
                                    &&lastLeftCorrectCharacter == falseDoubletLeftCharacter
                                    && lastLeftCorrectCharacter == lastRightCorrectCharacter
                                    && falseDoubletLeftCharacter == falseDoubletRightCharacter){

                                lastRightCorrectCharacter = '⚓';

                                ForegroundColorSpan style = new ForegroundColorSpan(Color.RED);
                                text.setSpan(style, i, i+1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                            }

                            if (i < original.length() - 1) {
                                lastRightOriginalCharacter = comparison.charAt(i);
                                lastRightPreviousOriginalCharacter = comparison.charAt(i + 1);
                            }
                            //

                            if (i < original.length() && answer.length() >= original.length()) {
                                charactersEqual = original.charAt(i) == answer.charAt(i + answer.length() - original.length());

                                if (charactersEqual){

                                    ForegroundColorSpan style = new ForegroundColorSpan(Color.GRAY);
                                    text.setSpan(style, i, i + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                }
                            } else if (i < original.length() && answer.length() < original.length()) {//&& i >= original.length() - answer.length()) {
                                if (i >= original.length() - answer.length()) {
                                    charactersEqual = original.charAt(i) == answer.charAt(i - (original.length() - answer.length()));

                                    if (charactersEqual) {

                                        ForegroundColorSpan style = new ForegroundColorSpan(Color.GRAY);
                                        text.setSpan(style, i, i + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    }
                                }
                                //
                                if(lastRightOriginalCharacter == lastRightPreviousOriginalCharacter
                                        && lastRightOriginalCharacter != falseDoubletLeftCharacter){

                                    ForegroundColorSpan style = new ForegroundColorSpan(Color.BLUE);
                                    text.setSpan(style, i+1, i+2, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                }
                                //
                            }
                            j--;
                        }

                        boolean isDifficultTemp = collocationCopy.isDifficult;

                        if(!englishLeft) {
                            if (answer.equals(original)) {
                                collocation.isDifficult = false;
                                collocationCopy.isDifficult = false;
                            } else {
                                collocation.isDifficult = true;
                                collocationCopy.isDifficult = true;
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

                        //скрываем клавиатуру по окончании ввода
                        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                        return true;


                    }
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