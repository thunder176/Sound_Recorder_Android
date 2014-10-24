package edu.bjtu.group1.SoundRecorder;

import java.io.File;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
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
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_list_item_1,
				loadRecorderFiles());
		mlv_review.setAdapter(arrayAdapter);
		mlv_review.setOnItemClickListener(new OnItemClickListener() {

			/**
			 * arg0 noteListView的指针，可以通过它获取listview所有信息 arg1
			 * 点击的item的view的指针，可以获取item的id arg2 item的位置 arg3
			 * item在listview中的第几行，通常与arg2相同
			 */
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				String strFileName = (String) mlv_review
						.getItemAtPosition(arg2);
//				Toast.makeText(getActivity(), strFileName, Toast.LENGTH_SHORT)
//						.show();
				MediaReview.getInstance().playRecordFileByName(strFileName);
			}
		});

		return rootView;
	}
	
	@Override
	public void onStop(){
		MediaReview.getInstance().stopPlay();
	}

	private String[] loadRecorderFiles() {
		File f = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_MUSIC).getAbsolutePath());
		return f.list();
	}

}
