package com.mrbttf.translator2;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity
{
    public static final String G_LOG = "Translator";

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    public String translatedText;

    private String filename = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(mSectionsPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position)
            {
                tabChanged(position);
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        translatedText="";

    }

    private void tabChanged(int position)
    {
        if(translatedText.isEmpty()) return;
        PlaceholderFragment curFragment = (PlaceholderFragment) mSectionsPagerAdapter.getItem(position);
        curFragment.setTranslation(translatedText);
        mSectionsPagerAdapter.notifyDataSetChanged();
        translatedText="";

    }

    //Called when button pressed
    private void translateText()
    {
        PlaceholderFragment curFragment = (PlaceholderFragment) mSectionsPagerAdapter.getItem(viewPager.getCurrentItem());
        translatedText = Translator.translateText(curFragment.getTranslation(),viewPager.getCurrentItem() != 1);
        viewPager.setCurrentItem(viewPager.getCurrentItem()==1 ? 0 : 1 ,true);
    }

    //Open enter text dialog
    private void EnterFilename()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.savefile_info);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(filename);
        builder.setView(input);

        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                    }
                });
        builder.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                input.setText("");
            }
        });


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                filename = input.getText().toString();
                if (filename.isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(), "File name cannot be empty", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    showSaveDialog();
                    dialog.dismiss();
                }
            }

        });
    }

    private void showSaveDialog()
    {
        FileSaver fileSaver = new FileSaver(this);
        fileSaver.setDirListener(new FileSaver.DirSelectedListener() {
            @Override
            public void dirSelected(File dir) {
                Log.d(G_LOG,dir.getName());
                File file = new File(dir, filename);
                try {
                    Log.d(G_LOG, String.valueOf(file.createNewFile()));
                } catch (IOException e) {
                    Log.d(G_LOG, "Can't create file");
                }
                FileWriter fw = null;
                try {
                    fw = new FileWriter(file.getAbsoluteFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BufferedWriter bw = new BufferedWriter(fw);
                PlaceholderFragment curFragment = (PlaceholderFragment) mSectionsPagerAdapter.getItem(viewPager.getCurrentItem());
                try {
                    bw.write(curFragment.getTranslation());
                    bw.close();
                } catch (IOException e)
                {
                    Log.d(G_LOG, e.getMessage());
                }
                finally {
                    Toast toast = Toast.makeText(getApplicationContext(), "Text is saved to file", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        }).showDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int id = item.getItemId();

        if(id == R.id.action_browse)
        {
            FileChooser fileChooser = new FileChooser(this);
            fileChooser.setFileListener(new FileChooser.FileSelectedListener() {
                @Override
                public void fileSelected(final File file)
                {
                    String content = null;
                    try {
                        content = new Scanner(new FileInputStream(file), "UTF-8").useDelimiter("\\Z").next();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    PlaceholderFragment curFragment = (PlaceholderFragment) mSectionsPagerAdapter.getItem(viewPager.getCurrentItem());
                    curFragment.setTranslation(content);
                    mSectionsPagerAdapter.notifyDataSetChanged();

                }
            }).showDialog();

            return true;
        } else if(id == R.id.action_save) {
            EnterFilename();
            return true;
        }
        else if(id == R.id.action_about)
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Made by MrBTTF\n2016", Toast.LENGTH_SHORT);
            toast.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
    {

        MainActivity mainActivity;
        private EditText editTextSource;
        private Button buttonTranslate;

        public PlaceholderFragment() {}

        public static PlaceholderFragment newInstance(boolean engFra) {
            return new PlaceholderFragment();
        }

        @Override
        public void onAttach(Activity activity)
        {
            super.onAttach(activity);
            mainActivity = (MainActivity) activity;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            editTextSource = (EditText) rootView.findViewById(R.id.editText_source);

            buttonTranslate = (Button) rootView.findViewById(R.id.button_translate);
            buttonTranslate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mainActivity.translateText();
                }
            });

            Button buttonClear = (Button) rootView.findViewById(R.id.button_clear);
            buttonClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    editTextSource.setText("");
                }
            });
            //The spike which props layout
            Space space = (Space) rootView.findViewById(R.id.space);
            space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,mainActivity.tabLayout.getMeasuredHeight()));

            return rootView;
        }


        public void setTranslation(String content)
        {
            editTextSource.setText(content);
        }

        public String getTranslation()
        {
            return editTextSource.getText().toString();
        }


    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {

        PlaceholderFragment fragmentEngFra;
        PlaceholderFragment fragmentFraEng;

        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            if(position==0) {
                if(fragmentEngFra==null)
                    fragmentEngFra = PlaceholderFragment.newInstance(true);
                return fragmentEngFra;
            }
            else {
                if(fragmentFraEng==null)
                   fragmentFraEng = PlaceholderFragment.newInstance(false);
                return fragmentFraEng;
            }
        }



        @Override
        public int getCount()
        {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "English to French";
                case 1:
                    return "French to English";
            }
            return null;
        }


    }
}
