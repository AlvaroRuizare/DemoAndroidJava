package com.example.formulario.View;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.formulario.Data.DBHelper;
import com.example.formulario.Data.Galeria_VPAdapter;
import com.example.formulario.R;

public class Galeria extends AppCompatActivity {
    ViewPager vpGaleria;
    LinearLayout llPuntos;
    int numeroPaginas;
    ImageView[] arrayPuntos;
    Galeria_VPAdapter vpAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_galeria);

        DBHelper dbhelper = new DBHelper(this);

        // Conseguimos los datos pasados por el bundle de la pantalla de Registro
        Bundle bundle = getIntent().getExtras();
        String usuarioEditar = bundle.getString("usuarioEditar", "Default");
        String usuarioLogueado = bundle.getString("usuarioLogueado", "Default");

        vpGaleria = findViewById(R.id.vpGaleria);
        llPuntos = findViewById(R.id.llPuntos);

        // Obtenemos imágenes y se las pasamos al adapter
        vpAdapter = new Galeria_VPAdapter(this, dbhelper.obtenerRutaImagenes(usuarioEditar, usuarioLogueado));

        vpGaleria.setAdapter(vpAdapter);

        // PUNTOS ViewPager
        numeroPaginas = vpAdapter.getCount();
        arrayPuntos = new ImageView[numeroPaginas];

        for (int i = 0; i < numeroPaginas; i++) { // Por cada una de las páginas...
            arrayPuntos[i] = new ImageView(this); // Se añade una imagen al array
            arrayPuntos[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.puntoinactivo)); // Se asigna un punto desactivado

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            params.setMargins(8, 0, 8, 0);

            llPuntos.addView(arrayPuntos[i], params); // Se añade el puntos al linear layout de puntos
        }

        // El primer punto se activa por defecto
        arrayPuntos[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.puntoactivo));

        // Al cambiar de página...
        vpGaleria.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for(int i = 0; i< numeroPaginas; i++){
                    arrayPuntos[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.puntoinactivo));
                }

                arrayPuntos[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.puntoactivo));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}