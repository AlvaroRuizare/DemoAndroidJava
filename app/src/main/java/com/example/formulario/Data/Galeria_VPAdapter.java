package com.example.formulario.Data;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.formulario.R;

import java.util.ArrayList;
import java.util.List;

public class Galeria_VPAdapter extends PagerAdapter {
    Context context;
    ArrayList<String> rutasFoto;

    public Galeria_VPAdapter(Context context, ArrayList<String> rutasFoto) {
        this.context = context;
        this.rutasFoto = rutasFoto;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return rutasFoto.size();
    }

    // Aquí se infla el viewpager(activity_galeria) usando el imageview(item_imagengaleria)
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater =  (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewGaleria = inflater.inflate(R.layout.item_imagengaleria, container, false);
        ImageView ivGaleria = viewGaleria.findViewById(R.id.ivGaleria);
        ivGaleria.setImageURI(Uri.parse(rutasFoto.get(position)));
        container.addView(viewGaleria);
        return viewGaleria;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

    }
}
