package com.example.library_activity_timetable;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.example.library_activity_timetable.listener.ISchedule;
import com.example.library_activity_timetable.listener.OnConfigHandleAdapter;
import com.example.library_activity_timetable.listener.OnDateBuildAapter;
import com.example.library_activity_timetable.listener.OnFlaglayoutClickAdapter;
import com.example.library_activity_timetable.listener.OnItemBuildAdapter;
import com.example.library_activity_timetable.listener.OnItemClickAdapter;
import com.example.library_activity_timetable.listener.OnItemLongClickAdapter;
import com.example.library_activity_timetable.listener.OnScrollViewBuildAdapter;
import com.example.library_activity_timetable.listener.OnSlideBuildAdapter;
import com.example.library_activity_timetable.listener.OnSpaceItemClickAdapter;
import com.example.library_activity_timetable.listener.OnWeekChangedAdapter;
import com.example.library_activity_timetable.model.Schedule;
import com.example.library_activity_timetable.model.ScheduleEnable;
import com.example.library_activity_timetable.model.ScheduleSupport;
import com.example.library_activity_timetable.operater.AbsOperater;
import com.example.library_activity_timetable.operater.SimpleOperater;
import com.example.library_activity_timetable.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程表控件，该类主要负责属性的设置，业务逻辑由{@link SimpleOperater}处理
 * 虽然这个类代码很多，但是都是属性设置，除了属性设置的方法：
 * {@link #showView()}
 * {@link #changeWeek(int, boolean)}
 * {@link #changeWeekOnly(int)}
 * {@link #changeWeekForce(int)}
 * {@link #updateView()}
 * {@link #updateSlideView()}
 * {@link #updateDateView()}
 * {@link #updateView()}
 */
public class Activity_TimetableView extends LinearLayout {

    protected static final String TAG = "Activity_TimetableView";

    //业务逻辑
    private AbsOperater operater;
    private Context context;
    protected AttributeSet attrs;

    // 当前周、学期、课程数据源
    private int curWeek = 1;
    private String curTerm = "Term";
    private List<Schedule> dataSource = null;

    //默认的本地配置名称
    private String configName="default_schedule_config";

    //上边距、左边距、项高度
    private int marTop, marLeft, itemHeight;

    //侧边栏宽度
    private int monthWidth;

    //旗标布局背景颜色
    private int flagBgcolor = Color.rgb(220, 230, 239);//背景颜色
    private boolean isShowFlaglayout = true;


    //侧边项的最大个数
    private int maxSlideItem = 12;

    //是否显示非本周课程
    private boolean isShowNotCurWeek = true;

    //课程项文本颜色
    private int itemTextColorWithThisWeek = Color.DKGRAY;//本周

    private boolean isShowWeekends=true;

    //监听器
    private ISchedule.OnWeekChangedListener onWeekChangedListener;//周次改变监听
    private ISchedule.OnScrollViewBuildListener onScrollViewBuildListener;//替换滚动布局构建监听
    private ISchedule.OnDateBuildListener onDateBuildListener;//日期栏构建监听
    private ISchedule.OnItemClickListener onItemClickListener;//课程表item点击监听
    private ISchedule.OnItemLongClickListener onItemLongClickListener;//课程表item长按监听
    private ISchedule.OnItemBuildListener onItemBuildListener;//课程表item构建监听
    private ISchedule.OnSlideBuildListener onSlideBuildListener;//侧边栏构建监听
    private ISchedule.OnSpaceItemClickListener onSpaceItemClickListener;//空白格子点击监听
    private ISchedule.OnFlaglayoutClickListener onFlaglayoutClickListener;//旗标布局点击监听
    private ISchedule.OnConfigHandleListener onConfigHandleListener;

    public Activity_TimetableView callback(ISchedule.OnConfigHandleListener onConfigHandleListener) {
        this.onConfigHandleListener = onConfigHandleListener;
        return this;
    }

    public ISchedule.OnConfigHandleListener onConfigHandleListener() {
        if(onConfigHandleListener==null) onConfigHandleListener=new OnConfigHandleAdapter();
        return onConfigHandleListener;
    }

    /**
     * 是否显示周末
     * @param isShowWeekends
     * @return
     */
    public Activity_TimetableView isShowWeekends(boolean isShowWeekends) {
        this.isShowWeekends = isShowWeekends;
        return this;
    }

    public boolean isShowWeekends() {
        return isShowWeekends;
    }

    public AbsOperater operater() {
        if (operater == null) operater = new SimpleOperater();
        return operater;
    }

    /**
     * 设置侧边栏宽度dp
     *
     * @param monthWidthDp
     * @return
     */
    public Activity_TimetableView monthWidthDp(int monthWidthDp) {
        this.monthWidth = ScreenUtils.dip2px(context, monthWidthDp);
        return this;
    }

    /**
     * 获取侧边栏宽度px
     *
     * @return
     */
    public int monthWidth() {
        return this.monthWidth;
    }

    /**
     * 获取本周课程项文本颜色
     *
     * @return
     */
    public int itemTextColorWithThisWeek() {
        return itemTextColorWithThisWeek;
    }


    /**
     * 设置旗标布局背景颜色
     *
     * @param color
     * @return
     */
    public Activity_TimetableView flagBgcolor(int color) {
        this.flagBgcolor = color;
        return this;
    }


    /**
     * 获取是否显示旗标布局
     *
     * @return
     */
    public boolean isShowFlaglayout() {
        return isShowFlaglayout;
    }

    /**
     * 设置是否显示旗标布局
     *
     * @param isShowFlaglayout
     * @return
     */
    public Activity_TimetableView isShowFlaglayout(boolean isShowFlaglayout) {
        this.isShowFlaglayout = isShowFlaglayout;
        return this;
    }

    /**
     * 获取旗标布局背景颜色
     *
     * @return
     */
    public int flagBgcolor() {
        return flagBgcolor;
    }

    /**
     * 获取旗标布局
     *
     * @return
     */
    public LinearLayout flagLayout() {
        return operater().getFlagLayout();
    }

    /**
     * 设置课程项长按监听器
     *
     * @param onItemLongClickListener
     * @return
     */
    public Activity_TimetableView callback(ISchedule.OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
        return this;
    }

    /**
     * 获取课程项长按监听器
     *
     * @return
     */
    public ISchedule.OnItemLongClickListener onItemLongClickListener() {
        if (onItemLongClickListener == null) onItemLongClickListener = new OnItemLongClickAdapter();
        return onItemLongClickListener;
    }

    /**
     * 设置日期栏构建监听器
     *
     * @param onDateBuildListener
     * @return
     */
    public Activity_TimetableView callback(ISchedule.OnDateBuildListener onDateBuildListener) {
        this.onDateBuildListener = onDateBuildListener;
        return this;
    }

    /**
     * 获取日期栏构建监听器
     *
     * @return
     */
    public ISchedule.OnDateBuildListener onDateBuildListener() {
        if (onDateBuildListener == null) onDateBuildListener = new OnDateBuildAapter();
        return onDateBuildListener;
    }

    /**
     * 获取周次改变监听器
     *
     * @return
     */
    public ISchedule.OnWeekChangedListener onWeekChangedListener() {
        if (onWeekChangedListener == null) onWeekChangedListener = new OnWeekChangedAdapter();
        return onWeekChangedListener;
    }

    /**
     * 设置周次改变监听器
     *
     * @param onWeekChangedListener
     * @return
     */
    public Activity_TimetableView callback(ISchedule.OnWeekChangedListener onWeekChangedListener) {
        this.onWeekChangedListener = onWeekChangedListener;
        onWeekChangedListener.onWeekChanged(curWeek);
        return this;
    }

    /**
     * 设置滚动布局构建监听器
     *
     * @param onScrollViewBuildListener
     * @return
     */
    public Activity_TimetableView callback(ISchedule.OnScrollViewBuildListener onScrollViewBuildListener) {
        this.onScrollViewBuildListener = onScrollViewBuildListener;
        return this;
    }

    /**
     * 获取滚动布局构建监听器
     *
     * @return
     */
    public ISchedule.OnScrollViewBuildListener onScrollViewBuildListener() {
        if (onScrollViewBuildListener == null)
            onScrollViewBuildListener = new OnScrollViewBuildAdapter();
        return onScrollViewBuildListener;
    }

    /**
     * 设置课程项构建监听器
     *
     * @param onItemBuildListener
     * @return
     */
    public Activity_TimetableView callback(ISchedule.OnItemBuildListener onItemBuildListener) {
        this.onItemBuildListener = onItemBuildListener;
        return this;
    }

    /**
     * 设置Item点击监听器
     *
     * @param onItemClickListener
     * @return
     */
    public Activity_TimetableView callback(ISchedule.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    /**
     * 获取侧边栏构建监听
     *
     * @return
     */
    public ISchedule.OnSlideBuildListener onSlideBuildListener() {
        if (onSlideBuildListener == null) onSlideBuildListener = new OnSlideBuildAdapter();
        return onSlideBuildListener;
    }

    /**
     * 设置侧边栏构建监听器
     *
     * @param onSlideBuildListener
     * @return
     */
    public Activity_TimetableView callback(ISchedule.OnSlideBuildListener onSlideBuildListener) {
        this.onSlideBuildListener = onSlideBuildListener;
        return this;
    }

    /**
     * 设置空白格子点击监听器，点击之后会出现一个旗标布局
     *
     * @param
     * @return
     */
    public Activity_TimetableView callback(ISchedule.OnSpaceItemClickListener onSpaceItemClickListener) {
        this.onSpaceItemClickListener = onSpaceItemClickListener;
        return this;
    }

    /**
     * 获取空白格子点击监听器
     *
     * @return
     */
    public ISchedule.OnSpaceItemClickListener onSpaceItemClickListener() {
        if (onSpaceItemClickListener == null)
            onSpaceItemClickListener = new OnSpaceItemClickAdapter();
        return onSpaceItemClickListener;
    }

    /**
     * 设置旗标布局点击监听器
     *
     * @param onFlaglayoutClickListener
     * @return
     */
    public Activity_TimetableView callback(ISchedule.OnFlaglayoutClickListener onFlaglayoutClickListener) {
        this.onFlaglayoutClickListener = onFlaglayoutClickListener;
        return this;
    }

    /**
     * 获取旗标布局点击监听器
     *
     * @return
     */
    public ISchedule.OnFlaglayoutClickListener onFlaglayoutClickListener() {
        if (onFlaglayoutClickListener == null)
            onFlaglayoutClickListener = new OnFlaglayoutClickAdapter();
        return onFlaglayoutClickListener;
    }

    /**
     * 设置当前周
     *
     * @param curWeek 当前周
     * @return
     */
    public Activity_TimetableView curWeek(int curWeek) {
        if (curWeek < 1) this.curWeek = 1;
        else if (curWeek > 25) this.curWeek = 25;
        else this.curWeek = curWeek;
        onBind(curWeek);
        return this;
    }

    /**
     * 设置开学时间来计算当前周
     *
     * @param startTime 满足"yyyy-MM-dd HH:mm:ss"模式的字符串
     * @return
     */
    public Activity_TimetableView curWeek(String startTime) {
        int week = ScheduleSupport.timeTransfrom(startTime);
        if (week == -1)
            curWeek(1);
        else
            curWeek(week);
        onBind(week);
        return this;
    }

    /**
     * 获取当前周
     *
     * @return
     */
    public int curWeek() {
        return curWeek;
    }

    /**
     * 设置当前学期
     *
     * @param curTerm
     * @return
     */
    public Activity_TimetableView curTerm(String curTerm) {
        this.curTerm = curTerm;
        return this;
    }

    /**
     * 获取当前学期
     *
     * @return
     */
    public String curTerm() {
        return curTerm;
    }

    /**
     * 获取数据源
     *
     * @return
     */
    public List<Schedule> dataSource() {
        if (dataSource == null) dataSource = new ArrayList<>();
        return dataSource;
    }

    /**
     * 设置数据源
     *
     * @param dataSource
     * @return
     */
    public Activity_TimetableView data(List<Schedule> dataSource) {
        this.dataSource = ScheduleSupport.getColorReflect(dataSource);
        return this;
    }

    /**
     * 设置数据源
     *
     * @param dataSource
     * @return
     */
    public Activity_TimetableView source(List<? extends ScheduleEnable> dataSource) {
        data(ScheduleSupport.transform(dataSource));
        return this;
    }

    /**
     * 周次改变时的回调
     */
    private void onBind(int cur) {
        onWeekChangedListener().onWeekChanged(cur);
    }

    /**
     * 设置最大节次
     *
     * @param maxSlideItem 最大节次
     * @return
     */
    public Activity_TimetableView maxSlideItem(int maxSlideItem) {
        this.maxSlideItem = maxSlideItem;
        return this;
    }

    /**
     * 获取最大节次
     *
     * @return 最大节次
     */
    public int maxSlideItem() {
        return maxSlideItem;
    }

    /**
     * 获取Item构建监听器
     *
     * @return
     */
    public ISchedule.OnItemBuildListener onItemBuildListener() {
        if (onItemBuildListener == null) onItemBuildListener = new OnItemBuildAdapter();
        return onItemBuildListener;
    }

    /**
     * 获取Item点击监听
     *
     * @return
     */
    public ISchedule.OnItemClickListener onItemClickListener() {
        if (onItemClickListener == null) onItemClickListener = new OnItemClickAdapter();
        return onItemClickListener;
    }

    /**
     * 设置是否显示非本周课程
     *
     * @param showNotCurWeek 如果为true，将显示非本周，否则隐藏非本周
     * @return
     */
    public Activity_TimetableView isShowNotCurWeek(boolean showNotCurWeek) {
        isShowNotCurWeek = showNotCurWeek;
        return this;
    }

    /**
     * 判断是否显示非本周课程
     *
     * @return true：显示，false：不显示
     */
    public boolean isShowNotCurWeek() {
        return isShowNotCurWeek;
    }

    /**
     * 设置上边距值
     *
     * @param marTopPx
     * @return
     */
    public Activity_TimetableView marTop(int marTopPx) {
        this.marTop = marTopPx;
        return this;
    }

    /**
     * 设置左边距值
     *
     * @param marLeftPx
     * @return
     */
    public Activity_TimetableView marLeft(int marLeftPx) {
        this.marLeft = marLeftPx;
        return this;
    }

    /**
     * 设置课程项的高度
     *
     * @param itemHeightPx
     * @return
     */
    public Activity_TimetableView itemHeight(int itemHeightPx) {
        this.itemHeight = itemHeightPx;
        return this;
    }

    /**
     * 获取课程项的高度
     *
     * @return
     */
    public int itemHeight() {
        return itemHeight;
    }

    /**
     * 获取左边距
     *
     * @return
     */
    public int marLeft() {
        return marLeft;
    }

    /**
     * 获取上边距
     *
     * @return
     */
    public int marTop() {
        return marTop;
    }

    /**
     * 初始化
     *
     * @param context
     * @param attrs
     */
    public Activity_TimetableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs=attrs;
        operater().init(context,attrs,this);
    }

    /**
     * 等同于showView()
     *
     * @see Activity_TimetableView#showView()
     */
    public void updateView() {
        showView();
    }

    /**
     * 隐藏旗标布局，立即生效
     */
    public Activity_TimetableView hideFlaglayout() {
        flagLayout().setVisibility(GONE);
        return this;
    }

    /**
     * 显示旗标布局，立即生效
     *
     * @return
     */
    public Activity_TimetableView showFlaglayout() {
        flagLayout().setVisibility(VISIBLE);
        return this;
    }


    /**
     * 将日期栏设为隐藏状态
     *
     * @return
     */
    public void hideDateView() {
        operater().getDateLayout().setVisibility(View.GONE);
    }

    /**
     * 将日期栏设为可见状态
     *
     * @return
     */
    public void showDateView() {
        operater().getDateLayout().setVisibility(View.VISIBLE);
    }

    /**
     * 更新日期栏
     */
    public void updateDateView() {
        operater().updateDateView();
    }

    /**
     * 侧边栏更新
     */
    public void updateSlideView() {
        operater().updateSlideView();
    }

    /**
     * 周次切换
     *
     * @param week      周次
     * @param isCurWeek 是否强制设置为本周
     */
    public void changeWeek(int week, boolean isCurWeek) {
        operater().changeWeek(week,isCurWeek);
    }

    /**
     * 仅仅切换周次，不修改当前周
     *
     * @param week
     */
    public void changeWeekOnly(int week) {
        operater().changeWeek(week,false);
    }

    /**
     * 切换周次且修改为当前周
     *
     * @param week
     */
    public void changeWeekForce(int week) {
        operater().changeWeek(week,true);
    }

    /**
     * 更新旗标布局的背景色
     */
    public void updateFlaglayout() {
        flagLayout().setBackgroundColor(flagBgcolor());
        if (!isShowFlaglayout()) hideFlaglayout();
    }

    public void showView(){
        operater().showView();
    }

    public Activity_TimetableView configName(String configName){
        this.configName=configName;
        return this;
    }

    public String configName(){
        return this.configName;
    }
}
