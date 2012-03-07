/*
	This file is part of BF3 Battlelog

    BF3 Battlelog is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BF3 Battlelog is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 */

package com.ninetwozero.battlelog;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import com.ninetwozero.battlelog.adapters.ThreadListAdapter;
import com.ninetwozero.battlelog.asynctasks.AsyncCreateNewThread;
import com.ninetwozero.battlelog.asynctasks.AsyncSessionSetActive;
import com.ninetwozero.battlelog.asynctasks.AsyncSessionValidate;
import com.ninetwozero.battlelog.datatypes.Board;
import com.ninetwozero.battlelog.datatypes.ShareableCookie;
import com.ninetwozero.battlelog.misc.*;
import com.ninetwozero.battlelog.services.ForumService;

import java.util.ArrayList;
import java.util.Locale;

public class ForumView extends ListActivity {

    // Attributes
    private final Context CONTEXT = this;
    private SharedPreferences sharedPreferences;
    private LayoutInflater layoutInflater;
    private Board.Forum currentForum;
    private long forumId;
    private String forumTitle, locale;
    private int currentPage = 1;
    private long latestRefresh = 0;

    // Elements
    private ListView listView;
    private SlidingDrawer slidingDrawer;
    private TextView slidingDrawerHandle;
    private OnDrawerOpenListener onDrawerOpenListener;
    private OnDrawerCloseListener onDrawerCloseListener;
    private EditText textareaTitle, textareaContent;
    private Button buttonPost, buttonMore;

    @Override
    public void onCreate(Bundle icicle) {

        // onCreate - save the instance state
        super.onCreate(icicle);

        // Set sharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Did it get passed on?
        if (icicle != null && icicle.containsKey(Constants.SUPER_COOKIES)) {

            ArrayList<ShareableCookie> shareableCookies = icicle
                    .getParcelableArrayList(Constants.SUPER_COOKIES);

            if (shareableCookies != null) {

                RequestHandler.setCookies(shareableCookies);

            } else {

                finish();

            }

        }

        // Setup the locale
        setupLocale();

        // Set the content view
        setContentView(R.layout.forum_view);

        // Prepare to tango
        this.layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Get the forumId
        forumId = getIntent().getLongExtra("forumId", 0);
        forumTitle = getIntent().getStringExtra("forumTitle");
        locale = sharedPreferences.getString(Constants.SP_BL_LOCALE, "en");

        // Update the title
        this.setTitle(getTitle().toString().replace("...", forumTitle));

        // Validate
        if (forumId == 0 || forumTitle == null) {
            return;
        }

        // Init
        initLayout();
        setupBottom();

    }

    @Override
    public void onResume() {

        super.onResume();

        // Setup the locale
        setupLocale();

        // Setup the session
        setupSession();

        // We need to reload
        reload();

    }

    public void initLayout() {

        // Set the top
        if (forumTitle != null) {

            ((TextView) findViewById(R.id.text_title)).setText(forumTitle);

        }

        // Get the listView
        if (listView == null) {

            listView = getListView();
            if (currentForum == null) {

                listView.setAdapter(new ThreadListAdapter(this, null,
                        layoutInflater));

            } else {

                listView.setAdapter(new ThreadListAdapter(this, currentForum
                        .getThreads(), layoutInflater));

            }

            // Let's get the button
            buttonMore = (Button) findViewById(R.id.button_more);

        }

    }

    // Define the SlidingDrawer
    public void setupBottom() {

        if (slidingDrawer == null) {

            slidingDrawer = (SlidingDrawer) findViewById(R.id.post_slider);
            slidingDrawerHandle = (TextView) findViewById(R.id.post_slide_handle_text);

            // Set the drawer listeners
            onDrawerCloseListener = new OnDrawerCloseListener() {

                @Override
                public void onDrawerClosed() {
                    slidingDrawer.setClickable(false);
                }

            };
            onDrawerOpenListener = new OnDrawerOpenListener() {

                @Override
                public void onDrawerOpened() {
                    slidingDrawer.setClickable(true);
                }

            };

            // Attach the listeners
            slidingDrawer.setOnDrawerOpenListener(onDrawerOpenListener);
            slidingDrawer.setOnDrawerCloseListener(onDrawerCloseListener);

            // Grab the field + button for further reference!
            textareaTitle = (EditText) findViewById(R.id.textarea_title);
            textareaContent = (EditText) findViewById(R.id.textarea_content);
            buttonPost = (Button) findViewById(R.id.button_new);

        }

    }

    public void reload() {

        // Set it up
        long now = System.currentTimeMillis() / 1000;

        if (currentForum == null) {

            new AsyncGetThreads(this, listView).execute(forumId);

        } else {

            if (listView.getAdapter() == null) {

                initLayout();

            }

            if ((latestRefresh + 300) < now) {

                new AsyncGetThreads(null, listView).execute(forumId);

            } else {

                Log.d(Constants.DEBUG_TAG,
                        "It's still fresh enough if you ask me!");

            }

        }

        // Save the latest refresh
        latestRefresh = now;

    }

    public void manualReload() {

        // Set it up
        latestRefresh = 0;
        reload();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate!!
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_forumview, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Let's act!
        if (item.getItemId() == R.id.option_reload) {

            this.reload();

        } else if (item.getItemId() == R.id.option_search) {

            startActivity(new Intent(this, ForumSearchView.class));

        } else if (item.getItemId() == R.id.option_back) {

            this.finish();

        }

        // Return true yo
        return true;

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

    }

    @Override
    public void onListItemClick(ListView l, View v, int p, long id) {

        startActivity(

        new Intent(this, ForumThreadView.class).putExtra(

                "threadId", id

                ).putExtra(

                        "threadTitle", ((Board.ThreadData) v.getTag()).getTitle()

                )

        );

    }

    private class AsyncGetThreads extends AsyncTask<Long, Void, Boolean> {

        // Attributes
        private Context context;
        private ProgressDialog progressDialog;
        private ListView list;

        // Construct
        public AsyncGetThreads(Context c, ListView l) {

            context = c;
            list = l;

        }

        @Override
        protected void onPreExecute() {

            if (context != null) {

                progressDialog = new ProgressDialog(this.context);
                progressDialog.setTitle(R.string.general_wait);
                progressDialog.setMessage("Downloading the threads...");
                progressDialog.show();

            }

        }

        @Override
        protected Boolean doInBackground(Long... arg0) {

            try {

                currentForum = ForumService.getThreadsForForum(locale,
                        arg0[0]);
                return (currentForum != null);

            } catch (Exception ex) {

                ex.printStackTrace();
                return false;

            }

        }

        @Override
        protected void onPostExecute(Boolean results) {

            if (context != null && this.progressDialog != null) {

                this.progressDialog.dismiss();
                if (currentForum.getNumPages() > 1) {

                    buttonMore.setVisibility(View.VISIBLE);
                    buttonMore
                            .setText(getString(R.string.info_xml_feed_button_pagination));

                } else {

                    buttonMore.setVisibility(View.GONE);

                }

            }

            if (results) {

                ((ThreadListAdapter) listView.getAdapter()).set(currentForum
                        .getThreads());

            }

        }

    }

    public void onPostSubmit(View v) {

        // Let's get the content
        String title = textareaTitle.getText().toString();
        String content = textareaContent.getText().toString();

        // Parse for the BBCODE!
        content = BBCodeUtils.toBBCode(content, null);

        // Ready... set... go!
        new AsyncCreateNewThread(this, forumId).execute(title, content,
                sharedPreferences.getString(Constants.SP_BL_CHECKSUM, ""));

    }

    public void resetPostFields() {

        // Reset
        textareaTitle.setText("");
        textareaContent.setText("");
        slidingDrawer.animateClose();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // Hotkeys
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (slidingDrawer.isOpened()) {

                slidingDrawer.animateClose();

            } else {

                this.finish();

            }
            return true;

        } else if (keyCode == KeyEvent.KEYCODE_SEARCH) {

            startActivity(new Intent(this, ForumSearchView.class));

        }

        return super.onKeyDown(keyCode, event);

    }

    public void onClick(View v) {

        if (v.getId() == R.id.button_more) {

            // Validate
            if ((currentPage - 1) == currentForum.getNumPages()) {

                v.setVisibility(View.GONE);

            }

            // Increment
            currentPage++;

            // Do the "get more"-thing
            new AsyncLoadMore(this, forumId).execute(currentPage);

        }
    }

    public class AsyncLoadMore extends AsyncTask<Integer, Void, Boolean> {

        // Attributes
        private Context context;
        private long forumId;
        private int page;
        private ArrayList<Board.ThreadData> threads;

        // Constructs
        public AsyncLoadMore(Context c, long f) {

            this.context = c;
            this.forumId = f;

        }

        @Override
        protected void onPreExecute() {

            buttonMore.setText(getString(R.string.label_downloading));
            buttonMore.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Integer... arg0) {

            try {

                page = arg0[0];
                threads = ForumService.getThreadsForForum(this.forumId, page,
                        locale);
                return true;

            } catch (Exception ex) {

                ex.printStackTrace();
                return false;

            }

        }

        @Override
        protected void onPostExecute(Boolean results) {

            if (context instanceof ForumView) {

                if (results) {

                    ((ThreadListAdapter) ((ForumView) context).getListView()
                            .getAdapter()).add(threads);
                    buttonMore
                            .setText(getString(R.string.info_xml_feed_button_pagination));

                } else {

                    Toast.makeText(context,
                            R.string.info_xml_threads_more_false,
                            Toast.LENGTH_SHORT).show();

                }

                buttonMore.setEnabled(true);

            }

        }

    }

    public void setupSession() {

        // Let's set "active" against the website
        new AsyncSessionSetActive().execute();

        // If we don't have a profile...
        if (SessionKeeper.getProfileData() == null) {

            // ...but we do indeed have a cookie...
            if (!sharedPreferences.getString(Constants.SP_BL_COOKIE_VALUE, "")
                    .equals("")) {

                // ...we set the SessionKeeper, but also reload the cookies!
                // Easy peasy!
                SessionKeeper
                        .setProfileData(SessionKeeper
                                .generateProfileDataFromSharedPreferences(sharedPreferences));
                RequestHandler.setCookies(

                        new ShareableCookie(

                                sharedPreferences.getString(Constants.SP_BL_COOKIE_NAME, ""),
                                sharedPreferences.getString(
                                        Constants.SP_BL_COOKIE_VALUE, ""),
                                Constants.COOKIE_DOMAIN

                        )

                        );

                // ...but just to be sure, we try to verify our session
                // "behind the scenes"
                new AsyncSessionValidate(this, sharedPreferences).execute();

            } else {

                // Aw man, that backfired.
                Toast.makeText(this, R.string.info_txt_session_lost,
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, Main.class));
                finish();

            }

        }

    }

    public void setupLocale() {

        if (!sharedPreferences.getString(Constants.SP_BL_LANG, "").equals("")) {

            Locale locale = new Locale(sharedPreferences.getString(
                    Constants.SP_BL_LANG, "en"));
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getResources().updateConfiguration(config,
                    getResources().getDisplayMetrics());

        }

    }

}
