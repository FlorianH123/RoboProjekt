import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mobileapplicationdev.roboproject.R;
import com.mobileapplicationdev.roboproject.models.RobotProfile;

import java.util.ArrayList;

public class RobotListAdapter extends ArrayAdapter {
    private Context context;
    private ArrayList<RobotProfile> profiles;
    private int textUnit;
    private float textSize;

    public RobotListAdapter(Context context, int textViewResourceId, ArrayList objects) {
        super(context, textViewResourceId, objects);

        textUnit = TypedValue.COMPLEX_UNIT_SP;
        textSize = 12;

        this.context = context;
        profiles = objects;
    }

    private class ViewHolder {
        TextView txtName;
        TextView txtIp;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.layout_robotlist, null);

            holder = new ViewHolder();
            holder.txtName = convertView.findViewById(R.id.robotNameTextView);
            holder.txtIp = convertView.findViewById(R.id.robotIpTextView);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        RobotProfile profile = profiles.get(position);
        holder.txtName.setText(profile.getName());

        holder.txtIp.setText(profile.getIp());
        return convertView;

    }
}
