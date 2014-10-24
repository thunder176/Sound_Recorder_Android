package edu.bjtu.group1.SoundRecorder;

import java.io.File;
import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Environment;
import android.util.Log;

public class MediaReview implements MediaPlayer.OnPreparedListener {
	private static MediaReview mMediaReview;

	private static MediaPlayer mMediaPlayer = null;

	private MediaReview() {
	}

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static MediaReview getInstance() {
		if (null == mMediaReview) {
			synchronized (MediaReview.class) {
				if (null == mMediaReview)
					mMediaReview = new MediaReview();
			}
		}
		return mMediaReview;
	}

	public void playRecordFileByName(String fileName) {
		String path = getRecordStorageDir() + File.separator + fileName;
		try {
			if (null == mMediaPlayer) {
				mMediaPlayer = new MediaPlayer();
			} else {
				stopPlay();
				mMediaPlayer = new MediaPlayer();
			}
			mMediaPlayer.setDataSource(path);
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnErrorListener(new OnErrorListener() {
				 
				 public boolean onError(MediaPlayer mp, int what, int extra) {
				 // 发生错误，停止录制
					 mMediaPlayer.stop();
					 mMediaPlayer.release();
					 mMediaPlayer = null;
					 Log.e("MediaPlayer_setOnErrorListener", "ERROR HAPPENED");
					 return false;
				 }
				 });
			mMediaPlayer.prepareAsync();
		} catch (IllegalArgumentException e) {
			Log.e("PLAYRECORD_IllegalArgumentException", e.getMessage());
		} catch (SecurityException e) {
			Log.e("PLAYRECORD_SecurityException", e.getMessage());
		} catch (IllegalStateException e) {
			Log.e("PLAYRECORD_IllegalStateException", e.getMessage());
		} catch (IOException e) {
			Log.e("PLAYRECORD_IOException", e.getMessage());
		}
	}

	private String getRecordStorageDir() {
		String path = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_MUSIC).getAbsolutePath();
		Log.e("RECORD_DIR", path);
		return path;
	}

	public void onPrepared(MediaPlayer arg0) {
		mMediaPlayer.start();		
	}
	
	public void stopPlay() {
		mMediaPlayer.stop();
		mMediaPlayer.release();
		mMediaPlayer = null;
	}
}
