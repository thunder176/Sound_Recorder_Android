package edu.bjtu.group1.SoundRecorder;

import com.kii.cloud.storage.KiiUser;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

//Singleton Pattern
public class FragmentReviewDetailsUnlogin extends Fragment {
	/**
	 * The fragment argument representing the ReviewDetails UI.
	 */
	private static FragmentReviewDetailsUnlogin instance_ReviewDetailsFragment = null;

	// Fragment status
	private boolean mbl_isFragmentReviewDetailsUnloginDisplay = false;

	// Playing status
	private boolean mbl_isRecordPlaying = false;
	private boolean mbl_isNeedToPlayAfterPause = false;

	// UI components
	private Button mbtn_review_details_upload = null;
	private Button mbtn_review_details_label = null;
	private TextView mtv_review_details_name = null;
	private Button mbtn_review_details_play = null;
	private SeekBar msb_review_details_seekbar = null;

	// UI for sign up and in
	private EditText met_review_details_username = null;
	private EditText met_review_details_pwd = null;
	private boolean mbl_isKiiLogin = false;

	// Record file name
	private String mstr_recordName = null;

	public FragmentReviewDetailsUnlogin() {
	}

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static FragmentReviewDetailsUnlogin getInstance() {
		if (instance_ReviewDetailsFragment == null) {
			synchronized (FragmentReviewDetailsUnlogin.class) {
				if (null == instance_ReviewDetailsFragment)
					instance_ReviewDetailsFragment = new FragmentReviewDetailsUnlogin();
			}
		}
		return instance_ReviewDetailsFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.e("Fragment_lifecircle_testing",
				"ReviewDetailsFragment_onCreateView");
		View rootView = null;
		if (mbl_isKiiLogin) {
			FragmentReviewDetailsLogin.getInstance().setRecordName(
					mstr_recordName);
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager
					.beginTransaction()
					.replace(R.id.container,
							FragmentReviewDetailsLogin.getInstance()).commit();
		}
		rootView = inflater.inflate(R.layout.fragment_review_details_signinup,
				container, false);

		// UI for sign up and in
		met_review_details_username = (EditText) rootView
				.findViewById(R.id.edittext_review_details_username);
		met_review_details_username.setText("");
		met_review_details_pwd = (EditText) rootView
				.findViewById(R.id.edittext_review_details_pwd);
		met_review_details_pwd.setText("");
		View v = rootView.findViewById(R.id.button_review_details_login);
		v.setOnClickListener(mClickListener);
		v = rootView.findViewById(R.id.button_review_details_register);
		v.setOnClickListener(mClickListener);

		mbtn_review_details_upload = (Button) rootView
				.findViewById(R.id.button_review_details_upload);
		mbtn_review_details_label = (Button) rootView
				.findViewById(R.id.button_review_details_label);
		mtv_review_details_name = (TextView) rootView
				.findViewById(R.id.textview_review_details_name);
		mtv_review_details_name.setText(mstr_recordName);
		mbtn_review_details_play = (Button) rootView
				.findViewById(R.id.button_review_details_play);
		msb_review_details_seekbar = (SeekBar) rootView
				.findViewById(R.id.seekbar_review_details);

		setListener();
		stopPlayDetails();

		// play when open
		// mbl_isRecordPlaying = true;
		// mbtn_review_details_play.setText(getActivity().getString(
		// R.string.button_review_details_pause));
		// msb_review_details_seekbar.setEnabled(true);
		// MediaReview.getInstance().playRecordByFileName(mstr_recordName);

		return rootView;
	}

	/**
	 * kii cloud sign up and in
	 */
	private final View.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			Activity activity = getActivity();
			if (activity == null) {
				return;
			}

			String[] text = new String[3];
			text[0] = met_review_details_username.getText().toString();
			text[1] = met_review_details_pwd.getText().toString();
			if (!KiiUser.isValidUserName(text[0])) {
				Toast.makeText(activity, "Username is not valid",
						Toast.LENGTH_LONG).show();
				return;
			}
			if (!KiiUser.isValidPassword(text[1])) {
				Toast.makeText(activity, "Password is not valid",
						Toast.LENGTH_LONG).show();
				return;
			}
			switch (v.getId()) {
			case R.id.button_review_details_login:
				text[2] = "login";
				break;
			case R.id.button_review_details_register:
				text[2] = "reg";
				break;
			}
			new LoginOrRegTask().execute(text[0], text[1], text[2]);
		}
	};

	/**
	 * kii cloud sign up and in
	 */
	class LoginOrRegTask extends AsyncTask<String, Void, Void> {

		FragmentProgressDialog dialog = null;
		String token = null;
		KiiUser user = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = FragmentProgressDialog.newInstance("In progress now");

			dialog.show(getFragmentManager(), "dialog");
		}

		@Override
		protected Void doInBackground(String... params) {
			String username = params[0];
			String password = params[1];
			String type = params[2];
			try {
				if (type.equals("reg")) {
					KiiUser.Builder builder = KiiUser.builderWithName(username);
					user = builder.build();
					user.register(password);
				} else {
					user = KiiUser.logIn(username, password);
				}
				token = user.getAccessToken();
			} catch (Exception e) {
				token = null;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dialog.dismiss();
			Activity activity = getActivity();
			if (activity == null) {
				return;
			}

			if (TextUtils.isEmpty(token)) {
				Toast.makeText(activity,
						"Reg or login failed, please try again later",
						Toast.LENGTH_LONG).show();
			} else {
				SaveOrLoadFileHelper.getInstance().saveToken(
						activity.getApplicationContext(), token);
				setIsKiiLogin(true);
				Toast.makeText(activity, "Login successful", Toast.LENGTH_LONG)
						.show();

				FragmentReviewDetailsLogin.getInstance().setRecordName(
						mstr_recordName);
				getFragmentManager()
						.beginTransaction()
						.replace(R.id.container,
								FragmentReviewDetailsLogin.getInstance())
						.commit();
			}
		}
	}

	// TODO update with AsyncTask
	Handler handler = new Handler();
	Runnable updateThread = new Runnable() {
		public void run() {
			// 获得歌曲现在播放位置并设置成播放进度条的值
			// String log = "Position: "
			// + MediaReview.getInstance().getCurrentPosition();
			// Log.e("FragmentReviewDetails::updateThread", log);
			msb_review_details_seekbar.setProgress(MediaReview.getInstance()
					.getCurrentPosition());

			// UI when stop
			if (MediaReview.getInstance().isPlayComplete()) {
				stopPlayDetails();
			} else {
				// 每次延迟10毫秒再启动线程
				handler.postDelayed(updateThread, 10);
			}
		}
	};

	private void setListener() {
		mbtn_review_details_upload.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Toast.makeText(getActivity(),
						"You need to sign in or sign up first!",
						Toast.LENGTH_LONG).show();
			}
		});
		mbtn_review_details_play
				.setOnClickListener(new Button.OnClickListener() {
					public void onClick(View arg0) {
						if (mbl_isRecordPlaying) {
							// click pause button
							mbl_isRecordPlaying = false;
							mbl_isNeedToPlayAfterPause = true;
							msb_review_details_seekbar.setEnabled(true);
							mbtn_review_details_play
									.setText(getActivity()
											.getString(
													R.string.button_review_details_play));

							MediaReview.getInstance().pause();
							// cancel thread for SeekBar
							handler.removeCallbacks(updateThread);
						} else if (!mbl_isRecordPlaying
								&& mbl_isNeedToPlayAfterPause) {
							// click play button when it was pause
							mbl_isRecordPlaying = true;
							msb_review_details_seekbar.setEnabled(true);
							mbtn_review_details_play
									.setText(getActivity()
											.getString(
													R.string.button_review_details_pause));

							MediaReview.getInstance().startAfterPause();
							// start thread for SeekBar
							handler.post(updateThread);
						} else if (!mbl_isRecordPlaying
								&& !mbl_isNeedToPlayAfterPause) {
							// click play again
							mbl_isRecordPlaying = true;
							msb_review_details_seekbar.setEnabled(true);
							mbtn_review_details_play
									.setText(getActivity()
											.getString(
													R.string.button_review_details_pause));

							MediaReview.getInstance().playRecordByFileName(
									mstr_recordName);
							// start thread for SeekBar
							handler.post(updateThread);
						}
					}
				});
		msb_review_details_seekbar
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						// fromUser判断是用户改变的滑块的值
						if (fromUser == true
								&& !MediaReview.getInstance().isPlayComplete()) {
							MediaReview.getInstance().seekTo(progress);
						}
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onStopTrackingTouch(SeekBar seekBar) {
					}
				});
	}

	public void setRecordName(String strFileName) {
		mstr_recordName = strFileName;
	}

	@Override
	public void onStart() {
		Log.e("Fragment_lifecircle_testing", "ReviewDetailsFragment_onStart");
		mbl_isFragmentReviewDetailsUnloginDisplay = true;
		if (null != met_review_details_username
				&& null != met_review_details_pwd) {
			met_review_details_username.setText("");
			met_review_details_pwd.setText("");
		}
		super.onStart();
	}

	@Override
	public void onResume() {
		Log.e("Fragment_lifecircle_testing", "ReviewDetailsFragment_onResume");
		super.onResume();
	}

	@Override
	public void onPause() {
		Log.e("Fragment_lifecircle_testing", "ReviewDetailsFragment_onPause");
		super.onPause();
	}

	@Override
	public void onStop() {
		Log.e("Fragment_lifecircle_testing", "ReviewDetailsFragment_onStop");
		mbl_isFragmentReviewDetailsUnloginDisplay = false;
		hideSoftInput();
		stopPlayDetails();
		getActivity().invalidateOptionsMenu();
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Log.e("Fragment_lifecircle_testing", "ReviewDetailsFragment_onDestroy");
		super.onDestroy();
	}

	public boolean isFragmentReviewDetailsUnloginDisplay() {
		return mbl_isFragmentReviewDetailsUnloginDisplay;
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

	public void setRecordDurationAndSeekBar(int time) {
		msb_review_details_seekbar.setMax(time);
		// start thread for SeekBar
		handler.post(updateThread);

		// String log = "Time: " + time;
		// Log.e("FragmentReviewDetails::setRecordDurationAndSeekBar", log);
	}

	public void setIsKiiLogin(boolean bool) {
		this.mbl_isKiiLogin = bool;
	}

	public void hideSoftInput() {
		View view = getActivity().getWindow().peekDecorView();
		if (view != null) {
			InputMethodManager inputmanger = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	public void stopPlayDetails() {
		// cancel thread for SeekBar
		handler.removeCallbacks(updateThread);
		mbl_isNeedToPlayAfterPause = false;
		mbl_isRecordPlaying = false;
		mbtn_review_details_play.setText(getActivity().getString(
				R.string.button_review_details_play));
		msb_review_details_seekbar.setProgress(0);
		msb_review_details_seekbar.setEnabled(false);

		MediaReview.getInstance().stopPlay();
	}

}
