package com.itstep.commander;

import java.io.File;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.webkit.MimeTypeMap;
import android.widget.*;
import android.view.Menu;

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
    private Context cntx;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        cntx = this;
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
                                getString(R.string.noOpen),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            };
            OnClickListener cancelButtonListener = new OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                }
            };

            new AlertDialog.Builder(this)
                    .setTitle(R.string.confirm)
                    .setMessage(getString(R.string.doOpen) +" "+ aDirectory.getName() + "?")
                    .setPositiveButton(R.string.y, okButtonListener)
                    .setNegativeButton(R.string.n, cancelButtonListener)
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

        position = aMenuInfo.position;
        menu.add(Menu.NONE, IDM_OPEN, Menu.NONE, R.string.open);
        menu.add(Menu.NONE, IDM_COPY, Menu.NONE, R.string.copy);
        menu.add(Menu.NONE, IDM_CUT, Menu.NONE, R.string.cut);
        menu.add(Menu.NONE, IDM_PASTE, Menu.NONE, R.string.paste);
        menu.add(Menu.NONE, IDM_DELETE, Menu.NONE, R.string.delete);
        menu.add(Menu.NONE, IDM_RENAME, Menu.NONE, R.string.rename);
        menu.add(Menu.NONE, IDM_NEW, Menu.NONE, R.string.newF);
        menu.add(Menu.NONE, IDM_INFO, Menu.NONE, R.string.aboutM);
        menu.setHeaderTitle(R.string.menu);
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
                        FileManager fm = new FileManager(cntx);
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
                                FileManager fm = new FileManager(cntx);
                                fm.delDirectory(f);
                                browseTo(temp);
                            }

                        };
                        OnClickListener cancelButtonListener = new OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                Toast.makeText(getApplicationContext(),
                                        R.string.okCancel,
                                        Toast.LENGTH_SHORT).show();
                            }
                        };

                        new AlertDialog.Builder(this)
                                .setTitle(R.string.confirm)
                                .setMessage(getString(R.string.isDelete) +" "+ f.getName() + "?")
                                .setPositiveButton(R.string.y, okButtonListener)
                                .setNegativeButton(R.string.n, cancelButtonListener)
                                .show();
                    }
                    break;
                case IDM_RENAME:
                    if(!(path.equals("/storage/sdcard0")) && !(path.equals("/storage/sdcard1"))) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(this);
                        alert.setTitle(R.string.rename);
                        final EditText input = new EditText(this);
                        input.setText(f.getName());
                        alert.setView(input);

                        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String value = input.getText().toString();
                                String temp = f.getParent();
                                f.renameTo(new File(temp + "/" + value));
                                browseTo(new File(temp));
                            }
                        });

                        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(getApplicationContext(),
                                        R.string.okCancel,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                        alert.show();
                    }
                    break;
                case IDM_NEW:
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setTitle(R.string.newFoldName);
                    final EditText input = new EditText(this);
                    alert.setView(input);

                    alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String value = input.getText().toString();
                                    if (isTrueName(value, f)) {
                                        if (f.isDirectory()) {
                                            FileManager fm = new FileManager(cntx);
                                            if (!fm.newFolder(f, value)) {
                                                Toast.makeText(getApplicationContext(),
                                                        R.string.noCrFold,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                            browseTo(f);
                                        } else {
                                            File PF = f.getParentFile();
                                            if (isTrueName(value, PF)) {
                                                FileManager fm = new FileManager(cntx);
                                                if (!fm.newFolder(PF, value)) {
                                                    Toast.makeText(getApplicationContext(),
                                                            R.string.noCrFold,
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                                browseTo(PF);
                                            }
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                R.string.noName,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                    });

                    alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Toast.makeText(getApplicationContext(),
                                    R.string.okCancel,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    alert.show();

                    break;
                case IDM_INFO:
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.aboutM)
                            .setMessage(new FileManager(cntx).getInfo(f))
                            .setCancelable(false)
                            .setNegativeButton(R.string.ok,
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.search:

                return true;

            case R.id.hidden:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.showHidFile);
                alert.setPositiveButton(R.string.y, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        setHiddenFlag(true);
                    }
                });

                alert.setNegativeButton(R.string.n, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        setHiddenFlag(false);
                    }
                });

                alert.show();
                return true;

            case R.id.aboutId:
                AlertDialog.Builder alertInfo = new AlertDialog.Builder(this);
                alertInfo.setTitle(R.string.aboutM);
                alertInfo.setMessage(R.string.info);
                alertInfo.setCancelable(false);
                alertInfo.setNegativeButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                alertInfo.show();
                return true;

            case R.id.exitId:
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
