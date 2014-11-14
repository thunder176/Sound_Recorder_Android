package edu.bjtu.group1.SoundRecorder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.util.Log;

public class MediaReview implements MediaPlayer.OnPreparedListener {
	private static MediaReview mMediaReview;

	private MediaPlayer mMediaPlayer = null;

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

	public void playRecordByFileName(String fileName) {
		String path = SaveOrLoadFileHelper.getInstance()
				.getRecordDirByFileName(fileName);
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
					// 发生错误，停止播放
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

	public String getDurationByFileName(String fileName) {
		String path = SaveOrLoadFileHelper.getInstance()
				.getRecordDirByFileName(fileName);
		if (null == mMediaPlayer) {
			mMediaPlayer = new MediaPlayer();
		} else {
			stopPlay();
			mMediaPlayer = new MediaPlayer();
		}
		try {
			mMediaPlayer.setDataSource(path);
			mMediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			Log.e("PLAYRECORD_IllegalArgumentException", e.getMessage());
		} catch (SecurityException e) {
			Log.e("PLAYRECORD_SecurityException", e.getMessage());
		} catch (IllegalStateException e) {
			Log.e("PLAYRECORD_IllegalStateException", e.getMessage());
		} catch (IOException e) {
			Log.e("PLAYRECORD_IOException", e.getMessage());
		}

		int iDuration = mMediaPlayer.getDuration();
		if (-1 != iDuration) {
			// String strDuration = new SimpleDateFormat("hh:mm:ss")
			// .format(new Date(iDuration));
			// Log.e("MediaReview::getDurationByFileName", strDuration);
			int hours = (iDuration % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
			int minutes = (iDuration % (1000 * 60 * 60)) / (1000 * 60);
			int seconds = (iDuration % (1000 * 60)) / 1000;
			String strDuration = hours + ":" + minutes + ":" + seconds;
			return strDuration;
		}
		return "";
	}

	public void onPrepared(MediaPlayer arg0) {
		if (FragmentReviewDetails.getInstance()
				.isFragmentReviewDetailsDisplay()) {
			FragmentReviewDetails.getInstance().setRecordDurationAndSeekBar(
					mMediaPlayer.getDuration());
		}
		mMediaPlayer.start();
	}

	public void stopPlay() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	public boolean deleteRecordByFileName(String strFileName) {
		return SaveOrLoadFileHelper.getInstance().deleteRecordByFileName(
				strFileName);
	}

	public int renameRecordByFileName(String oldName, String newName) {
		return SaveOrLoadFileHelper.getInstance().renameRecordByFileName(
				oldName, newName);
	}

	public int getCurrentPosition() {
		return this.mMediaPlayer.getCurrentPosition();
	}

	public void start() {
		this.mMediaPlayer.start();
		
	}

	public void pause() {
		this.mMediaPlayer.pause();
	}

	public void seekTo(int progress) {
		this.mMediaPlayer.seekTo(progress);
	}

}
