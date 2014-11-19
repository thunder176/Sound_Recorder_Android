package edu.bjtu.group1.SoundRecorder;

import android.app.Fragment;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

// Singleton Pattern
public class FragmentCapture extends Fragment {

	/**
	 * The fragment argument representing the Capture UI.
	 */
	private static FragmentCapture instance_CaptureFragment = null;

	private ImageButton mbtn_capture = null;
	private TextView mtv_captureStatus = null;

	private Chronometer mTimer = null;
	// 如果录音中切换Review，停止录音；如果点Home键，保证CaptureFragment一致
	private boolean mbl_isRecording = false;

	// TODO 自动适应屏幕转换
	public FragmentCapture() {
	}

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static FragmentCapture getInstance() {
		if (instance_CaptureFragment == null) {
			synchronized (FragmentCapture.class) {
				if (null == instance_CaptureFragment)
					instance_CaptureFragment = new FragmentCapture();
			}
		}
		return instance_CaptureFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_capture, container,
				false);
		mbtn_capture = (ImageButton) rootView
				.findViewById(R.id.image_button_capture);
		mtv_captureStatus = (TextView) rootView
				.findViewById(R.id.textview_capture_status);
		mTimer = (Chronometer) rootView.findViewById(R.id.chronometer_capture);

		mbtn_capture.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mbl_isRecording) {
					// Toast.makeText(getActivity(), "capture end.",
					// Toast.LENGTH_SHORT).show();
					// mbl_isRecording = false;

					// stop recording and release
					stopRecordingInCaptureFragment();
				} else {

					// Toast.makeText(getActivity(), "capture begin.",
					// Toast.LENGTH_SHORT).show();
					// mbl_isRecording = true;

					// check the SD card
					int result = MediaCapture.getInstance().startRecording();
					if (-1 == result) {
						Toast.makeText(getActivity(),
								"Please check your SD card.",
								Toast.LENGTH_SHORT).show();
						return;
					}

					startRecordingInCaptureFragment();
				}
			}
		});

		return rootView;
	}

	public void startRecordingInCaptureFragment() {
		mbtn_capture.setImageResource(R.drawable.capture_notification);
		mtv_captureStatus.setText(getActivity()
				.getString(R.string.capture_tips));
		mTimer.setBase(SystemClock.elapsedRealtime());
		mTimer.start();
		mbl_isRecording = true;
	}

	public void stopRecordingInCaptureFragment() {
		MediaCapture.getInstance().stopRecording();
		mbtn_capture.setImageResource(R.drawable.capture_start);
		mtv_captureStatus.setText("");
		mTimer.stop();
		mTimer.setBase(SystemClock.elapsedRealtime());
		mbl_isRecording = false;
	}

	public boolean isRecordingNow() {
		return mbl_isRecording;
	}

	@Override
	public void onResume() {
		Log.e("Fragment_lifecircle_testing", "CaptureFragment_onResume");
		if (mbl_isRecording) {
			// UI when capturing
			mbtn_capture.setImageResource(R.drawable.capture_notification);
		} else {
			mbtn_capture.setImageResource(R.drawable.capture_start);
		}
		if (mbl_isRecording) {
			// UI when capturing
			mtv_captureStatus.setText(getActivity().getString(
					R.string.capture_tips));
		}
		if (!mbl_isRecording) {
			// UI when not capturing
			mTimer.setBase(SystemClock.elapsedRealtime());
		} else {
			mTimer.setBase(MediaCapture.getInstance().getCaptureTime());
			mTimer.start();
		}

		super.onResume();
	}

	@Override
	public void onStart() {
		Log.e("Fragment_lifecircle_testing", "CaptureFragment_onStart");
		super.onStart();
	}

	@Override
	public void onPause() {
		Log.e("Fragment_lifecircle_testing", "CaptureFragment_onPause");
		super.onPause();
	}

	@Override
	public void onStop() {
		Log.e("Fragment_lifecircle_testing", "CaptureFragment_onStop");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Log.e("Fragment_lifecircle_testing", "CaptureFragment_onDestroy");
		super.onDestroy();
	}
}
