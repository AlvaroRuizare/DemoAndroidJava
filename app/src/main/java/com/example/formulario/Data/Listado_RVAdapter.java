package com.example.formulario.Data;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.formulario.R;
import com.example.formulario.View.ListadoViewModel;
import com.google.android.material.checkbox.MaterialCheckBox;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.widget.ImageViewCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;

public class Listado_RVAdapter extends RecyclerView.Adapter<Listado_RVAdapter.MyViewHolder> implements Serializable {
    private ClickListener clickListener;
    Context context;
    ArrayList<UsuarioModel> listaApartado;
    ArrayList<UsuarioModel> listaFiltrada;
    int tipoUsuarioLogueado;
    String usuarioLogueado;
    ArrayList<UsuarioModel> listaTodos;


    // CONSTRUCTOR
    public Listado_RVAdapter(Context context, ArrayList<UsuarioModel> listaApartado, ArrayList<UsuarioModel> listaFiltrada, ClickListener clickListener, int tipoUsuario, String usuarioLogueado){
        this.context = context;
        this.listaApartado = listaApartado;
        this.listaFiltrada = listaFiltrada;
        this.clickListener = clickListener;
        this.tipoUsuarioLogueado = tipoUsuario;
        this.usuarioLogueado = usuarioLogueado;
    }


    @Override // Contar los elementos que hay en total
    public int getItemCount() {
        return listaFiltrada.size();
    }


    public ArrayList<UsuarioModel> getListaFiltrada() {
        return listaFiltrada;
    }


    public void setListaApartado(ArrayList<UsuarioModel> listaApartado) {
        this.listaApartado = listaApartado;
    }



    // Parecido al onCreate, obtiene los elementos del archivo item_recyclerview.xml,
    static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView mcvListado;
        ImageView sivUsuario;
        TextView tvNombreU, tvCorreoU, tvFechaNacU, tvTipoU;
        MaterialCheckBox mcbCheckbox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            sivUsuario = itemView.findViewById(R.id.sivUsuario);
            tvNombreU = itemView.findViewById(R.id.tvNombreU);
            tvCorreoU = itemView.findViewById(R.id.tvCorreoU);
            tvTipoU = itemView.findViewById(R.id.tvTipoU);
            tvFechaNacU = itemView.findViewById(R.id.tvFechaNacU);
            mcvListado = itemView.findViewById(R.id.mcvListado);
            mcbCheckbox = itemView.findViewById(R.id.mcbCheckbox);
        }
    }


    // Interfaz que inicializa las funciones, que se rellenan luego al crear el adapter en Listado.java
    public interface ClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }


    @NonNull
    @Override // Inflar el layout y dar aspecto a las filas
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_recyclerview, parent, false);

        return new MyViewHolder(view);
    }


    @Override // Asignar valores a los elementos que se muestran en la pantalla
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        UsuarioModel usuario = listaFiltrada.get(position);

        if (tipoUsuarioLogueado == 2){ // Si es administrador, puede checkear
            holder.mcbCheckbox.setVisibility(VISIBLE);
        }

        holder.sivUsuario.setImageURI(Uri.parse(listaFiltrada.get(position).getFotoPerfil()));
        holder.tvNombreU.setText(listaFiltrada.get(position).getNombre());
        holder.tvCorreoU.setText("Correo: " + listaFiltrada.get(position).getCorreo());
        holder.tvFechaNacU.setText("Fecha nac.: " + listaFiltrada.get(position).getFechaNacimiento());
        holder.tvTipoU.setText("Tipo: " + listaFiltrada.get(position).getTipoUsuario());

        // Cuando el checkbox cambia, actualizamos el estado del objeto Item
        holder.mcbCheckbox.setOnCheckedChangeListener(null); // Importante: evitar eventos duplicados
        holder.mcbCheckbox.setChecked(usuario.isSeleccionado());
        holder.mcbCheckbox.setOnCheckedChangeListener((buttonView, estaChequeado) -> {
            usuario.setSeleccionado(estaChequeado);
        });

        if (listaFiltrada.get(position).isBaja()){ // Si esta de baja...
            holder.mcvListado.setCardBackgroundColor(context.getColor(R.color.md_theme_error));
        } else if(listaFiltrada.get(position).getTipoUsuario().equals("Administrador")){ // Si es admin...
            holder.mcvListado.setCardBackgroundColor(context.getColor(R.color.md_theme_tertiaryContainer));
        } else { // Si no es admin...
            holder.mcvListado.setCardBackgroundColor(context.getColor(R.color.md_theme_primaryContainer));
        }

        // Quitar colores imagen
        holder.sivUsuario.clearColorFilter();
        ImageViewCompat.setImageTintList(holder.sivUsuario, null);

        if (tipoUsuarioLogueado == 2) { // Si el usuario es administrador...
            holder.mcvListado.setOnLongClickListener(v -> {
                clickListener.onItemLongClick(position); // Se habilita long click
                return true;
            });
        } else if (tipoUsuarioLogueado == 1) { // Si el usuario es usuario normal...
            if(usuarioLogueado.equals(listaFiltrada.get(position).getNombre())){ // Y el usuario es él mismo...
                holder.mcvListado.setOnClickListener(v -> {
                    clickListener.onItemClick(position); // Se habilita click normal solo en su mismo usuario
                });
            }
        }
    }


    /**
     * Filtra la lista del apartado cada vez que se pulsa una tecla
     */
    public void filtrar(String sBusqueda) { // Desde el onChanged del observer...
        listaFiltrada = new ArrayList<>();
        for (UsuarioModel usuario : listaApartado) { // Por cada usuario...
            // Si coincide filtro...
            if (usuario.getNombre().toLowerCase().contains(sBusqueda.toLowerCase()) ||
                    usuario.getCorreo().toLowerCase().contains(sBusqueda.toLowerCase()) ||
                    usuario.getFechaNacimiento().toLowerCase().contains(sBusqueda.toLowerCase())){
                listaFiltrada.add(usuario);
            }
        }
    }
}
