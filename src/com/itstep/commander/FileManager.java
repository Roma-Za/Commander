package com.itstep.commander;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.Date;

public class FileManager {
    private static final int BUFFER = 2048;
    private Context context;
    public FileManager(Context c){
        context = c;
    }
    public void delDirectory(File f){
        if (f.isDirectory()) {
            File[] arrPath = f.listFiles();
            for (File file : arrPath)
                delDirectory(file);
            f.delete();
        } else f.delete();
    }

    public String getInfo(File f){
        StringBuilder sb = new StringBuilder();
       sb.append(context.getString(R.string.type));
        try {
            Boolean type = f.isFile();
            sb.append(type ? context.getString(R.string.file) : context.getString(R.string.folder));
        }
        catch (Exception e){
            sb.append(context.getString(R.string.err_));
        }
        sb.append("\n");
        sb.append(context.getString(R.string.absolutPath));
        sb.append("\n");
        try {
            sb.append(f.getAbsolutePath());
        }
        catch (Exception e){
            sb.append(context.getString(R.string.err_));
        }
        sb.append("\n");
        sb.append(context.getString(R.string.isRead));
        try {
            Boolean r = f.canRead();
            sb.append(r ? context.getString(R.string.y) : context.getString(R.string.n));
        }
        catch (Exception e){
           sb.append(context.getString(R.string.err_));
        }
        sb.append("\n");
        sb.append(context.getString(R.string.isWrite));
        try {
            Boolean w = f.canWrite();
           sb.append(w? context.getString(R.string.y) : context.getString(R.string.n));
        }
        catch (Exception e){
            sb.append(context.getString(R.string.err_));
        }
        sb.append("\n");
        sb.append(context.getString(R.string.isHidden));
        try {
            Boolean h = f.isHidden();
           sb.append(h? context.getString(R.string.y) : context.getString(R.string.n));
        }
        catch (Exception e){
            sb.append(context.getString(R.string.err_));
        }
        sb.append("\n");
        sb.append(context.getString(R.string.size));
        long zizeFileByte = 0;
        try {
            zizeFileByte = getDirectoryLength(f);
            float sizeK = zizeFileByte/1024f;
            float sizeM = zizeFileByte/1024f/1024f;
            float sizeG = zizeFileByte/1024f/1024f/1024f;
            if(sizeG > 1) sb.append(sizeG + "GB");
            else if(sizeM > 1) sb.append(sizeM + "MB");
            else sb.append(sizeK + "KB");
        }
        catch (Exception e){
            sb.append(context.getString(R.string.err_));
        }
        sb.append("\n");
        sb.append(context.getString(R.string.date));
        sb.append("\n");
        try {
            sb.append(new Date(f.lastModified()).toString());
        }
        catch (Exception e){
           sb.append(context.getString(R.string.err_));
        }

        return sb.toString();
    }

    private long getDirectoryLength(File f){
        long sum = 0;

            Boolean isDir = false;
            try {
                isDir = f.isDirectory();
            } catch (Exception e) {
                Log.e("isDirectory", e.getMessage());
            }
            if (isDir) {
                File[] arrPath = f.listFiles();
                if (arrPath.length > 0)
                    for (File file : arrPath)
                        sum += getDirectoryLength(file);
            } else {
                try {
                    sum += f.length();
                } catch (Exception e) {
                    Log.e("length", e.getMessage());
                }
            }

        return  sum;
    }

    public void  paste(String from, String to ){
        File old_file = new File(from);
        File temp_dir = new File(to);
        byte[] data = new byte[BUFFER];
        int read = 0;

        if(old_file.isFile() && temp_dir.isDirectory() && temp_dir.canWrite()){
            String file_name = from.substring(from.lastIndexOf("/"), from.length());
            File cp_file = new File(to + file_name);

            try {
                BufferedOutputStream o_stream = new BufferedOutputStream(
                        new FileOutputStream(cp_file));
                BufferedInputStream i_stream = new BufferedInputStream(
                        new FileInputStream(old_file));

                while((read = i_stream.read(data, 0, BUFFER)) != -1)
                    o_stream.write(data, 0, read);

                o_stream.flush();
                i_stream.close();
                o_stream.close();

            } catch (FileNotFoundException e) {
                Log.e("FileNotFoundException", e.getMessage());

            } catch (IOException e) {
                Log.e("IOException", e.getMessage());
            }

        }else if(old_file.isDirectory() && temp_dir.isDirectory() && temp_dir.canWrite()) {
            String files[] = old_file.list();
            String dir = to + from.substring(from.lastIndexOf("/"), from.length());
            int len = files.length;

            if(!new File(dir).mkdir())
                Log.e("mkdir", ""+context.getString(R.string.noCrFold));

            for(int i = 0; i < len; i++)
                paste(from + "/" + files[i], dir);

        } else if(!temp_dir.canWrite())
            Log.e("canWrite", ""+context.getString(R.string.noWrite));

    }
    public Boolean newFolder(File target, String name){
        File f = new File(target.getAbsolutePath() + "/" + name);
        try {
            f.mkdirs();
            return true;
        }
        catch (Exception e){
            return false;
        }

    }
}
