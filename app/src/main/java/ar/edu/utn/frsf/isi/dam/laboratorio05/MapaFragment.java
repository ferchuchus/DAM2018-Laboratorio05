package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Objects;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapaFragment extends SupportMapFragment implements OnMapReadyCallback {
    private static final int EVENTO_UPDATE_MAP=50;
    private GoogleMap miMapa;
    private OnMapaListener listener;
    private int tipoMapa=0;
    private ArrayList<Reclamo> listaReclamos= new ArrayList<Reclamo>();
    private ReclamoDao reclamoDao;

    public MapaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup
            container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container,
                savedInstanceState);
        Bundle argumentos = getArguments();
        if (argumentos != null) {
            tipoMapa = argumentos.getInt("tipo_mapa", 0);
        }
        getMapAsync(this);
        reclamoDao= MyDatabase.getInstance(this.getActivity()).getReclamoDao();
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        miMapa = map;
        switch (tipoMapa) {
           case 1:
               miMapa.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                     @Override
                      public void onMapLongClick(LatLng latLng) {
                      listener.coordenadasSeleccionadas(latLng);
                      }
               });
               break;
           case 2:
               obtenerReclamos();
               break;

       }
       actualizarMapa();
    }

    private void actualizarMapa(){
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    9999);
        }
        miMapa.setMyLocationEnabled(true);
    }

    public interface OnMapaListener {
        public void coordenadasSeleccionadas(LatLng c);
    }

    public void setListener(OnMapaListener listener){
        this.listener=listener;
    }

    private void obtenerReclamos(){
        listaReclamos.clear();
        Runnable cargarReclamos= new Runnable() {
            @Override
            public void run() {
                listaReclamos.addAll(reclamoDao.getAll());
                Message copleteMessage= handler.obtainMessage(EVENTO_UPDATE_MAP);
                copleteMessage.sendToTarget();
                }
            };
        Thread thread= new Thread(cargarReclamos);
        thread.start();
    }

    Handler handler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==EVENTO_UPDATE_MAP){
                ArrayList<MarkerOptions> marcadores= new ArrayList<>();
                for(int i=0; listaReclamos.size()>i; i++){
                    LatLng latLng= new LatLng(listaReclamos.get(i).getLatitud(),
                            listaReclamos.get(i).getLongitud());
                    miMapa.addMarker(new MarkerOptions().position(latLng));
                    marcadores.add(new MarkerOptions().position(latLng));
                }
                LatLngBounds.Builder builder= new LatLngBounds.Builder();
                for(MarkerOptions markerOptions: marcadores) builder.include(markerOptions.getPosition());
                LatLngBounds bounds= builder.build();
                int padding= 0;
                CameraUpdate cu= CameraUpdateFactory.newLatLngBounds(bounds, padding);
                miMapa.moveCamera(cu);
            }
        }
    };
}
