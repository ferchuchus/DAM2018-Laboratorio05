package ar.edu.utn.frsf.isi.dam.laboratorio05.Fragment;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ar.edu.utn.frsf.isi.dam.laboratorio05.R;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

import static android.app.Activity.RESULT_OK;

public class NuevoReclamoFragment extends Fragment {

    public interface OnNuevoLugarListener {
       void obtenerCoordenadas();
    }

    public void setListener(OnNuevoLugarListener listener) {
        this.listener = listener;
    }

    private Reclamo reclamoActual;
    private ReclamoDao reclamoDao;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    private EditText reclamoDesc;
    private EditText mail;
    private Spinner tipoReclamo;
    private TextView tvCoord;
    private Button buscarCoord;
    private Button btnGuardar;
    private OnNuevoLugarListener listener;
    private Button btnCargarImagen;
    private ImageView iViewFoto;
    private String directorioFoto;
    private String directorioAudio;
    private ImageButton iBtnGrabar;
    private ImageButton iBtnReproducir;
    private Boolean grabando = false;
    private Boolean reproduciendo = false;

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_SAVE = 2;

    private ArrayAdapter<Reclamo.TipoReclamo> tipoReclamoAdapter;

    public NuevoReclamoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        reclamoDao = MyDatabase.getInstance(this.getActivity()).getReclamoDao();

        View v = inflater.inflate(R.layout.fragment_nuevo_reclamo, container, false);

        reclamoDesc = (EditText) v.findViewById(R.id.reclamo_desc);
        mail = (EditText) v.findViewById(R.id.reclamo_mail);
        tipoReclamo = (Spinner) v.findViewById(R.id.reclamo_tipo);
        tvCoord = (TextView) v.findViewById(R.id.reclamo_coord);
        buscarCoord = (Button) v.findViewById(R.id.btnBuscarCoordenadas);
        btnGuardar = (Button) v.findViewById(R.id.btnGuardar);
        btnCargarImagen = (Button) v.findViewById(R.id.btnImagen);
        iViewFoto = (ImageView) v.findViewById(R.id.iViewFoto);
        iBtnGrabar = (ImageButton) v.findViewById(R.id.iBttnGrabar);
        iBtnReproducir = (ImageButton) v.findViewById(R.id.iBttnPlay);

        iBtnGrabar.setOnClickListener(listenerPlayer);
        iBtnReproducir.setOnClickListener(listenerPlayer);

        tipoReclamoAdapter = new ArrayAdapter<Reclamo.TipoReclamo>(getActivity(), android.R.layout.simple_spinner_item, Reclamo.TipoReclamo.values());
        tipoReclamoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoReclamo.setAdapter(tipoReclamoAdapter);

        int idReclamo = 0;
        if (getArguments() != null) {
            idReclamo = getArguments().getInt("idReclamo", 0);
        }


        cargarReclamo(idReclamo);
        iniciar();

        buscarCoord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.obtenerCoordenadas();
            }
        });

        btnCargarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 1);

                    } else {
                        sacarFoto();
                        validacion(tipoReclamo.getSelectedItem().toString());
                    }
                } else {
                    sacarFoto();
                    validacion(tipoReclamo.getSelectedItem().toString());
                }
            }
        });

        //Validación Reclamo

        tipoReclamo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                validacion(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        reclamoDesc.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                validacion(tipoReclamo.getSelectedItem().toString());
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveOrUpdateReclamo();
            }
        });
        return v;
    }
     private void vaciar(){
         mail.setText(R.string.texto_vacio);
         tvCoord.setText("0;0");
         reclamoDesc.setText(R.string.texto_vacio);
         getActivity().getFragmentManager().popBackStack();
         iViewFoto.setImageResource(android.R.drawable.ic_menu_camera);
         tipoReclamo.setSelection(0);
        iniciar();
     }
    private void iniciar(){
        boolean edicionActivada = !tvCoord.getText().toString().equals("0;0");
        reclamoDesc.setEnabled(edicionActivada);
        mail.setEnabled(edicionActivada);
        tipoReclamo.setEnabled(edicionActivada);
        btnGuardar.setEnabled(false);
        btnCargarImagen.setEnabled(edicionActivada);
        iBtnReproducir.setEnabled(false);
        iBtnGrabar.setEnabled(edicionActivada);
    }

    private void validacion(String nombreTipo){
        if ((nombreTipo.equals("VEREDAS")||nombreTipo.equals("CALLE_EN_MAL_ESTADO")) && directorioFoto != null)
            btnGuardar.setEnabled(true);
        else if ((!nombreTipo.equals("VEREDAS")&&!nombreTipo.equals("CALLE_EN_MAL_ESTADO"))&&(directorioAudio != null || reclamoDesc.getText().length() >= 8))
            btnGuardar.setEnabled(true);
        else
            btnGuardar.setEnabled(false);
    }

    private void cargarReclamo(final int id) {
        if (id > 0) {
            Runnable hiloCargaDatos = new Runnable() {
                @Override
                public void run() {
                    reclamoActual = reclamoDao.getById(id);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            buscarCoord.setEnabled(false);
                            mail.setText(reclamoActual.getEmail());
                            tvCoord.setText(reclamoActual.getLatitud() + ";" + reclamoActual.getLongitud());
                            reclamoDesc.setText(reclamoActual.getReclamo());
                            Reclamo.TipoReclamo[] tipos = Reclamo.TipoReclamo.values();
                            for (int i = 0; i < tipos.length; i++) {
                                if (tipos[i].equals(reclamoActual.getTipo())) {
                                    tipoReclamo.setSelection(i);
                                    break;
                                }
                            }
                            directorioFoto = reclamoActual.getPathFoto();
                            if (directorioFoto != null)
                                cargarImagen();
                            directorioAudio = reclamoActual.getPathAudio();
                            if (directorioAudio != null)
                                iBtnReproducir.setEnabled(true);
                        }
                    });
                }
            };
            Thread t1 = new Thread(hiloCargaDatos);
            t1.start();
        } else {
            String coordenadas = "0;0";
            if (getArguments() != null) coordenadas = getArguments().getString("latLng", "0;0");
            tvCoord.setText(coordenadas);
            reclamoActual = new Reclamo();
        }

    }

    private void saveOrUpdateReclamo() {
        reclamoActual.setEmail(mail.getText().toString());
        reclamoActual.setReclamo(reclamoDesc.getText().toString());
        reclamoActual.setTipo(tipoReclamoAdapter.getItem(tipoReclamo.getSelectedItemPosition()));
        if (tvCoord.getText().toString().length() > 0 && tvCoord.getText().toString().contains(";")) {
            String[] coordenadas = tvCoord.getText().toString().split(";");
            reclamoActual.setLatitud(Double.valueOf(coordenadas[0]));
            reclamoActual.setLongitud(Double.valueOf(coordenadas[1]));
        }
        if (directorioFoto != null) {
            reclamoActual.setPathFoto(directorioFoto);
        }
        if (directorioAudio != null) {
            reclamoActual.setPathAudio(directorioAudio);
        }
        Runnable hiloActualizacion = new Runnable() {
            @Override
            public void run() {
                if (reclamoActual.getId() > 0) {
                    reclamoDao.update(reclamoActual); //OJO QUE NO ACTUALIZA Y  ESTO VINO HECHO --> No le encuentro el motivo de igual mantera. Da code 1555
                } else reclamoDao.insert(reclamoActual);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                          vaciar();

                    }
                });
            }
        };
        Thread t1 = new Thread(hiloActualizacion);
        t1.start();
    }

    // Imagen
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,    /* prefix */
                ".jpg",    /* suffix */
                dir    /* directory */
        );
        directorioFoto = image.getAbsolutePath();
        return image;
    }

    private void sacarFoto() {
        Intent iFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (iFoto.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity().getApplicationContext(), "ar.edu.utn.frsf.isi.dam.laboratorio05.fileprovider", photoFile);
                iFoto.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(iFoto, REQUEST_IMAGE_SAVE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            iViewFoto.setImageBitmap(imageBitmap);
        }
        if (requestCode == REQUEST_IMAGE_SAVE && resultCode == RESULT_OK) {
            cargarImagen();
        }
    }

    private void cargarImagen() {
        File file = new File(directorioFoto);
        Bitmap imageBitmap = null;
        try {
            imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.fromFile(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (imageBitmap != null) {
            iViewFoto.setImageBitmap(imageBitmap);
        }
    }

    //Audio
    View.OnClickListener listenerPlayer = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iBttnPlay:
                    opcionRepoduccion();
                    break;
                case R.id.iBttnGrabar:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.RECORD_AUDIO)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 2);
                        } else opcionGrabacion();
                    } else opcionGrabacion();
                    break;
            }
        }
    };

    private void opcionRepoduccion() {
        if (reproduciendo) {
            reproduciendo = false;
            terminarReproducir();
            iBtnReproducir.setEnabled(true);
            iBtnGrabar.setEnabled(true);
            validacion(tipoReclamo.getSelectedItem().toString());
        } else {
            reproduciendo = true;
            reproducir();
            iBtnReproducir.setEnabled(false);
            iBtnGrabar.setEnabled(false);
        }
    }

    private void opcionGrabacion() {
        if (grabando) {
            grabando = false;
            terminarGrabar();
            iBtnGrabar.setImageResource(android.R.drawable.presence_audio_online);
            iBtnReproducir.setEnabled(true);
            validacion(tipoReclamo.getSelectedItem().toString());
        } else {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String nombreArchivoAudio = "/MP3_" + timeStamp + ".3gp";
            directorioAudio= getActivity().getExternalFilesDir(Environment.getExternalStorageDirectory().getAbsolutePath())+nombreArchivoAudio;
            reclamoActual.setPathAudio(directorioAudio);
            grabando = true;
            grabar();
            iBtnGrabar.setImageResource(android.R.drawable.presence_audio_busy);
            iBtnReproducir.setEnabled(false);
        }
    }

    private void grabar() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(directorioAudio);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() no funciono");
        }
    }

    private void terminarGrabar() {

        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;

        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    private void reproducir() {
        if (reclamoActual.getPathAudio() != null) {
            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(reclamoActual.getPathAudio());
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() no funciono");
            }
        }else
            iBtnReproducir.setEnabled(false);
    }

    private void terminarReproducir() {
        mPlayer.release();
        mPlayer = null;
    }

}

