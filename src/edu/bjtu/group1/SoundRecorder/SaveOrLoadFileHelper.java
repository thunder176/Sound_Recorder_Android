package edu.bjtu.group1.SoundRecorder;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.Log;

public class SaveOrLoadFileHelper {

	private static SaveOrLoadFileHelper mSaveOrLoadFileHelper;
	private final String RECORDS_FILE_FOLDER = Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
			.getAbsolutePath()
			+ File.separator + "MySoundRecords";
	private final String RECORDS_KIICLOUD_FILE_FOLDER = Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
			.getAbsolutePath()
			+ File.separator + "MySoundRecords" + File.separator + "kiiClouds";
	public static final String PREFS_NAME = "bjtu_sound_recorder_group1";
	public static final String KEY_TOEKN = "token";

	private SaveOrLoadFileHelper() {
	}

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static SaveOrLoadFileHelper getInstance() {
		if (null == mSaveOrLoadFileHelper) {
			synchronized (SaveOrLoadFileHelper.class) {
				if (null == mSaveOrLoadFileHelper)
					mSaveOrLoadFileHelper = new SaveOrLoadFileHelper();
			}
		}
		return mSaveOrLoadFileHelper;
	}

	public String[] loadRecorderFiles() {
		File f = new File(RECORDS_FILE_FOLDER);
		String[] fList = f.list();
		ArrayList<String> arStr = new ArrayList<String>();
		for (String str : fList) {
			if (str.endsWith("amr") || str.endsWith("3gp")) {
				arStr.add(str);
			}
		}
		String[] result = new String[arStr.size()];
		arStr.toArray(result);
		return result;
	}

	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	public String getRecordStorageDir() {
		String path = RECORDS_FILE_FOLDER;
		new File(path).mkdirs();
		// Log.e("RECORD_DIR", path);
		return path;
	}

	public String getKiiDirByFileName(String fileName) {
		String path = RECORDS_KIICLOUD_FILE_FOLDER;
		new File(path).mkdirs();
		return path + File.separator + fileName;
	}

	public String getRecordDirByFileName(String fileName) {
		String path = RECORDS_FILE_FOLDER;
		new File(path).mkdirs();
		// Log.e("RECORD_DIR", path);
		return path + File.separator + fileName;
	}

	public boolean deleteRecordByFileName(String strFileName) {
		try {
			File file = new File(getRecordDirByFileName(strFileName));
			if (file.isFile() && file.exists()) {
				// deleteFile(path);
				if (file.delete()) {
					Log.e("SaveOrLoadFileHelper::deleteRecordByFileName",
							"success");
					return true;
				} else {
					Log.e("SaveOrLoadFileHelper::deleteRecordByFileName",
							"failed");
				}
			}
		} catch (Exception e) {
			Log.e("SaveOrLoadFileHelper::deleteRecordByFileName",
					e.getMessage());
		}
		return false;
	}

	public int renameRecordByFileName(String oldName, String newName) {
		if (!oldName.equals(newName)) {// 新的文件名和以前文件名不同时,才有必要进行重命名
			File oldfile = new File(getRecordDirByFileName(oldName));
			File newfile = new File(getRecordDirByFileName(newName));
			if (!oldfile.exists()) {
				// 重命名文件不存在
				return 1;
			}
			if (newfile.exists()) {
				// 若在该目录下已经有一个文件和新文件名相同，则不允许重命名
				return 2;
			} else {
				oldfile.renameTo(newfile);
			}
		} else {
			System.out.println("新文件名和旧文件名相同...");
			return 3;
		}
		return 0;
	}

	public void saveToken(Context context, String token) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		Editor e = prefs.edit();
		e.putString(KEY_TOEKN, token);
		e.commit();
		Log.e("SaveOrLoadFileHelper::saveToken", token);
	}

	public String getToken(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		String token = prefs.getString(KEY_TOEKN, "");
		Log.e("SaveOrLoadFileHelper::getToken", token);
		return token;
	}

	public String getLastModifiedTime(String fileName) {
		File f = new File(getRecordDirByFileName(fileName));
		return Long.toString(f.lastModified());
	}

}
