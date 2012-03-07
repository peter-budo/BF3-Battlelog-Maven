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

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.*;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import com.ninetwozero.battlelog.adapters.UnlockListAdapter;
import com.ninetwozero.battlelog.asynctasks.AsyncSessionSetActive;
import com.ninetwozero.battlelog.asynctasks.AsyncSessionValidate;
import com.ninetwozero.battlelog.datatypes.*;
import com.ninetwozero.battlelog.misc.*;
import com.ninetwozero.battlelog.services.UserProfileService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class UnlockView extends TabActivity {

    // Attributes
    private SharedPreferences sharedPreferences;
    private AsyncGetDataSelf getDataAsync;
    private ProfileData profileData;
    private HashMap<Long, UnlockDataWrapper> unlocks;
    private long selectedPersona;
    private int selectedPosition;

    // Elements
    private ProgressBar progressBar;
    private TabHost tabHost;
    private LayoutInflater layoutInflater;
    private ListView[] listView;
    private TextView textEmpty;

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

        // Get the intent
        if (getIntent().hasExtra("profile")) {

            profileData = (ProfileData) getIntent().getParcelableExtra(
                    "profile");

        } else {

            Toast.makeText(this, R.string.info_general_noprofile,
                    Toast.LENGTH_SHORT).show();
            return;

        }

        // Is the profileData null?!
        if (profileData == null || profileData.getProfileId() == 0) {
            finish();
            return;
        }

        // Get the pos
        selectedPersona = getIntent().getLongExtra("selectedPersona", 0);
        selectedPosition = 0;

        // Setup the locale
        setupLocale();

        // Set the content view
        setContentView(R.layout.unlocks_view);

        // Prepare to tango
        this.layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listView = new ListView[5];
        this.textEmpty = (TextView) findViewById(R.id.text_empty);

        // Init!
        initActivity();

    }

    public final void initActivity() {

        // Fix the tabs
        tabHost = (TabHost) findViewById(android.R.id.tabhost);

        // Let's set them up
        setupTabsPrimary(

                new String[] {

                        "Weapons", "Attachments", "Kit", "Vehicles", "Skills"

                }, new int[] {

                        R.drawable.tab_selector_unlocks_weapons,
                        R.drawable.tab_selector_unlocks_attachments,
                        R.drawable.tab_selector_unlocks_kits,
                        R.drawable.tab_selector_unlocks_vehicles,
                        R.drawable.tab_selector_unlocks_skills,

                }, new int[] {

                        R.layout.tab_content_unlocks, R.layout.tab_content_unlocks,
                        R.layout.tab_content_unlocks, R.layout.tab_content_unlocks,
                        R.layout.tab_content_unlocks

                }

        );

    }

    private final View createTabView(final Context context, final int logo) {

        View view = LayoutInflater.from(context).inflate(
                R.layout.unlock_tab_layout, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.image_tab);
        imageView.setImageResource(logo);
        return view;

    }

    private void setupTabsPrimary(final String[] titleArray,
            final int[] logoArray, final int[] layoutArray) {

        // Init
        TabHost.TabSpec spec;

        // Iterate them tabs
        for (int i = 0, max = titleArray.length; i < max; i++) {

            // Num
            final int num = i;
            View tabview = createTabView(tabHost.getContext(), logoArray[num]);

            // Let's set the content
            spec = tabHost.newTabSpec(titleArray[num]).setIndicator(tabview)
                    .setContent(

                            new TabContentFactory() {

                                public View createTabContent(String tag) {

                                    View v = layoutInflater.inflate(layoutArray[num],
                                            null);
                                    v.setTag("tab #" + num);
                                    return v;
                                }

                            }

                    );

            // Add the tab
            tabHost.addTab(spec);

        }

        // Assign values
        tabHost.setOnTabChangedListener(

                new OnTabChangeListener() {

                    @Override
                    public void onTabChanged(String tabId) {

                        switch (tabHost.getCurrentTab()) {

                            case 0:
                                setupList(unlocks.get(selectedPersona).getWeapons(), 0);
                                break;

                            case 1:
                                setupList(unlocks.get(selectedPersona).getAttachments(), 1);
                                break;

                            case 2:
                                setupList(unlocks.get(selectedPersona).getKitUnlocks(), 2);
                                break;

                            case 3:
                                setupList(
                                        unlocks.get(selectedPersona).getVehicleUpgrades(),
                                        3);
                                break;

                            case 4:
                                setupList(unlocks.get(selectedPersona).getSkills(), 4);
                                break;

                            default:
                                break;

                        }

                    }

                }

                );

    }

    public void reload() {

        // ASYNC!!!
        new AsyncGetDataSelf(this).execute(profileData);

    }

    public void doFinish() {
    }

    private class AsyncGetDataSelf extends
            AsyncTask<ProfileData, Void, Boolean> {

        // Attributes
        Context context;
        ProgressDialog progressDialog;

        public AsyncGetDataSelf(Context c) {

            this.context = c;
            this.progressDialog = null;

        }

        @Override
        protected void onPreExecute() {

            // Let's see
            if (unlocks == null) {

                this.progressDialog = new ProgressDialog(this.context);
                this.progressDialog.setTitle(context
                        .getString(R.string.general_wait));
                this.progressDialog
                        .setMessage(getString(R.string.general_downloading));
                this.progressDialog.show();

            }

        }

        @Override
        protected Boolean doInBackground(ProfileData... arg0) {

            try {

                if (arg0[0].getPersonaId() == 0) {

                    profileData = UserProfileService.getPersonaIdFromProfile(profileData);
                    if (selectedPersona == 0) {
                        selectedPersona = profileData.getPersonaId();
                    }
                    unlocks = UserProfileService.getUnlocksForUser(profileData);

                } else {

                    if (selectedPersona == 0) {
                        selectedPersona = arg0[0].getPersonaId();
                    }
                    unlocks = UserProfileService.getUnlocksForUser(arg0[0]);

                }

                return (unlocks != null);

            } catch (WebsiteHandlerException ex) {

                ex.printStackTrace();
                return false;

            }

        }

        @Override
        protected void onPostExecute(Boolean result) {

            // Fail?
            if (!result) {

                if (this.progressDialog != null)
                    this.progressDialog.dismiss();
                Toast.makeText(this.context, R.string.general_no_data,
                        Toast.LENGTH_SHORT).show();
                ((Activity) this.context).finish();
                return;

            }

            // Do actual stuff, like sending to an adapter
            switch (tabHost.getCurrentTab()) {

                case 0:
                    setupList(unlocks.get(selectedPersona).getWeapons(), 0);
                    break;

                case 1:
                    setupList(unlocks.get(selectedPersona).getAttachments(), 1);
                    break;

                case 2:
                    setupList(unlocks.get(selectedPersona).getKitUnlocks(), 2);
                    break;

                case 3:
                    setupList(unlocks.get(selectedPersona).getVehicleUpgrades(), 3);
                    break;

                case 4:
                    setupList(unlocks.get(selectedPersona).getSkills(), 4);
                    break;

                default:
                    break;

            }

            // Go go go
            if (this.progressDialog != null)
                this.progressDialog.dismiss();
            return;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_unlock, menu);
        return super.onCreateOptionsMenu(menu);

    }

    public void setupList(ArrayList<UnlockData> data, int pos) {

        if (listView[pos] == null) {

            listView[pos] = (ListView) tabHost.findViewWithTag("tab #" + pos)
                    .findViewById(R.id.list_unlocks);
            listView[pos].setAdapter(new UnlockListAdapter(this, data,
                    layoutInflater));

        } else {

            ((UnlockListAdapter) listView[pos].getAdapter()).setDataArray(data);
            ((UnlockListAdapter) listView[pos].getAdapter())
                    .notifyDataSetChanged();

        }

        // Is it empty?
        if (data == null || data.size() == 0) {

            textEmpty.setVisibility(View.VISIBLE);
            return;

        } else {

            textEmpty.setVisibility(View.GONE);

        }

        return;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Let's act!
        if (item.getItemId() == R.id.option_reload) {

            this.reload();

        } else if (item.getItemId() == R.id.option_change) {

            generateDialogPersonaList(

                    this, profileData.getPersonaIdArray(),
                    profileData.getPersonaNameArray(),
                    profileData.getPlatformIdArray()

            ).show();

        } else if (item.getItemId() == R.id.option_back) {

            ((Activity) this).finish();

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
        outState.putParcelableArrayList(Constants.SUPER_COOKIES,
                RequestHandler.getCookies());

    }

    public Dialog generateDialogPersonaList(final Context context,
            final long[] personaId, final String[] persona, final long[] ls) {

        // Attributes
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Set the title and the view
        builder.setTitle(R.string.info_dialog_soldierselect);
        String[] listNames = new String[personaId.length];

        for (int i = 0, max = personaId.length; i < max; i++) {

            listNames[i] = persona[i] + " "
                    + DataBank.resolvePlatformId((int) ls[i]);

        }
        builder.setSingleChoiceItems(

                listNames, selectedPosition, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {

                        if (personaId[item] != selectedPersona) {

                            // Update it
                            selectedPersona = profileData.getPersonaId(item);

                            // Store selected position
                            selectedPosition = item;

                            // Load the new!
                            switch (tabHost.getCurrentTab()) {

                                case 0:
                                    setupList(unlocks.get(selectedPersona).getWeapons(), 0);
                                    break;

                                case 1:
                                    setupList(
                                            unlocks.get(selectedPersona).getAttachments(),
                                            1);
                                    break;

                                case 2:
                                    setupList(unlocks.get(selectedPersona).getKitUnlocks(),
                                            2);
                                    break;

                                case 3:
                                    setupList(unlocks.get(selectedPersona)
                                            .getVehicleUpgrades(), 3);
                                    break;

                                case 4:
                                    setupList(unlocks.get(selectedPersona).getSkills(), 4);
                                    break;

                                default:
                                    break;

                            }

                        }

                        dialog.dismiss();

                    }

                }

                );

        // CREATE
        return builder.create();

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
