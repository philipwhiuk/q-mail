package com.fsck.k9.ui.messageview.ical;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;

import com.fsck.k9.R;
import com.fsck.k9.ical.ICalData.ICalendarData;
import com.fsck.k9.mailstore.ICalendarViewInfo;
import com.fsck.k9.view.ToolableViewAnimator;


public class LockedICalendarView extends ToolableViewAnimator implements OnClickListener {
    private ViewStub iCalendarViewStub;
    private ICalendarViewInfo iCalendarViewInfo;
    private ICalendarViewCallback iCalendarCallback;
    private ICalendarData iCalendarData;
    private boolean showSummary;


    public LockedICalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LockedICalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockedICalendarView(Context context) {
        super(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (isInEditMode()) {
            return;
        }

        View unlockButton = findViewById(R.id.locked_button);
        unlockButton.setOnClickListener(this);

        iCalendarViewStub = (ViewStub) findViewById(R.id.icalendar_stub);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.locked_button: {
                showUnlockedView();
                break;
            }
        }
    }

    private void showUnlockedView() {
        if (iCalendarViewStub == null) {
            throw new IllegalStateException("Cannot display unlocked iCalendar!");
        }

        ICalendarView view = (ICalendarView) iCalendarViewStub.inflate();
        view.setCallback(iCalendarCallback);
        view.setICalendar(iCalendarViewInfo, iCalendarData);
        view.setShowSummary(showSummary);
        iCalendarViewStub = null;

        setDisplayedChild(1);
    }

    public void setICalendar(ICalendarViewInfo iCalendarViewInfo, ICalendarData iCalendarData) {
        this.iCalendarViewInfo = iCalendarViewInfo;
        this.iCalendarData = iCalendarData;
    }

    public void setCallback(ICalendarViewCallback iCalendarCallback) {
        this.iCalendarCallback = iCalendarCallback;
    }

    public void setShowSummary(boolean showSummary) {
        this.showSummary = showSummary;
    }
}
