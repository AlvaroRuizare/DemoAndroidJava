package com.example.formulario.View;

import static com.example.formulario.View.Registro.REQUEST_ID_MULTIPLE_PERMISSIONS;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.formulario.Data.DBHelper;
import com.example.formulario.R;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Login extends AppCompatActivity {
    ListadoViewModel viewModel;
    TextInputEditText tietUsuario, tietContrasena;
    DBHelper dbhelper;
    Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        viewModel = new ViewModelProvider(this).get(ListadoViewModel.class);

        dbhelper = new DBHelper(this); // Se inicializa BD
        traerElementos(); // se hacen los findViewById

        // Se inicializa snackbar
        snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "",
                Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.md_theme_tertiary));
        snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.md_theme_onTertiary));

        comprobarYPedirPermisos(Login.this);
    }


    /**
     * Comprobar permisos aplicación
     */
    public boolean comprobarYPedirPermisos(final Activity context) {

        int permisoNotificaciones = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        );

        int permisoLocalizacion = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        );

        List<String> listaPermisosNecesarios = new ArrayList<>();

        if (permisoNotificaciones != PackageManager.PERMISSION_GRANTED) { // Si no tiene permisos de notificacion
            listaPermisosNecesarios.add(Manifest.permission.POST_NOTIFICATIONS); // Añade el permiso de notificaciones a la lista
        }

        if (permisoLocalizacion != PackageManager.PERMISSION_GRANTED) { // Si no tiene permisos de localización
            listaPermisosNecesarios.add(Manifest.permission.ACCESS_FINE_LOCATION); // Añade el permiso de localización a la lista
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
     * Trae todos los elementos del xml de login para poder trabajar con ellos
     */
    public void traerElementos() {
        tietUsuario = findViewById(R.id.tietUsuario); // TextInput usuario
        tietContrasena = findViewById(R.id.tietContrasena); // TextInput contraseña
    }


    /**
     * Al pulsar TextView 'Crear cuenta', navega a Registro
     */
    public void tvCrearCuenta(View view) {
        startActivity(new Intent(Login.this, Registro.class));
    }


    /**
     * Al pulsar botón 'Iniciar sesión', comprueba las credenciales
     */
    public void btnIniciarSesion(View view) {
        String valorCampoUsuario = String.valueOf(tietUsuario.getText());
        String valorCampoContrasena = String.valueOf(tietContrasena.getText());

        if (valorCampoUsuario.isBlank() || valorCampoContrasena.isBlank()){
            snackbar.setText("Los campos no pueden estar vacíos").show();
        }
        else if (dbhelper.estaDebaja(String.valueOf(tietUsuario.getText()))){
            snackbar.setText("El usuario está dado de baja").show();
        }
        else if(dbhelper.credencialesCorrectas(
                valorCampoUsuario,
                valorCampoContrasena)
        ) {
            Intent intent = new Intent(this, Listado.class);
            Bundle bundle = new Bundle(); // Se crea el bundle

            // Le pasamos el usuario al bundle
            bundle.putString("usuarioLogueado", String.valueOf(tietUsuario.getText()));

            intent.putExtras(bundle); // Le pasamos contenidos del bundle al intent
            startActivity(intent); // Pasamos a la siguiente página
        }
        else {
            snackbar.setText("Credenciales incorrectas").show();
        }
    }
}