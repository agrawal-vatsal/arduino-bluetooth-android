package drdo.project;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

public class GraphPlotActivity extends AppCompatActivity {

    BarChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_plot);
        chart = findViewById(R.id.chart);
        Intent intent = getIntent();
        String data = intent.getStringExtra("data");
        boolean start = false;
        ArrayList<Integer> yValues = new ArrayList<>();
        ArrayList<Character> xValues = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            if (start) {
                if (data.charAt(i) == '$')
                    break;
                if (Character.isLetter(data.charAt(i)))
                    xValues.add(data.charAt(i));
                else if (Character.isDigit(data.charAt(i)))
                    yValues.add(Integer.parseInt(String.format("%c", data.charAt(i))));
            } else if (data.charAt(i) == '#')
                start = true;
        }
        List<BarEntry> entries = new ArrayList<>();
        if (yValues.size() != xValues.size())
            Toast.makeText(getApplicationContext(), "Invalid data sent", Toast.LENGTH_SHORT).show();
        for (int i = 0; i < yValues.size(); i++)
            entries.add(new BarEntry(xValues.get(i), yValues.get(i)));
        BarDataSet dataSet = new BarDataSet(entries, "Label");
        BarData barData = new BarData(dataSet);
        chart.setData(barData);
        chart.invalidate();
    }
}
