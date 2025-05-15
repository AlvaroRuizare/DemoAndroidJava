package com.example.formulario.View;

import static android.app.PendingIntent.getActivity;
import static android.view.View.GONE;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.formulario.Data.DBHelper;
import com.example.formulario.Services.ForegroundService;
import com.example.formulario.Data.Listado_RVAdapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.example.formulario.Data.Listado_VPAdapter;
import com.example.formulario.Data.UsuarioModel;
import com.example.formulario.R;
import com.example.formulario.View.Fragments.AdminsFragment;
import com.example.formulario.View.Fragments.BajasFragment;
import com.example.formulario.View.Fragments.UsuariosFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class Listado extends AppCompatActivity {
    Context context = Listado.this;
    ListadoViewModel viewModel;
    TextInputEditText tietBusqueda;
    ShapeableImageView sivMenu;
    TabLayout tlListado;
    String[] TITULOS_APARTADO = new String[] {"Usuarios", "Administradores", "De baja"};
    ViewPager2 vp2Apartados;
    Listado_VPAdapter vpAdapter;
    Snackbar snackbar;
    UsuariosFragment usuariosFragment;
    AdminsFragment adminsFragment;
    BajasFragment bajasFragment;

    boolean isServiceRunning;
    public static final int CODE_EDITAR_USUARIO = 1000; // modificar usuario


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listado);
        crearSnackBar();
        viewModel = new ViewModelProvider(this).get(ListadoViewModel.class);

        // Definimos el launcher, que espera a que se pulse el botón de editar(guardar) usuario para ejecutarse
        viewModel.arLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == CODE_EDITAR_USUARIO) {
                            // There are no request codes
                            Intent data = result.getData();

                            Bundle bundle = data.getExtras();
                            UsuarioModel usuarioEditado = (UsuarioModel) bundle.getSerializable("usuarioEditado");

                            if (viewModel.getApartado() == 1){ // Apartado usuarios
                                actualizarUsuarioLista(viewModel.getListaUsuarios(), usuarioEditado); // ACTUALIZAR USUARIO LISTA
                                usuariosFragment.getRvAdapter().notifyDataSetChanged(); // ACTUALIZAR LISTADO
                            } else if (viewModel.getApartado() == 2) { // Apartado admins
                                actualizarUsuarioLista(viewModel.getListaAdmins(), usuarioEditado);
                                adminsFragment.getRvAdapter().notifyDataSetChanged();
                            } else if (viewModel.getApartado() == 3) { // Apartado bajas
                                actualizarUsuarioLista(viewModel.getListaBajas(), usuarioEditado);
                                bajasFragment.getRvAdapter().notifyDataSetChanged();
                            }
                        }
                    }
                });

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startForegroundService(new Intent(Listado.this, ForegroundService.class));
            isServiceRunning = true;
        } else {
            snackbar.setText("Se han denegado los permisos de localización");
            snackbar.show();
        }

        // Conseguimos los datos pasados por el bundle de la pantalla de Login
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            viewModel.usuarioLogueado = bundle.getString("usuarioLogueado", "Default");
        }

        DBHelper dbhelper = new DBHelper(this);

        // Para que cuente el espacio de los elementos ui del móvil
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        traerElementos(); // se hacen los findViewById

        // Al escribir en el filtro...
        tietBusqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int aft ) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                String busqueda = s.toString().trim();
                viewModel.setMldBusqueda(busqueda); // Se notifica a observer
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Al pulsar menú checkbox...
        sivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarMenuCheckbox(v, R.menu.checkbox_menu);
            }
        });

        Listado_RVAdapter.ClickListener clickListener = new Listado_RVAdapter.ClickListener() { // Rellenamos funcionalidad de click
            @Override // Si el usuario logueado es un usuario...
            public void onItemClick(int position) {
                dialogoBorrarEditar(position, dbhelper);
            }

            @Override // Si el usuario logueado es un administrador...
            public void onItemLongClick(int position) {
                dialogoBorrarEditar(position, dbhelper);
            }
        };

        vpAdapter = new Listado_VPAdapter(getSupportFragmentManager(), getLifecycle());

        viewModel.actualizarListasUsuarios(context, viewModel.usuarioLogueado);
        viewModel.rellenarListasApartado();

        // Le pasamos los datos del adapter al fragment
        // Dentro de cada uno, creamos un RecycleViewAdapter diferente
        usuariosFragment = new UsuariosFragment(this, viewModel.getListaUsuarios(), clickListener, dbhelper.obtenerTipoUsuario(viewModel.usuarioLogueado), viewModel.usuarioLogueado);
        vpAdapter.anadirFragment(usuariosFragment);

        // Si es administrador, muestra el resto de usuarios
        if(dbhelper.obtenerTipoUsuario(viewModel.usuarioLogueado) == 2) {
            adminsFragment = new AdminsFragment(this, viewModel.getListaAdmins(), clickListener, dbhelper.obtenerTipoUsuario(viewModel.usuarioLogueado), viewModel.usuarioLogueado);
            bajasFragment = new BajasFragment(this, viewModel.getListaBajas(), clickListener, dbhelper.obtenerTipoUsuario(viewModel.usuarioLogueado), viewModel.usuarioLogueado);

            vpAdapter.anadirFragment(adminsFragment);
            vpAdapter.anadirFragment(bajasFragment);
        } else {
            sivMenu.setVisibility(GONE);
        }

        vp2Apartados.setAdapter(vpAdapter);

        // PONER TITULO TABS
        new TabLayoutMediator(tlListado, vp2Apartados, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(TITULOS_APARTADO[position]);
            }
        }).attach();

    }


    /**
     * Obtener ViewModel de la clase Listado
     */
    public ListadoViewModel getViewModel() {
        return viewModel;
    }


    /**
     * Trae todos los elementos del xml de Listado para poder trabajar con ellos
     */
    public void traerElementos() {
        sivMenu = findViewById(R.id.sivMenu);
        vp2Apartados = findViewById(R.id.vp2Apartados);
        tlListado = findViewById(R.id.tlListado);
        tietBusqueda = findViewById(R.id.tietBusqueda);
    }


    /**
     * Asignar snackbar a la propiedad snackbar de la clase
     */
    private void crearSnackBar() {
        snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "",
                Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.md_theme_tertiary));
        snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.md_theme_onTertiary));
    }


    /**
     * Muestra el menu de opciones CheckBox
     */
    private void mostrarMenuCheckbox(View v, @MenuRes int menuRes) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(menuRes, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem opcionPulsada) {
                int id = opcionPulsada.getItemId(); // Se obtiene la opción pulsada

                if (id == R.id.iSeleccionarTodos) { // Si se ha pulsado 'Seleccionar todos'...
                    viewModel.toggleSeleccionar();
                    switch (viewModel.getApartado()){
                        case 1:
                            usuariosFragment.getRvAdapter().notifyDataSetChanged();
                            break;
                        case 2:
                            adminsFragment.getRvAdapter().notifyDataSetChanged();
                            break;
                        case 3:
                            bajasFragment.getRvAdapter().notifyDataSetChanged();
                            break;
                    }
                    return true;
                } else if (id == R.id.iBorrarSeleccion) { // Si se ha pulsado 'Borrar selección'...
                    switch (viewModel.getApartado()){
                        case 1:
                            viewModel.setListaFiltrada(usuariosFragment.getRvAdapter().getListaFiltrada());
                            viewModel.borrarSeleccion(snackbar);
                            usuariosFragment.getRvAdapter().notifyDataSetChanged();
                            break;
                        case 2:
                            viewModel.setListaFiltrada(adminsFragment.getRvAdapter().getListaFiltrada());
                            viewModel.borrarSeleccion(snackbar);
                            adminsFragment.getRvAdapter().notifyDataSetChanged();
                            break;
                        case 3:
                            viewModel.setListaFiltrada(bajasFragment.getRvAdapter().getListaFiltrada());
                            viewModel.borrarSeleccion(snackbar);
                            bajasFragment.getRvAdapter().notifyDataSetChanged();
                            break;
                    }
                    return true;
                }
                return false; // Si ningún id coincide, no se manejó el botón, se devuelve false
            }
        });

        popup.show();
    }


    /**
     * Muestra el diálogo de borrar o editar usuario
     */
    public void dialogoBorrarEditar(int position, DBHelper dbhelper) {
        // Se obtiene el usuario seleccionado
        viewModel.usuarioSeleccionado = viewModel.getListaApartado().get(position);

        // Se inicia el AlertDialog
        MaterialAlertDialogBuilder madb = new MaterialAlertDialogBuilder(Listado.this)
            .setTitle("Opciones de usuario")
            .setMessage("¿Qué deseas hacer con el usuario?")

            // Boton cancelar
            .setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            })

            // Boton editar
            .setPositiveButton("Editar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    viewModel.opcionEditarUsuario();
                }
            });

        // Si el usuario es administrador...
        if(dbhelper.obtenerTipoUsuario(viewModel.usuarioLogueado) == 2){
            // Boton borrar
            madb.setNegativeButton("Borrar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    viewModel.opcionBorrarUsuario(dbhelper, context, viewModel.usuarioLogueado, viewModel.usuarioSeleccionado, false);
                }
            });
        }

        madb.show();
    }


    /**
     * Se actualiza el usuario en la lista
     */
    private void actualizarUsuarioLista(ArrayList<UsuarioModel> listaUsuarios, UsuarioModel usuarioEditado) {
        for (int i = 0; i < listaUsuarios.size(); i++) { // Por cada usuario...
            UsuarioModel usuario = listaUsuarios.get(i);
            if(usuario.getId() == usuarioEditado.getId()){ // Cuando coincida el usuario de la lista antigua con el editado...
                listaUsuarios.set(i, usuarioEditado); // El usuario antiguo se sustituye por el editado
                break; // Se sale
            }
        }
    }


    /**
     * Al salir del listado
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener el servicio solo si la app se está cerrando
        if (isServiceRunning) {
            Intent serviceIntent = new Intent(this, ForegroundService.class);
            stopService(serviceIntent);
            isServiceRunning = false;
        }
    }
}