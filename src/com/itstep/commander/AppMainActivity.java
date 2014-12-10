package com.itstep.commander;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.webkit.MimeTypeMap;
import android.widget.*;

public class AppMainActivity extends ListActivity {

    private ArrayList<String> directoryEntries = new ArrayList<String>();
    private File currentDirectory = new File("/");
    private static final int IDM_OPEN = 101;
    private static final int IDM_COPY = 102;
    private static final int IDM_CUT = 103;
    private static final int IDM_PASTE = 104;
    private static final int IDM_DELETE = 105;
    private static final int IDM_RENAME = 106;
    private static final int IDM_INFO = 107;
    private int position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       browseTo(new File("/"));

       ListView lv = getListView();
        registerForContextMenu(lv);
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

            new AlertDialog.Builder(this)
                    .setTitle("Подтверждение")
                    .setMessage("Хотите открыть файл " + aDirectory.getName() + "?")
                    .setPositiveButton("Да", okButtonListener)
                    .setNegativeButton("Нет", cancelButtonListener)
                    .show();
        }
    }

    private void fill(File[] files) {
        ArrayList<String> names = new ArrayList<String>();
        directoryEntries.clear();
        names.clear();

        if (currentDirectory.getParent() != null) {
            directoryEntries.add("..");
            names.add("..");
        }

        for (File file : files) {
            directoryEntries.add(file.getAbsolutePath());
            names.add(file.getName());
        }

        ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this, R.layout.row, names);
        setListAdapter(directoryList);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        int selectionRowID = position;
        String selectedFileString = directoryEntries.get(selectionRowID);

        if(selectedFileString.equals("..")){
            upOneLevel();
        } else {

            File clickedFile = null;
            clickedFile = new File(selectedFileString);
            if (clickedFile != null)
                 browseTo(clickedFile);
        }
    }
    private long getDirectoryLength(File f){
        long sum = 0;
        if(f.isFile()) {
            return f.length();
        }else{
            File[] arrPath = f.listFiles();
            if(arrPath.length > 0) {
                for (File file : arrPath) {
                    sum += getDirectoryLength(file);
                }
            }else{
                return sum;
            }
        }
        return  sum;
    }
    private void delOllInDirectory(File f){
        if (f.isDirectory()) {
            File[] arrPath = f.listFiles();
            for (File file : arrPath)
                delOllInDirectory(file);
            f.delete();
        } else f.delete();
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo aMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;

        // Получаем позицию элемента в списке
        position = aMenuInfo.position;
        menu.add(Menu.NONE, IDM_OPEN, Menu.NONE, "Открыть");
        menu.add(Menu.NONE, IDM_COPY, Menu.NONE, "Копировать");
        menu.add(Menu.NONE, IDM_CUT, Menu.NONE, "Вырезать");
        menu.add(Menu.NONE, IDM_PASTE, Menu.NONE, "Вставить");
        menu.add(Menu.NONE, IDM_DELETE, Menu.NONE, "Удалить");
        menu.add(Menu.NONE, IDM_RENAME, Menu.NONE, "Переименовать");
        menu.add(Menu.NONE, IDM_INFO, Menu.NONE, "Инфо");
        menu.setHeaderTitle("Меню");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        File f = new File(directoryEntries.get(position));
        switch (item.getItemId())
        {

            case IDM_OPEN:
                browseTo(f);
                break;
            case IDM_COPY:

                break;
            case IDM_CUT:

                break;
            case IDM_PASTE:

                break;
            case IDM_DELETE:
                    if(f.isFile()){
                        File temp = f.getParentFile();
                        boolean isDel = f.delete();
                        browseTo(temp);
                        Toast.makeText(getApplicationContext(),
                                "удаление - " + isDel,
                                Toast.LENGTH_SHORT).show();
                    }else{
                        File temp = f.getParentFile();
                        delOllInDirectory(f);
                        browseTo(temp);
                    }
                break;
            case IDM_RENAME:
               // f.renameTo(File newPath)
                break;
            case IDM_INFO:

                if(f.isFile()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Информация")
                            .setMessage("тип: файл" +
                                            "\nабсолютный путь: \n" +
                                            f.getAbsolutePath() +
                                            "\nдоступно для чтения: " +
                                            (f.canRead() ? "да" : "нет") +
                                            "\nдоступно для записи: " +
                                            (f.canWrite() ? "да" : "нет") +
                                            "\nскрытый: " +
                                            (f.isHidden() ? "да" : "нет") +
                                            "\nразмер: " +
                                            f.length()/1024f/1024f + "MB" +
                                            "\nдата последней модификации: " +
                                            new Date(f.lastModified()).toString()

                            )
                            .setCancelable(false)
                            .setNegativeButton("ОК",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    })
                            .show();
                }else{
                    new AlertDialog.Builder(this)
                            .setTitle("Информация")
                            .setMessage("тип: каталог" +
                                            "\nабсолютный путь: \n" +
                                            f.getAbsolutePath() +
                                            "\nдоступно для чтения: " +
                                            (f.canRead() ? "да" : "нет") +
                                            "\nскрытый: " +
                                            (f.isHidden() ? "да" : "нет") +
                                            "\nразмер: " +
                                            getDirectoryLength(f)/1024f/1024f + "MB" +
                                            "\nдата последней модификации: " +
                                            new Date(f.lastModified()).toString()
                            )
                            .setCancelable(false)
                            .setNegativeButton("ОК",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    })
                            .show();
                }
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }
}
