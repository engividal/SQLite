package dominando.android.hotel;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Ismael on 02/01/16.
 */
public class HotelListFragment extends ListFragment
        implements ActionMode.Callback,
        AdapterView.OnItemLongClickListener,
        LoaderManager.LoaderCallbacks<Cursor>{
    ListView mListView;
    ActionMode mActionMode;
    CursorAdapter mAdapter;
    String mTextoBusca;

    HotelRepositorio mRepositorio;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRepositorio = new HotelRepositorio(getActivity());

        mAdapter = new HotelCursorAdapter(getActivity(), null);

        mListView = getListView();
        setListAdapter(mAdapter);
        mListView.setOnItemLongClickListener(this);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mActionMode != null){
            iniciarModoExclusao();
            atualizarTitulo();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if(mActionMode == null) {
            Activity activity = getActivity();
            if (activity instanceof AoClicarNoHotel) {
                Cursor cursor = (Cursor) l.getItemAtPosition(position);

                Hotel hotel =  HotelRepositorio.hotelFromCursor(cursor);

                AoClicarNoHotel listener = (AoClicarNoHotel) activity;
                listener.clicouNoHotel(hotel);
            }
        }else {
            atualizarItensMarcados(mListView, position);
            if(qtdeItensMarcados() == 0){
                mActionMode.finish();
            }
        }
    }

    public void buscar(String s) {
        mTextoBusca = TextUtils.isEmpty(s) ? null : s;
        getLoaderManager().restartLoader(0, null, this);
    }

    public void limparBusca() {
       buscar(null);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.menu_delete_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if(item.getItemId() == R.id.acao_delete){
            remover();
            mode.finish();
            return true;
        }
        return false;
    }

    private void remover() {
        final List<Hotel> hoteisExcluidos = new ArrayList<>();
        SparseBooleanArray checked = mListView.getCheckedItemPositions();
        for(int i = checked.size()-1; i >= 0; i--){
            if(checked.valueAt(i)){
                int position = checked.keyAt(i);
                Cursor cursor = (Cursor) mListView.getItemAtPosition(position);
                Hotel hotel = mRepositorio.hotelFromCursor(cursor);
                hoteisExcluidos.add(hotel);
                mRepositorio.excluir(hotel);
            }
        }
        Activity activity = getActivity();
        if (activity instanceof AoExcluirHoteis){
            AoExcluirHoteis aoExcluirHoteis =
                    (AoExcluirHoteis) activity;
            aoExcluirHoteis.exclusaoCompleta(hoteisExcluidos);
        }
        Snackbar.make(mListView, getString(R.string.mensagem_excluir, hoteisExcluidos.size()), Snackbar.LENGTH_LONG)
                .setAction(R.string.desfazer, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (Hotel hotel:hoteisExcluidos){
                            hotel.id = 0;
                            mRepositorio.salvar(hotel);
                        }
                        limparBusca();
                    }
                }).show();

    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        for (int i = 0; i< mListView.getCount(); i++){
            mListView.setItemChecked(i, false);
        }
        mListView.clearChoices();
        mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        limparBusca();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        boolean consumed = (mActionMode == null);
        if (consumed){
            iniciarModoExclusao();
            mListView.setItemChecked(position, true);
            atualizarItensMarcados(mListView, position);
        }
        return consumed;
    }

    private void iniciarModoExclusao(){
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        mActionMode = activity.startSupportActionMode(this);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    private void atualizarItensMarcados(ListView l, int position){
        l.setItemChecked(position, l.isItemChecked(position));
        atualizarTitulo();
    }

    private int qtdeItensMarcados(){
        SparseBooleanArray checked = mListView.getCheckedItemPositions();
        int checkedCount = 0;
        for (int i = 0; i < checked.size(); i++){
            if(checked.valueAt(i)){
                checkedCount++;
            }
        }
        return checkedCount;
    }
    private void atualizarTitulo(){
        int checkedCount = qtdeItensMarcados();
        String selecionados = getResources().getQuantityString(
                R.plurals.numero_selecionados,
                checkedCount, checkedCount
        );
        mActionMode.setTitle(selecionados);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mRepositorio.buscar(getActivity(), mTextoBusca);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public interface AoClicarNoHotel {
    void clicouNoHotel(Hotel hotel);
}
    public interface AoExcluirHoteis{
        void exclusaoCompleta(List<Hotel> hoteis);
    }
}
