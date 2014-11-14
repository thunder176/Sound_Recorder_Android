package edu.bjtu.group1.SoundRecorder;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;

//Singleton Pattern
public class FragmentReviewDetails extends Fragment {
	/**
	 * The fragment argument representing the ReviewDetails UI.
	 */
	private static FragmentReviewDetails instance_ReviewDetailsFragment = null;

	// Fragment status
	private boolean mbl_isFragmentReviewDetailsDisplay = false;

	// Playing status
	private boolean mbl_isRecordPlaying = false;

	// UI components
	private ListView mlv_review_details = null;
	private Button mbtn_review_details_share = null;
	private Button mbtn_review_details_label = null;
	private TextView mtv_review_details_name = null;
	private Button mbtn_review_details_play = null;
	private SeekBar msb_review_details_seekbar = null;

	// Record file name
	private String mstr_recordName = null;
	private int mi_recordDuration = 0;

	public void setRecordDurationAndSeekBar(int time) {
		this.mi_recordDuration = time;
		msb_review_details_seekbar.setMax(mi_recordDuration);
	}

	// TODO 自动适应屏幕转换
	public FragmentReviewDetails() {
	}

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static FragmentReviewDetails getInstance() {
		if (instance_ReviewDetailsFragment == null) {
			synchronized (FragmentReviewDetails.class) {
				if (null == instance_ReviewDetailsFragment)
					instance_ReviewDetailsFragment = new FragmentReviewDetails();
			}
		}
		return instance_ReviewDetailsFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_review_details,
				container, false);
		mbtn_review_details_share = (Button) rootView
				.findViewById(R.id.button_review_details_share);
		mbtn_review_details_label = (Button) rootView
				.findViewById(R.id.button_review_details_label);
		mtv_review_details_name = (TextView) rootView
				.findViewById(R.id.textview_review_details_name);
		mbtn_review_details_play = (Button) rootView
				.findViewById(R.id.button_review_details_play);
		msb_review_details_seekbar = (SeekBar) rootView
				.findViewById(R.id.seekbar_review_details);

		doInit();
		// play when open
		MediaReview.getInstance().playRecordByFileName(mstr_recordName);
		mbl_isRecordPlaying = true;
		mbtn_review_details_play.setText(getActivity().getString(
				R.string.button_review_details_stop));
		setListener();
		return rootView;
	}

	// TODO update with AsyncTask
	Handler handler = new Handler();
	Runnable updateThread = new Runnable() {
		public void run() {
			// 获得歌曲现在播放位置并设置成播放进度条的值
			msb_review_details_seekbar.setProgress(MediaReview.getInstance()
					.getCurrentPosition());
			// 每次延迟100毫秒再启动线程
			handler.postDelayed(updateThread, 100);
		}
	};

	private void setListener() {
		mbtn_review_details_play
				.setOnClickListener(new Button.OnClickListener() {
					public void onClick(View arg0) {
						if (!mbl_isRecordPlaying) {
							MediaReview.getInstance().start();
							mbtn_review_details_play
									.setText(getActivity()
											.getString(
													R.string.button_review_details_stop));
							// 启动
							handler.post(updateThread);
						} else {
							MediaReview.getInstance().pause();
							mbtn_review_details_play
									.setText(getActivity()
											.getString(
													R.string.button_review_details_play));
							// 取消线程
							handler.removeCallbacks(updateThread);
						}
					}
				});
		msb_review_details_seekbar
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						// fromUser判断是用户改变的滑块的值
						if (fromUser == true) {
							MediaReview.getInstance().seekTo(progress);
						}
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
					}

					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
					}
				});
	}

	private void doInit() {
		mtv_review_details_name.setText(mstr_recordName);
	}

	public void setRecordName(String strFileName) {
		mstr_recordName = strFileName;
	}

	@Override
	public void onResume() {
		Log.e("Fragment_lifecircle_testing", "ReviewDetailsFragment_onResume");
		mbl_isFragmentReviewDetailsDisplay = true;
		super.onStart();
	}

	@Override
	public void onPause() {
		Log.e("Fragment_lifecircle_testing", "ReviewDetailsFragment_onPause");
		mbl_isFragmentReviewDetailsDisplay = false;
		super.onStop();
	}

	public boolean isFragmentReviewDetailsDisplay() {
		return mbl_isFragmentReviewDetailsDisplay;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) { // 返回键
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.container, FragmentReview.getInstance())
					.commit();
		}
		return true;
	}

}
