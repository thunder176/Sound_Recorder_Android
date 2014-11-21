
package edu.bjtu.group1.SoundRecorder;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

public class FragmentProgressDialog extends DialogFragment {
    public static FragmentProgressDialog newInstance(String msg) {
        FragmentProgressDialog frag = new FragmentProgressDialog();
        Bundle args = new Bundle();
        args.putString("msg", msg);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String msg = getArguments().getString("msg");
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(msg);
        return dialog;
    }
    
}
