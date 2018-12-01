package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;

public class BuscarFragment extends Fragment{

    private Spinner spinnerTipoReclamo;
    private Button btnBuscar;
    private ArrayAdapter adapterTipoReclamo;
    private OnBuscarListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_buscar, container, false);
        spinnerTipoReclamo= (Spinner) v.findViewById(R.id.spinnerTipos);
        btnBuscar= (Button) v.findViewById(R.id.btnBuscar);
        List<String> tiposReclamos= getNames();
        adapterTipoReclamo= new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, tiposReclamos);
        spinnerTipoReclamo.setAdapter(adapterTipoReclamo);

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.buscarReclamosTipo(spinnerTipoReclamo.getSelectedItem().toString());
            }
        });

        return v;
    }

    private List<String> getNames(){
        List<String> stringList= new ArrayList<>();
        List<Reclamo.TipoReclamo> list= Arrays.asList(Reclamo.TipoReclamo.values());
        for (int i=0; list.size()>i; i++){
            stringList.add(list.get(i).toString());
        }
        return  stringList;
    }


    public interface OnBuscarListener{
        public void buscarReclamosTipo(String tipo);
    }

    public void setListener (OnBuscarListener listener){ this.listener=listener;}
}
