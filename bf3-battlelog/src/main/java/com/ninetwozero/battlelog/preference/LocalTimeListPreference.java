
package com.ninetwozero.battlelog.preference;

import static com.ninetwozero.battlelog.datatypes.ProfileSettings.LOCAL_TIME_VALUES;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class LocalTimeListPreference extends ListPreference {

    private final int MILLISECONDS_TO_MINUTE = 60000;

    public LocalTimeListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LocalTimeListPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateDialogView() {
        ListView view = new ListView(getContext());
        view.setAdapter(adapter());
        setEntries(entries());
        setEntryValues(entryValues());
        setValueIndex(initializeIndex());
        return view;
    }

    private ListAdapter adapter() {
        return new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_singlechoice);
    }

    private CharSequence[] entries() {
        return listOfTimes();
    }

    private CharSequence[] entryValues() {
        return LOCAL_TIME_VALUES;
    }

    private CharSequence[] listOfTimes() {
        Date today = new Date(today());
        List<String> hours = new ArrayList<String>();
        for (CharSequence value : LOCAL_TIME_VALUES) {
            hours.add(timeWithOffset(today, value));
        }
        return hours.toArray(new CharSequence[hours.size()]);
    }

    private String timeWithOffset(Date today, CharSequence value) {
        Date date = new Date(today.getTime());
        return formatter().format(date.getTime() + offsetValue(value));
    }

    private int initializeIndex() {
        return timeSharedPreference() != -1 ? timeSharedPreference() : indexFromTimeZone();
    }

    private int indexFromTimeZone() {
        TimeZone zone = TimeZone.getDefault();
        return indexOf("" + zone.getRawOffset() / MILLISECONDS_TO_MINUTE);
    }

    private int indexOf(CharSequence value) {
        List<CharSequence> valuesList = Arrays.asList(LOCAL_TIME_VALUES);
        return valuesList.indexOf(value) != -1 ? valuesList.indexOf(value) : 0;
    }

    private int timeSharedPreference() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getContext());

        return indexOf(preference.getString(getKey(), "0"));
    }

    private long today() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
    }

    private SimpleDateFormat formatter() {
        return new SimpleDateFormat("HH:mm");
    }

    private long offsetValue(CharSequence value) {
        try {
            return Integer.parseInt(value.toString()) * MILLISECONDS_TO_MINUTE;
        } catch (NumberFormatException e) {
            Log.d("LocalTimeListPreference",
                    "Failed to parse local time CharSequence \"" + value.toString() + "\" to long");
        }
        return 0;
    }
}
