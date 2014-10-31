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
			Log.e("RECORD_FOLDER_EXIST", "The folder has been existed:" + path);
		}
		// Log.e("RECORD_DIR", path);
		return path;
	}

	public String getRecordDirByFileName(String fileName) {
		String path = RECORDS_FILE_FOLDER + File.separator + fileName;
		// Log.e("RECORD_DIR", path);
		return path;
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

}
