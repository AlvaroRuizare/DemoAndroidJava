package com.example.formulario.View;

import static android.app.PendingIntent.getActivity;

import android.Manifest;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.formulario.Data.DBHelper;
import com.example.formulario.Data.UsuarioModel;
import com.example.formulario.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class ListadoViewModel extends AndroidViewModel {
    private Context context;
    private MutableLiveData<String> mldBusqueda = new MutableLiveData<>();
    private MutableLiveData<ArrayList<UsuarioModel>> mldListaTodos = new MutableLiveData<>(); // lista privada
    private ArrayList<UsuarioModel> listaTodos = new ArrayList<>(); // lista pública
    private ArrayList<UsuarioModel> listaApartado = new ArrayList<>(); // lista pública
    private ArrayList<UsuarioModel> listaFiltrada = new ArrayList<>(); // lista pública
    private ArrayList<UsuarioModel> listaUsuarios = new ArrayList<>(); // lista pública
    private ArrayList<UsuarioModel> listaAdmins = new ArrayList<>(); // lista pública
    private ArrayList<UsuarioModel> listaBajas = new ArrayList<>(); // lista pública
    DBHelper dbhelper;
    String usuarioLogueado;
    public UsuarioModel usuarioSeleccionado;
    ActivityResultLauncher<Intent> arLauncher;
    private int apartado = 0;
    double coordenadasX;
    double coordenadasY;


    public static int NOTIFICATION_ID = 1; // identificador único para la notificación
    public static final String CHANNEL_ID = "id_notificacion_borrado"; // id de canal de notificaciones
    public static final String DESCRIPTION = "Test notification"; // descripción del canal de notificaciones

    public ListadoViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        dbhelper = new DBHelper(context);
    }


    // GET-SET
    public LiveData<ArrayList<UsuarioModel>> getMldListaTodos(){
        if (mldListaTodos == null){ // Si la lista está vacía...
            // Se inicializan ambas listas
            mldListaTodos = new MutableLiveData<ArrayList<UsuarioModel>>();
            listaTodos = new ArrayList<>();
        }
        return mldListaTodos;
    }


    public ArrayList<UsuarioModel> getListaApartado() {
        if (listaApartado == null){ // Si la lista está vacía...
            listaApartado = new ArrayList<>();
        }

        if (apartado == 1){
            setListaApartado(getListaUsuarios());
        } else if (apartado == 2) {
            setListaApartado(getListaAdmins());
        } else if (apartado == 3) {
            setListaApartado(getListaBajas());
        }

        return listaApartado;
    }

    public ArrayList<UsuarioModel> getListaUsuarios() {
        return listaUsuarios;
    }

    public ArrayList<UsuarioModel> getListaAdmins() {
        return listaAdmins;
    }

    public ArrayList<UsuarioModel> getListaBajas() {
        return listaBajas;
    }

    public ArrayList<UsuarioModel> getListaFiltrada() {
        if (listaFiltrada.isEmpty()){
            listaFiltrada = listaApartado;
        }

        return listaFiltrada;
    }

    public int getApartado() {
        return apartado;
    }

    public MutableLiveData<String> getMldBusqueda() {
        return mldBusqueda;
    }


    public void setApartado(int apartado) {
        this.apartado = apartado;
    }

    public void setListaApartado(ArrayList<UsuarioModel> listaApartado) {
        this.listaApartado = listaApartado;
    }

    public void setListaFiltrada(ArrayList<UsuarioModel> listaFiltrada) {
        this.listaFiltrada = listaFiltrada;
    }

    public void setMldListaTodos(ArrayList<UsuarioModel> listaTodos) {
        if (mldListaTodos == null){ // Si la lista está vacía...
            // Se inicializan ambas listas
            mldListaTodos = new MutableLiveData<ArrayList<UsuarioModel>>();
            this.listaTodos = new ArrayList<>();
        }

        this.listaTodos = listaTodos;
        this.mldListaTodos.setValue(this.listaTodos);
    }

    public void setMldBusqueda(String sBusqueda) {
        this.mldBusqueda.setValue(sBusqueda); // SetValue notifica a observer
    }


    /**
     * Vacía la lista de todos los usuarios del ViewModel
     */
    public void clearMldListaTodos(){
        if(listaTodos != null){
            listaTodos.clear();
        }

        if(mldListaTodos != null){
            mldListaTodos.setValue(new ArrayList<>(listaTodos));
        }
    }


    /**
     * Añade usuario a la lista de todos los usuarios del ViewModel
     */
    public void clearListasApartado(){
        listaUsuarios.clear();
        listaAdmins.clear();
        listaBajas.clear();
    }


    /**
     * Utilizar la lista de todos los usuarios para volver a generar la lista filtrada
     */
    public void rellenarListasApartado(){
        clearListasApartado();

        for(UsuarioModel usuario : listaTodos){
            if (usuario.getTipoUsuario().equals("Usuario") && !usuario.isBaja()){
                listaUsuarios.add(usuario);
            } else if (usuario.getTipoUsuario().equals("Administrador") && !usuario.isBaja()) {
                listaAdmins.add(usuario);
            } else if (usuario.isBaja()) {
                listaBajas.add(usuario);
            }
        }
    }


    /**
     * Seleccionar o deseleccionar los usuarios
     */
    public void toggleSeleccionar() {
        boolean todosSeleccionados = true;

        // Se comprueba si todos están seleccionados...
        for (UsuarioModel usuario : listaApartado) {
            if(!usuario.isSeleccionado()){
                todosSeleccionados = false;
                break;
            }
        }
        
        if (!todosSeleccionados){ // Si no están todos seleccionados...
            seleccionarTodos();
        } else { // Si están todos seleccionados...
            deSeleccionarTodos();
        }
    }
    private void seleccionarTodos() {
        for (UsuarioModel usuario : listaApartado) {
                usuario.setSeleccionado(true);
        }
    }
    public void deSeleccionarTodos() {
        for (UsuarioModel usuario : listaApartado) {
            usuario.setSeleccionado(false);
        }
    }


    /**
     * Borrar usuarios seleccionados
     */
    public void borrarSeleccion(Snackbar snackbar) {
        boolean haySeleccionados = false;

        // Por cada usuario de la lista...
        for (UsuarioModel usuario : listaFiltrada) {
            if (usuario.isSeleccionado()){ // Si el usuario está seleccionado...
                haySeleccionados = true;
                opcionBorrarUsuario(dbhelper, context, usuarioLogueado, usuario, true); // Borrar de BD
            }
        }

        if (!haySeleccionados){ // Si no hay usuarios seleccionados...
            snackbar.setText("No hay ningún usuario seleccionado");
            snackbar.show();
        } else { // Si hay usuarios seleccionados...
            // Borrar de listas
            listaFiltrada.removeIf(UsuarioModel::isSeleccionado);
            listaApartado.removeIf(UsuarioModel::isSeleccionado);
        }

    }


    /**
     * Funcion que se ejecuta al borrar un usuario
     */
    public void opcionBorrarUsuario(DBHelper dbhelper, Context context, String usuarioLogueado, UsuarioModel usuarioBorrar, boolean borradoDesdeSeleccion) {
        // Borrar
        dbhelper.borrarUsuario(usuarioBorrar.getNombre());

        // Si no se ha borrado mediante selección, se tiene que actualizar la lista
        if (!borradoDesdeSeleccion){
            // Actualizar lista
            actualizarListasUsuarios(context, usuarioLogueado); // SE LLAMA AL OBSERVER
        }

        // Enviar notificación
        crearCanalNotificacion();
        construirEnviarNotificacion(usuarioBorrar);
    }


    /**
     * Funcion que se ejecuta al pulsar el botón de editar
     */
    public void opcionEditarUsuario() {
        Intent intentRegistroEditar = new Intent(context, Registro.class);
        Bundle bundle = new Bundle(); // Se crea el bundle

        // Le pasamos el usuario al bundle
        bundle.putSerializable("usuarioEditar", usuarioSeleccionado);
        bundle.putString("usuarioLogueado", usuarioLogueado);

        intentRegistroEditar.putExtras(bundle); // Le pasamos contenidos del bundle al intent

        arLauncher.launch(intentRegistroEditar); // Se lanza el launcher, que espera a que se pulse el botón de editar(guardar) usuario
    }


    /**
     * Rellena la lista de usuarios
     */
    public void actualizarListasUsuarios(Context context, String usuarioLogueado) {
        ArrayList<UsuarioModel> listaTodos = new ArrayList<>();

        // SE MODIFICA LISTA DE TODOS LOS USUARIOS -> LLAMA AL OBSERVER
        clearMldListaTodos();
        clearListasApartado();

        DBHelper dbhelper = new DBHelper(context);
        Cursor cursorUsuarios = dbhelper.obtenerUsuarios(usuarioLogueado); // Obtener cursor de usuarios

        cursorUsuarios.moveToFirst();
        do{ // Por cada usuario de la base de datos...
            int id = cursorUsuarios.getInt(0);
            String usuario = cursorUsuarios.getString(1);
            String contrasena = cursorUsuarios.getString(2);
            String correo = cursorUsuarios.getString(3);
            String fechaNacimiento = cursorUsuarios.getString(4);
            String tipoUsuario = cursorUsuarios.getString(5);
            String rutaFoto = cursorUsuarios.getString(6);
            int deBaja = cursorUsuarios.getInt(7);

            // Rellena datos de usuario
            UsuarioModel modeloUsuario = new UsuarioModel(id, rutaFoto, usuario, contrasena, correo, fechaNacimiento, tipoUsuario, rutaFoto, deBaja, false);

            listaTodos.add(modeloUsuario);
        } while (cursorUsuarios.moveToNext());

        setMldListaTodos(listaTodos);
        rellenarListasApartado(); // Se actualiza lista de los usuarios del apartado
    }


    /**
     * NOTIFICACION - Crea el canal de la notificación
     */
    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canalNotificacion = new NotificationChannel(
                    CHANNEL_ID,
                    DESCRIPTION,
                    NotificationManager.IMPORTANCE_HIGH
            );

            // Luz notificación
            canalNotificacion.enableLights(true);
            canalNotificacion.setLightColor(Color.MAGENTA);

            canalNotificacion.enableVibration(true); // Habilitar vibración

            NotificationManager gestorNotificaciones =
                    (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
            gestorNotificaciones.createNotificationChannel(canalNotificacion);
        }
    }


    /**
     * NOTIFICACION - Construir y enviar una notificación
     */
    private void construirEnviarNotificacion(UsuarioModel usuarioBorrado) {
        // Intent clicar notificación
        Intent intent = new Intent(context, Listado.class);
        Bundle bundle = new Bundle(); // Se crea el bundle
        bundle.putString("usuarioLogueado", usuarioLogueado); // Le pasamos el usuario logueado al bundle
        intent.putExtras(bundle); // Le pasamos contenidos del bundle al intent
        intent.setAction(Intent.ACTION_VIEW);

        // Creamos Pending Intent que permite que se almacene en la notificación el usuario que estaba logueado,
        // que hemos pasado dentro del intent normal
        PendingIntent pendingIntent = getActivity(
                context,
                6,
                intent, // pasamos intent con el usuario logueado
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Construir la notificacion
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.login) // Icono
                .setContentTitle("Aviso de borrado") // Titulo
                .setContentText("El usuario " + usuarioLogueado + " ha borrado al usuario " + usuarioBorrado.getNombre()) // Texto
                .setContentIntent(pendingIntent) // Al hacer click
                .setAutoCancel(true); // Que desaparezca al hacer click

        // Si no se han concedido los permisos de notificación...
        ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS);

        // Mostrar notificación
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID++, builder.build());
    }

}
