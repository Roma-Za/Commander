package com.itstep.commander;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.*;

public class AppMainActivity extends ListActivity {
    private ArrayList<String> directoryEntries = new ArrayList<String>();
    private File currentDirectory = new File("/");
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       browseTo(new File("/"));

       ListView lv = getListView();
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {

                Toast toast = Toast.makeText(getApplicationContext(),
                        "контекстное меню!", Toast.LENGTH_SHORT);
                toast.show();

                return true;
            }
        });
    }

    private void upOneLevel(){
        if(currentDirectory.getParent() != null) {
            browseTo(currentDirectory.getParentFile());
        }
    }

    private void browseTo(final File aDirectory){
        if (aDirectory.isDirectory()){
            currentDirectory = aDirectory;
            fill(aDirectory.listFiles());
            TextView titleManager = (TextView) findViewById(R.id.titleManager);
            titleManager.setText(aDirectory.getAbsolutePath());
        } else {
            OnClickListener okButtonListener = new OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1) {

                    String mime = get_mime_by_filename(aDirectory.getAbsolutePath());
                    Intent intent1 = new Intent();
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent1.setAction(android.content.Intent.ACTION_VIEW);

                    intent1.setDataAndType(Uri.fromFile(new File(aDirectory
                            .getAbsolutePath())), mime);

                    try {
                        startActivity(intent1);
                    } catch (ActivityNotFoundException e) {

                        Toast.makeText(getApplicationContext(),
                                "Couldn't open: unknown file type",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            };
            OnClickListener cancelButtonListener = new OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
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
    public String get_mime_by_filename(String filename){
        String ext;
        String type;

        int lastdot = filename.lastIndexOf(".");
        if(lastdot > 0){
            ext = filename.substring(lastdot + 1);
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(ext);
            if(type != null) {
                return type;
            }
        }
        return "application/octet-stream";
    }
}
