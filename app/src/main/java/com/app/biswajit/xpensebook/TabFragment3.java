package com.app.biswajit.xpensebook;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabFragment3#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabFragment3 extends Fragment implements Updatable{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private PieChart pieChart;
    private CardView cardView;
    private View rootView;
    private LinearLayout linearLayout;

    public TabFragment3() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TabFragment3.
     */
    // TODO: Rename and change types and number of parameters
    public static TabFragment3 newInstance(String param1, String param2) {
        TabFragment3 fragment = new TabFragment3();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.tab_fragment3, container, false);
        cardView = (CardView) rootView.findViewById(R.id.cardViewGraph);
        pieChart = (PieChart) rootView.findViewById(R.id.piechart);
        linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
        return rootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void update() {
        Log.i("Fragment3","Inside Fragment3");

        int rColor = (int) getResources().getColor(R.color.R);
        int python = (int) getResources().getColor(R.color.Python);
        int cpp = (int) getResources().getColor(R.color.CPP);
        int java = (int) getResources().getColor(R.color.Java);
        int colorOne = (int) getResources().getColor(R.color.color_one);

        linearLayout.removeAllViews();

        final LinearLayout layout1 = new LinearLayout(this.getContext());
        layout1.setLayoutParams(new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                30));
        layout1.setGravity(Gravity.CENTER_VERTICAL);
        final View view1 = new View(this.getContext());
        view1.setLayoutParams(new
                LinearLayout.LayoutParams(20, LinearLayout.LayoutParams.MATCH_PARENT));
        view1.setBackgroundColor(rColor);

        TextView tv1 = new TextView(this.getContext());
        tv1.setLayoutParams(new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        tv1.setPadding(15,0,0,0);
        tv1.setText("Test4");
        tv1.setTextSize(10);

        layout1.addView(view1);
        layout1.addView(tv1);

        //##################
        final LinearLayout layout2 = new LinearLayout(this.getContext());
        LinearLayout.LayoutParams params = new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                30);

        params.setMargins(0,10,0,0);
        layout2.setLayoutParams(params);
        layout2.setGravity(Gravity.CENTER_VERTICAL);
        final View view2 = new View(this.getContext());
        view2.setLayoutParams(new
                LinearLayout.LayoutParams(20, LinearLayout.LayoutParams.MATCH_PARENT));
        view2.setBackgroundColor(python);

        TextView tv2 = new TextView(this.getContext());
        tv2.setLayoutParams(new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        tv2.setPadding(15,0,0,0);
        tv2.setText("Test3");
        tv2.setTextSize(10);

        layout2.addView(view2);
        layout2.addView(tv2);

        //##################
        final LinearLayout layout3 = new LinearLayout(this.getContext());
        params.setMargins(0,10,0,0);
        layout3.setLayoutParams(params);
        layout3.setGravity(Gravity.CENTER_VERTICAL);
        final View view3 = new View(this.getContext());
        view3.setLayoutParams(new
                LinearLayout.LayoutParams(20, LinearLayout.LayoutParams.MATCH_PARENT));
        view3.setBackgroundColor(cpp);
        TextView tv3 = new TextView(this.getContext());
        tv3.setLayoutParams(new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        tv3.setPadding(15,0,0,0);
        tv3.setText("Test2");
        tv3.setTextSize(10);

        layout3.addView(view3);
        layout3.addView(tv3);

        //##################
        final LinearLayout layout4 = new LinearLayout(this.getContext());
        params.setMargins(0,5,0,0);
        layout4.setLayoutParams(params);
        layout4.setGravity(Gravity.CENTER_VERTICAL);

        final View view4 = new View(this.getContext());
        view4.setLayoutParams(new
                LinearLayout.LayoutParams(20, LinearLayout.LayoutParams.MATCH_PARENT));
        view4.setBackgroundColor(java);

        TextView tv4 = new TextView(this.getContext());
        tv4.setLayoutParams(new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        tv4.setPadding(15,0,0,0);
        tv4.setText("Test1");
        tv4.setTextSize(10);

        layout4.addView(view4);
        layout4.addView(tv4);

        //##################

        linearLayout.addView(layout1);
        linearLayout.addView(layout2);
        linearLayout.addView(layout3);
        linearLayout.addView(layout4);

        pieChart.clearChart();
        // Set the data and color to the pie chart
        pieChart.addPieSlice(
                new PieModel(
                        "Test4",
                        Integer.parseInt("10"),
                        Color.parseColor("#FFA726")));
        pieChart.addPieSlice(
                new PieModel(
                        "Test3",
                        Integer.parseInt("20"),
                        Color.parseColor("#66BB6A")));
        pieChart.addPieSlice(
                new PieModel(
                        "Test2",
                        Integer.parseInt("40"),
                        Color.parseColor("#EF5350")));
        pieChart.addPieSlice(
                new PieModel(
                        "Test1",
                        Integer.parseInt("30"),
                        Color.parseColor("#29B6F6")));

        // To animate the pie chart
        pieChart.startAnimation();

    }
}
