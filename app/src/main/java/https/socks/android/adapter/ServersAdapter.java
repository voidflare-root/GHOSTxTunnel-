package https.socks.android.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.nphdevs.bluespace.R;

import java.io.InputStream;
import java.util.List;

import https.socks.android.model.ServerModel;

public class ServersAdapter extends RecyclerView.Adapter<ServersAdapter.ViewHolder> {
    private List<ServerModel> servers;
    private ServerClick listener;

    public ServersAdapter(List<ServerModel> servers) {
        this.servers = servers;
    }

    public void setOnClick(ServerClick listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.server_view, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(servers.get(position));
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView serverName, serverInfo;
        private ImageView serverImage;
        private Context context;

        public ViewHolder(View itemView, ServerClick listener) {
            super(itemView);
            context = itemView.getContext();
            itemView.findViewById(R.id.serverCard).setOnClickListener(v -> listener.click(getAdapterPosition()));
            serverName = itemView.findViewById(R.id.sName);
            serverImage = itemView.findViewById(R.id.sIcon);
            serverInfo = itemView.findViewById(R.id.sInfo);
        }

        public void bind(ServerModel server) {
            serverName.setText(server.getServerName());
            serverInfo.setText(server.getServerInfo());
            String f = server.getServerFlag() + ".png";
            try {
                InputStream inputStream = context.getAssets().open("flags/" + f);
                serverImage.setImageDrawable(Drawable.createFromStream(inputStream, f));
                inputStream.close();
            } catch (Exception ignore) {

            }
        }
    }

    public interface ServerClick {
        void click(int p);
    }
}
