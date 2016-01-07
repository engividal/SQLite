package dominando.android.hotel;


import android.app.Dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by Ismael on 04/01/16.
 */
public class SobreDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE){
                    Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.nglauber.com.br"));
                    startActivity(it);
                }
            }
        };
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.sobre_titulo)
                .setMessage(R.string.sobre_mensagem)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(R.string.sobre_botao_site, listener)
                .create();

        return dialog;
    }
}
