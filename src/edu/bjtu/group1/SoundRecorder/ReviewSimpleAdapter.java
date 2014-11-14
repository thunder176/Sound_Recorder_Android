/** 
 *  
 */
package edu.bjtu.group1.SoundRecorder;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * @author
 * 
 */
public class ReviewSimpleAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private ArrayList<HashMap<String, Object>> mData;
	private int mLayoutID;
	private String mFlag[];
	private int mItemIDs[];

	public ReviewSimpleAdapter(Context context,
			ArrayList<HashMap<String, Object>> data, int layoutID,
			String flag[], int ItemIDs[]) {
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mData = data;
		this.mLayoutID = layoutID;
		this.mFlag = flag;
		this.mItemIDs = ItemIDs;
	}

	// How many items are in the data set represented by this Adapter.
	public int getCount() {
		return mData.size();
	}

	// Get the data item associated with the specified position in the data set.
	public Object getItem(int position) {
		return mData.get(position);
	}

	// Get the row id associated with the specified position in the list.
	public long getItemId(int position) {
		return position;
	}

	// Get a View that displays the data at the specified position in the data
	// set.
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(mLayoutID, null);
		for (int i = 0; i < mFlag.length; i++) {// 备注1
			if (convertView.findViewById(mItemIDs[i]) instanceof ImageView) {
				ImageView iv = (ImageView) convertView.findViewById(mItemIDs[i]);
				iv.setBackgroundResource((Integer) mData.get(position).get(
						mFlag[i]));
				
				final String strFileName = ((TextView) convertView
						.findViewById(R.id.listview_item_title)).getText().toString();

				iv.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								Toast.makeText(v.getContext(), strFileName,
										Toast.LENGTH_SHORT).show();
								MediaReview.getInstance().playRecordByFileName(
										strFileName);
							}
						});
			} else if (convertView.findViewById(mItemIDs[i]) instanceof TextView) {
				TextView tv = (TextView) convertView.findViewById(mItemIDs[i]);
				tv.setText((String) mData.get(position).get(mFlag[i]));
			} else {
				// ...备注2
			}
		}

		// add listener of Button and CheckBox etc...
		// ((ImageButton) convertView.findViewById(R.id.listview_item_image))
		// .setOnClickListener(new View.OnClickListener() {
		// public void onClick(View v) {
		// Toast.makeText(v.getContext(),
		// "ReviewSimpleAdapter ImageButton",
		// Toast.LENGTH_SHORT).show();
		// }
		// });
		// ((CheckBox) convertView.findViewById(R.id.))
		// .setOnCheckedChangeListener(new OnCheckedChangeListener() {
		// public void onCheckedChanged(CompoundButton buttonView,
		// boolean isChecked) {
		// Toast.makeText(v.getContext(), "ReviewSimpleAdapter CheckBox",
		// Toast.LENGTH_SHORT)
		// .show();
		// }
		// });
		
		return convertView;
	}
}