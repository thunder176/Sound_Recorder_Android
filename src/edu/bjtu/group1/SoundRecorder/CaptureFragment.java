package edu.bjtu.group1.SoundRecorder;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

// Singleton Pattern
public class CaptureFragment extends Fragment {

	/**
	 * The fragment argument representing the Capture UI.
	 */
	private static CaptureFragment instance_CaptureFragment = null;

	private ImageButton mbtn_capture = null;
	private TextView mtv_captureStatus = null;
	private boolean mbl_isRecording = false;

	private CaptureFragment() {
	}

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static CaptureFragment getInstance() {
		if (instance_CaptureFragment == null) {
			synchronized (CaptureFragment.class) {
				if (null == instance_CaptureFragment)
					instance_CaptureFragment = new CaptureFragment();
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
		mbtn_capture.setImageResource(R.drawable.capture_start);
		mtv_captureStatus = (TextView) rootView
				.findViewById(R.id.textview_capture_status);

		mbtn_capture.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mbl_isRecording) {
					// Toast.makeText(getActivity(), "capture end.",
					// Toast.LENGTH_SHORT).show();
					// mbl_isRecording = false;

					// stop recording and release
					MediaCapture.getInstance().stopRecording();
					mbtn_capture.setImageResource(R.drawable.capture_start);
					mtv_captureStatus.setText("");
					mbl_isRecording = false;
				} else {

					// Toast.makeText(getActivity(), "capture begin.",
					// Toast.LENGTH_SHORT).show();
					// mbl_isRecording = true;

					// prepare
					int result = MediaCapture.getInstance().startRecording();
					if (-1 == result) {
						Toast.makeText(getActivity(),
								"Please check your SD card.",
								Toast.LENGTH_SHORT).show();
						return;
					}
					mbtn_capture
							.setImageResource(R.drawable.capture_notification);
					mtv_captureStatus.setText(getActivity().getString(
							R.string.capture_tips));
					mbl_isRecording = true;
				}
			}
		});

		return rootView;
	}

	@Override
	public void onResume() {
		Log.e("Fragment_lifecircle_testing", "CaptureFragment_onResume");
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
}
