package com.example.formulario.View.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.formulario.Data.Listado_RVAdapter;
import com.example.formulario.Data.UsuarioModel;
import com.example.formulario.R;
import com.example.formulario.View.Listado;
import com.example.formulario.View.ListadoViewModel;

import java.util.ArrayList;

public class BajasFragment extends Fragment {
    // PROPIEDADES DE CLASE TRAÍDAS DE CONSTRUCTOR
    Context context;
    ArrayList<UsuarioModel> listaApartado;
    ArrayList<UsuarioModel> listaFiltrada;
    Listado_RVAdapter.ClickListener clickListener;
    int tipoUsuarioLogueado;
    String usuarioLogueado;

    // RESTO DE PROPIEDADES DE CLASE
    Listado listado;
    Listado_RVAdapter rvAdapter;
    FragmentManager fManager;
    RecyclerView rvBajas;
    View clApartado;
    ListadoViewModel viewModel;

    public BajasFragment(Context context, ArrayList<UsuarioModel> listaApartado, Listado_RVAdapter.ClickListener clickListener, int tipoUsuarioLogueado, String usuarioLogueado) {
        this.context = context;
        this.listaApartado = listaApartado;
        this.clickListener = clickListener;
        this.tipoUsuarioLogueado = tipoUsuarioLogueado;
        this.usuarioLogueado = usuarioLogueado;
    }


    public Listado_RVAdapter getRvAdapter() {
        return rvAdapter;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    // Inflar el layout e inicializar cosas básicas de la vista
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        listado = (Listado) getActivity(); // Traer instacia de listado sin reestablecer datos
        viewModel = listado.getViewModel(); // Obtener viewModel definido en Listado
        clApartado = inflater.inflate(R.layout.fragment_bajas, container, false);
        rvBajas = clApartado.findViewById(R.id.rvBajas);
        fManager = getActivity().getSupportFragmentManager();
        rvBajas.setLayoutManager(new GridLayoutManager(getContext(), 1));

        viewModel.setApartado(3); // Se indica al ViewModel que estamos en el apartado 3
        viewModel.setListaApartado(listaApartado);
        viewModel.setListaFiltrada(listaApartado);

        // Crear nuevo objeto adapter
        rvAdapter = new Listado_RVAdapter(
                context,
                listaApartado,
                viewModel.getListaFiltrada(),
                clickListener,
                tipoUsuarioLogueado,
                usuarioLogueado
        );

        // Establecer adapter en el recylerView
        rvBajas.setAdapter(rvAdapter);

        // Observar cambios de la lista de todos los usuarios
        // Al cambiar los datos de la lista se llama a lo que está dentro de los corchetes
        viewModel.getMldListaTodos().observe(getViewLifecycleOwner(), listaTodosUsuarios -> { // ONCHANGED...
            // Inflar recyclerView de nuevo con los datos actualizados del viewModel
            rvAdapter.setListaApartado(viewModel.getListaBajas());
            rvAdapter.notifyDataSetChanged(); // Se actualiza recyclerView
        });

        // Observar cambios del mldBusqueda del viewModel
        // Al cambiar el texto del filtro se llama a lo que está dentro de los corchetes
        viewModel.getMldBusqueda().observe(getViewLifecycleOwner(), sBusqueda -> { // ONCHANGED...
            rvAdapter.filtrar(sBusqueda);
            rvAdapter.notifyDataSetChanged();
            viewModel.setListaFiltrada(rvAdapter.getListaFiltrada()); // Se actualiza lista filtrada en viewModel
        });

        return clApartado;
    }


    // Después de inflar el layout...
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getActivity().getApplicationContext();
    }


    // Al volver a la pantalla...
    @Override
    public void onResume() {
        super.onResume();

        viewModel.setApartado(3);
        viewModel.setListaApartado(listaApartado);
        viewModel.setListaFiltrada(listaApartado);
    }
}