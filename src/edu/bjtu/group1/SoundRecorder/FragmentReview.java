package edu.bjtu.group1.SoundRecorder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
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

	// UI components
	private SearchView msv_review = null;
	private ListView mlv_review = null;

	// ListView Data
	private ArrayList<HashMap<String, Object>> mArrayList = null;
	private String mFilterConstraint = "";

	// Record file name
	private String mstr_fileName = null;

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
		// 生成动态数组，加入数据
		if (null != mArrayList) {
			mArrayList.clear();
		}
		mArrayList = new ArrayList<HashMap<String, Object>>();
		String[] recorderFiles = SaveOrLoadFileHelper.getInstance()
				.loadRecorderFiles();
		for (int i = 0; i < recorderFiles.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("listview_item_title", recorderFiles[i]);
			map.put("listview_item_image", R.drawable.review_recording_play);// 图像资源的ID
			map.put("listview_item_duration", MediaReview.getInstance()
					.getDurationByFileName(recorderFiles[i]));
			mArrayList.add(map);
		}

		updateListView();
	}

	private void updateListView() {
		if (null == mArrayList) {
			return;
		}
		ArrayList<HashMap<String, Object>> newArrayList;
		if (mFilterConstraint.equals("")) {
			newArrayList = mArrayList;
		} else {
			newArrayList = new ArrayList<HashMap<String, Object>>();
			for (HashMap<String, Object> hsmap : mArrayList) {
				if (((String) hsmap.get("listview_item_title"))
						.contains(mFilterConstraint)) {
					newArrayList.add(hsmap);
				}
			}
		}

		// 生成适配器的Item和动态数组对应的元素
		ReviewSimpleAdapter listItemAdapter = new ReviewSimpleAdapter(
				getActivity(), newArrayList, // 数据源
				R.layout.listview_review_items,// ListItem的XML实现
				// 动态数组与ImageItem对应的子项
				new String[] { "listview_item_title", "listview_item_image",
						"listview_item_duration" },
				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.listview_item_title, R.id.listview_item_image,
						R.id.listview_item_duration });

		// show the items
		mlv_review.setAdapter(listItemAdapter);

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
				FragmentReviewDetails.getInstance().setRecordName(strFileName);
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager
						.beginTransaction()
						.replace(R.id.container,
								FragmentReviewDetails.getInstance()).commit();
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

			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}
		});
	}

	// 长按菜单响应函数
	public boolean onContextItemSelected(MenuItem item) {
		hideSoftInput();

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		mstr_fileName = mArrayList.get(info.position)
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
										.renameRecordByFileName(mstr_fileName,
												strNewName);
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
									initListView();
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
			if (MediaReview.getInstance().deleteRecordByFileName(mstr_fileName)) {
				if (null != mArrayList) {
					Iterator<HashMap<String, Object>> it = mArrayList.iterator();
					while (it.hasNext()) {
						if (((String) it.next().get("listview_item_title"))
								.equals(mstr_fileName)) {
							it.remove();
						}
					}
				}
				updateListView();
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onResume() {
		Log.e("Fragment_lifecircle_testing", "ReviewFragment_onResume");
		super.onResume();
	}

	@Override
	public void onStart() {
		Log.e("Fragment_lifecircle_testing", "ReviewFragment_onStart");
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
		MediaReview.getInstance().stopPlay();
		hideSoftInput();
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		Log.e("Fragment_lifecircle_testing", "ReviewFragment_onDestroy");
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

}
