package edu.bjtu.group1.SoundRecorder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.SystemClock;
import android.util.Log;

public class MediaCapture {

	private static MediaCapture mMediaCapture;

	private static MediaRecorder mRecorder = null;

	private String mstr_lastRecordFileName = null;

	private long ml_captureTime = 0;

	private MediaCapture() {
	}

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static MediaCapture getInstance() {
		if (null == mMediaCapture) {
			synchronized (MediaCapture.class) {
				if (null == mMediaCapture)
					mMediaCapture = new MediaCapture();
			}
		}
		return mMediaCapture;
	}

	public int startRecording() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		// Before Save a File on External Storage
		if (!SaveOrLoadFileHelper.getInstance().isExternalStorageReadable()) {
			return -1;
		}

		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd hh_mm_ss");
		String date = sDateFormat.format(new java.util.Date());
		mstr_lastRecordFileName = date + ".amr";
		mRecorder.setOutputFile(SaveOrLoadFileHelper.getInstance()
				.getRecordStorageDir() + File.separator + date + ".amr");

		try {
			mRecorder.prepare();
		} catch (IllegalStateException e) {
			Log.e("RECORDER_PREPARE", e.getMessage());
		} catch (IOException e) {
			Log.e("RECORDER_PREPARE", e.getMessage());
		}

		mRecorder.setOnErrorListener(new OnErrorListener() {

			public void onError(MediaRecorder arg0, int arg1, int arg2) {
				mRecorder.stop();
				mRecorder.release();
				mRecorder = null;
				Log.e("MediaRecorder_setOnErrorListener", "ERROR HAPPENED");
			}
		});
		ml_captureTime = SystemClock.elapsedRealtime();
		mRecorder.start();

		return 0;
	}

	public String getLastRecordFileName() {
		return mstr_lastRecordFileName;
	}

	public void stopRecording() {
		ml_captureTime = 0;
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}

	public long getCaptureTime() {
		return ml_captureTime;
	}

}
