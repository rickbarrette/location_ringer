/**
 * LegalActivity.java
 * @date Sep 16, 2012
 * @author ricky barrette
 * 
 * Copyright 2012 Richard Barrette 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License
 */
package org.RickBarrette.android.LocationRinger;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * This is a super simple web activity to display legal information to the user
 * 
 * @author ricky barrette
 */
public class LegalActivity extends Activity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leagal_activity);
		final WebView wv = (WebView) findViewById(R.id.webview);
		wv.loadUrl("file:///android_asset/legal.html");
	}

}