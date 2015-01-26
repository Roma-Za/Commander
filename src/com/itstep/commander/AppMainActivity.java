package com.itstep.commander;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.webkit.MimeTypeMap;
import android.widget.*;

public class AppMainActivity extends ListActivity {

    private ArrayList<String> directoryEntries = new ArrayList<String>();
    private File startDirectory = new File("storage");
    private File currentDirectory = startDirectory;
    private static final int IDM_OPEN = 101;
    private static final int IDM_COPY = 102;
    private static final int IDM_CUT = 103;
    private static final int IDM_PASTE = 104;
    private static final int IDM_DELETE = 105;
    private static final int IDM_RENAME = 106;
    private static final int IDM_NEW = 107;
    private static final int IDM_INFO = 108;

    private int position;
    private String PathFrom = "";
    private Boolean isCopy = true;
    private SharedPreferences mSettings;
    private static final String PREFS_NAME = "ManagerPrefsFile";
    private static final String PREFS_HIDDEN = "hidden";
    private Boolean isShowHidden = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if(getLastNonConfigurationInstance()!=null)
            currentDirectory = (File)getLastNonConfigurationInstance();

        mSettings = getSharedPreferences(PREFS_NAME, 0);

        if(mSettings.contains(PREFS_HIDDEN)) {
            isShowHidden = mSettings.getBoolean(PREFS_HIDDEN, false);
        }

       browseTo(currentDirectory);

       ListView lv = getListView();
        registerForContextMenu(lv);
    }
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isCopy", isCopy);
        outState.putString("PathFrom", PathFrom);
    }
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isCopy = savedInstanceState.getBoolean("isCopy");
        PathFrom = savedInstanceState.getString("PathFrom");
    }

    public Object onRetainNonConfigurationInstance() {
        return currentDirectory;
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
            if(currentDirectory.getParentFile() != null)
            if(!((currentDirectory.getParent()).equals("/"))){
            directoryEntries.add("..");
            names.add("..");
            }

        for (File file : files) {

                if(file.isHidden()){
                    if(isShowHidden) {
                        directoryEntries.add(file.getAbsolutePath());
                        names.add(file.getName());
                    }
                }else{
                    directoryEntries.add(file.getAbsolutePath());
                    names.add(file.getName());
                }
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
        menu.add(Menu.NONE, IDM_NEW, Menu.NONE, "Новая папка");
        menu.add(Menu.NONE, IDM_INFO, Menu.NONE, "Инфо");
        menu.setHeaderTitle("Меню");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        final String path = directoryEntries.get(position);
        if(path!="..") {
            final File f = new File(path);
            switch (item.getItemId()) {
                case IDM_OPEN:
                    browseTo(f);
                    break;
                case IDM_COPY:
                    if(!(path.equals("/storage/sdcard0")) && !(path.equals("/storage/sdcard1"))) {
                        PathFrom = path;
                        isCopy = true;
                    }
                    break;
                case IDM_CUT:
                    if(!(path.equals("/storage/sdcard0")) && !(path.equals("/storage/sdcard1"))) {
                        PathFrom = path;
                        isCopy = false;
                    }
                    break;
                case IDM_PASTE:
                    if(!(path.equals("/storage/sdcard0")) && !(path.equals("/storage/sdcard1"))) {
                        FileManager fm = new FileManager();
                        if (f.isDirectory()) {
                            fm.paste(PathFrom, path);
                            browseTo(f);
                        } else {
                            String p = f.getParent();
                            fm.paste(PathFrom, p);
                            browseTo(new File(p));
                        }
                        if (!isCopy) fm.delDirectory(new File(PathFrom));
                    }
                    break;
                case IDM_DELETE:
                    if(!(path.equals("/storage/sdcard0")) && !(path.equals("/storage/sdcard1"))) {
                        OnClickListener okButtonListener = new OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {

                                File temp = f.getParentFile();
                                FileManager fm = new FileManager();
                                fm.delDirectory(f);
                                browseTo(temp);
                            }

                        };
                        OnClickListener cancelButtonListener = new OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                Toast.makeText(getApplicationContext(),
                                        "ок, отмена ",
                                        Toast.LENGTH_SHORT).show();
                            }
                        };

                        new AlertDialog.Builder(this)
                                .setTitle("Подтверждение")
                                .setMessage("Хотите удалить " + f.getName() + "?")
                                .setPositiveButton("Да", okButtonListener)
                                .setNegativeButton("Нет", cancelButtonListener)
                                .show();
                    }
                    break;
                case IDM_RENAME:
                    if(!(path.equals("/storage/sdcard0")) && !(path.equals("/storage/sdcard1"))) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(this);
                        alert.setTitle("Переименование");
                        final EditText input = new EditText(this);
                        input.setText(f.getName());
                        alert.setView(input);

                        alert.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String value = input.getText().toString();
                                String temp = f.getParent();
                                f.renameTo(new File(temp + "/" + value));
                                browseTo(new File(temp));
                            }
                        });

                        alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(getApplicationContext(),
                                        "ок, отмена ",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                        alert.show();
                    }
                    break;
                case IDM_NEW:
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setTitle("Создать папку с именем:");
                    final EditText input = new EditText(this);
                    alert.setView(input);

                    alert.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String value = input.getText().toString();
                                    if (isTrueName(value, f)) {
                                        if (f.isDirectory()) {
                                            FileManager fm = new FileManager();
                                            if (!fm.newFolder(f, value)) {
                                                Toast.makeText(getApplicationContext(),
                                                        "Не удалось создать папку",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                            browseTo(f);
                                        } else {
                                            File PF = f.getParentFile();
                                            if (isTrueName(value, PF)) {
                                                FileManager fm = new FileManager();
                                                if (!fm.newFolder(PF, value)) {
                                                    Toast.makeText(getApplicationContext(),
                                                            "Не удалось создать папку",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                                browseTo(PF);
                                            }
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "Имя не подходит",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                    });

                    alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Toast.makeText(getApplicationContext(),
                                    "ок, отмена ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    alert.show();

                    break;
                case IDM_INFO:
                    new AlertDialog.Builder(this)
                            .setTitle("Информация")
                            .setMessage(new FileManager().getInfo(f))
                            .setCancelable(false)
                            .setNegativeButton("ОК",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    })
                            .show();
                    break;
                default:
                    return super.onContextItemSelected(item);
            }
        }
        return true;
    }

    private boolean isTrueName(String value, File f) {
        if(f.isFile()) f = f.getParentFile();
        String [] names = f.list();
        for (String s : names){
          if(s.equals(value)) {
              return false;
          }
        }

        return  true;
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
      if(keycode == KeyEvent.KEYCODE_BACK && !currentDirectory.getAbsolutePath().equals("/storage")) {
          upOneLevel();
            return true;

        } else if(keycode == KeyEvent.KEYCODE_BACK && currentDirectory.getAbsolutePath().equals("/storage")) {
          finish();

          return false;
      }
        return false;
    }

    private int group1Id = 1;

    int searchId = Menu.FIRST;
    int hiddenId = Menu.FIRST +1;
    int themeId = Menu.FIRST +2;
    int aboutId = Menu.FIRST +3;
    int exitId = Menu.FIRST +4;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(group1Id, searchId, searchId, "Поиск");
        menu.add(group1Id, hiddenId, hiddenId, "Скрытые файлы");
        menu.add(group1Id, themeId, themeId, "Темы");
        menu.add(group1Id, aboutId, aboutId, "О программе");
        menu.add(group1Id, exitId, exitId, "Выход");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case 1:

                return true;

            case 2:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Показывать скрытые файлы?");
                alert.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        setHiddenFlag(true);
                    }
                });

                alert.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        setHiddenFlag(false);
                    }
                });

                alert.show();
                return true;

            case 3:

                return true;

            case 4:

                return true;

            case 5:
                finish();
                return true;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void setHiddenFlag(Boolean flag) {
        isShowHidden = flag;

        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(PREFS_HIDDEN, isShowHidden);
        editor.apply();
        browseTo(currentDirectory);
    }
}
