package ru.vitkt.watchingcorn;

import ru.vitkt.watchingcorn.R;

import android.os.Build;
import android.os.Bundle;

import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class CornPreference extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			addPreferencesFromResource(R.xml.pref);
		} else {
			getFragmentManager()
					.beginTransaction()
					.replace(android.R.id.content, new CornPreferenceFragment())
					.commit();
		}
	}

	private class CornPreferenceFragment extends PreferenceFragment {

		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref);

		}
	}

}
