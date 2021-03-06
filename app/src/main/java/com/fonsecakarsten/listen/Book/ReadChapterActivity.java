package com.fonsecakarsten.listen.Book;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.fonsecakarsten.listen.Database.BookChapterDbHelper;
import com.fonsecakarsten.listen.Database.BookContract.bookChapterEntry;
import com.fonsecakarsten.listen.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import static com.fonsecakarsten.listen.Database.BookContract.bookChapterEntry.TABLE_NAME;

/**
 * Created by Karsten on 7/16/2017.
 */

public class ReadChapterActivity extends AppCompatActivity {

    private TextToSpeech speech;
    private myPagerAdapter adapter;
    private ViewPager mViewPager;
    private String bookTitle;
    private String chapterTitle;
    private ArrayList<String> chapterText = new ArrayList<>();
    private FloatingActionButton speakBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_chapter_activity);

        // Get the chapter data
        Bundle extras = getIntent().getExtras();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.app_bar);
        chapterTitle = extras.getString("CHAPTER_NAME");
        toolbar.setTitle(chapterTitle);
        bookTitle = extras.getString("BOOK_TITLE");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new myPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(adapter);
        getChapter();

        speakBTN = findViewById(R.id.FAB3);
        speakBTN.bringToFront();
        speakBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });

    }

    private void getChapter() {
        BookChapterDbHelper mDbHelper = new BookChapterDbHelper(this, bookTitle);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String selection = bookChapterEntry.COLUMN_NAME_CHAPTER_TITLE + "=?";
        String[] selectionArgs = {chapterTitle};
        String[] columnsToReturn = {bookChapterEntry.COLUMN_NAME_CHAPTER_DATA};

        // SELECT all_columns FROM table_name WHERE bookChapterEntry._ID == chapter
        Cursor c = db.query(
                TABLE_NAME,    // The table to query
                columnsToReturn,                // The columns to return
                selection,                      // The columns for the WHERE clause
                selectionArgs,                  // The values for the WHERE clause
                null,                           // don't group the rows
                null,                           // don't filter by row groups
                null);                          // The sort order
        c.moveToNext();
        int chapterDataColumn = c.getColumnIndex(bookChapterEntry.COLUMN_NAME_CHAPTER_DATA);
        final String idk = c.getString(chapterDataColumn);
        c.close();

        // CONVERT JSON INTO ARRAYLIST IN BACKGROUND
        new AsyncTask<String, Void, ArrayList>() {
            @Override
            protected ArrayList doInBackground(String... params) {
                ArrayList<String> text = new ArrayList<>();
                try {
                    JSONObject json = new JSONObject(idk);
                    JSONArray array = json.getJSONArray("chapterArray");
                    for (int i = 0; i < array.length(); i++) {
                        text.add(array.getString(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return text;
            }

            @Override
            protected void onPostExecute(ArrayList arrayList) {
                super.onPostExecute(arrayList);
                if (arrayList.size() > 0) {
                    chapterText = arrayList;
                    adapter.notifyDataSetChanged();
                }
            }
        }.execute(idk);
    }

    private void speak() {
        if (speech == null) {
            speech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status != TextToSpeech.ERROR) {
                        speech.setLanguage(Locale.getDefault());
                        String utteranceId = this.hashCode() + "";
                        speech.speak(chapterText.get(mViewPager.getCurrentItem()), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                        speakBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause, null));

                    }
                }
            });
        } else {
            speech.stop();
            speakBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play, null));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return (super.onOptionsItemSelected(item));
    }

    public void onPause() {
        if (speech != null) {
            speech.stop();
            speech.shutdown();
        }
        super.onPause();
    }


    private class myPagerAdapter extends FragmentStatePagerAdapter {
        myPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            Fragment fragment = new PageFragment();
            Bundle bundleArgs = new Bundle();
            bundleArgs.putString("PageText", chapterText.get(i));
            fragment.setArguments(bundleArgs);
            return fragment;
        }

        @Override
        public int getCount() {
            return chapterText.size();
        }
    }


}
