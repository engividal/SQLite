package dominando.android.hotel;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

/**
 * Created by Ismael on 07/01/16.
 */
public class HotelCursorAdapter extends CursorAdapter {
    public HotelCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_hotel, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtMessage = (TextView) view.findViewById(R.id.txtNome);
        RatingBar rtbEstrelas = (RatingBar) view.findViewById(R.id.rtbEstrelas);

        rtbEstrelas.setRating(cursor.getFloat(cursor.getColumnIndex(HotelSQLHelper.COLUNA_ESTRELAS)));
        txtMessage.setText(cursor.getString(cursor.getColumnIndex(HotelSQLHelper.COLUNA_NOME)));

        int status = cursor.getInt(cursor.getColumnIndex(HotelSQLHelper.COLUNA_STATUS));
        if (status == Hotel.Status.EXCLUIR.ordinal()){
            txtMessage.setTextColor(Color.RED);
        } else {
            txtMessage.setTextColor(Color.BLACK);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        ListView listView = (ListView) parent;
        int color = listView.isItemChecked(position) ? Color.argb(0xFF, 0x31, 0xB6, 0xE7) : Color.TRANSPARENT;
        v.setBackgroundColor(color);


        return v;
    }
}
