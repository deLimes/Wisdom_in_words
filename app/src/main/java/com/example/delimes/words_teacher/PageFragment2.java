package com.example.delimes.words_teacher;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.os.Environment.MEDIA_MOUNTED;
import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;

public class PageFragment2 extends android.support.v4.app.Fragment {

    private int pageNumber;
    boolean recordsAreMerged = false;
    int numberOfOccurrences = 0;

    // Создаём пустой массив для хранения имен котов
    ArrayList<String> dictionary = new ArrayList<String>();

    int secretClicks = 0;

    private boolean isScrollEnabled = true;
    int mLastFirstVisibleItem;
    int mLastFirstItem;
    private boolean scrollMoves = false;
    boolean mIsScrollingUp = false;
    int gridPosition = 0;
    public EditText editTextHostname, editTextPortname;

    private boolean EnglishTextLayout = false;
    char[] ArrayEnglishCharacters = {'h', 'j', 'k', 'l', 'y', 'u', 'i', 'o', 'p', '[', ']', 'n', 'm',
            'g', 'f', 'd', 's', 'a', 'b', 'v', 'c', 'x', 'z', 't', 'r', 'e', 'w', 'q', '`'};
    List engList = Arrays.asList(ArrayEnglishCharacters);

    char[] ArrayRussianCharacters = {'р', 'о', 'л', 'д', 'ж', 'э', 'н', 'г', 'ш', 'щ', 'з', 'х', 'ъ', 'т', 'ь', 'б', 'ю',
            'п', 'а', 'в', 'ы', 'ф', 'и', 'м', 'с', 'ч', 'я', 'е', 'к', 'у', 'ц', 'й', 'ё'};
    List rusList = Arrays.asList(ArrayRussianCharacters);


    private WebView mWebView;

    public void enableScroll(boolean isScrollEnabled ) {
        this.isScrollEnabled = isScrollEnabled ;
    }



    public void mergeRecords(ArrayList<String> arrayList ) {

        /*
        for (int i = arrayList.size(); i >= 0; i--) {

        }
        */
        if (!recordsAreMerged) {
            if (arrayList.size() % 2 == 0) {

                for (int i = 0; i < arrayList.size(); i++) {
                    // if (i % 2 == 0) {
                    arrayList.set(i, arrayList.get(i) + "~" + arrayList.get(i + 1));
                    arrayList.remove(i + 1);
                    //}

                }

                recordsAreMerged = true;
            }
        }

    }

    public void splitRecords(ArrayList<String> arrayList ) {

        /*
        for (int i = arrayList.size(); i >= 0; i--) {

        }
        */
        if(recordsAreMerged) {
            //if (arrayList.size() % 2 == 0) {
            for (int i = 0; i < arrayList.size(); i++) {
                if (i % 2 == 0) {
                    String[] result = arrayList.get(i).split("~");
                    arrayList.set(i, result[0]);
                    arrayList.add(i + 1, result[1]);
                }

            }

            recordsAreMerged = false;
            //}
        }

    }

    public void updateAdapter() {


//        LayoutInflater inflater = LayoutInflater.from(getActivity());
//        View page = inflater.inflate(R.layout.fragment_page, null);
//
//
//        final GridView gridView = (GridView) page.findViewById(R.id.gridView);
//
//        Adapter adapter = gridView.getAdapter();
//        //((ArrayAdapter<String>)adapter).getFilter().filter("some text");
//
//        //updateAdapter
//        ArrayList<String> dictionaryCopy =  (ArrayList<String>)dictionary.clone();
//        ((ArrayAdapter<String>)adapter).clear();
//        ((ArrayAdapter<String>)adapter).addAll(dictionaryCopy);
//        //((ArrayAdapter<String>)adapter).addAll(dictionaryCopy.toArray(new String[dictionaryCopy.size()]));
//
//        //gridView.setAdapter(null);
//        //gridView.setAdapter((ArrayAdapter<String>)adapter);
//        //gridView.clearTextFilter();//фыва
//        gridView.setFilterText("     ");//фыва
//        gridView.clearTextFilter();//фыва
//        //updateAdapter

        /*
        for (int i = 0; i < gridView.getChildCount(); i++) {
            if (i % 2 != 0) {
                View a = gridView.getChildAt(i);
                if (a != null) {
                    //a.setWillNotDraw(true);
                    //a.setVisibility(View.GONE);
                    if(((CheckedTextView) gridView.getChildAt(i-1)).isChecked()&& !((CheckedTextView)a).isChecked()) {
                        a.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
        */

    }

    public void fillDictionary() {

        String[] array = getResources().getStringArray(R.array.dictionary);
        /*for(int i = 0; i < array.length; i++) {
            arrayList.add(array[i]);
        }*/

        //dictionary = new ArrayList<String>(Arrays.asList(array));
        dictionary.addAll(new ArrayList<String>(Arrays.asList(array)));

    }



    public static PageFragment2 newInstance(int page) {
        PageFragment2 fragment = new PageFragment2();
        Bundle args=new Bundle();
        args.putInt("num", page);
        fragment.setArguments(args);
        return fragment;
    }

    public PageFragment2() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View page=inflater.inflate(R.layout.fragment_page2, container, false);
//        TextView pageHeader=(TextView)result.findViewById(R.id.displayText);
//        String header = String.format("Фрагмент %d", pageNumber+1);
//        pageHeader.setText(header);

        //MyWebViewClient view = new MyWebViewClient();
        final WebView mWebView = (WebView) page.findViewById(R.id.webView);
        editTextHostname = (EditText) page.findViewById(R.id.hostname);
        editTextPortname = (EditText) page.findViewById(R.id.portname);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyCustomChromeClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        //mWebView.loadUrl("https://translate.google.com/?hl=ru#ru/en/Переводчик");


        editTextHostname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable hostname) {

                List<android.support.v4.app.Fragment> fragments = getFragmentManager().getFragments();
                android.support.v4.app.Fragment frag = fragments.get(0);

                ((PageFragment)frag).fillReceiverList(hostname.toString(), editTextPortname.getText().toString());

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

                List<android.support.v4.app.Fragment> fragments = getFragmentManager().getFragments();
                android.support.v4.app.Fragment frag = fragments.get(0);

                ((PageFragment)frag).fillReceiverList(editTextHostname.getText().toString(), portname.toString());

            }
        });

        ///////////////////////mWebView.loadUrl("file:///android_res/raw/mypage.html");

//        mWebView.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView v, String url) {
//                v.loadUrl(url);
//                return true;
//            }
//            @Override
//            public void onPageFinished(WebView v, String url) {
//                //  v.loadUrl("javascript:document.getElementById('gt-res-listen').click();");
//            }
//        });
        //getActivity().setContentView(view);



//        final View button8 = page.findViewById(R.id.button8);
//
//        //final View button8 = getActivity().findViewById(R.id.button8);
//        button8.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                Toast.makeText(getActivity().getApplicationContext(), "Должно быть четное количество карточек",
//                        Toast.LENGTH_SHORT).show();
//
//
//                WebView mWebView = page.findViewById(R.id.webView);
//                mWebView.getSettings().setJavaScriptEnabled(true);
//                mWebView.getSettings().setDomStorageEnabled(true);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
//                }
//
//
//                //mWebView.loadUrl("javascript:(function b(){document.getElementById('gt-res-listen').click();})");
//                //mWebView.loadUrl("javascript:document.getElementById('gt-res-listen').click();");
//                //mWebView.loadUrl("javascript:if (document.getElementById) {alert(\"Prived!!\");}");
//                //mWebView.loadUrl("javascript:(alert(\"Prived!!\");)");
//
//               // mWebView.loadUrl("javascript:alert('Prived');");
//                //mWebView.loadUrl("javascript:document.getElementById('gt-res-listen').click();");
//                String js = "javascript:document.getElementById('gt-res-listen').click();";
//                //js = "javascript:alert('Prived');";
//                js = "javascript:(function(){"+
//                        "l=document.getElementById('gt-res-listen');"+
//                        "e=document.createEvent('HTMLEvents');"+
//                        "e.initEvent('click',true,true);"+
//                        "l.dispatchEvent(e);"+
//                        "})()";
//
//                js = "javascript:l=document.getElementById('gt-res-listen');e=document.createEvent('HTMLEvents');e.initEvent('click',true,true);l.dispatchEvent(e);";
//                js = "javascript:(function(){document.getElementById('gt-res-listen').click();})()";
//                js = "javascript:(function(){alert(document.getElementById('result_box')));})()";
//                js = "javascript:alert(document.getElementById('result_box').textContent);";
//                js = "javascript:document.querySelector('button[type=\"gt-res-listen\"]').click()";
//
//                js = "javascript:(window.onload = function(){alert(document.getElementById('result_box'));})()";
//                //js = "file:///android_res/raw/mypage.html";
//
//                String text = "https://translate.google.com/?hl=ru#en/ru/переводчик";
//                //mWebView.loadUrl(js);
//
//                //mWebView.loadUrl("file:///android_res/raw/mypage.html");
//                //mWebView.loadUrl(getActivity().getFilesDir().getAbsolutePath()
//                //       + File.separator + "mypage.html");
//
//                mWebView.loadUrl(getExternalStorageDirectory().getAbsolutePath() + File.separator + "mypage.mht");
//
////                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
////                    mWebView.evaluateJavascript(js, new ValueCallback<String>() {
////                        @Override
////                        public void onReceiveValue(String s) {
////
////                            Toast.makeText(getActivity().getApplicationContext(), s,
////                                    Toast.LENGTH_SHORT).show();
////
////                        }
////
////                });
////                }else{
////                    mWebView.loadUrl(js);
////                }
//            }
//            });


        return page;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        editTextHostname.setText(prefs.getString("hostname", "192.168.0.1"));
        editTextPortname.setText(prefs.getString("portname", "7373"));

        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //mWebView.loadUrl("https://translate.google.com/?hl=ru#ru/en/Переводчик");
    }

    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
        editPrefs.putString("hostname", editTextHostname.getText().toString());
        editPrefs.putString("portname", editTextPortname.getText().toString());
        editPrefs.commit();

    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest url)

        {
            view.loadUrl(url.toString());
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            Toast.makeText(getActivity().getApplicationContext(), "Finish", Toast.LENGTH_SHORT).show();
            //view.loadUrl("javascript:alert('Prived');");
//            view.loadUrl("javascript:(window.onload = function(){" +
//                    "alert(document.getElementById('UseTranslator'));" +
//                    "})()");
            //view.saveWebArchive("file:///android_res/raw/mypage.html");
            //view.saveWebArchive(getActivity().getFilesDir().getAbsolutePath()
            //       + File.separator + "mypage.html");

            //view.saveWebArchive(getExternalStorageDirectory().getAbsolutePath() + File.separator + "mypage.xml");
//            view.saveWebArchive(getExternalStorageDirectory().getAbsolutePath() + File.separator + "mypage.mht", false, new ValueCallback<String>() {
//                @Override
//                public void onReceiveValue(String s) {
//
//                    Toast.makeText(getActivity().getApplicationContext(), s,
//                            Toast.LENGTH_SHORT).show();
//
//                }
//                //view.loadUrl("javascript:document.getElementById(\"gt-res-listen\").click();");
//
//
//            });
        }
    }

    private class MyCustomChromeClient extends WebChromeClient
    {

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            //return super.onJsAlert(view, url, message, result);

            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                    message,
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            return true;
        }
    }

}
