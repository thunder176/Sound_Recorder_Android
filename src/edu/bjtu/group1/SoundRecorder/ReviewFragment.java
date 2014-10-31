package edu.bjtu.group1.SoundRecorder;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Fragment;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

//Singleton Pattern
public class ReviewFragment extends Fragment {
	/**
	 * The fragment argument representing the Capture UI.
	 */
	private static ReviewFragment instance_ReviewFragment = null;

	private SearchView msv_review = null;
	private ListView mlv_review = null;

	private ReviewFragment() {
	}

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static ReviewFragment getInstance() {
		if (instance_ReviewFragment == null) {
			synchronized (ReviewFragment.class) {
				if (null == instance_ReviewFragment)
					instance_ReviewFragment = new ReviewFragment();
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

		// 简单实现方法
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

		// 生成动态数组，加入数据
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		String[] recorderFiles = SaveOrLoadFileHelper.getInstance()
				.loadRecorderFiles();
		for (int i = 0; i < recorderFiles.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("listview_item_image", R.drawable.recording_play);// 图像资源的ID
			map.put("listview_item_title", recorderFiles[i]);
			map.put("listview_item_duration", MediaReview.getInstance()
					.getDurationByFileName(recorderFiles[i]));
			listItem.add(map);
		}
		// 生成适配器的Item和动态数组对应的元素
		SimpleAdapter listItemAdapter = new SimpleAdapter(getActivity(),
				listItem, // 数据源
				R.layout.listview_review_items,// ListItem的XML实现
				// 动态数组与ImageItem对应的子项
				new String[] { "listview_item_image", "listview_item_title",
						"listview_item_duration" },
				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.listview_item_image, R.id.listview_item_title,
						R.id.listview_item_duration });

		// 添加并且显示
		mlv_review.setAdapter(listItemAdapter);

		// 添加点击
		mlv_review.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				HashMap<String, Object> map = (HashMap<String, Object>) mlv_review
						.getItemAtPosition(arg2);
				String strFileName = (String) map.get("listview_item_title");
				Toast.makeText(getActivity(), strFileName, Toast.LENGTH_SHORT)
						.show();
				MediaReview.getInstance().playRecordByFileName(strFileName);
			}
		});

		// 添加长按点击
		mlv_review
				.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						menu.setHeaderTitle(getActivity().getString(
								R.string.review_context_menu_title));
						menu.add(0, 0, 0, getActivity().getString(
										R.string.review_context_menu_rename));
						menu.add(0, 1, 0, getActivity().getString(
										R.string.review_context_menu_delete));
					}
				});

		return rootView;
	}

	// 长按菜单响应函数
	public boolean onContextItemSelected(MenuItem item) {
		Toast.makeText(getActivity(), "长按菜单" + item.getItemId(),
				Toast.LENGTH_SHORT).show();
		if (0 == item.getItemId()) {
			// Do rename operation

		} else if (1 == item.getItemId()) {
			// Do delete operation

		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onStop() {
		MediaReview.getInstance().stopPlay();
		super.onStop();
	}

}
