package br.com.pentagrupo.pentalog.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import br.com.pentagrupo.pentalog.R;
import br.com.pentagrupo.pentalog.entities.OperacaoDescarga;

public class ListOperacaoDescargaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<OperacaoDescarga> listOperacaoDescarga;

    private OnItemClickListener onItemClick;

    public ListOperacaoDescargaAdapter(ArrayList<OperacaoDescarga> pListOperacaoDescarga) {
        listOperacaoDescarga = pListOperacaoDescarga;
    }

    public void clear() {
        listOperacaoDescarga.clear();
        notifyDataSetChanged();
    }

    public void add(OperacaoDescarga pOperacaoDescarga) {
        listOperacaoDescarga.add(pOperacaoDescarga);
        notifyDataSetChanged();
    }

    public void setOnItemClickListenner(OnItemClickListener pOnItemClick) {
        onItemClick = pOnItemClick;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;
//        if(viewType == VIEW_TYPE_ITEM) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_operacao_descarga_item, parent, false);
            // set the view's size, margins, paddings and layout parameters
            vh = new ViewHolder(v);
//        }

//        else{
//            // create a new view
//            View v = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.list_progress_item, parent, false);
//            // set the view's size, margins, paddings and layout parameters
//            vh = new ProgressViewHolder(v);
//        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if(holder instanceof ViewHolder){
            ViewHolder mViewHolder = (ViewHolder) holder;
            mViewHolder.setView(listOperacaoDescarga.get(position));
        }
//        else{
//            ProgressViewHolder mProgressHolder = (ProgressViewHolder) holder;
//            mProgressHolder.progressBar.setIndeterminate(true);
//        }
    }

    @Override
    public int getItemCount() {
        return listOperacaoDescarga.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nome;
        TextView cliente;
        TextView placaVeiculo;
        TextView fornecedor;
        TextView tipoProduto;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            nome = (TextView) itemView.findViewById(R.id.name);
            cliente = (TextView) itemView.findViewById(R.id.cliente);
            placaVeiculo = (TextView) itemView.findViewById(R.id.placaVeiculo);
            fornecedor = (TextView) itemView.findViewById(R.id.fornecedor);
            tipoProduto = (TextView) itemView.findViewById(R.id.tipoProduto);
        }

        @Override
        public void onClick(View v) {
            try {
                onItemClick.onItemClick(getAdapterPosition(), v,listOperacaoDescarga.get(getAdapterPosition()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setView(OperacaoDescarga pOperacaoDescarga) {
            nome.setText(pOperacaoDescarga.name);
            setText(cliente, "Cliente: " + pOperacaoDescarga.cliente);
            setText(placaVeiculo, "Placa do Veículo: " + pOperacaoDescarga.placaVeiculo);
            setText(fornecedor, "Fornecedor: " + pOperacaoDescarga.fornecedor);
            setText(tipoProduto, "Tipo de Produto: " + pOperacaoDescarga.tipoProduto);
        }

        private void setText(TextView pView, String pText) {
            if (!TextUtils.isEmpty(pText)) {
                pView.setText(pText);
                pView.setVisibility(View.VISIBLE);
            } else {
                pView.setVisibility(View.GONE);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View v, Object objClicked);
    }
}
