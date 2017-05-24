package com.sn1006.atkins.sprint;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

/**
 * Created by jonathanbrooks on 2017-05-23.
 */

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_session);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();

        int count = prefScreen.getPreferenceCount();

        for(int i = 0; i < count; i++) {
            Preference p = prefScreen.getPreference(i);
            /* check if checkbox pref (not yet used, but for future)
               if not, then take value and pass to setPreferenceSummary
               if is checkbox, don't need to because will change in xml shown/hidden attributes
               and checkbox values returned are booleans so we would get an error below req string */
            if(!(p instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }
    }

    public void setPreferenceSummary(Preference preference, String value) {
        if(preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if(prefIndex >= 0) {
                //get preference label associated with the value above
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }else if(preference instanceof EditTextPreference) {
            //set preference summary for edit text fields
            preference.setSummary(value);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //find pref by its key, and if not null or checkbox, set pref summary for it
        Preference preference = findPreference(key);
        if (null != preference) {
            if(!(preference instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }
    }

    //register preference change listener when created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

    }

    //unregister preference change listener when destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
