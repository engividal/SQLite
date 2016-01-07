package dominando.android.hotel;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Ismael on 02/01/16.
 */
public class HotelListFragment extends ListFragment
        implements ActionMode.Callback, AdapterView.OnItemLongClickListener{
    ListView mListView;
    ActionMode mActionMode;

    List<Hotel> mHoteis;
    ArrayAdapter<Hotel> mAdapter;
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
        mListView = getListView();
        limparBusca();
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
                Hotel hotel = (Hotel) l.getItemAtPosition(position);

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
        if (s == null || s.trim().equals("")) {
            limparBusca();
            return;
        }
        List<Hotel> hoteisEncontrados = mRepositorio.buscarHotel("%" + s + "%");

        mListView.setOnItemLongClickListener(null);
        mAdapter = new ArrayAdapter<Hotel>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                hoteisEncontrados);
        setListAdapter(mAdapter);
    }

    public void limparBusca() {
        mHoteis = mRepositorio.buscarHotel(null);
        mListView.setOnItemLongClickListener(this);

        mAdapter = new ArrayAdapter<Hotel>(
                getActivity(),
                android.R.layout.simple_list_item_activated_1,
                mHoteis);
        setListAdapter(mAdapter);
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
                Hotel hotel = mHoteis.remove(position);
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

    public interface AoClicarNoHotel {
    void clicouNoHotel(Hotel hotel);
}
    public interface AoExcluirHoteis{
        void exclusaoCompleta(List<Hotel> hoteis);
    }
}
