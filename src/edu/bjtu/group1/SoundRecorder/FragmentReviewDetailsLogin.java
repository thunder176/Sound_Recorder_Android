package edu.bjtu.group1.SoundRecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiObjectCallBack;
import com.kii.cloud.storage.callback.KiiQueryCallBack;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;
import com.kii.cloud.storage.resumabletransfer.KiiDownloader;
import com.kii.cloud.storage.resumabletransfer.KiiRTransfer;
import com.kii.cloud.storage.resumabletransfer.KiiRTransferCallback;
import com.kii.cloud.storage.resumabletransfer.KiiRTransferManager;
import com.kii.cloud.storage.resumabletransfer.KiiUploader;
import com.kii.cloud.storage.resumabletransfer.StateStoreAccessException;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

//Singleton Pattern
public class FragmentReviewDetailsLogin extends Fragment {
	/**
	 * The fragment argument representing the ReviewDetails UI.
	 */
	private static FragmentReviewDetailsLogin instance_ReviewDetailsFragment = null;

	// Fragment status
	private boolean mbl_isFragmentReviewDetailsLoginDisplay = false;

	// Playing status
	private boolean mbl_isRecordPlaying = false;
	private boolean mbl_isNeedToPlayAfterPause = false;
	private boolean mbl_isPlayingKiiRecord = false;

	// UI components
	private Button mbtn_review_details_upload = null;
	private Button mbtn_review_details_label = null;
	private TextView mtv_review_details_name = null;
	private Button mbtn_review_details_play = null;
	private SeekBar msb_review_details_seekbar = null;

	// Record file name
	private String mstr_recordName = null;

	/**
	 * need for kii cloud upload
	 */
	private static final String BUCKET_NAME = "BjtuSoundRecorderGroup1";
	private static final String STATUS_SUSPENDED = "suspended";
	private static final String STATUS_ERROR = "error";
	private static final String STATUS_UPLOADING = "uploading";
	private static final String STATUS_FINISHED = "finished";

	private ListView mlv_review_details_upload = null;
	private UploaderAdapter mKiiUploadListAdapter;
	private ProgressDialog mKiiUploadProgress;

	static class UploadViewHolder {
		TextView title = null;
		ProgressBar progress = null;
		TextView status = null;
	}

	static class UploaderObj {
		KiiUploader uploader = null;
		long completedInBytes = 0;
		long totalSizeinBytes = 0;
		String status = STATUS_UPLOADING;
	}

	// define a custom list adapter to handle KiiUploaders
	public class UploaderAdapter extends ArrayAdapter<UploaderObj> {

		int resource;
		String response;
		Context context;
		private LayoutInflater inflater = null;

		// initialize the adapter
		public UploaderAdapter(Context context, int resource,
				List<UploaderObj> items) {
			super(context, resource, items);

			// save the resource for later
			this.resource = resource;
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// create the view
			UploadViewHolder mHolder = null;

			// get a reference to the uploader
			UploaderObj mUploaderObj = getItem(position);

			// if it's not already created
			if (convertView == null) {

				// create the view by inflating the xml resource
				// (res/layout/row.xml)
				mHolder = new UploadViewHolder();
				convertView = inflater
						.inflate(R.layout.kiifile_list_item, null);
				mHolder.progress = (ProgressBar) (convertView
						.findViewById(R.id.progressBarKiiFile));
				mHolder.title = (TextView) (convertView
						.findViewById(R.id.textViewTitle));
				mHolder.status = (TextView) (convertView
						.findViewById(R.id.textViewStatus));
				convertView.setTag(mHolder);
			}
			// it's already created, reuse it
			else {
				mHolder = (UploadViewHolder) convertView.getTag();
			}
			mHolder.title.setText(mUploaderObj.uploader.getSourceFile()
					.getName().toString());
			mHolder.progress
					.setProgress((int) ((mUploaderObj.completedInBytes * 100) / (mUploaderObj.totalSizeinBytes + 1)));
			if (mUploaderObj.status == STATUS_SUSPENDED) {
				mHolder.status
						.setText(R.string.text_review_details_kii_suspended);
			} else if (mUploaderObj.status == STATUS_ERROR) {
				mHolder.status.setText(R.string.text_review_details_kii_error);
			} else if (mUploaderObj.status == STATUS_UPLOADING) {
				mHolder.status
						.setText(R.string.text_review_details_kii_uploading);
			} else if (mUploaderObj.status == STATUS_FINISHED) {
				mHolder.status
						.setText(R.string.text_review_details_kii_finished);
			}

			mlv_review_details_upload
					.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> arg0, View arg1,
								final int position, long id) {
							onUploadListItemClick(null, arg1, position, id);
						}
					});

			mlv_review_details_upload
					.setOnItemLongClickListener(new OnItemLongClickListener() {
						public boolean onItemLongClick(AdapterView<?> parent,
								View view, final int position, long id) {
							onUploadListItemLongClick(null, null, position, id);
							return true;
						}
					});

			return convertView;
		}
	}

	public void onUploadListItemClick(ListView l, View v, final int position,
			long id) {

		final UploaderObj o = mKiiUploadListAdapter.getItem(position);

		if (o.status == STATUS_SUSPENDED || o.status == STATUS_ERROR) {
			// build the alert
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(
					getString(R.string.text_review_details_kii_ask_resume))
					.setCancelable(true)
					.setPositiveButton(
							R.string.text_review_details_kii_common_yes,
							new DialogInterface.OnClickListener() {

								// if the user chooses 'yes',
								public void onClick(DialogInterface dialog,
										int id) {

									// perform the delete action on the tapped
									// object
									performResumeUpload(position);
								}
							})
					.setNegativeButton(
							R.string.text_review_details_kii_common_no,
							new DialogInterface.OnClickListener() {

								// if the user chooses 'no'
								public void onClick(DialogInterface dialog,
										int id) {

									// simply dismiss the dialog
									dialog.cancel();
								}
							});

			// show the dialog
			builder.create().show();
		} else if (o.status == STATUS_UPLOADING) {
			// build the alert
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.text_review_details_kii_ask_suspend)
					.setCancelable(true)
					.setPositiveButton(
							R.string.text_review_details_kii_common_yes,
							new DialogInterface.OnClickListener() {

								// if the user chooses 'yes',
								public void onClick(DialogInterface dialog,
										int id) {

									// perform the delete action on the tapped
									// object
									performSuspendUpload(position);
								}
							})
					.setNegativeButton(
							R.string.text_review_details_kii_common_no,
							new DialogInterface.OnClickListener() {

								// if the user chooses 'no'
								public void onClick(DialogInterface dialog,
										int id) {

									// simply dismiss the dialog
									dialog.cancel();
								}
							});

			// show the dialog
			builder.create().show();
		}
	}

	public void onUploadListItemLongClick(ListView l, View v,
			final int position, long id) {

		// build the alert
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.text_review_details_kii_ask_delete)
				.setCancelable(true)
				.setPositiveButton(R.string.text_review_details_kii_common_yes,
						new DialogInterface.OnClickListener() {

							// if the user chooses 'yes',
							public void onClick(DialogInterface dialog, int id) {

								// perform the delete action on the tapped
								// object
								performDeleteUpload(position);
							}
						})
				.setNegativeButton(R.string.text_review_details_kii_common_no,
						new DialogInterface.OnClickListener() {

							// if the user chooses 'no'
							public void onClick(DialogInterface dialog, int id) {

								// simply dismiss the dialog
								dialog.cancel();
							}
						});

		// show the dialog
		builder.create().show();
	}

	// the user has chosen to delete an object
	// perform that action here...
	void performDeleteUpload(int position) {

		// show a progress dialog to the user
		mKiiUploadProgress = ProgressDialog.show(getActivity(), "",
				getString(R.string.text_review_details_kii_in_progressing),
				true);

		// get the object to delete based on the index of the row that was
		// tapped
		final UploaderObj o = mKiiUploadListAdapter.getItem(position);

		if (o.status == STATUS_FINISHED) {
			// remove the object from the list adapter
			mKiiUploadListAdapter.remove(o);
			mKiiUploadProgress.dismiss();
		} else {
			// delete the uploader asynchronously
			o.uploader.terminateAsync(new KiiRTransferCallback() {
				public void onTerminateCompleted(KiiRTransfer operator,
						java.lang.Exception e) {
					if (e == null) {
						mKiiUploadListAdapter.remove(o);
					} else {
						Toast.makeText(
								getActivity(),
								R.string.text_review_details_kii_delete_uploader_failed,
								Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
					mKiiUploadProgress.dismiss();
				}
			});
		}
	}

	void performResumeUpload(int position) {

		final UploaderObj o = mKiiUploadListAdapter.getItem(position);
		new UploadFileTask().execute(o);
	}

	void performSuspendUpload(int position) {

		// show a progress dialog to the user
		mKiiUploadProgress = ProgressDialog.show(getActivity(), "",
				getString(R.string.text_review_details_kii_in_progressing),
				true);

		final UploaderObj o = mKiiUploadListAdapter.getItem(position);

		o.uploader.suspendAsync(new KiiRTransferCallback() {
			public void onSuspendCompleted(KiiRTransfer operator,
					java.lang.Exception e) {
				if (e == null) {
					o.status = STATUS_SUSPENDED;
				} else {
					Toast.makeText(getActivity(),
							R.string.text_review_details_kii_suspend_failed,
							Toast.LENGTH_SHORT).show();
					o.status = STATUS_ERROR;
					e.printStackTrace();
				}
				mKiiUploadProgress.dismiss();
				mKiiUploadListAdapter.notifyDataSetChanged();
			}
		});
	}

	private class UploadFileTask extends AsyncTask<UploaderObj, String, String> {

		private UploaderObj mUploaderObj;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(UploaderObj... params) {
			mUploaderObj = params[0];

			mUploaderObj.uploader.transferAsync(new KiiRTransferCallback() {

				public void onStart(KiiRTransfer operator) {
					mUploaderObj.status = STATUS_UPLOADING;
					mKiiUploadListAdapter.notifyDataSetChanged();
				}

				public void onProgress(KiiRTransfer operator,
						long completedInBytes, long totalSizeinBytes) {
					mUploaderObj.completedInBytes = completedInBytes;
					mUploaderObj.totalSizeinBytes = totalSizeinBytes;
					mKiiUploadListAdapter.notifyDataSetChanged();
				}

				public void onTransferCompleted(KiiRTransfer operator,
						java.lang.Exception e) {
					if (e == null) {
						mUploaderObj.status = STATUS_FINISHED;
					} else {
						mUploaderObj.status = STATUS_ERROR;
						e.printStackTrace();
					}
					mKiiUploadListAdapter.notifyDataSetChanged();
				}
			});

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}
	}

	// load any existing uploaders associated with this user from the server.
	// this is done on view creation
	private void loadUploaders() {
		boolean isError = false;
		UploaderObj mUploaderObj = null;
		mKiiUploadProgress = ProgressDialog.show(getActivity(), "",
				getString(R.string.text_review_details_kii_in_progressing),
				true);

		// default to an empty adapter
		mKiiUploadListAdapter.clear();

		KiiBucket fbucket = KiiUser.getCurrentUser().bucket(BUCKET_NAME);
		KiiRTransferManager manager = fbucket.getTransferManager();
		List<KiiUploader> suspended = null;
		try {
			suspended = manager.listUploadEntries(getActivity()
					.getApplicationContext());
		} catch (StateStoreAccessException e1) {
			// Failed to access the local storage.
			// This is a rare error; you should be able to safely retry.
			Toast.makeText(getActivity(),
					R.string.text_review_details_kii_load_uploaders_failed,
					Toast.LENGTH_SHORT).show();
			isError = true;
		}
		if (isError == false) {
			for (KiiUploader uploader : suspended) {
				mUploaderObj = new UploaderObj();
				mUploaderObj.uploader = uploader;
				mKiiUploadListAdapter.add(mUploaderObj);
				new UploadFileTask().execute(mUploaderObj);
			}
		}
		mKiiUploadProgress.dismiss();
	}

	/**
	 * kii cloud upload finished
	 */

	/**
	 * need for kii cloud download
	 */
	private static final String STATUS_DOWNLOADING = "Downloading";
	private static final String STATUS_KIIFILE = "KiiFile";
	private static final String STATUS_KIIFILE_lIST_TITLE = "ListKiiFile";
	private static final String STATUS_DOWNLOADER_lIST_TITLE = "ListDownloader";

	private ListView mlv_review_details_download;
	private DownloaderAdapter mKiiDownloadListAdapter;
	private ProgressDialog mDownloadProgress;

	static class DownloadViewHolder {
		TextView title = null;
		ProgressBar progress = null;
		TextView status = null;
	}

	static class DownloaderObj {
		KiiDownloader downloader = null;
		KiiObject kiiFile = null;
		long completedInBytes = 0;
		long totalSizeinBytes = 0;
		String status = STATUS_DOWNLOADING;
	}

	// define a custom list adapter to handle KiiDownloaders
	public class DownloaderAdapter extends ArrayAdapter<DownloaderObj> {

		private LayoutInflater inflater = null;

		// initialize the adapter
		public DownloaderAdapter(Context context, int resource,
				List<DownloaderObj> items) {
			super(context, resource, items);
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// create the view
			DownloadViewHolder mHolder = null;

			// get a reference to the downloader
			DownloaderObj mDownloaderObj = getItem(position);

			// if it's not already created
			if (convertView == null) {

				// create the view by inflating the xml resource
				// (res/layout/row.xml)
				mHolder = new DownloadViewHolder();
				convertView = inflater
						.inflate(R.layout.kiifile_list_item, null);
				mHolder.progress = (ProgressBar) (convertView
						.findViewById(R.id.progressBarKiiFile));
				mHolder.title = (TextView) (convertView
						.findViewById(R.id.textViewTitle));
				mHolder.status = (TextView) (convertView
						.findViewById(R.id.textViewStatus));
				convertView.setTag(mHolder);
			}
			// it's already created, reuse it
			else {
				mHolder = (DownloadViewHolder) convertView.getTag();
			}

			if (mDownloaderObj.status == STATUS_KIIFILE_lIST_TITLE) {
				mHolder.progress.setVisibility(View.GONE);
				mHolder.title
						.setText(R.string.text_review_details_kii_files_in_cloud);
				mHolder.title.setTextColor(android.graphics.Color.BLUE);
				mHolder.title.setTypeface(Typeface.DEFAULT_BOLD,
						Typeface.ITALIC);
				mHolder.status.setText(R.string.text_review_details_kii_size);
				mHolder.status.setTextColor(android.graphics.Color.BLUE);
				mHolder.status.setTypeface(Typeface.DEFAULT_BOLD,
						Typeface.ITALIC);
			} else if (mDownloaderObj.status == STATUS_DOWNLOADER_lIST_TITLE) {
				mHolder.progress.setVisibility(View.GONE);
				mHolder.title
						.setText(R.string.text_review_details_kii_downloading_files);
				mHolder.title.setTextColor(android.graphics.Color.BLUE);
				mHolder.title.setTypeface(Typeface.DEFAULT_BOLD,
						Typeface.ITALIC);
				mHolder.status.setText(R.string.text_review_details_kii_status);
				mHolder.status.setTextColor(android.graphics.Color.BLUE);
				mHolder.status.setTypeface(Typeface.DEFAULT_BOLD,
						Typeface.ITALIC);
			} else if (mDownloaderObj.status == STATUS_KIIFILE) {
				mHolder.title.setTextColor(android.graphics.Color.BLACK);
				mHolder.title.setTypeface(Typeface.DEFAULT_BOLD,
						Typeface.NORMAL);
				mHolder.status.setTextColor(android.graphics.Color.BLACK);
				mHolder.status.setTypeface(Typeface.DEFAULT_BOLD,
						Typeface.NORMAL);
				mHolder.progress.setVisibility(View.GONE);
				mHolder.title.setText(mDownloaderObj.kiiFile.getString("title",
						" "));
				mHolder.status.setText(String.valueOf(mDownloaderObj.kiiFile
						.getLong("fileSize", 0)));
			} else {
				mHolder.title.setTextColor(android.graphics.Color.BLACK);
				mHolder.title.setTypeface(Typeface.DEFAULT_BOLD,
						Typeface.NORMAL);
				mHolder.status.setTextColor(android.graphics.Color.BLACK);
				mHolder.status.setTypeface(Typeface.DEFAULT_BOLD,
						Typeface.NORMAL);
				mHolder.title.setText(mDownloaderObj.downloader.getDestFile()
						.getName().toString());
				mHolder.progress.setVisibility(View.VISIBLE);
				mHolder.progress
						.setProgress((int) ((mDownloaderObj.completedInBytes * 100) / (mDownloaderObj.totalSizeinBytes + 1)));
				if (mDownloaderObj.status == STATUS_SUSPENDED) {
					mHolder.status
							.setText(R.string.text_review_details_kii_suspended);
				} else if (mDownloaderObj.status == STATUS_ERROR) {
					mHolder.status
							.setText(R.string.text_review_details_kii_error);
				} else if (mDownloaderObj.status == STATUS_DOWNLOADING) {
					mHolder.status
							.setText(R.string.text_review_details_kii_downloading);
				} else if (mDownloaderObj.status == STATUS_FINISHED) {
					mHolder.status
							.setText(R.string.text_review_details_kii_finished);
				}
			}

			if (mDownloaderObj.status != STATUS_KIIFILE_lIST_TITLE
					&& mDownloaderObj.status != STATUS_DOWNLOADER_lIST_TITLE) {

				mlv_review_details_download
						.setOnItemClickListener(new OnItemClickListener() {
							public void onItemClick(AdapterView<?> arg0,
									View arg1, final int position, long id) {
								onDownloadListItemClick(null, arg1, position,
										id);
							}
						});

				mlv_review_details_download
						.setOnItemLongClickListener(new OnItemLongClickListener() {
							public boolean onItemLongClick(
									AdapterView<?> parent, View view,
									final int position, long id) {
								onDownloadListItemLongClick(null, null,
										position, id);
								return true;
							}
						});
			}
			return convertView;
		}
	}

	public void performDeleteKiiFileDownload(int position) {
		final DownloaderObj o = mKiiDownloadListAdapter.getItem(position);
		// show a progress dialog to the user
		mDownloadProgress = ProgressDialog.show(getActivity(), "",
				getString(R.string.text_review_details_kii_in_progressing),
				true);
		o.kiiFile.delete(new KiiObjectCallBack() {
			public void onDeleteCompleted(int token, java.lang.Exception e) {
				if (e == null) {
					mKiiDownloadListAdapter.remove(o);
				} else {
					Toast.makeText(getActivity(),
							R.string.text_review_details_kii_delete_failed,
							Toast.LENGTH_SHORT).show();
				}
				mDownloadProgress.dismiss();
			}
		});
	}

	void performDeleteDownload(int position) {
		// show a progress dialog to the user
		mDownloadProgress = ProgressDialog.show(getActivity(), "",
				getString(R.string.text_review_details_kii_in_progressing),
				true);

		// get the object to delete based on the index of the row that was
		// tapped
		final DownloaderObj o = mKiiDownloadListAdapter.getItem(position);

		if (o.status == STATUS_FINISHED) {
			// remove the object from the list adapter
			mKiiDownloadListAdapter.remove(o);
			mDownloadProgress.dismiss();
		} else {
			// delete the object asynchronously
			o.downloader.terminateAsync(new KiiRTransferCallback() {
				public void onTerminateCompleted(KiiRTransfer operator,
						java.lang.Exception e) {
					if (e == null) {
						mKiiDownloadListAdapter.remove(o);
					} else {
						Toast.makeText(getActivity(),
								R.string.text_review_details_kii_delete_failed,
								Toast.LENGTH_SHORT).show();
					}
					mDownloadProgress.dismiss();
				}
			});
		}
	}

	void performResumeDownload(int position) {
		final DownloaderObj o = mKiiDownloadListAdapter.getItem(position);
		new DownloadFileTask().execute(o);
	}

	void performSuspendDownload(int position) {
		// show a progress dialog to the user
		mDownloadProgress = ProgressDialog.show(getActivity(), "",
				getString(R.string.text_review_details_kii_in_progressing),
				true);
		final DownloaderObj o = mKiiDownloadListAdapter.getItem(position);
		o.downloader.suspendAsync(new KiiRTransferCallback() {
			public void onSuspendCompleted(KiiRTransfer operator,
					java.lang.Exception e) {
				if (e == null) {
					o.status = STATUS_SUSPENDED;
				} else {
					Toast.makeText(getActivity(),
							R.string.text_review_details_kii_suspend_failed,
							Toast.LENGTH_SHORT).show();
					o.status = STATUS_ERROR;
				}
				mDownloadProgress.dismiss();
				mKiiDownloadListAdapter.notifyDataSetChanged();
			}
		});
	}

	void performAddDownload(int position) {
		final DownloaderObj o = mKiiDownloadListAdapter.getItem(position);
		DownloaderObj mDownloaderObj = null;
		KiiDownloader mDownloader = null;
		String title;

		title = o.kiiFile.getString("title",
				getString(R.string.text_review_details_kii_default_title));
		try {
			mDownloader = o.kiiFile.downloader(getActivity()
					.getApplicationContext(), new File(SaveOrLoadFileHelper
					.getInstance().getKiiDirByFileName(title)));
		} catch (Exception e) {
			Toast.makeText(getActivity(),
					R.string.text_review_details_kii_add_downloader_failed,
					Toast.LENGTH_SHORT).show();
			return;
		}
		mDownloaderObj = new DownloaderObj();
		mDownloaderObj.kiiFile = o.kiiFile;
		mDownloaderObj.downloader = mDownloader;
		mDownloaderObj.status = STATUS_DOWNLOADING;
		mKiiDownloadListAdapter.add(mDownloaderObj);
		new DownloadFileTask().execute(mDownloaderObj);
	}

	public void onDownloadListItemClick(ListView l, View v, final int position,
			long id) {
		final DownloaderObj o = mKiiDownloadListAdapter.getItem(position);

		if (o.status == STATUS_SUSPENDED || o.status == STATUS_ERROR) {
			// build the alert
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.text_review_details_kii_ask_resume)
					.setCancelable(true)
					.setPositiveButton(
							R.string.text_review_details_kii_common_yes,
							new DialogInterface.OnClickListener() {

								// if the user chooses 'yes',
								public void onClick(DialogInterface dialog,
										int id) {

									// perform the delete action on the tapped
									// object
									performResumeDownload(position);
								}
							})
					.setNegativeButton(
							R.string.text_review_details_kii_common_no,
							new DialogInterface.OnClickListener() {

								// if the user chooses 'no'
								public void onClick(DialogInterface dialog,
										int id) {

									// simply dismiss the dialog
									dialog.cancel();
								}
							});

			// show the dialog
			builder.create().show();
		} else if (o.status == STATUS_DOWNLOADING) {
			// build the alert
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.text_review_details_kii_ask_suspend)
					.setCancelable(true)
					.setPositiveButton(
							R.string.text_review_details_kii_common_yes,
							new DialogInterface.OnClickListener() {

								// if the user chooses 'yes',
								public void onClick(DialogInterface dialog,
										int id) {

									// perform the delete action on the tapped
									// object
									performSuspendDownload(position);
								}
							})
					.setNegativeButton(
							R.string.text_review_details_kii_common_no,
							new DialogInterface.OnClickListener() {

								// if the user chooses 'no'
								public void onClick(DialogInterface dialog,
										int id) {

									// simply dismiss the dialog
									dialog.cancel();
								}
							});

			// show the dialog
			builder.create().show();
		} else if (o.status == STATUS_KIIFILE) {
			// build the alert
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.text_review_details_kii_ask_download)
					.setCancelable(true)
					.setPositiveButton(
							R.string.text_review_details_kii_common_yes,
							new DialogInterface.OnClickListener() {

								// if the user chooses 'yes',
								public void onClick(DialogInterface dialog,
										int id) {

									// perform the delete action on the tapped
									// object
									performAddDownload(position);
								}
							})
					.setNegativeButton(
							R.string.text_review_details_kii_common_no,
							new DialogInterface.OnClickListener() {

								// if the user chooses 'no'
								public void onClick(DialogInterface dialog,
										int id) {

									// simply dismiss the dialog
									dialog.cancel();
								}
							});

			// show the dialog
			builder.create().show();
		} else if (o.status == STATUS_FINISHED) {
			// TODO play the Records
			// build the alert
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.text_review_details_kii_ask_play)
					.setCancelable(true)
					.setPositiveButton(
							R.string.text_review_details_kii_common_yes,
							new DialogInterface.OnClickListener() {

								// if the user chooses 'yes',
								public void onClick(DialogInterface dialog,
										int id) {

									// perform the delete action on the tapped
									// object
									String title = o.kiiFile
											.getString(
													"title",
													getString(R.string.text_review_details_kii_default_title));
									stopPlayDetails();
									mstr_recordName = title;
									mtv_review_details_name
											.setText(mstr_recordName);
									mbl_isPlayingKiiRecord = true;

									// MediaReview.getInstance().playKiiRecordByName();
									// playKiiRecord();
								}
							})
					.setNegativeButton(
							R.string.text_review_details_kii_common_no,
							new DialogInterface.OnClickListener() {

								// if the user chooses 'no'
								public void onClick(DialogInterface dialog,
										int id) {

									// simply dismiss the dialog
									dialog.cancel();
								}
							});

			// show the dialog
			builder.create().show();
		}
	}

	public void onDownloadListItemLongClick(ListView l, View v,
			final int position, long id) {
		final DownloaderObj o = mKiiDownloadListAdapter.getItem(position);
		if (o.status == STATUS_KIIFILE) {

			// build the alert
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.text_review_details_kii_ask_delete)
					.setCancelable(true)
					.setPositiveButton(
							R.string.text_review_details_kii_common_yes,
							new DialogInterface.OnClickListener() {

								// if the user chooses 'yes',
								public void onClick(DialogInterface dialog,
										int id) {

									// perform the delete action on the tapped
									// object
									performDeleteKiiFileDownload(position);
								}
							})
					.setNegativeButton(
							R.string.text_review_details_kii_common_no,
							new DialogInterface.OnClickListener() {

								// if the user chooses 'no'
								public void onClick(DialogInterface dialog,
										int id) {

									// simply dismiss the dialog
									dialog.cancel();
								}
							});

			// show the dialog
			builder.create().show();
		} else {

			// build the alert
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.text_review_details_kii_ask_delete)
					.setCancelable(true)
					.setPositiveButton(
							R.string.text_review_details_kii_common_yes,
							new DialogInterface.OnClickListener() {

								// if the user chooses 'yes',
								public void onClick(DialogInterface dialog,
										int id) {

									// perform the delete action on the tapped
									// object
									performDeleteUpload(position);
								}
							})
					.setNegativeButton(
							R.string.text_review_details_kii_common_no,
							new DialogInterface.OnClickListener() {

								// if the user chooses 'no'
								public void onClick(DialogInterface dialog,
										int id) {

									// simply dismiss the dialog
									dialog.cancel();
								}
							});

			// show the dialog
			builder.create().show();
		}
	}

	private void loadKiiFiles() {
		DownloaderObj mDownloaderObj = null;
		// mDownloadProgress = ProgressDialog.show(getActivity(), "",
		// getString(R.string.text_review_details_kii_in_progressing),
		// true);
		// first of all add kiiFile list title
		mDownloaderObj = new DownloaderObj();
		mDownloaderObj.kiiFile = null;
		mDownloaderObj.status = STATUS_KIIFILE_lIST_TITLE;
		mDownloaderObj.downloader = null;
		mKiiDownloadListAdapter.add(mDownloaderObj);

		KiiQuery query = new KiiQuery(null);
		query.sortByAsc("_created");

		KiiBucket fbucket = KiiUser.getCurrentUser().bucket(BUCKET_NAME);
		fbucket.query(new KiiQueryCallBack<KiiObject>() {
			DownloaderObj mDownloaderObj = null;

			public void onQueryCompleted(int token,
					KiiQueryResult<KiiObject> result, java.lang.Exception e) {
				if (e == null) {
					// add the KiiFiles to the adapter (adding to the listview)
					List<KiiObject> kiiFilesLists = result.getResult();
					for (KiiObject kiiFile : kiiFilesLists) {
						mDownloaderObj = new DownloaderObj();
						mDownloaderObj.kiiFile = kiiFile;
						mDownloaderObj.status = STATUS_KIIFILE;
						mDownloaderObj.downloader = null;
						mKiiDownloadListAdapter.add(mDownloaderObj);
					}
				} else {
					Toast.makeText(
							getActivity(),
							R.string.text_review_details_kii_load_kiifile_failed,
							Toast.LENGTH_SHORT).show();
				}
				loadDownloaders();
			}
		}, query);
		// mDownloadProgress.dismiss();
	}

	// load any existing downloaders associated with this user from the server.
	// this is done on view creation
	private void loadDownloaders() {
		boolean isError = false;
		DownloaderObj mDownloaderObj = null;

		// all add downloader list title
		mDownloaderObj = new DownloaderObj();
		mDownloaderObj.kiiFile = null;
		mDownloaderObj.status = STATUS_DOWNLOADER_lIST_TITLE;
		mDownloaderObj.downloader = null;
		mKiiDownloadListAdapter.add(mDownloaderObj);

		// mDownloadProgress = ProgressDialog.show(getActivity(), "",
		// getString(R.string.text_review_details_kii_in_progressing),
		// true);

		KiiBucket fbucket = KiiUser.getCurrentUser().bucket(BUCKET_NAME);
		KiiRTransferManager manager = fbucket.getTransferManager();
		List<KiiDownloader> suspended = null;
		try {
			suspended = manager.listDownloadEntries(getActivity()
					.getApplicationContext());
		} catch (StateStoreAccessException e1) {
			// Failed to access the local storage.
			// This is a rare error; you should be able to safely retry.
			Toast.makeText(getActivity(),
					R.string.text_review_details_kii_load_downloader_failed,
					Toast.LENGTH_SHORT).show();
			isError = true;
		}
		if (isError == false) {
			for (KiiDownloader downloader : suspended) {
				mDownloaderObj = new DownloaderObj();
				mDownloaderObj.downloader = downloader;
				mDownloaderObj.status = STATUS_DOWNLOADING;
				mKiiDownloadListAdapter.add(mDownloaderObj);
				new DownloadFileTask().execute(mDownloaderObj);
			}
		}
		// mDownloadProgress.dismiss();
	}

	private class DownloadFileTask extends
			AsyncTask<DownloaderObj, String, String> {

		private DownloaderObj mDownloaderObj;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(DownloaderObj... params) {
			KiiObject mKiiFile = null;
			mDownloaderObj = params[0];
			try {
				mKiiFile = mDownloaderObj.kiiFile;
				mKiiFile.refresh();
			} catch (Exception e) {
				mDownloaderObj.status = STATUS_ERROR;
				// mKiiDownloadListAdapter.notifyDataSetChanged();
				e.printStackTrace();
				return null;
			}

			mDownloaderObj.downloader.transferAsync(new KiiRTransferCallback() {
				public void onStart(KiiRTransfer operator) {
					mDownloaderObj.status = STATUS_DOWNLOADING;
					mKiiDownloadListAdapter.notifyDataSetChanged();
				}

				public void onProgress(KiiRTransfer operator,
						long completedInBytes, long totalSizeinBytes) {
					mDownloaderObj.completedInBytes = completedInBytes;
					mDownloaderObj.totalSizeinBytes = totalSizeinBytes;
					mKiiDownloadListAdapter.notifyDataSetChanged();
				}

				public void onTransferCompleted(KiiRTransfer operator,
						java.lang.Exception e) {
					if (e == null) {
						mDownloaderObj.status = STATUS_FINISHED;
						mKiiDownloadListAdapter.notifyDataSetChanged();
					} else {
						mDownloaderObj.status = STATUS_ERROR;
						mKiiDownloadListAdapter.notifyDataSetChanged();
						Toast.makeText(
								getActivity(),
								R.string.text_review_details_kii_download_failed,
								Toast.LENGTH_SHORT).show();
					}
				}
			});

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}
	}

	/**
	 * kii cloud download finished
	 */

	public FragmentReviewDetailsLogin() {
	}

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static FragmentReviewDetailsLogin getInstance() {
		if (instance_ReviewDetailsFragment == null) {
			synchronized (FragmentReviewDetailsLogin.class) {
				if (null == instance_ReviewDetailsFragment)
					instance_ReviewDetailsFragment = new FragmentReviewDetailsLogin();
			}
		}
		return instance_ReviewDetailsFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.e("Fragment_lifecircle_testing",
				"FragmentReviewDetailsLogin_onCreateView");
		View rootView = null;
		setHasOptionsMenu(true);
		rootView = inflater.inflate(R.layout.fragment_review_details_login,
				container, false);

		mbl_isPlayingKiiRecord = false;
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

		// kii cloud login check
		if (KiiUser.getCurrentUser() == null) {
			Toast.makeText(getActivity(),
					"You need to sign in or sign up first!", Toast.LENGTH_LONG)
					.show();
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager
					.beginTransaction()
					.replace(R.id.container,
							FragmentReviewDetailsUnlogin.getInstance())
					.commit();
		}
		// kii cloud download
		mKiiDownloadListAdapter = new DownloaderAdapter(getActivity(),
				R.layout.kiifile_list_item, new ArrayList<DownloaderObj>());
		mlv_review_details_download = (ListView) rootView
				.findViewById(R.id.listview_review_details_download);
		mlv_review_details_download.setAdapter(mKiiDownloadListAdapter);
		// query for any previously-created downloaderss
		mKiiDownloadListAdapter.clear();
		loadKiiFiles();
		// kii cloud upload
		mlv_review_details_upload = (ListView) rootView
				.findViewById(R.id.listview_review_details_upload);
		mKiiUploadListAdapter = new UploaderAdapter(getActivity(),
				R.layout.kiifile_list_item, new ArrayList<UploaderObj>());
		mlv_review_details_upload.setAdapter(mKiiUploadListAdapter);
		loadUploaders();

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
			// upload the current record file
			public void onClick(View v) {
				UploaderObj mUploaderObj = null;
				KiiObject kfile = null;
				KiiUploader uploader = null;

				if (mstr_recordName != null) {
					// File localFile = new
					// File(Environment.getExternalStorageDirectory(),
					// mFilePath);
					File localFile = new File(SaveOrLoadFileHelper
							.getInstance().getRecordDirByFileName(
									mstr_recordName));
					if (localFile.isFile()) {
						try {
							KiiBucket bucket = KiiUser.getCurrentUser().bucket(
									BUCKET_NAME);
							kfile = bucket.object();
							kfile.set("title", localFile.getName());
							kfile.set("fileSize", localFile.length());
							uploader = kfile.uploader(getActivity(), localFile);
						} catch (Exception e) {
							Toast.makeText(
									getActivity(),
									R.string.text_review_details_kii_create_uploader_failed,
									Toast.LENGTH_LONG).show();
							return;
						}
						mUploaderObj = new UploaderObj();
						mUploaderObj.uploader = uploader;
						mKiiUploadListAdapter.add(mUploaderObj);
						new UploadFileTask().execute(mUploaderObj);
					} else {
						Toast.makeText(
								getActivity(),
								R.string.text_review_details_kii_file_does_not_exist,
								Toast.LENGTH_LONG).show();
					}
				}
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

							if (!mbl_isPlayingKiiRecord) {
								MediaReview.getInstance().playRecordByFileName(
										mstr_recordName);
							} else {
								// play records in kii cloud Folder
								MediaReview.getInstance().playKiiRecordByName(
										mstr_recordName);
							}
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_review_details, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_capture) {
			// show capture fragment
			stopPlayDetails();
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.container, FragmentCapture.getInstance())
					.commit();
			return true;
		} else if (id == R.id.action_review) {
			// show capture fragment
			stopPlayDetails();
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.container, FragmentReview.getInstance())
					.commit();
			return true;
		} else if (id == R.id.action_logout) {
			if (KiiUser.getCurrentUser() == null) {
				Toast.makeText(getActivity(),
						"You need to sign in or sign up first!",
						Toast.LENGTH_LONG).show();
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager
						.beginTransaction()
						.replace(R.id.container,
								FragmentReviewDetailsUnlogin.getInstance())
						.commit();
			} else {
				KiiUser.logOut();
				SaveOrLoadFileHelper.getInstance().saveToken(
						getActivity().getApplicationContext(), "");
				FragmentReviewDetailsUnlogin.getInstance().setIsKiiLogin(false);

				Toast.makeText(getActivity(),
						"Current user is loged out, please login again",
						Toast.LENGTH_LONG).show();
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager.beginTransaction()
						.replace(R.id.container, FragmentReview.getInstance())
						.commit();
			}
		} else if (id == R.id.action_tutorial) {
			Toast.makeText(getActivity(), "tutorial action.",
					Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		Log.e("Fragment_lifecircle_testing", "ReviewDetailsFragment_onStart");
		mbl_isFragmentReviewDetailsLoginDisplay = true;
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
		mbl_isFragmentReviewDetailsLoginDisplay = false;
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

	public boolean isFragmentReviewDetailsLoginDisplay() {
		return mbl_isFragmentReviewDetailsLoginDisplay;
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
