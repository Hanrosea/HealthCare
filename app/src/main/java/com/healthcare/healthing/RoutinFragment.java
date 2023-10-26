package com.healthcare.healthing;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RoutinFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private WebView webView_1, webView_2, webView_3, webView_4;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    private String mParam1;
    private String mParam2;

    public RoutinFragment() {
    }

    public static RoutinFragment newInstance(String param1, String param2) {
        RoutinFragment fragment = new RoutinFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_routin, container, false);

        webView_1 = view.findViewById(R.id.webView_1);
        webView_2 = view.findViewById(R.id.webView_2);
        webView_3 = view.findViewById(R.id.webView_3);
        webView_4 = view.findViewById(R.id.webView_4);

        initWebView(webView_1, "https://www.youtube.com/embed/2rb3GZw0IEw");
        initWebView(webView_2, "https://www.youtube.com/embed/7Melm8LEFqY");
        initWebView(webView_3, "https://www.youtube.com/embed/KCAwey51gUc");
        initWebView(webView_4, "https://www.youtube.com/embed/PfibseV_kus");

        return view;
    }

    private void initWebView(WebView webView, String url) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onHideCustomView() {
                ((FrameLayout) getActivity().getWindow().getDecorView()).removeView(mCustomView);
                mCustomView = null;
                mCustomViewCallback.onCustomViewHidden();
                mCustomViewCallback = null;

                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (mCustomView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                mCustomView = view;
                mCustomView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
                ((FrameLayout) getActivity().getWindow().getDecorView()).addView(mCustomView);
                mCustomViewCallback = callback;

                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (isYouTubeUrl(url)) {
                    // YouTube 동영상 URL인 경우 YouTube 앱으로 열도록 처리합니다.
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.setPackage("com.google.android.youtube"); // YouTube 앱의 패키지 이름
                    startActivity(intent);
                    return true;
                } else {
                    // 다른 URL은 WebView 내에서 열도록 처리합니다.
                    view.loadUrl(url);
                    return true;
                }
            }
        });
        webView.loadData("<html><body><iframe src=\"" + url + "\" width=\"100%\" height=\"100%\"></iframe></body></html>", "text/html", "UTF-8");
    }

    private boolean isYouTubeUrl(String url) {
        return url != null && (url.startsWith("http://www.youtube.com") || url.startsWith("https://www.youtube.com"));
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mCustomView != null) {
                        WebChromeClient.CustomViewCallback callback = mCustomViewCallback;
                        webView_1.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                        webView_2.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                        webView_3.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                        callback.onCustomViewHidden();
                        return true;
                    }
                }
                return false;
            }
        });
    }


}
