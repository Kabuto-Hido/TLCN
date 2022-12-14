package hcmute.edu.vn.tlcn.attendanceapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import hcmute.edu.vn.tlcn.attendanceapp.adapter.CalendarAdapter;
import hcmute.edu.vn.tlcn.attendanceapp.model.Record;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AttendanceCalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AttendanceCalendarFragment extends Fragment implements CalendarAdapter.OnItemListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AttendanceCalendarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AttendanceCalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AttendanceCalendarFragment newInstance(String param1, String param2) {
        AttendanceCalendarFragment fragment = new AttendanceCalendarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    View view;
    TextView monthYearTV;
    RecyclerView calendarRecyclerView;
    Button btnPrevMonth, btnNextMonth;
    LocalDate selectedDate;
    User_singeton user_singeton = User_singeton.getInstance();
    User user;
    CalendarAdapter calendarAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_attendance_calendar, container, false);

        mapping();
        user = user_singeton.getUser();
        selectedDate = LocalDate.now();
        setMonthView();

        btnPrevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDate = selectedDate.minusMonths(1);
                setMonthView();
            }
        });

        btnNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDate = selectedDate.plusMonths(1);
                setMonthView();
            }
        });

        return view;
    }

    private void setMonthView() {
        monthYearTV.setText(monthYearFromDate(selectedDate));
        LinkedHashMap<String, String> dayInMonth = daysInMonthMap(selectedDate);

        calendarAdapter = new CalendarAdapter(dayInMonth,getContext(),this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity().getApplicationContext(),7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private LinkedHashMap<String,String> daysInMonthMap(LocalDate date){
        LinkedHashMap<String,String> daysInMonthMap = new LinkedHashMap<>();
        YearMonth yearMonth = YearMonth.from(date);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String yM = date.format(formatter);

        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        int maxDayInMonth = daysInMonth + dayOfWeek;

        for(int i = 1; i<=42; i++){
            if(i <= dayOfWeek){
                daysInMonthMap.put(String.valueOf(i-dayOfWeek),"");
            }
            else if(i <= maxDayInMonth){
                String dateReport;
                if(String.valueOf(i - dayOfWeek).length()==1){
                    dateReport = yM + "-0" + (i - dayOfWeek);
                }
                else {
                    dateReport = yM + "-" + (i - dayOfWeek);
                }

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference recordRef = database.getReference("record");
                int finalI = i;
                recordRef.child(user.getPhone()).addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                //check-in
                                DataSnapshot dataSnapshot = snapshot.child(dateReport).child("checkIn");
                                Record record = dataSnapshot.getValue(Record.class);

                                String getDate;
                                String day;
                                if (record != null) {
                                    getDate = record.getDay();
                                    if(getDate.charAt(8) == '0'){
                                        day = getDate.substring(9,10);
                                    }else {
                                        day = getDate.substring(8, 10);
                                    }
                                    boolean equals = day.equals(String.valueOf(finalI - dayOfWeek));
                                    if(equals){
                                        daysInMonthMap.put(String.valueOf(finalI -dayOfWeek),record.getStatus());
                                    }
                                    else{
                                        daysInMonthMap.put(String.valueOf(finalI -dayOfWeek),"not yet");
                                    }
                                }
                                else{
                                    DataSnapshot dataSnapshot3 = snapshot.child(dateReport).child("absent");
                                    Record absentRecord = dataSnapshot3.getValue(Record.class);
                                    if (absentRecord != null) {
                                        getDate = absentRecord.getDay();
                                        if(getDate.charAt(8) == '0'){
                                            day = getDate.substring(9,10);
                                        }else {
                                            day = getDate.substring(8, 10);
                                        }
                                        boolean equals = day.equals(String.valueOf(finalI - dayOfWeek));
                                        if(equals){
                                            daysInMonthMap.put(String.valueOf(finalI -dayOfWeek),absentRecord.getStatus());
                                        }
                                        else{
                                            daysInMonthMap.put(String.valueOf(finalI -dayOfWeek),"not yet");
                                        }
                                    }
                                    else{
                                        daysInMonthMap.put(String.valueOf(finalI -dayOfWeek),"not yet");
                                    }

                                }

                                calendarAdapter.notifyDataSetChanged();

                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        }
        return daysInMonthMap;
    }

    private void mapping() {
        monthYearTV = view.findViewById(R.id.monthYearTV);
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);

        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
    }

    private String monthYearFromDate(LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }


    @Override
    public void onItemClick(int pos, String dayText) {
        if(dayText.equals("")){
            Toast.makeText(getActivity(), dayText+" "+ monthYearFromDate(selectedDate), Toast.LENGTH_SHORT).show();
        }
    }
}