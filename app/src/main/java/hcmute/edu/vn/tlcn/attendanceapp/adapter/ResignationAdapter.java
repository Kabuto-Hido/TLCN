package hcmute.edu.vn.tlcn.attendanceapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.edu.vn.tlcn.attendanceapp.R;
import hcmute.edu.vn.tlcn.attendanceapp.model.DayOffRequest;
import hcmute.edu.vn.tlcn.attendanceapp.model.Record;
import hcmute.edu.vn.tlcn.attendanceapp.model.Statistic;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;

public class ResignationAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private List<DayOffRequest> lstRequest;

    public ResignationAdapter(Context context, int layout, List<DayOffRequest> lstRequest) {
        this.context = context;
        this.layout = layout;
        this.lstRequest = lstRequest;
    }

    @Override
    public int getCount() {
        return lstRequest.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder{
        TextView txtSenderName,txtDayResignation, txtReason;
        Button btnAccept, btnDeny;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout,null);

            holder.txtSenderName = (TextView) convertView.findViewById(R.id.txtSenderName);
            holder.txtDayResignation = (TextView) convertView.findViewById(R.id.txtDayResignation);
            holder.txtReason = (TextView) convertView.findViewById(R.id.txtReason);
            holder.btnAccept = (Button) convertView.findViewById(R.id.btnAccept);
            holder.btnDeny = (Button) convertView.findViewById(R.id.btnDeny);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        DayOffRequest dayOffRequest = lstRequest.get(position);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        myRef.child(dayOffRequest.getUserPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                holder.txtSenderName.setText(user.getFullName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.txtDayResignation.setText(dayOffRequest.getDateOff());
        holder.txtReason.setText(dayOffRequest.getReason());

        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference dayOffReportRef = database.getReference("dayoffreport");

                dayOffReportRef.orderByChild("status").startAt("waiting").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            DayOffRequest dayOff = dataSnapshot.getValue(DayOffRequest.class);
                            String phone = dayOff.getUserPhone();
                            String day = dayOff.getDateOff();

                            if(phone.equals(dayOffRequest.getUserPhone())
                                    && day.equals(dayOffRequest.getDateOff())){

                                String reqId = dataSnapshot.getKey();
                                dayOffReportRef.child(reqId).child("status").setValue("Accept");
                                Toast.makeText(context, "Accept for "+holder.txtSenderName.getText().toString()+" successfully!!", Toast.LENGTH_SHORT).show();
                                lstRequest.remove(position);
                                notifyDataSetChanged();

                                DatabaseReference recordRef = database.getReference("record");
                                recordRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //check if user not check in
                                        DataSnapshot dataSnapshot = snapshot.child(phone).child(day).child("checkIn");
                                        Record checkInRecord = dataSnapshot.getValue(Record.class);
                                        if (checkInRecord == null) {
                                            Record absentRecord = new Record(phone, day, "", "absent with permission", "absent");
                                            recordRef.child(phone).child(day).child("absent").setValue(absentRecord);

                                            String currentMonth = day.substring(5,7);
                                            String currentYear = day.substring(0,4);

                                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                                            DatabaseReference statisticRef = database.getReference("statistic");
                                            statisticRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    DataSnapshot dataSnapshot = snapshot.child(currentYear).child(currentMonth);
                                                    Statistic monthStatistic = dataSnapshot.getValue(Statistic.class);

                                                    DataSnapshot dataSnapshot2 = snapshot.child(phone).child(currentYear).child(currentMonth);
                                                    Statistic empStatistic = dataSnapshot2.getValue(Statistic.class);


                                                    int countAbsentWithPer;
                                                    if(monthStatistic == null){
                                                        Statistic newStatistic = new Statistic(0,0,1,0,currentMonth,currentYear,"");
                                                        statisticRef.child(currentYear).child(currentMonth).setValue(newStatistic);


                                                    }
                                                    else{
                                                        countAbsentWithPer = monthStatistic.getAbsentWithPer();
                                                        countAbsentWithPer++;
                                                        monthStatistic.setAbsentWithPer(countAbsentWithPer);

                                                        statisticRef.child(currentYear).child(currentMonth).child("absentWithPer").setValue(countAbsentWithPer);

                                                    }

                                                    if(empStatistic == null){
                                                        Statistic newStatistic = new Statistic(0,0,1,0,currentMonth,currentYear,phone);
                                                        statisticRef.child(phone).child(currentYear).child(currentMonth).setValue(newStatistic);
                                                    }
                                                    else{
                                                        countAbsentWithPer = empStatistic.getAbsentWithPer();
                                                        countAbsentWithPer++;
                                                        empStatistic.setAbsentWithPer(countAbsentWithPer);

                                                        statisticRef.child(phone).child(currentYear).child(currentMonth).child("absentWithPer").setValue(countAbsentWithPer);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                break;
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        holder.btnDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference dayOffReportRef = database.getReference("dayoffreport");

                dayOffReportRef.orderByChild("status").startAt("waiting").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            DayOffRequest dayOff = dataSnapshot.getValue(DayOffRequest.class);
                            String phone = dayOff.getUserPhone();
                            String day = dayOff.getDateOff();

                            if(phone.equals(dayOffRequest.getUserPhone())
                                    && day.equals(dayOffRequest.getDateOff())){
                                String reqId = dataSnapshot.getKey();
                                dayOffReportRef.child(reqId).child("status").setValue("Deny");
                                lstRequest.remove(position);
                                notifyDataSetChanged();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        return convertView;
    }
}
