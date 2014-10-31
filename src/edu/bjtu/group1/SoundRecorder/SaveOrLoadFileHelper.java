package edu.bjtu.group1.SoundRecorder;

import java.io.File;

import android.os.Environment;
import android.util.Log;

public class SaveOrLoadFileHelper {

	private static SaveOrLoadFileHelper mSaveOrLoadFileHelper;
	private final String RECORDS_FILE_FOLDER = Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
			.getAbsolutePath()
			+ File.separator + "MySoundRecords";

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
		return f.list();
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
		if (!new File(path).mkdir()) {
			Log.e("RECORD_FOLDER_EXIST", "can't make the folder in:" + path);
		}
		Log.e("RECORD_DIR", path);
		return path;
	}

	public String getRecordDirByFileName(String fileName) {
		String path = RECORDS_FILE_FOLDER + File.separator + fileName;
		Log.e("RECORD_DIR", path);
		return path;
	}

}
