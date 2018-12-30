package ar.edu.utn.frsf.isi.dam.laboratorio05.Fragment;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.Objects;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapaFragment extends SupportMapFragment implements OnMapReadyCallback {
    private static final int EVENTO_LISTA_RECLAMOS=1;
    private static final int EVENTO_RECLAMO=2;
    private static final int EVENTO_MAPA_CALOR=3;
    private static final int EVENTO_RECLAMO_TIPO=4;
    private GoogleMap miMapa;
    private OnMapaListener listener;
    private int tipoMapa=0;
    private ArrayList<Reclamo> listaReclamos= new ArrayList<Reclamo>();
    private ReclamoDao reclamoDao;
    private Reclamo reclamo;

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
            case 3:
                obtenerReclamo();
                break;
            case 4:
                obtenerReclamos();
                break;
            case 5:
                obtenerReclamosTipo();

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
                Message completeMessage;
                if(tipoMapa==2) completeMessage= handler.obtainMessage(EVENTO_LISTA_RECLAMOS);
                else completeMessage= handler.obtainMessage(EVENTO_MAPA_CALOR);
                completeMessage.sendToTarget();
                }
            };
        Thread thread= new Thread(cargarReclamos);
        thread.start();
    }

    private void obtenerReclamo(){
        Runnable cargarReclamo=(new Runnable() {
            @Override
            public void run() {
                reclamo=reclamoDao.getById(getArguments().getInt("idReclamo"));
                Message completeMessage= handler.obtainMessage(EVENTO_RECLAMO);
                completeMessage.sendToTarget();
            }
        });
        Thread thread= new Thread(cargarReclamo);
        thread.start();
    }

    private void obtenerReclamosTipo() {
            Runnable reclamosTipo= new Runnable() {
                @Override
                public void run() {
                    listaReclamos.clear();
                    listaReclamos.addAll(reclamoDao.getByTipo(getArguments().getString("tipo_reclamo")));
                    if(!listaReclamos.isEmpty()){
                    Message completeMessage= handler.obtainMessage(EVENTO_RECLAMO_TIPO);
                    completeMessage.sendToTarget();}
                }
            };
            Thread thread= new Thread(reclamosTipo);
            thread.start();
    }

    Handler handler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            CameraUpdate cu;
            LatLng latLng;
            switch (msg.what){
                case EVENTO_LISTA_RECLAMOS:
                    ArrayList<MarkerOptions> marcadores= new ArrayList<>();
                    for(int i=0; listaReclamos.size()>i; i++){
                        latLng= new LatLng(listaReclamos.get(i).getLatitud(),
                                listaReclamos.get(i).getLongitud());
                        miMapa.addMarker(new MarkerOptions().position(latLng));
                        marcadores.add(new MarkerOptions().position(latLng));
                    }
                    LatLngBounds.Builder builder= new LatLngBounds.Builder();
                    for(MarkerOptions markerOptions: marcadores) builder.include(markerOptions.getPosition());
                    LatLngBounds bounds= builder.build();
                    cu= CameraUpdateFactory.newLatLngBounds(bounds, 0);
                    miMapa.moveCamera(cu);
                    break;
                case EVENTO_RECLAMO:
                    latLng= new LatLng(reclamo.getLatitud(), reclamo.getLongitud());
                    miMapa.addMarker(new MarkerOptions().position(latLng));
                    Circle circulo= miMapa.addCircle(new CircleOptions().center(latLng)
                    .radius(500));
                    circulo.setFillColor(Color.TRANSPARENT);
                    circulo.setStrokeColor(Color.RED);
                    cu= CameraUpdateFactory.newLatLngZoom(latLng, 15.0f);
                    miMapa.moveCamera(cu);
                    break;
                case EVENTO_MAPA_CALOR:
                    ArrayList<LatLng>coordenadas = new ArrayList<>();
                    LatLngBounds.Builder builder1= new LatLngBounds.Builder();
                    for(int i=0; listaReclamos.size()>i; i++){
                        latLng= new LatLng(listaReclamos.get(i).getLatitud(),
                                listaReclamos.get(i).getLongitud());
                        coordenadas.add(latLng);
                        builder1.include(latLng);
                    }
                    TileProvider heatMapTileProvider = new HeatmapTileProvider.Builder().data(coordenadas)
                            .build();
                    miMapa.addTileOverlay(new TileOverlayOptions().tileProvider(heatMapTileProvider));
                    LatLngBounds latLngBounds= builder1.build();
                    cu= CameraUpdateFactory.newLatLngBounds(latLngBounds, 0);
                    miMapa.moveCamera(cu);
                    break;
                case EVENTO_RECLAMO_TIPO:
                    ArrayList<LatLng> coordenadas1= new ArrayList<>();
                    LatLngBounds.Builder builder2= new LatLngBounds.Builder();
                    PolylineOptions polylineOptions= new PolylineOptions();
                    for(int i=0; listaReclamos.size()>i; i++){
                        latLng= new LatLng(listaReclamos.get(i).getLatitud(),
                                listaReclamos.get(i).getLongitud());
                        miMapa.addMarker(new MarkerOptions().position(latLng));
                        coordenadas1.add(latLng);
                        builder2.include(latLng);
                        polylineOptions.add(latLng);
                    }
                    miMapa.addPolyline(polylineOptions.width(5).color(Color.RED));
                    LatLngBounds latLngBounds1= builder2.build();
                    cu= CameraUpdateFactory.newLatLngBounds(latLngBounds1, 0);
                    miMapa.moveCamera(cu);
                    break;

            }
        }
    };
}
