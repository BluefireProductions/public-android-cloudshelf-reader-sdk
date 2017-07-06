//  Copyright (c) 2014 Readium Foundation and/or its licensees. All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, 
//  are permitted provided that the following conditions are met:
//  1. Redistributions of source code must retain the above copyright notice, this 
//  list of conditions and the following disclaimer.
//  2. Redistributions in binary form must reproduce the above copyright notice, 
//  this list of conditions and the following disclaimer in the documentation and/or 
//  other materials provided with the distribution.
//  3. Neither the name of the organization nor the names of its contributors may be 
//  used to endorse or promote products derived from this software without specific 
//  prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
//  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
//  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
//  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
//  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
//  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
//  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
//  OF THE POSSIBILITY OF SUCH DAMAGE
//  Copyright (c) 2014 Readium Foundation and/or its licensees. All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification,
//  are permitted provided that the following conditions are met:
//  1. Redistributions of source code must retain the above copyright notice, this
//  list of conditions and the following disclaimer.
//  2. Redistributions in binary form must reproduce the above copyright notice,
//  this list of conditions and the following disclaimer in the documentation and/or
//  other materials provided with the distribution.
//  3. Neither the name of the organization nor the names of its contributors may be
//  used to endorse or promote products derived from this software without specific
//  prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
//  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
//  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
//  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
//  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
//  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
//  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
//  OF THE POSSIBILITY OF SUCH DAMAGE

package com.bluefire.sampleapplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.readium.sdk.android.Container;

import com.bluefire.bclreader.publicclasses.BCLReaderInitializer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends Activity {

	public static final String BOOKS_DIRECTORY_NAME = "books";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.container_list);

		try {
			// Read file from Assets and write to internal storage
			copyBooksFromAssetsToBooksDir();
		} catch (IOException e) {
			e.printStackTrace();
		}

		final ListView view = (ListView) findViewById(R.id.containerList);
		final List<String> list = getInnerBooks();

		BookListAdapter bookListAdapter = new BookListAdapter(this, list);
		view.setAdapter(bookListAdapter);

		if (list.isEmpty()) {
			Toast.makeText(
					this,
					getBooksDirectory().getAbsolutePath() + "/ is empty, copy epub3 test file first please.",
					Toast.LENGTH_LONG).show();
		}

		view.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
									long arg3) {
				String bookName = list.get(arg2);
				String path = getBooksDirectory() + "/" + bookName;
				Toast.makeText(MainActivity.this, "Select " + bookName, Toast.LENGTH_SHORT).show();

				Intent intent = new Intent(MainActivity.this, ReaderControllerActivity.class);
				intent.putExtra("path", path);
				startActivity(intent);
			}
		});

	}

	private void copyBooksFromAssetsToBooksDir() throws IOException {
		String[] bookNames = getAssets().list("");
		if(!getBooksDirectory().exists()) {
			getBooksDirectory().mkdir();
		}
		for (String bookName:bookNames){
			File f = new File(getBooksDirectory() + "/" + bookName);
			if (!f.exists() && isNameEpubBook(bookName)) try {
				InputStream is = getAssets().open(bookName);
				int size = is.available();
				byte[] buffer = new byte[size];
				is.read(buffer);
				is.close();
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(buffer);
				fos.close();
			} catch (Exception e) { throw new RuntimeException(e); }
		}

	}

	private File getBooksDirectory(){
		return new File(getFilesDir(), BOOKS_DIRECTORY_NAME);
	}

	private List<String> getInnerBooks() {     // get books in /sdcard/epubtest path
		List<String> list = new ArrayList<String>();

		File[] files = getBooksDirectory().listFiles();

		if (files != null) {
			for (File f : files) {
				if (f.isFile()) {
					String name = f.getName();
					if (isNameEpubBook(name)) {
						list.add(name);
						Log.i("books", name);
					}
				}
			}
		}
		Collections.sort(list, new Comparator<String>() {

			@Override
			public int compare(String s1, String s2) {
				return s1.compareToIgnoreCase(s2);
			}

		});
		return list;
	}

	private boolean isNameEpubBook(String fileName){
		return fileName.length() > 5 && fileName.substring(fileName.length() - 5).equals(".epub");
	}




}
