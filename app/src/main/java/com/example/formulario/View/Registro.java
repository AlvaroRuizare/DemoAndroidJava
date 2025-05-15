package com.example.formulario.View;

import static android.view.View.VISIBLE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.formulario.Data.DBHelper;
import com.example.formulario.Data.UsuarioModel;
import com.example.formulario.R;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Registro extends AppCompatActivity {
    Context context;
    ListadoViewModel viewModel;
    FloatingActionButton fabLocalizacion;
    Double coordenadasX;
    Double coordenadasY;
    ShapeableImageView sivFoto;
    String rutaFoto;
    TextInputEditText tietUsuario, tietContrasena, tietContrasena2, tietCorreo, tietFechaNac;
    AutoCompleteTextView actvTipoUsuario;
    TextView tvYaTengo;
    MaterialSwitch msBaja;
    MaterialButton bCrearCuenta;
    UsuarioModel usuarioEditar;
    Snackbar snackbar;
    boolean estaEditando;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 101;
    public static final int REQUEST_ID_MULTIMEDIA = 103;
    public static final int CODE_EDITAR_USUARIO = 1000; // modificar usuario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        context = getApplicationContext();
        viewModel = new ViewModelProvider(this).get(ListadoViewModel.class);
        traerElementos(); // se hacen los findViewById
        DBHelper dbhelper = new DBHelper(Registro.this); // Se inicializa SQLite

        Bundle bundleListado = getIntent().getExtras(); // Conseguimos los datos pasados por el bundle de la pantalla de Listado
        estaEditando = estaEditando(bundleListado);

        // Inicializamos listeners
        View.OnClickListener listenerCortoFotoPefil = null;
        View.OnLongClickListener listenerLargoFotoPefil = null;
        View.OnClickListener listenerBotonRegistro; // Declaramos listener

        // Si el usuario está editando...
        if(estaEditando){
            usuarioEditar = (UsuarioModel) bundleListado.getSerializable("usuarioEditar");
            viewModel.usuarioLogueado = bundleListado.getString("usuarioLogueado");
            rutaFoto = dbhelper.obtenerRutaImagenes(usuarioEditar.getNombre(), viewModel.usuarioLogueado).get(0); // Se obtiene ruta de la primera foto del usuario

            View.OnClickListener listenerBotonLocalizacion; // Inicializamos listener boton localizacion
            fabLocalizacion.setVisibility(VISIBLE);

            // Establecer valores en los campos
            if (rutaFoto != null){ // Si hay ruta de foto
                // Quitar colores imagen
                sivFoto.clearColorFilter();
                ImageViewCompat.setImageTintList(sivFoto, null);

                // Mostrar imagen
                sivFoto.setImageURI(Uri.parse(rutaFoto));
            } else {  // Si no hay ruta de foto
                sivFoto.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.login));
            }
            tietUsuario.setText(usuarioEditar.getNombre());
            tietContrasena.setText(usuarioEditar.getContrasena());
            tietContrasena2.setText(usuarioEditar.getContrasena());
            tietCorreo.setText(usuarioEditar.getCorreo());
            tietFechaNac.setText(usuarioEditar.getFechaNacimiento());
            actvTipoUsuario.setText(usuarioEditar.getTipoUsuario());
            msBaja.setChecked(usuarioEditar.isBaja());
            tvYaTengo.setVisibility(View.GONE);
            bCrearCuenta.setText(R.string.strBotonEditar);

            if(dbhelper.obtenerTipoUsuario(viewModel.usuarioLogueado) == 1){ // Si el usuario es un usuario base...
                msBaja.setVisibility(View.GONE);
            }

            // Definimos click listener localizacion
            listenerBotonLocalizacion = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Obtener coordenadas de base de datos
                    Double coordenadasX = dbhelper.obtenerCoordenadasX(dbhelper.obtenerIdUsuario(usuarioEditar.getNombre()));
                    Double coordenadasY = dbhelper.obtenerCoordenadasY(dbhelper.obtenerIdUsuario(usuarioEditar.getNombre()));

                    // Establecer localización maps
                    Uri gmmIntentUri = Uri.parse(
                            "geo:0,0?q=" + coordenadasX + "," + coordenadasY
                    );

                    // Navegar a maps
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            };
            fabLocalizacion.setOnClickListener(listenerBotonLocalizacion);

            // Definimos click listener editar
            listenerBotonRegistro = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int valorDeBaja;
                    int codigoSalida;

                    String valorRutaFoto = rutaFoto;
                    String valorUsuario = tietUsuario.getText().toString().trim();
                    String valorContrasena = tietContrasena.getText().toString().trim();
                    String valorContrasena2 = tietContrasena2.getText().toString().trim();
                    String valorCorreo = tietCorreo.getText().toString().trim();
                    String valorFechaNac = tietFechaNac.getText().toString().trim();
                    String valorTipoUsuario = actvTipoUsuario.getText().toString().trim();
                    if (msBaja.isChecked()) valorDeBaja = 1; else valorDeBaja = 0;

                    if(sonCamposValidos(
                            valorRutaFoto,
                            valorUsuario,
                            valorContrasena,
                            valorContrasena2,
                            valorCorreo,
                            valorFechaNac,
                            valorTipoUsuario,
                            snackbar
                    )){
                        codigoSalida = dbhelper.actualizarUsuario(
                                valorUsuario,
                                valorContrasena,
                                valorCorreo,
                                valorFechaNac,
                                valorTipoUsuario,
                                valorDeBaja,
                                usuarioEditar.getNombre(),
                                snackbar
                        );


                        if(codigoSalida == 0){ // Si se consigue actualizar en base de datos..
                            // DAR NUEVOS VALORES AL OBJETO USUARIOEDITAR
                            usuarioEditar.setFotoPerfil(valorRutaFoto);
                            usuarioEditar.setNombre(valorUsuario);
                            usuarioEditar.setContrasena(valorContrasena);
                            usuarioEditar.setCorreo(valorCorreo);
                            usuarioEditar.setFechaNacimiento(valorFechaNac);
                            usuarioEditar.setTipoUsuario(valorTipoUsuario);
                            usuarioEditar.setDeBaja(valorDeBaja);

                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("usuarioEditado", usuarioEditar);
                            intent.putExtras(bundle);
                            setResult(CODE_EDITAR_USUARIO, intent); // Se establece el código de resultado para enlazar
                            finish(); // Se sale de la pantalla de editar
                        } else {
                            snackbar.setText("Error al editar");
                            snackbar.show();
                        }
                    }
                }
            };

            // Definimos click listener foto
            if(usuarioEditar.getNombre().equals(viewModel.usuarioLogueado) || dbhelper.obtenerTipoUsuario(viewModel.usuarioLogueado) == 2){ // Si el usuario que está editando es él mismo o es un admin...
                listenerCortoFotoPefil = new View.OnClickListener() { // Al clicar foto...
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Registro.this, Galeria.class);
                        Bundle bundle = new Bundle(); // Se crea el bundle

                        // Le pasamos el nombre de usuario al bundle
                        bundle.putString("usuarioEditar", usuarioEditar.getNombre());
                        bundle.putString("usuarioLogueado", viewModel.usuarioLogueado);

                        intent.putExtras(bundle); // Le pasamos contenidos del bundle al intent
                        startActivity(intent); // Navegamos a 'Galería'
                    }
                };

                listenerLargoFotoPefil = new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if(comprobarYPedirPermisos(Registro.this)){ // Comprueba permisos...
                            mostrarMenuOpcionesImagen(Registro.this); // Elegir cámara o galería o añadir foto
                        }

                        return false;
                    }

                };
            }

        }
        // Si el usuario está creando cuenta...
        else {
            // Definimos click listener foto
            listenerCortoFotoPefil = new View.OnClickListener() { // Al clicar foto...
                @Override
                public void onClick(View v) {
                    if(comprobarYPedirPermisos(Registro.this)){ // Comprueba permisos...
                        mostrarMenuTipoImagen(Registro.this, "añadir"); // Elegir cámara o galería
                    }
                }
            };

            msBaja.setVisibility(View.GONE); // Se oculta el switch de dar de baja

            // Se van cargando las coordenadas actuales
            cargarCoordenadas();

            // Definimos click listener crear
            listenerBotonRegistro = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String valorRutaFoto = rutaFoto;
                    String valorUsuario = tietUsuario.getText().toString().trim();
                    String valorContrasena = tietContrasena.getText().toString().trim();
                    String valorContrasena2 = tietContrasena2.getText().toString().trim();
                    String valorCorreo = tietCorreo.getText().toString().trim();
                    String valorFechaNac = tietFechaNac.getText().toString().trim();
                    String valorTipoUsuario = actvTipoUsuario.getText().toString().trim();

                    //insertarUsuariosRapido(dbhelper, rutaFoto, snackbar);

                    if(sonCamposValidos(
                        valorRutaFoto,
                        valorUsuario,
                        valorContrasena,
                        valorContrasena2,
                        valorCorreo,
                        valorFechaNac,
                        valorTipoUsuario,
                        snackbar
                    )){
                        dbhelper.insertarUsuario(
                                coordenadasX,
                                coordenadasY,
                                rutaFoto,
                                valorUsuario,
                                valorContrasena,
                                valorCorreo,
                                valorFechaNac,
                                valorTipoUsuario,
                                snackbar
                        );
                    }
                }
            };
        }

        bCrearCuenta.setOnClickListener(listenerBotonRegistro); // Se asigna el clicklistener

        rellenarDesplegable(); // rellenar desplegable tiposUsuario
        tietFechaNac.setShowSoftInputOnFocus(false); // evitar que salte el teclado al pulsar datepicker

        // Al cambiar foco de fecha nacimiento, llama a calendario
        tietFechaNac.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) // Si está siendo enfocado...
                mostrarCalendario(v); // Muestra calendario
        });

        // Botón foto
        sivFoto.setOnClickListener(listenerCortoFotoPefil);
        sivFoto.setOnLongClickListener(listenerLargoFotoPefil);

        // Crear snackbar clase
        snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "",
                Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.md_theme_tertiary));
        snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.md_theme_onTertiary));
    }


    /**
     * Función temporal para insertar los usuarios necesarios para hacer pruebas
     */
    private void insertarUsuariosRapido(DBHelper dbhelper, String rutaFoto, Snackbar snackbar) {
        // TESTING
        for (int i = 0; i < 15; i++) { // insertar usuarios rapido
            dbhelper.insertarUsuario(
                    96.15,
                    93.15,
                    "/storage/emulated/0/Pictures/20250428_10_04_54.jpg",
                    "usuario" + i,
                    "usuario" + i,
                    "usuario" + i + "@gmail.com",
                    "Apr 28, 2025",
                    "Usuario",
                    snackbar
            );
        }

        dbhelper.insertarUsuario(
                coordenadasX,
                coordenadasY,
                "/storage/emulated/0/Download/qapyobjubpsmkvyxnuqs.png",
                "Admin",
                "admin",
                "admin" + "@gmail.com",
                "Apr 28, 2025",
                "Administrador",
                snackbar
        );

        dbhelper.insertarUsuario(
                coordenadasX,
                coordenadasY,
                "/storage/emulated/0/Download/qapyobjubpsmkvyxnuqs.png",
                "Admib",
                "admib",
                "admib" + "@gmail.com",
                "Apr 28, 2025",
                "Administrador",
                snackbar
        );

        dbhelper.insertarUsuario(
                coordenadasX,
                coordenadasY,
                "/storage/emulated/0/Download/th (1).jpg",
                "debaja",
                "debaja",
                "debaja" + "@gmail.com",
                "Apr 28, 2025",
                "Usuario",
                snackbar
        );

        dbhelper.insertarUsuario(
                coordenadasX,
                coordenadasY,
                "/storage/emulated/0/Download/th (1).jpg",
                "adminbaja",
                "adminbaja",
                "adminbaja" + "@gmail.com",
                "Apr 28, 2025",
                "Administrador",
                snackbar
        );
    }


    /**
     * Comprueba si el usuario introducido es válido
     */
    public boolean esUsuarioValido(String valorUsuario) {
        Boolean usuarioValido = false;

        if(valorUsuario.length() > 4)
            usuarioValido = true;

        return usuarioValido;
    }


    /**
     * Comprueba si las contraseñas introducidas son válidas
     */
    public boolean esContrasenaValida(String contrasena1, String contrasena2) {
        boolean contrasenaValida = false;

        if(contrasena1.length() > 4 && (contrasena1.equals(contrasena2)))
            contrasenaValida = true;

        return contrasenaValida;
    }


    /**
     * Comprueba si el correo introducido es válido
     */
    public boolean esCorreoValido(String valorCorreo) {
        boolean correoValido = false;

        if(valorCorreo.length() > 8 && valorCorreo.contains("@"))
            correoValido = true;

        return correoValido;
    }


    /**
     * Comprobar si los campos tienen datos válidos
     */
    public boolean sonCamposValidos(
            String valorRutaFoto,
            String valorUsuario,
            String valorContrasena,
            String valorContrasena2,
            String valorCorreo,
            String valorFechaNac,
            String valorTipoUsuario,
            Snackbar snackbar
    ){
        boolean todoValido = true;

        if(!esUsuarioValido(valorUsuario)){
            snackbar.setText("Usuario no válido").show();
            todoValido = false;
        } else if (!esContrasenaValida(valorContrasena, valorContrasena2)) {
            snackbar.setText("Contraseña no válida o no coinciden").show();
            todoValido = false;
        } else if (!esCorreoValido(valorCorreo)) {
            snackbar.setText("Correo no válido").show();
            todoValido = false;
        } else if (valorRutaFoto == null || valorRutaFoto.isEmpty()) {
            snackbar.setText("Imagen requerida").show();
            todoValido = false;
        } else if (valorFechaNac != null && valorFechaNac.isEmpty()) {
            snackbar.setText("Fecha de nacimiento requerida").show();
            todoValido = false;
        } else if (valorTipoUsuario != null && valorTipoUsuario.isEmpty()) {
            snackbar.setText("Tipo de usuario requerido").show();
            todoValido = false;
        }

        return todoValido;
    }


    /**
     * Trae todos los elementos del xml de registro para poder trabajar con ellos
     */
    public void traerElementos() {
        fabLocalizacion = findViewById(R.id.fabLocalizacion); // Floating Action Button localizacion
        sivFoto = findViewById(R.id.sivFoto); // Foto perfil
        tietUsuario = findViewById(R.id.tietUsuario); // TextInput usuario
        tietContrasena = findViewById(R.id.tietContrasena); // TextInput contraseña
        tietContrasena2 = findViewById(R.id.tietContrasena2); // TextInput repetir contraseña
        tietCorreo = findViewById(R.id.tietCorreo); // TextInput correo
        tietFechaNac = findViewById(R.id.tietFechaNac); // TextInput fecha nacimiento
        msBaja = findViewById(R.id.msBaja); // MaterialSwitch baja
        tvYaTengo = findViewById(R.id.tvYaTengo); // TextView 'Ya tengo cuenta'
        bCrearCuenta = findViewById(R.id.bCrearCuenta); // Boton crear cuenta
        actvTipoUsuario = findViewById(R.id.actvTipoUsuario); // Desplegable tipo usuario
    }


    /**
     * Rellena la lista de tipos de usuario
     * @return una lista con los tipos de usuario
     */
    public ArrayList<String> rellenarListaDesplegable() {
        DBHelper dbFormulario = new DBHelper(this);

        ArrayList<String> listaTiposUsuario = new ArrayList<>();

        // Se obtiene un cursor con los resultados de la consulta
        Cursor cursorTiposUsuario = dbFormulario.obtenerTiposUsuario();

        if (cursorTiposUsuario.getCount() == 0) // Si el cursor no tiene datos...
            listaTiposUsuario.add("No hay datos");
        else // Si tiene datos...
            while (cursorTiposUsuario.moveToNext()) // Por cada dato...
                // Se añade a la lista la primera columna que devuelve la consulta
                listaTiposUsuario.add(cursorTiposUsuario.getString(0));

        return listaTiposUsuario; // Devuelve la lista rellena
    }


    /**
     * Rellena el desplegable de tipos de usuario
     */
    public void rellenarDesplegable(){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, // Contexto
                R.layout.item_desplegable, // Layout dentro del desplegable
                rellenarListaDesplegable() // Elementos del desplegable
        );

        actvTipoUsuario.setAdapter(adapter);
    }


    /**
     * Abre un calendario donde elegir la fecha de nacimiento
     */
    public void mostrarCalendario(View view) {
        // LINEAS CONTROLAR RANGO DE FECHA
        // LocalDate fechaActual = LocalDate.now();
        // int anoActualMenos100 = fechaActual.getYear() - 100;
        // Calendar calendario = Calendar.getInstance();
        // calendario.set(anoActualMenos100, 1, 1);
        // long fechaDesdeMs = calendario.getTimeInMillis();
        // Fecha hasta
        // ZonedDateTime zdtFechaHasta = ZonedDateTime.of(2015,1,1,0,0,0,0, ZoneId.of("UTC+1"));
        // long fechaHastaMs = zdtFechaHasta.with(LocalTime.MIDNIGHT).toInstant().toEpochMilli();
        // Fecha actual sin problemas utc y en ms
        // ZonedDateTime zdtFechaActual = ZonedDateTime.now(ZoneOffset.UTC);
        // long zdtFechaActualMs = zdtFechaActual.with(LocalTime.MIDNIGHT).toInstant().toEpochMilli();

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                //.setStart(fechaDesdeMs)
                //.setEnd(fechaHastaMs)
                .build();

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Escoge tu fecha de nacimiento")
                .setCalendarConstraints(constraints)
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> { // Al seleccionar...
            tietFechaNac.setText(datePicker.getHeaderText()); // Rellena campo de fecha nacimiento
        });

        datePicker.show(getSupportFragmentManager(), "FECHA_HORA"); // Mostrar calendario
    }


    /**
     * Al clicar TextView 'Ya tengo cuenta', navega hacia la pantalla anterior
     */
    public void navegarHaciaAtras(View view) {
        this.finish();
    }


    /**
     * Comprueba si el usuario está editando o no
     */
    public boolean estaEditando(Bundle bundle){
        boolean estaEditando = false;

        if (bundle != null){
            estaEditando = true;
        }

        return estaEditando;
    }


    /**
     * LOCALIZACION - Guardar coordenadas actuales en las variables de clase
     */
    private void cargarCoordenadas() {
        // Solicitud de localización
        LocationRequest locationRequest = new LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, // Prioridad
            5000 // Intervalo (ms)
        )
        .setMinUpdateIntervalMillis(2000) // Intervalo mínimo
        .build(); // Lanza solicitud de localización

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Si tiene permisos...
            if (ActivityCompat.checkSelfPermission(Registro.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActivado()) { // Si tiene el gps activado...
                    LocationServices.getFusedLocationProviderClient(Registro.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                // Cuando se termina la solicitud de localización...
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    LocationServices.getFusedLocationProviderClient(Registro.this)
                                            .removeLocationUpdates(this);

                                    // Si se ha conseguido un resultado...
                                    if (locationResult != null && locationResult.getLocations().size() > 0) {
                                        int index = locationResult.getLocations().size() - 1; // Índice de localizaciones

                                        // Obtener latitud y longitud
                                        double latitud = locationResult.getLocations().get(index).getLatitude();
                                        double longitud = locationResult.getLocations().get(index).getLongitude();

                                        // Establecer coordenadas en la clase
                                        coordenadasX = latitud;
                                        coordenadasY = longitud;
                                    }
                                }
                            }, Looper.getMainLooper());
                } else { // Si tiene el gps desactivado...
                    snackbar.setText("La localización no está activada en los ajustes");
                }
            } else { // Si no tiene permisos de localización...
                // Pide permisos
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }


    /**
     * LOCALIZACION - Comprueba si el móvil tiene activada la localización
     */
    private boolean gpsActivado() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }


    /**
     * FOTOS - Comprobar permisos
     */
    public boolean comprobarYPedirPermisos(final Activity context) {
        List<String> listaPermisosNecesarios = new ArrayList<>();

        int permisoGaleria = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
        );

        int permisoCamara = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
        );

        if (permisoCamara != PackageManager.PERMISSION_GRANTED) { // Si no tiene permisos de cámara
            listaPermisosNecesarios.add(Manifest.permission.CAMERA); // Añade el permiso de cámara a la lista
        }

        if (permisoGaleria != PackageManager.PERMISSION_GRANTED) { // Si no tiene permisos de galería
            listaPermisosNecesarios.add(Manifest.permission.READ_MEDIA_IMAGES); // Añade el permiso de galería a la lista
        }

        if (!listaPermisosNecesarios.isEmpty()) { // Si hay permisos necesarios...
            ActivityCompat.requestPermissions( // Se piden los permisos
                    context,
                    listaPermisosNecesarios.toArray(new String[listaPermisosNecesarios.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS
            );
            return false;
        }
        return true;
    }


    /**
     * FOTOS - Muestra el menú para escoger qué hacer con la imagen
     */
    public void mostrarMenuOpcionesImagen(Context context){
        final String[] opciones = {"Cambiar foto de perfil", "Añadir nueva imagen", "Salir" }; // array de opciones

        // Crear AlertDialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);

        builder.setItems(opciones, new DialogInterface.OnClickListener() { // Rellenar AlertDialog
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (opciones[i]) {
                    case "Cambiar foto de perfil": // Al seleccionar echar foto...
                        // Abrir menu tipo imagen
                        if(comprobarYPedirPermisos(Registro.this)){ // Comprueba permisos...
                            mostrarMenuTipoImagen(Registro.this, "cambiar"); // Elegir cámara o galería o añadir foto
                        }
                        break;
                    case "Añadir nueva imagen": // Al seleccionar galeria...
                        // Abrir menu tipo imagen
                        if(comprobarYPedirPermisos(Registro.this)){ // Comprueba permisos...
                            mostrarMenuTipoImagen(Registro.this, "añadir"); // Elegir cámara o galería o añadir foto
                        }
                        break;
                    case "Salir":
                        dialogInterface.dismiss();
                        break;
                }
            }
        });
        builder.show();
    }


    /**
     * FOTOS - Muestra el menú para escoger el tipo de imagen
     */
    public void mostrarMenuTipoImagen(Context context, String queHacer){
        final String[] opciones = {"Echar foto", "Escoger de galería", "Salir" }; // array de opciones

        // Crear AlertDialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);

        builder.setItems(opciones, new DialogInterface.OnClickListener() { // Rellenar AlertDialog
            // Inicializamos codigos de cambiar
            int codigoCamara = 0;
            int codigoGaleria = 1;

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Si se quiere añadir una foto, se cambian los códigos
                if(queHacer.equals("añadir")){
                    codigoCamara = 2;
                    codigoGaleria = 3;
                }

                switch (opciones[i]) {
                    case "Echar foto": // Al seleccionar echar foto...
                        // Abrir cámara
                        Intent intentCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intentCamara, codigoCamara);
                        break;
                    case "Escoger de galería": // Al seleccionar galeria...
                        // Abrir galería
                        Intent intentGaleria = new Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        );
                        startActivityForResult(intentGaleria, codigoGaleria);
                        break;
                    case "Salir":
                        dialogInterface.dismiss();
                        break;
                }
            }
        });
        builder.show();
    }


    /**
     * FOTOS - Después de elegir permisos
     */
    @Override
    public void onRequestPermissionsResult(int codigoSolicitud, String[] permisos, int[] grantResults) {
        super.onRequestPermissionsResult(codigoSolicitud, permisos, grantResults);
        switch (codigoSolicitud) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:
                if (ContextCompat.checkSelfPermission(Registro.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        ||
                    ContextCompat.checkSelfPermission(Registro.this,
                        Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) { // Si no tiene permisos de galería o imagen...
                        ActivityCompat.requestPermissions(
                                this,
                                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES},
                                REQUEST_ID_MULTIMEDIA
                        );
                    snackbar.setText("Se necesitan permisos de cámara y almacenamiento para continuar").show();
                } else { // Si tiene los permisos...
                    if (estaEditando){
                        mostrarMenuOpcionesImagen(Registro.this); // Mostrar menú cámara o galería
                    } else {
                        mostrarMenuTipoImagen(Registro.this, "añadir");
                    }
                }
                break;
        }
    }


    /**
     * FOTOS - Después de elegir imagen de la galería o echar foto
     */
    @Override
    protected void onActivityResult(int codigoSolicitud, int codigoResultado, Intent datosIntent) {
        super.onActivityResult(codigoSolicitud, codigoResultado, datosIntent);

        if (codigoResultado != RESULT_CANCELED) {
            DBHelper dbHelper = new DBHelper(this);

            String nombreUsuarioEditar;

            if(estaEditando){ // Si el usuario no está editando...
                nombreUsuarioEditar = usuarioEditar.getNombre(); // El usuario sobre el que trabajar es el usuario seleccionado
            } else {
                nombreUsuarioEditar = viewModel.usuarioLogueado; // El usuario sobre el que trabajar es el usuario logueado
            }

            switch (codigoSolicitud) {
                case 0: // CAMARA - CAMBIAR FOTO
                    if (codigoResultado == RESULT_OK && datosIntent != null) {
                        Bitmap imagenSeleccionada = (Bitmap) datosIntent.getExtras().get("data");

                        // Quitar colores imagen
                        sivFoto.clearColorFilter();
                        ImageViewCompat.setImageTintList(sivFoto, null);

                        // Establecer imagen
                        sivFoto.setImageBitmap(imagenSeleccionada);

                        // Conseguir uri imagen
                        Uri uriImagen = null;
                        try {
                            uriImagen = conseguirUriImagen(getApplicationContext(), imagenSeleccionada);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        // Conseguir ruta de imagen
                        File archivoImagen = new File(conseguirRutaReal(uriImagen));

                        // Cambiar ruta foto en la BD
                        dbHelper.cambiarRutaFotoPerfil(nombreUsuarioEditar, archivoImagen.toString());
                    }
                    break;
                case 1: // GALERIA - CAMBIAR FOTO
                    if (codigoResultado == RESULT_OK && datosIntent != null) {
                        Uri imagenSeleccionada = datosIntent.getData();

                        String[] columaDireccionImagen = {MediaStore.Images.Media.DATA};

                        if (imagenSeleccionada != null) {
                            Cursor cursor = getContentResolver().query(imagenSeleccionada, columaDireccionImagen, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int indice = cursor.getColumnIndex(columaDireccionImagen[0]);
                                String rutaImagen = cursor.getString(indice);

                                // Quitar colores imagen
                                sivFoto.clearColorFilter();
                                ImageViewCompat.setImageTintList(sivFoto, null);

                                // Establecer imagen
                                sivFoto.setImageBitmap(BitmapFactory.decodeFile(rutaImagen));
                                cursor.close();

                                // Conseguir ruta de imagen
                                File archivoImagen = new File(conseguirRutaReal(imagenSeleccionada));

                                // Cambiar ruta foto en la BD
                                dbHelper.cambiarRutaFotoPerfil(nombreUsuarioEditar, archivoImagen.toString());
                            }
                        }
                    }
                    break;
                case 2: // CAMARA - AÑADIR FOTO (TAMBIÉN AL CREAR NUEVO USUARIO)
                    if (codigoResultado == RESULT_OK && datosIntent != null) {
                        Bitmap imagenSeleccionada = (Bitmap) datosIntent.getExtras().get("data");

                        // Conseguir uri imagen
                        Uri uriImagen = null;
                        try {
                            uriImagen = conseguirUriImagen(getApplicationContext(), imagenSeleccionada);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        // Conseguir ruta de imagen
                        File archivoImagen = new File(conseguirRutaReal(uriImagen));

                        if(estaEditando) { // Si el usuario está siendo editado...
                            dbHelper.guardarRutaFotoNueva(nombreUsuarioEditar, archivoImagen.toString()); // Guardar ruta foto en la BD
                        } else { // Si el usuario está siendo insertado
                            sivFoto.clearColorFilter(); // Quitar colores imagen
                            ImageViewCompat.setImageTintList(sivFoto, null);

                            sivFoto.setImageBitmap(imagenSeleccionada); // Establecer imagen
                            rutaFoto = archivoImagen.toString(); // Guardar ruta foto en la clase
                        }
                    }
                    break;
                case 3: // GALERIA - AÑADIR FOTO (TAMBIÉN AL CREAR NUEVO USUARIO)
                    if (codigoResultado == RESULT_OK && datosIntent != null) {
                        Uri imagenSeleccionada = datosIntent.getData();

                        String[] columaDireccionImagen = {MediaStore.Images.Media.DATA};

                        if (imagenSeleccionada != null) {
                            Cursor cursor = getContentResolver().query(imagenSeleccionada, columaDireccionImagen, null, null, null);
                            if (cursor != null) {
                                // Conseguir ruta de imagen
                                File archivoImagen = new File(conseguirRutaReal(imagenSeleccionada));

                                if(estaEditando) { // Si el usuario está siendo editado...
                                    dbHelper.guardarRutaFotoNueva(nombreUsuarioEditar, archivoImagen.toString()); // Guardar ruta foto en la BD
                                } else { // Si el usuario está siendo insertado
                                    cursor.moveToFirst();
                                    int indice = cursor.getColumnIndex(columaDireccionImagen[0]);
                                    String rutaImagen = cursor.getString(indice);
                                    cursor.close();

                                    // Quitar colores imagen
                                    sivFoto.clearColorFilter();
                                    ImageViewCompat.setImageTintList(sivFoto, null);

                                    // Establecer imagen
                                    sivFoto.setImageBitmap(BitmapFactory.decodeFile(rutaImagen));
                                    rutaFoto = archivoImagen.toString(); // Guardar ruta foto en la clase
                                }
                            }
                        }
                    }
                    break;
            }
        }
    }


    /**
     * FOTOS - Conseguir ruta de la uri
     */
    public Uri conseguirUriImagen(Context inContext, Bitmap imagenRecibida) throws IOException {
        // INTENTO SIN GUARDAR EN LA GALERIA
        /*Uri uriDevolver;
        File imagenTemporal = File.createTempFile("imagenTemporal", ".png");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        imagenRecibida.compress(Bitmap.CompressFormat.PNG, 100, bytes);

        byte[] bitmapData = bytes.toByteArray();

        FileOutputStream fileOutPut = new FileOutputStream(imagenTemporal);
        fileOutPut.write(bitmapData);
        fileOutPut.flush();
        fileOutPut.close();

        uriDevolver = Uri.fromFile(imagenTemporal);

        return uriDevolver;*/

        // GUARDANDO EN LA GALERIA
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        imagenRecibida.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(
                inContext.getContentResolver(),
                imagenRecibida,
                obtenerFechaActualString(),
                null);

        return Uri.parse(path);
    }


    /**
     * Obtener fecha actual en forma de String
     */
    private static String obtenerFechaActualString() {
        // Fecha actual como nombre de imagen
        LocalDateTime fechaActual = LocalDateTime.now(); // Conseguir fecha actual
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyyMMdd_HH_mm_ss"); // Definir el formato
        String fechaActualFormateada = fechaActual.format(formateador); // Formatear la fecha actual
        return fechaActualFormateada;
    }


    /**
     * FOTOS - Conseguir path de la galeria a partir de la uri
     */
    public String conseguirRutaReal(Uri uri) {
        String rutaFoto = "";
        if (getContentResolver() != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                rutaFoto = cursor.getString(idx);
                cursor.close();
            }
        }

        return rutaFoto;
    }
}
