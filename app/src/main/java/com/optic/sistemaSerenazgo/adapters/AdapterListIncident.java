package com.optic.sistemaSerenazgo.adapters;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.optic.sistemaSerenazgo.R;
import com.optic.sistemaSerenazgo.activities.DetailIndicent;
import com.optic.sistemaSerenazgo.models.Incidents;

//CREAMOS UN ADAPTADOR PARA INFLAR LOS DATOS EN UNA LISTA DE DATOS UTLIZANDO UN RECYCLERVIEW

public class AdapterListIncident extends RecyclerView.Adapter<AdapterListIncident.viewHolderIncident> {

    List<Incidents> incidents;
    Context context;


    public AdapterListIncident(Context context, List<Incidents> incidents){
        this.context = context;
        this.incidents = incidents;
    }


    @NonNull
    @Override
    public viewHolderIncident onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_list,parent,false);
        AdapterListIncident.viewHolderIncident holder = new viewHolderIncident(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final viewHolderIncident holder, final int position) {

        final Incidents ls = incidents.get(position);

        //SETETAMOS LOS DATOS EN EL LAYOUT

        holder.textNumFicha.setText("#"+ls.getNumFicha());
        holder.textFecha.setText(ls.getFecha());
        holder.textHora.setText(ls.getHora());
        holder.textModalidad.setText(ls.getModalidad());

        switch (ls.getModalidad()) {
            case "Hurto":
                holder.imageView.setImageResource(R.drawable.hurto);
                break;
            case "Asalto a mano armada":
                holder.imageView.setImageResource(R.drawable.asalto);
                break;
            case "Acc.Tránsito":
                holder.imageView.setImageResource(R.drawable.car_accident);
                break;
            case "Inspección de licencias":
                holder.imageView.setImageResource(R.drawable.licencia);
                break;
            case "Robo":
                holder.imageView.setImageResource(R.drawable.robo);
                break;
            case "Microcomercialización de drogas":
                holder.imageView.setImageResource(R.drawable.droga);
                break;
            case "Violencia Familiar":
                holder.imageView.setImageResource(R.drawable.violencia);
                break;
            default:
                holder.imageView.setImageResource(R.drawable.alarma);
                break;
        }


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.mBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(view.getContext(), DetailIndicent.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("detail",ls);
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);
                ((Activity)context).finish();
            }
        });

    }

    @Override
    public int getItemCount() {
        return incidents.size();
    }


    public class viewHolderIncident extends RecyclerView.ViewHolder
    {

        TextView    textNumFicha , textFecha , textHora, textModalidad;
        CardView    cardView;
        ImageView   imageView;
        ProgressBar mBar;

        public viewHolderIncident(@NonNull final View itemView) {
            super(itemView);

            textNumFicha  = itemView.findViewById(R.id.listNumFicha);
            textFecha     = itemView.findViewById(R.id.listFecha);
            textHora      = itemView.findViewById(R.id.listHora);
            textModalidad = itemView.findViewById(R.id.listModalidad);
            cardView      = itemView.findViewById(R.id.card);
            imageView     = itemView.findViewById(R.id.imgIncident);
            mBar          = itemView.findViewById(R.id.bar_item);

        }
    }

}
