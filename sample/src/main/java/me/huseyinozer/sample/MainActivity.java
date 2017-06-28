package me.huseyinozer.sample;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.Arrays;

import me.huseyinozer.TooltipIndicator;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private ViewPagerAdapter adapter;

    private TooltipIndicator indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        indicator = (TooltipIndicator) findViewById(R.id.tooltip_indicator);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        indicator.setupViewPager(viewPager);

        indicator.setToolTipDrawables(Arrays.asList(
                ContextCompat.getDrawable(MainActivity.this, R.drawable.img0),
                ContextCompat.getDrawable(MainActivity.this, R.drawable.img1),
                ContextCompat.getDrawable(MainActivity.this, R.drawable.img2),
                ContextCompat.getDrawable(MainActivity.this, R.drawable.img3),
                ContextCompat.getDrawable(MainActivity.this, R.drawable.img4)
        ));

    }
}
