package me.huseyinozer;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;

import me.huseyinozer.tooltipindicator.R;

/**
 * Created by Huseyin Ozer.
 */
public class TooltipIndicator extends RelativeLayout implements ViewPager.OnPageChangeListener, View.OnTouchListener {

    private static final String TAG = "ToolTipIndicator";

    private int lineWidth;
    private int lineWidthSelected;
    private int lineHeight;
    private int lineMargin;

    private int lineWidthWithMargin;
    private int lineWidthSelectedWithMargin;

    private int tooltipWidth;
    private int tooltipHeight;

    private int selectedLineDrawableResource;
    private int unselectedLineDrawableResource;

    private ViewPager viewPager;
    private PagerAdapter adapter;

    private LinearLayout linesLayout;
    private RelativeLayout tooltipView;
    private AppCompatImageView tooltipViewImage;
    private int selectedPosition = -1;

    private ValueAnimator expandAnimator;
    private ValueAnimator collapseAnimator;

    private Drawable[] drawableList;

    public TooltipIndicator(Context context) {
        this(context, null);
    }

    public TooltipIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TooltipIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.setClipChildren(false);
        this.setClipToPadding(false);
        this.setClickable(true);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TooltipIndicator);

        try {

            lineWidth = (int) typedArray.getDimension(R.styleable.TooltipIndicator_ti_lineWidth, dpToPx(16));
            lineHeight = (int) typedArray.getDimension(R.styleable.TooltipIndicator_ti_lineHeight, dpToPx(6));

            lineWidthSelected = (int) typedArray.getDimension(R.styleable.TooltipIndicator_ti_lineWidthSelected, dpToPx(32));

            lineMargin = (int) typedArray.getDimension(R.styleable.TooltipIndicator_ti_lineMargin, dpToPx(4));

            lineWidthWithMargin = lineWidth + (lineMargin * 2);
            lineWidthSelectedWithMargin = lineWidthSelected + (lineMargin * 2);

            tooltipWidth = (int) typedArray.getDimension(R.styleable.TooltipIndicator_ti_tooltipWidth, dpToPx(100));
            tooltipHeight = (int) typedArray.getDimension(R.styleable.TooltipIndicator_ti_tooltipHeight, dpToPx(180));

            selectedLineDrawableResource = typedArray.getResourceId(R.styleable.TooltipIndicator_ti_selectedLineDrawable, R.drawable.tooltip_indicator_rounded_line_selected);
            unselectedLineDrawableResource = typedArray.getResourceId(R.styleable.TooltipIndicator_ti_unselectedLineDrawable, R.drawable.tooltip_indicator_rounded_line_unselected);

        } finally {
            typedArray.recycle();
        }

    }

    public void setupViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        this.adapter = viewPager.getAdapter();

        if (adapter == null) {
            throw new NullPointerException("ViewPager's adapter cannot be null.");
        }

        this.removeAllViews();
        initIndicatorLines();
        initToolTipView();
        selectPage(0);

        viewPager.removeOnPageChangeListener(this);
        viewPager.addOnPageChangeListener(this);
        this.setOnTouchListener(this);
    }

    public void setToolTipDrawables(List<Drawable> drawableList) {
        if (adapter == null) {
            throw new NullPointerException("ViewPager's adapter cannot be null.");
        }
        this.drawableList = drawableList.toArray(new Drawable[adapter.getCount()]);
    }

    //region Initialize ToolTip and Lines

    private void initToolTipView() {

        LayoutParams layoutParams = new LayoutParams(tooltipWidth, tooltipHeight);
        layoutParams.topMargin = -(layoutParams.height + dpToPx(8));

        tooltipView = new RelativeLayout(getContext());
        tooltipView.setLayoutParams(layoutParams);
        tooltipView.setBackgroundResource(R.drawable.tooltip_indicator_rounded_line_selected);
        tooltipView.setPadding(dpToPx(4),dpToPx(4),dpToPx(4),dpToPx(4));

        tooltipViewImage = new AppCompatImageView(getContext());
        tooltipViewImage.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        tooltipView.setScaleX(0);
        tooltipView.setScaleY(0);
        tooltipView.setAlpha(0);
        tooltipView.setTranslationY(layoutParams.height / 2);

        tooltipView.addView(tooltipViewImage);

        this.addView(tooltipView);
    }

    private void initIndicatorLines() {

        linesLayout = new LinearLayout(getContext());
        linesLayout.setVerticalGravity(LinearLayout.HORIZONTAL);

        int linesCount = adapter.getCount();

        for (int i = 0; i < linesCount; i++) {
            View lineView = new View(getContext());
            lineView.setBackgroundResource(unselectedLineDrawableResource);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(lineWidth, lineHeight);
            layoutParams.leftMargin = lineMargin;
            layoutParams.rightMargin = lineMargin;

            lineView.setLayoutParams(layoutParams);

            linesLayout.addView(lineView);
        }

        this.addView(linesLayout);
    }

    //endregion

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int x = (int) event.getX();

        if (drawableList == null || drawableList.length == 0) {
            return true;
        }

        int toolTipX = Math.max(0, x);
        toolTipX = Math.min(toolTipX, getMeasuredWidth());

        tooltipView.setX(toolTipX - tooltipView.getMeasuredWidth() / 2);

        int pos;
        int totalWidthBefore = selectedPosition * lineWidthWithMargin;

        if (toolTipX >= totalWidthBefore + lineWidthSelectedWithMargin) {
            pos = (selectedPosition + 1) + (toolTipX - (totalWidthBefore + lineWidthSelectedWithMargin)) / lineWidthWithMargin;
        } else if (toolTipX > totalWidthBefore) {
            pos = selectedPosition;
        } else {
            pos = toolTipX / lineWidthWithMargin;
        }

        pos = Math.min(pos, drawableList.length - 1);

        if (tooltipViewImage.getBackground() != drawableList[pos]) {
            tooltipViewImage.setBackgroundDrawable(drawableList[pos]);
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                tooltipView.animate().alpha(1).scaleX(1).scaleY(1).translationY(1).setInterpolator(new OvershootInterpolator()).start();
                break;

            case MotionEvent.ACTION_UP:
                tooltipView.animate().alpha(0).scaleX(0).scaleY(0).translationY(tooltipView.getMeasuredHeight() / 2).setInterpolator(new LinearInterpolator()).start();
                break;

        }

        return true;
    }

    private void selectPage(int position) {

        if (selectedPosition != -1) {
            collapseView(linesLayout.getChildAt(selectedPosition));
        }

        selectedPosition = position;

        expandView(linesLayout.getChildAt(selectedPosition));
    }

    private void expandView(final View selectedView) {

        selectedView.setBackgroundResource(selectedLineDrawableResource);

        if (expandAnimator != null) {
            expandAnimator.end();
        }

        expandAnimator = ValueAnimator.ofObject(new IntEvaluator(), lineWidth, lineWidthSelected);
        expandAnimator.setInterpolator(new DecelerateInterpolator());
        expandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) selectedView.getLayoutParams();
                layoutParams.width = (int) animation.getAnimatedValue();

                selectedView.setLayoutParams(layoutParams);
            }
        });

        expandAnimator.setDuration(200);
        expandAnimator.start();
    }

    private void collapseView(final View selectedView) {

        selectedView.setBackgroundResource(unselectedLineDrawableResource);

        if (collapseAnimator != null) {
            collapseAnimator.end();
        }

        collapseAnimator = ValueAnimator.ofObject(new IntEvaluator(), lineWidthSelected, lineWidth);
        collapseAnimator.setInterpolator(new DecelerateInterpolator());
        collapseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) selectedView.getLayoutParams();
                layoutParams.width = (int) animation.getAnimatedValue();

                selectedView.setLayoutParams(layoutParams);
            }
        });

        collapseAnimator.setDuration(500);
        collapseAnimator.start();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        selectPage(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public int dpToPx(int dp) {
        Resources r = getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

}
