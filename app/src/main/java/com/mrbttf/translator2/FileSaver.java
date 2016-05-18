package com.mrbttf.translator2;

import android.app.Activity;
import android.app.Dialog;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

public class FileSaver {
    private static final String PARENT_DIR = "..";

    private final Activity activity;
    private ListView list;
    private Dialog dialog;
    private File currentPath;

    // filter on file extension
    private String extension = null;
    public void setExtension(String extension) {
        this.extension = (extension == null) ? null :
                extension.toLowerCase();
    }

    // file selection event handling
    public interface DirSelectedListener {
        void dirSelected(File dir);
    }
    public FileSaver setDirListener(DirSelectedListener dirListener) {
        this.dirListener = dirListener;
        return this;
    }
    private DirSelectedListener dirListener;

    public FileSaver(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity);
        list = new ListView(activity);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                String fileChosen = list.getItemAtPosition(which).toString();
                File chosenFile = new File(fileChosen);

                if (fileChosen == "..Choose current")
                {
                    if (dirListener != null)
                    {
                        dirListener.dirSelected(currentPath.getAbsoluteFile());
                    }
                    dialog.dismiss();
                }
                else if (fileChosen == "..")
                {
                    if(currentPath != null)
                        refresh(currentPath.getParentFile());
                }
                else
                {
                    refresh(chosenFile);
                }
            }
        });
        dialog.setContentView(list);
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        refresh(Environment.getExternalStorageDirectory());
    }

    public void showDialog() {
        dialog.show();
    }


    /**
     * Sort, filter and display the files for the given path.
     */
    private void refresh(File path)
    {
        this.currentPath = path;
        if (path.exists()) {
            File[] dirs = path.listFiles(new FileFilter() {
                @Override public boolean accept(File file) {
                    return (file.isDirectory() && file.canRead());
                }
            });

            Arrays.sort(dirs);
            ArrayList<File> dirList = new ArrayList<>();
            dirList.add(new File(".."));
            dirList.add(new File("..Choose current"));
            for (File dir : dirs)
            {
                dirList.add(dir);
            }

            // refresh the user interface
            dialog.setTitle(currentPath.getPath());
            list.setAdapter(new ArrayAdapter(activity,
                    android.R.layout.simple_list_item_1, dirList.toArray()) {
                @Override public View getView(int pos, View view, ViewGroup parent) {
                    view = super.getView(pos, view, parent);
                    ((TextView) view).setSingleLine(true);
                    return view;
                }
            });
        }
    }

}