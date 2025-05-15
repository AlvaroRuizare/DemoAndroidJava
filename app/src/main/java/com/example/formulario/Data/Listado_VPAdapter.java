package com.example.formulario.Data;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;


public class Listado_VPAdapter extends FragmentStateAdapter{

    private ArrayList<Fragment> listaFragments = new ArrayList<>();

    public Listado_VPAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }


    @Override
    public int getItemCount() {
        return listaFragments.size();
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return listaFragments.get(position);
    }


    /**
     * Añadir un fragment a la lista de fragments
     */
    public void anadirFragment(Fragment fragment) {
        listaFragments.add(fragment);
    }
}
