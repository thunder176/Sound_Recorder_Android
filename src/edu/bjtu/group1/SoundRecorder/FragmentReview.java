package edu.bjtu.group1.SoundRecorder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import edu.bjtu.group1.SoundRecorder.FragmentReviewDetailsLogin.DownloaderObj;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SimpleAdapter;
import android.widget.Toast;

//Singleton Pattern
public class FragmentReview extends Fragment {
	/**
	 * The fragment argument representing the Capture UI.
	 */
	private static FragmentReview instance_ReviewFragment = null;

	// Fragment status
	private boolean mbl_isFragmentReviewDisplay = false;
	private boolean mbl_isDataPrepared = false;
	private boolean mbl_isDataPrepareTaskDoing = false;

	// UI components
	private SearchView msv_review = null;
	private ListView mlv_review = null;

	// ListView Data
	private ArrayList<HashMap<String, Object>> mArrayList = null;
	private String mFilterConstraint = "";
	private ArrayList<HashMap<String, Object>> mSearchedArrayList = null;
	private final int LISTVIEW_CACHE_INIT_ITEMS = 20;
	private final int LISTVIEW_CACHE_ADD_ITEMS = 10;
	private int mi_lvCacheCurrentSumItemsNum = 0;
	private boolean mbl_isCacheNumChanged = false;
	private ArrayList<HashMap<String, Object>> mCacheArrayList = null;

	// Record file name
	private String mstr_oldFileName = null;

	public FragmentReview() {
	}

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static FragmentReview getInstance() {
		if (instance_ReviewFragment == null) {
			synchronized (FragmentReview.class) {
				if (null == instance_ReviewFragment)
					instance_ReviewFragment = new FragmentReview();
			}
		}
		return instance_ReviewFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_review, container,
				false);
		msv_review = (SearchView) rootView.findViewById(R.id.searchview_review);
		mlv_review = (ListView) rootView.findViewById(R.id.listview_review);

		initSearchView();
		initListView();

		return rootView;
	}

	private void initSearchView() {
		// SearchView
		msv_review.setIconifiedByDefault(false); // expand by default
		msv_review.setSubmitButtonEnabled(false);
		msv_review.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			public boolean onQueryTextChange(String newText) {
				if (TextUtils.isEmpty(newText)) {
					// Clear the text filter.
					// Log.e("ReviewFragment::onQueryTextChange",
					// "mlv_review.clearTextFilter()");
					mFilterConstraint = "";
					mi_lvCacheCurrentSumItemsNum = LISTVIEW_CACHE_INIT_ITEMS;
					mbl_isCacheNumChanged = false;
					updateListView();
					// mlv_review.clearTextFilter();
				} else {
					// Sets the initial value for the text filter.
					// Log.e("ReviewFragment::onQueryTextChange",
					// newText.toString());
					mFilterConstraint = newText;
					updateListView();
				}
				return false;
			}

			public boolean onQueryTextSubmit(String arg0) {
				Log.e("ReviewFragment::onQueryTextSubmit", arg0);
				return false;
			}

		});
	}

	private void initListView() {
		// ListView UI 简单实现方法
		// ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
		// getActivity(), android.R.layout.simple_list_item_1,
		// SaveOrLoadFileHelper.getInstance().loadRecorderFiles());
		// mlv_review.setAdapter(arrayAdapter);
		// mlv_review.setOnItemClickListener(new OnItemClickListener() {
		//
		// /*
		// * arg0 noteListView的指针，可以通过它获取listview所有信息 arg1
		// * 点击的item的view的指针，可以获取item的id arg2 item的位置 arg3
		// * item在listview中的第几行，通常与arg2相同
		// */
		// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
		// long arg3) {
		// String strFileName = (String) mlv_review
		// .getItemAtPosition(arg2);
		// // Toast.makeText(getActivity(), strFileName,
		// // Toast.LENGTH_SHORT)
		// // .show();
		// MediaReview.getInstance().playRecordFileByName(strFileName);
		// }
		// });

		// ListView advanced UI
		// now it's done in prepareData();

		mi_lvCacheCurrentSumItemsNum = LISTVIEW_CACHE_INIT_ITEMS;
		mbl_isCacheNumChanged = false;
		updateListView();
	}

	private void updateListView() {
		if (!mbl_isDataPrepared) {
			List<String> data = new ArrayList<String>();
			data.add("Data is preparing, please come back later!");
			mlv_review.setAdapter(new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_expandable_list_item_1, data));
			data = null;
			return;
		}
		if (null == mArrayList) {
			return;
		}

		// Search the string of mFilterConstraint
		if (mFilterConstraint.equals("")) {
			mSearchedArrayList = mArrayList;
		} else {
			if (null != mSearchedArrayList) {
				mSearchedArrayList = null;
			}
			mSearchedArrayList = new ArrayList<HashMap<String, Object>>();
			for (HashMap<String, Object> hsmap : mArrayList) {
				if (((String) hsmap.get("listview_item_title"))
						.contains(mFilterConstraint)) {
					mSearchedArrayList.add(hsmap);
				}
			}
		}

		// Cache items as setting
		// TODO load data from SharedPreferences
		if (null != mCacheArrayList) {
			mCacheArrayList = null;
		}
		mCacheArrayList = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < mi_lvCacheCurrentSumItemsNum
				&& i < mSearchedArrayList.size(); i++) {
			mCacheArrayList.add(mSearchedArrayList.get(i));
		}

		// 生成适配器的Item和动态数组对应的元素
		ReviewSimpleAdapter listItemAdapter = new ReviewSimpleAdapter(
				getActivity(), mCacheArrayList, // 数据源
				R.layout.listview_review_items,// ListItem的XML实现
				// 动态数组与ImageItem对应的子项
				new String[] { "listview_item_title", "listview_item_image",
						"listview_item_duration" },
				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.listview_item_title, R.id.listview_item_image,
						R.id.listview_item_duration });

		// show the items
		mlv_review.setAdapter(listItemAdapter);

		if (mbl_isCacheNumChanged) {
			mlv_review.setSelection(mi_lvCacheCurrentSumItemsNum
					- 2 * LISTVIEW_CACHE_ADD_ITEMS);
			listItemAdapter.notifyDataSetInvalidated();
		}

		// add click Listener
		mlv_review.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				hideSoftInput();

				ListView lv = (ListView) arg0;
				HashMap<String, Object> hmItem = (HashMap<String, Object>) lv
						.getItemAtPosition(arg2);
				String strFileName = (String) hmItem.get("listview_item_title");

				// show review detail fragment
				FragmentReviewDetailsUnlogin.getInstance().setRecordName(
						strFileName);
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager
						.beginTransaction()
						.replace(R.id.container,
								FragmentReviewDetailsUnlogin.getInstance())
						.commit();
				// Toast.makeText(getActivity(), strFileName,
				// Toast.LENGTH_SHORT)
				// .show();
				// MediaReview.getInstance().playRecordByFileName(strFileName);
			}
		});

		// add ContextMenu Listener
		mlv_review
				.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						menu.setHeaderTitle(getActivity().getString(
								R.string.review_context_menu_title));
						menu.add(
								0,
								0,
								0,
								getActivity().getString(
										R.string.review_context_menu_rename));
						menu.add(
								0,
								1,
								0,
								getActivity().getString(
										R.string.review_context_menu_delete));
					}
				});
		mlv_review.setOnScrollListener(new OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				hideSoftInput();
				mbl_isCacheNumChanged = false;
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					if (view.getLastVisiblePosition() == view.getCount() - 1) {
						if (mSearchedArrayList.size() > mCacheArrayList.size()) {
							mi_lvCacheCurrentSumItemsNum += LISTVIEW_CACHE_ADD_ITEMS;
							mbl_isCacheNumChanged = true;
							updateListView();
						}
					}
				}

			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
	}

	// 长按菜单响应函数
	public boolean onContextItemSelected(MenuItem item) {
		hideSoftInput();

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		mstr_oldFileName = mArrayList.get(info.position)
				.get("listview_item_title").toString();
		// Log.e("ReviewFragment::onContextItemSelected", strFileName);

		if (0 == item.getItemId()) {
			// Do rename operation
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			LayoutInflater factory = LayoutInflater.from(getActivity());
			final View textEntryView = factory.inflate(
					R.layout.dialog_rename_record, null);
			builder.setTitle(getActivity().getString(
					R.string.review_rename_dialog_title));
			builder.setView(textEntryView);
			builder.setPositiveButton(
					getActivity().getString(R.string.review_rename_dialog_ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							EditText etNewName = (EditText) textEntryView
									.findViewById(R.id.edittext_newname);
							String strNewName = etNewName.getText().toString()
									+ ".amr";
							if (strNewName.equals(".amr")) {
								Toast.makeText(
										getActivity(),
										getActivity()
												.getString(
														R.string.review_rename_result_null_name),
										Toast.LENGTH_SHORT).show();
							} else {
								int result = MediaReview.getInstance()
										.renameRecordByFileName(
												mstr_oldFileName, strNewName);
								if (1 == result) {
									Toast.makeText(
											getActivity(),
											getActivity()
													.getString(
															R.string.review_rename_result_not_exist),
											Toast.LENGTH_SHORT).show();
								} else if (2 == result) {
									Toast.makeText(
											getActivity(),
											getActivity()
													.getString(
															R.string.review_rename_result_file_exist),
											Toast.LENGTH_SHORT).show();
								} else if (3 == result) {
									Toast.makeText(
											getActivity(),
											getActivity()
													.getString(
															R.string.review_rename_result_same_name),
											Toast.LENGTH_SHORT).show();
								} else if (0 == result) {
									Toast.makeText(
											getActivity(),
											getActivity()
													.getString(
															R.string.review_rename_result_success),
											Toast.LENGTH_SHORT).show();
									if (null != mArrayList) {
										for (HashMap<String, Object> hp : mArrayList) {
											if (hp.get("listview_item_title")
													.equals(mstr_oldFileName)) {
												hp.put("listview_item_title",
														strNewName);
											}
										}
									}
									updateListView();
								}
							}
							return;
						}
					});
			builder.setNegativeButton(
					getActivity().getString(
							R.string.review_rename_dialog_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							return;
						}
					});
			builder.create().show();

		} else if (1 == item.getItemId()) {
			// Do delete operation
			MediaReview.getInstance().stopPlay();
			if (MediaReview.getInstance().deleteRecordByFileName(
					mstr_oldFileName)) {
				if (null != mArrayList) {
					Iterator<HashMap<String, Object>> it = mArrayList
							.iterator();
					while (it.hasNext()) {
						if (((String) it.next().get("listview_item_title"))
								.equals(mstr_oldFileName)) {
							it.remove();
						}
					}
				}
				updateListView();
			}
		}
		return super.onContextItemSelected(item);
	}

	public boolean isFragmentReviewDisplay() {
		return mbl_isFragmentReviewDisplay;
	}

	@Override
	public void onResume() {
		Log.e("Fragment_lifecircle_testing", "ReviewFragment_onResume");
		super.onResume();
	}

	@Override
	public void onStart() {
		Log.e("Fragment_lifecircle_testing", "ReviewFragment_onStart");
		mbl_isFragmentReviewDisplay = true;
		// fix a bug to display mFilterConstraint correctly
		if (!mFilterConstraint.equals("")) {
			msv_review.setQuery(mFilterConstraint, false);
		}
		super.onStart();
	}

	@Override
	public void onPause() {
		Log.e("Fragment_lifecircle_testing", "ReviewFragment_onPause");
		super.onPause();
	}

	@Override
	public void onStop() {
		Log.e("Fragment_lifecircle_testing", "ReviewFragment_onStop");
		mbl_isFragmentReviewDisplay = false;
		MediaReview.getInstance().stopPlay();
		hideSoftInput();
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Log.e("Fragment_lifecircle_testing", "ReviewFragment_onDestroy");
		mFilterConstraint = "";
		super.onDestroy();
	}

	public void hideSoftInput() {
		View view = getActivity().getWindow().peekDecorView();
		if (view != null) {
			InputMethodManager inputmanger = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	public String millisToString(int iDuration) {
		int hours = (iDuration % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
		int minutes = (iDuration % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (iDuration % (1000 * 60)) / 1000;
		String strDuration = hours + ":" + minutes + ":" + seconds;
		return strDuration;
	}

	public void prepareData() {
		if (!mbl_isDataPrepareTaskDoing && !mbl_isDataPrepared) {
			Log.e("FragmentReview::prepareData", "new prepareDataTask()");
			prepareDataTask asyncTask = new prepareDataTask();
			asyncTask.execute();
		}
	}

	void reverseArray(String[] str) {
		int i = 0, n = str.length - 1;
		while (n > 2 * i) {
			String temp = str[i];
			str[i] = str[n - i];
			str[n - i] = temp;
			i++;
		}
	}

	private class prepareDataTask extends
			AsyncTask<DownloaderObj, String, String> {

		@Override
		protected String doInBackground(DownloaderObj... params) {
			mbl_isDataPrepareTaskDoing = true;
			if (null != mArrayList) {
				mArrayList.clear();
			}
			mArrayList = new ArrayList<HashMap<String, Object>>();
			String[] recorderFiles = SaveOrLoadFileHelper.getInstance()
					.loadRecorderFiles();
			// sort by last Modified time
			Arrays.sort(recorderFiles, new Comparator<Object>() {
				public int compare(Object a, Object b) {
					String s1 = (String) a;
					String s2 = (String) b;
					return SaveOrLoadFileHelper
							.getInstance()
							.getLastModifiedTime(s1)
							.compareTo(
									SaveOrLoadFileHelper.getInstance()
											.getLastModifiedTime(s2));
				}
			});
			// reverse
			reverseArray(recorderFiles);

			for (int i = 0; i < recorderFiles.length; i++) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("listview_item_title", recorderFiles[i]);
				map.put("listview_item_image", R.drawable.review_recording_play);// 图像资源的ID
				map.put("listview_item_duration", millisToString(MediaReview
						.getInstance()
						.getRecordDurationMillis(recorderFiles[i])));
				mArrayList.add(map);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			mbl_isDataPrepared = true;
			mbl_isDataPrepareTaskDoing = false;
			super.onPostExecute(result);
		}
	}

	public void addRecordingInfo() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("listview_item_title", MediaCapture.getInstance()
				.getLastRecordFileName());
		map.put("listview_item_image", R.drawable.review_recording_play);// 图像资源的ID
		map.put("listview_item_duration",
				millisToString(MediaReview.getInstance()
						.getRecordDurationMillis(
								MediaCapture.getInstance()
										.getLastRecordFileName())));
		mArrayList.add(0, map);
	}

}
