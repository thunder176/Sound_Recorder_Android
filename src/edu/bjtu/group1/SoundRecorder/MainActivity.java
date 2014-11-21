package edu.bjtu.group1.SoundRecorder;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiUserCallBack;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements
		FragmentNavigationDrawer.NavigationDrawerCallbacks {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private FragmentNavigationDrawer mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	// show notification if press home when capturing
	BackgroundLogo bgLogo = null;

	// Define symbol for fragment
	private final int FRAGMENT_CAPTURE_DISAPLAY = 1;
	private final int FRAGMENT_REVIEW_DISAPLAY = 2;
	private final int FRAGMENT_REVIEW_DETAILS_UNLOGIN_DISAPLAY = 3;
	private final int FRAGMENT_REVIEW_DETAILS_LOGIN_DISAPLAY = 4;
	private static int mi_currentDisplayFragment = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e("Activity_lifecircle_testing", "MainActivity_onCreate");
		super.onCreate(savedInstanceState);

		// Kii Cloud initialize
		Kii.initialize("62d45ebe", "63838d6bb158eab79ccf7f0470946648",
				Kii.Site.CN);
		// kii cloud sigh in with token
		String token = SaveOrLoadFileHelper.getInstance().getToken(
				getApplicationContext());
		if (token.equals("")) {
			// token is not exist
			FragmentReviewDetailsUnlogin.getInstance().setIsKiiLogin(false);
		} else {
			// Use the access token to sign-in again
			KiiUser.loginWithToken(new KiiUserCallBack() {
				@Override
				public void onLoginCompleted(int token, KiiUser user,
						Exception exception) {
					if (exception != null) {
						// Error handling
						return;
					} else {
						FragmentReviewDetailsUnlogin.getInstance()
								.setIsKiiLogin(true);
					}
				}
			}, token);
		}

		setContentView(R.layout.activity_main);
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(true);

		// show Logo when capturing in the background
		bgLogo = new BackgroundLogo(this);

		// prepare data in review fragment
		FragmentReview.getInstance().prepareData();

		mNavigationDrawerFragment = (FragmentNavigationDrawer) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

		mi_currentDisplayFragment = 0;
		// capture fragment
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.container, FragmentCapture.getInstance())
				.commit();
	}

	@Override
	protected void onResume() {
		Log.e("Activity_lifecircle_testing", "MainActivity_onResume");
		// capture fragment or last fragment
		Log.e("Change_of_orientation", "onCreate:mi_currentDisplayFragment = "
				+ mi_currentDisplayFragment);
		if (FRAGMENT_REVIEW_DISAPLAY == mi_currentDisplayFragment) {
			Log.e("Change_of_orientation", "onCreate:FragmentReview");
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.container, FragmentReview.getInstance())
					.commit();
		} else if (FRAGMENT_REVIEW_DETAILS_UNLOGIN_DISAPLAY == mi_currentDisplayFragment) {
			Log.e("Change_of_orientation",
					"onCreate:FragmentReviewDetailsUnlogin");
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager
					.beginTransaction()
					.replace(R.id.container,
							FragmentReviewDetailsUnlogin.getInstance())
					.commit();
		} else if (FRAGMENT_REVIEW_DETAILS_LOGIN_DISAPLAY == mi_currentDisplayFragment) {
			Log.e("Change_of_orientation",
					"onCreate:FragmentReviewDetailsLogin");
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager
					.beginTransaction()
					.replace(R.id.container,
							FragmentReviewDetailsLogin.getInstance()).commit();
		} else if (FRAGMENT_CAPTURE_DISAPLAY == mi_currentDisplayFragment) {
			Log.e("Change_of_orientation", "onCreate:FragmentCapture");
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.container, FragmentCapture.getInstance())
					.commit();
		}
		bgLogo.cancelNotification();
		super.onResume();
	}

	@Override
	protected void onStart() {
		Log.e("Activity_lifecircle_testing", "MainActivity_onStart");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		Log.e("Activity_lifecircle_testing", "MainActivity_onRestart");
		super.onRestart();
	}

	@Override
	public void onPause() {
		Log.e("Activity_lifecircle_testing", "MainActivity_onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.e("Activity_lifecircle_testing", "MainActivity_onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.e("Activity_lifecircle_testing", "MainActivity_onDestroy");
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.e("Change_of_orientation", "MainActivity_onSaveInstanceState");
		super.onSaveInstanceState(outState);
		Kii.onSaveInstanceState(outState);
		// adapt to the change of orientation
		if (FragmentCapture.getInstance().isFragmentCaptureDisplay()) {
			Log.e("Change_of_orientation",
					"onSaveInstanceState:FragmentCapture");
			outState.putInt("LastFragment", FRAGMENT_CAPTURE_DISAPLAY);
			// mi_currentDisplayFragment = FRAGMENT_CAPTURE_DISAPLAY;
		} else if (FragmentReview.getInstance().isFragmentReviewDisplay()) {
			Log.e("Change_of_orientation", "onSaveInstanceState:FragmentReview");
			outState.putInt("LastFragment", FRAGMENT_REVIEW_DISAPLAY);
			// mi_currentDisplayFragment = FRAGMENT_REVIEW_DISAPLAY;
		} else if (FragmentReviewDetailsUnlogin.getInstance()
				.isFragmentReviewDetailsUnloginDisplay()) {
			Log.e("Change_of_orientation",
					"onSaveInstanceState:FragmentReviewDetails");
			outState.putInt("LastFragment",
					FRAGMENT_REVIEW_DETAILS_UNLOGIN_DISAPLAY);
			// mi_currentDisplayFragment =
			// FRAGMENT_REVIEW_DETAILS_UNLOGIN_DISAPLAY;
		} else if (FragmentReviewDetailsUnlogin.getInstance()
				.isFragmentReviewDetailsUnloginDisplay()) {
			Log.e("Change_of_orientation",
					"onSaveInstanceState:FragmentReviewDetails");
			outState.putInt("LastFragment",
					FRAGMENT_REVIEW_DETAILS_LOGIN_DISAPLAY);
			// mi_currentDisplayFragment =
			// FRAGMENT_REVIEW_DETAILS_LOGIN_DISAPLAY;
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mi_currentDisplayFragment = savedInstanceState.getInt("LastFragment");
		Log.e("Change_of_orientation",
				"onRestoreInstanceState:mi_currentDisplayFragment = "
						+ mi_currentDisplayFragment);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) { // 返回键
			if (FragmentReviewDetailsUnlogin.getInstance()
					.isFragmentReviewDetailsUnloginDisplay()) {
				FragmentReviewDetailsUnlogin.getInstance().onKeyDown(keyCode,
						event);
				return true;
			} else if (FragmentReviewDetailsLogin.getInstance()
					.isFragmentReviewDetailsLoginDisplay()) {
				FragmentReviewDetailsLogin.getInstance().onKeyDown(keyCode,
						event);
				return true;
			}
			if (FragmentCapture.getInstance().isRecordingNow()) {
				bgLogo.showNotification(getString(R.string.capture_tips_background));
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	// deal with home button pressed
	protected void onUserLeaveHint() {
		if (FragmentCapture.getInstance().isRecordingNow()) {
			bgLogo.showNotification(getString(R.string.capture_tips_background));
		}
		super.onUserLeaveHint();
	}

	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		/*
		 * FragmentManager fragmentManager = getFragmentManager();
		 * fragmentManager .beginTransaction() .replace(R.id.container,
		 * PlaceholderFragment.newInstance(position + 1)).commit();
		 */
	}

	public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section1);
			break;
		case 2:
			mTitle = getString(R.string.title_section2);
			break;
		case 3:
			mTitle = getString(R.string.title_section3);
			break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_capture) {
			// Toast.makeText(this, "capture action.",
			// Toast.LENGTH_SHORT).show();
			// show capture fragment
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.container, FragmentCapture.getInstance())
					.commit();
			return true;
		} else if (id == R.id.action_review) {
			// Toast.makeText(this, "review action.",
			// Toast.LENGTH_SHORT).show();

			// if it's capturing, stop
			FragmentCapture cf = FragmentCapture.getInstance();
			if (cf.isRecordingNow()) {
				cf.stopRecordingInCaptureFragment();
			}
			// show capture fragment
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.container, FragmentReview.getInstance())
					.commit();
			return true;
		} else if (id == R.id.action_tutorial) {
			Toast.makeText(this, "tutorial action.", Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// /**
	// * A placeholder fragment containing a simple view.
	// */
	// public static class PlaceholderFragment extends Fragment {
	// /**
	// * The fragment argument representing the section number for this
	// * fragment.
	// */
	// private static final String ARG_SECTION_NUMBER = "section_number";
	//
	// /**
	// * Returns a new instance of this fragment for the given section number.
	// */
	// public static PlaceholderFragment newInstance(int sectionNumber) {
	// PlaceholderFragment fragment = new PlaceholderFragment();
	// Bundle args = new Bundle();
	// args.putInt(ARG_SECTION_NUMBER, sectionNumber);
	// fragment.setArguments(args);
	// return fragment;
	// }
	//
	// public PlaceholderFragment() {
	// }
	//
	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// View rootView = inflater.inflate(R.layout.fragment_main, container,
	// false);
	// return rootView;
	// }
	//
	// @Override
	// public void onAttach(Activity activity) {
	// super.onAttach(activity);
	// ((MainActivity) activity).onSectionAttached(getArguments().getInt(
	// ARG_SECTION_NUMBER));
	// }
	// }

}
