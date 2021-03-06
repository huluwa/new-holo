package com.krayzk9s.imgurholo.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.krayzk9s.imgurholo.R;
import com.krayzk9s.imgurholo.libs.JSONParcelable;
import com.krayzk9s.imgurholo.tools.ApiCall;
import com.krayzk9s.imgurholo.tools.onImageSelected;
import com.krayzk9s.imgurholo.ui.ImagesFragment;
import com.krayzk9s.imgurholo.ui.SingleImageFragment;

import org.json.JSONException;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Copyright 2013 Kurt Zimmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ImgurHoloActivity extends FragmentActivity implements onImageSelected {
    public static final String IMAGE_DATA_LINK = "link";
    public static final String IMAGE_DATA_COVER = "cover";
    public static final String VERTICAL_HEIGHT_SETTING = "VerticalHeight";
    public static final String IMAGE_DATA_TYPE = "type";
    public static final String IMAGE_DATA_TITLE = "title";
    public static final String IMAGE_DATA_DESCRIPTION = "description";
    public static final String IMAGE_DATA_WIDTH = "width";
    public static final String IMAGE_DATA_HEIGHT = "height";
    public static final String IMAGE_DATA_SIZE = "size";
    public static final String IMAGE_DATA_VIEWS = "views";
    ApiCall apiCall;
    String theme;
    public static final String IMAGE_PAGER_INTENT = "com.krayzk9s.imgurholo.IMAGE_PAGER";
    public static final String IMAGES_INTENT = "com.krayzk9s.imgurholo.IMAGES";
    public static final String COMMENTS_INTENT = "com.krayzk9s.imgurholo.COMMENTS";
    public static final String ALBUMS_INTENT = "com.krayzk9s.imgurholo.ALBUMS";
    public static final String MESSAGE_INTENT = "com.krayzk9s.imgurholo.MESSAGES";
    public static final String ACCOUNT_INTENT = "com.krayzk9s.imgurholo.ACCOUNT";
	public static final String IMAGE_INTENT = "com.krayzk9s.imgurholo.IMAGE";
    public static final String HOLO_DARK = "Holo Dark";
    public static final String HOLO_LIGHT = "Holo Light";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		apiCall = new ApiCall();
		apiCall.setSettings(settings);
		theme = settings.getString("theme", MainActivity.HOLO_LIGHT);
		if (theme.equals(MainActivity.HOLO_LIGHT))
			setTheme(R.style.AppTheme);
		else
			setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (NoSuchFieldException e) {
            // Ignore
            Log.e("Error!", e.toString());
        } catch (IllegalAccessException e) {
            // Ignore
            Log.e("Error!", e.toString());
        }

        if(Integer.parseInt(settings.getString(getString(R.string.icon_size), getString(R.string.onetwenty))) < 120) { //getting rid of 90 because it may crash the app for large screens
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(getString(R.string.icon_size), getString(R.string.onetwenty));
            editor.commit();
        }
        setContentView(R.layout.activity_other);
        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    public ApiCall getApiCall() {
        return apiCall;
    }

    @Override
    public boolean onCreateOptionsMenu(
            Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (getApiCall().settings.getString("theme", MainActivity.HOLO_LIGHT).equals(MainActivity.HOLO_LIGHT))
            inflater.inflate(R.menu.main, menu);
        else
            inflater.inflate(R.menu.main_dark, menu);
        menu.findItem(R.id.action_settings).setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                /*
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                NavUtils.navigateUpTo(this, intent);*/
                return true;
            case R.id.action_settings:
                    Intent myIntent = new Intent(this, SettingsActivity.class);
                    startActivity(myIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void imageSelected(ArrayList<JSONParcelable> data, int position) {
        FrameLayout displayFrag = (FrameLayout) findViewById(R.id.frame_layout_child);
        if (displayFrag == null) {
            // DisplayFragment (Fragment B) is not in the layout (handset layout),
            // so start DisplayActivity (Activity B)
            // and pass it the info about the selected item
            if(getApiCall().settings.getBoolean("ImagePagerEnabled", false) == false) {
                Intent intent = new Intent();
                intent.putExtra("id", data.get(position));
                Log.d("data", data.get(position).toString());
                intent.setAction(ImgurHoloActivity.IMAGE_INTENT);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivity(intent);
            }
            else {
                Intent intent = new Intent();
                intent.putExtra("ids", data);
                intent.putExtra("start", position);
                intent.setAction(ImgurHoloActivity.IMAGE_PAGER_INTENT);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivity(intent);
            }
        } else {
            // DisplayFragment (Fragment B) is in the layout (tablet layout),
            // so tell the fragment to update
            try {
                if (data.get(position).getJSONObject().has("is_album") && data.get(position).getJSONObject().getBoolean("is_album")) {
                    ImagesFragment fragment = new ImagesFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("imageCall", "3/album/" + data.get(position).getJSONObject().getString("id"));
                    bundle.putString("id", data.get(position).getJSONObject().getString("id"));
                    bundle.putParcelable("albumData", data.get(position));
                    fragment.setArguments(bundle);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frame_layout_child, fragment).commitAllowingStateLoss();
                } else {
                    SingleImageFragment singleImageFragment = new SingleImageFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("gallery", true);
                    bundle.putParcelable("imageData", data.get(position));
                    singleImageFragment.setArguments(bundle);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frame_layout_child, singleImageFragment).commitAllowingStateLoss();
                }
            } catch (JSONException e) {
                Log.e("Error!", e.toString());
            }
        }
    }
}
