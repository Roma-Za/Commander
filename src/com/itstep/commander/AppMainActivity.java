package com.itstep.commander;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AppMainActivity extends ListActivity {
    private ArrayList<String> directoryEntries = new ArrayList<String>();
    private File currentDirectory = new File("/");
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       browseTo(new File("/"));
    }

    private void upOneLevel(){
        if(currentDirectory.getParent() != null) {
            browseTo(currentDirectory.getParentFile());
        }
    }

    private void browseTo(final File aDirectory){
        //if we want to browse directory
        if (aDirectory.isDirectory()){
            //fill list with files from this directory
            currentDirectory = aDirectory;
            fill(aDirectory.listFiles());

            //set titleManager text
            TextView titleManager = (TextView) findViewById(R.id.titleManager);
            titleManager.setText(aDirectory.getAbsolutePath());
        } else {
            //if we want to open file, show this dialog:
            //listener when YES button clicked
            OnClickListener okButtonListener = new OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1) {
                    //intent to navigate file
                    Intent i = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("file://" + aDirectory.getAbsolutePath()));
                    //start this activity
                    startActivity(i);
                }
            };
            //listener when NO button clicked
            OnClickListener cancelButtonListener = new OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    //do nothing
                    //or add something you want
                }
            };

            //create dialog
            new AlertDialog.Builder(this)
                    .setTitle("Подтверждение") //title
                    .setMessage("Хотите открыть файл "+ aDirectory.getName() + "?") //message
                    .setPositiveButton("Да", okButtonListener) //positive button
                    .setNegativeButton("Нет", cancelButtonListener) //negative button
                    .show(); //show dialog
        }
    }

    private void fill(File[] files) {
        ArrayList<String> names = new ArrayList<String>();
        //clear list
        directoryEntries.clear();
        names.clear();

        if (currentDirectory.getParent() != null) {
            directoryEntries.add("..");
            names.add("..");
        }

        //add every file into list
        for (File file : files) {
            directoryEntries.add(file.getAbsolutePath());
            names.add(file.getName());
        }

        //create array adapter to show everything
        ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this, R.layout.row, names);
        setListAdapter(directoryList);
    }

    //when you clicked onto item
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //get selected file name
        int selectionRowID = position;
        String selectedFileString = directoryEntries.get(selectionRowID);

        //if we select ".." then go upper
        if(selectedFileString.equals("..")){
            upOneLevel();
        } else {
            //browse to clicked file or directory using browseTo()
            File clickedFile = null;
            clickedFile = new File(selectedFileString);
            if (clickedFile != null)
                 browseTo(clickedFile);
        }
    }
}
