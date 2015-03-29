/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.ionicframework.ionicapp395632;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.LinearLayoutSoftKeyboardDetect;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;

public class CordovaApp extends CordovaActivity {
	public static CordovaWebView webView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.init();
		// Set by <content src="index.html" /> in config.xml
		loadUrl(launchUrl);
	}
	
	public void onButton1Click(View view) {
		startActivity(new Intent(this, TestActivity.class));
	}
	
	@Override
	protected void createViews() {
		LayoutInflater inflater = (LayoutInflater) this .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.activity, null);
        // This builds the view.  We could probably get away with NOT having a LinearLayout, but I like having a bucket!

        LOG.d(TAG, "CordovaActivity.createViews()");

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        root = new LinearLayoutSoftKeyboardDetect(this, width, height);
        root.setOrientation(LinearLayout.VERTICAL);
//        root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));
        root.setLayoutParams(new LinearLayout.LayoutParams(1, 1));
        root.setVisibility(View.INVISIBLE);

        webView = appView;
        appView.setId(100);
        appView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1.0F));

        // Add web view but make it invisible while loading URL
        appView.setVisibility(View.INVISIBLE);
        
        // need to remove appView from any existing parent before invoking root.addView(appView)
        ViewParent parent = appView.getParent();
        if ((parent != null) && (parent != root)) {
            LOG.d(TAG, "removing appView from existing parent");
            ViewGroup parentGroup = (ViewGroup) parent;
            parentGroup.removeView(appView);
        }
        root.addView((View) appView);
//        root.setVisibility(View.INVISIBLE);
        view.addView(root);
        setContentView(view);

        int backgroundColor = preferences.getInteger("BackgroundColor", Color.BLACK);
        root.setBackgroundColor(backgroundColor);
	}
}
