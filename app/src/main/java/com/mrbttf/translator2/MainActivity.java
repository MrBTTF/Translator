package com.mrbttf.translator2;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;
import android.widget.Space;
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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity
{
    public static final String G_LOG = "Translator";

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private HashMap<String,String> engFraMap;
    private HashMap<String,String> fraEngMap;


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

        loadDict();

    }


    private void loadDict()
    {
        engFraMap = new HashMap<>();
        fraEngMap = new HashMap<>();
        AssetManager am = getAssets();
        InputStream is = null;
        try
        {
            is = am.open("dict.txt");

            InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);

            String line1=null;
            String line2=null;
            while ((line1 = br.readLine()) != null)
            {
                line2 = br.readLine();
                fraEngMap.put(line1,line2);
                engFraMap.put(line2,line1);
            }

            br.close();
            isr.close();
            is.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void tabChanged(int position)
    {
        if(translatedText=="") return;
        PlaceholderFragment curFragment = (PlaceholderFragment) mSectionsPagerAdapter.getItem(position);
        curFragment.setTranslation(translatedText);
        mSectionsPagerAdapter.notifyDataSetChanged();
        translatedText="";

    }

    public void changePage()
    {
        viewPager.setCurrentItem(viewPager.getCurrentItem()==1 ? 0 : 1 ,true);
    }


    void EnterFilename()
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

    public HashMap<String, String> getEngFraMap() {
        return engFraMap;
    }

    public HashMap<String, String> getFraEngMap() {
        return fraEngMap;
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

        private boolean engFra;

        public PlaceholderFragment() {}

        public static PlaceholderFragment newInstance(boolean engFra) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.engFra = engFra;
            return fragment;
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

                    onTranslateButtonClick(v);
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


        public void onTranslateButtonClick(View view)
        {
            HashMap<String,String> curMap;
            if(engFra)
                curMap=mainActivity.getEngFraMap();
            else
                curMap=mainActivity.getFraEngMap();

            String text = editTextSource.getText().toString();

            if(text.isEmpty()) return;

            String[] words = text.split(" ");

            StringBuffer translation = new StringBuffer();
            for (String word : words)
            {

                String p="";
                if(isPunctuation(word.charAt(word.length()-1)))
                {
                    p= String.valueOf(word.charAt(word.length()-1));
                    word =word.substring(0, word.length() -1);
                }

               if(!curMap.containsKey(word.toLowerCase()))
                {
                    translation.append(word + p + " ");
                    continue;
                }

                StringBuffer trword = new StringBuffer(curMap.get(word.toLowerCase()).toLowerCase());
                if(Character.isUpperCase(word.charAt(0)))
                {
                    trword.setCharAt(0,Character.toUpperCase(trword.charAt(0)));
                }

                translation.append(trword + p + " ");
            }

            mainActivity.translatedText = translation.substring(0, translation.length() - 1).toString();
            mainActivity.changePage();

        }


        public boolean isPunctuation(char c) {
            return c == ','
                    || c == '.'
                    || c == '!'
                    || c == '?'
                    || c == ':'
                    || c == ';' ;
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
