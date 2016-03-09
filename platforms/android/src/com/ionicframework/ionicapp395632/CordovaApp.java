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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


/**
 * Cordova generated class which starts the application as an embedded webview.
 * After the start of the webview, a native activity is placed above.
 * */
public class CordovaApp extends CordovaActivity {
	public static CordovaWebView webView;
	
	/**
	 * Generates the application by starting the embedded webview and
	 * the native activity above.
	 * @param savedInstanceState Contains the activity's data.
	 * */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.init();
		
		// loads and starts the passed URL
		// Set by <content src="index.html" /> in config.xml
		loadUrl(launchUrl);
		
		// starts a new activity
		startActivity(new Intent(this, Activity1.class));
	}
	
	/**
	 * Sets the webview instance.
	 * */
	@Override
	protected CordovaWebView makeWebView() {
		return webView = super.makeWebView();
	}
	
	/**
	 * Sets the activity content to an explicit view.
	 * @param view Contains the content which should be displayed.
	 * */
	@Override
	public void setContentView(View view) {
		// view.setVisibility(View.INVISIBLE);
		super.setContentView(view);
	}

}
