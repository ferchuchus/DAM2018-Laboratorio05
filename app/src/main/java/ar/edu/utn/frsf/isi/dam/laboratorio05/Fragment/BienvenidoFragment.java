package ar.edu.utn.frsf.isi.dam.laboratorio05.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ar.edu.utn.frsf.isi.dam.laboratorio05.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class BienvenidoFragment extends Fragment {


    public BienvenidoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bienvenido, container, false);
    }

}