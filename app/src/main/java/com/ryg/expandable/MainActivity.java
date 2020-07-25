package com.ryg.expandable;

import java.util.ArrayList;
import java.util.List;

import com.ryg.expandable.ui.PinnedHeaderExpandableListView;
import com.ryg.expandable.ui.StickyLayout;
import com.ryg.expandable.ui.PinnedHeaderExpandableListView.OnHeaderUpdateListener;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
// https://blog.csdn.net/singwhatiwanna/article/details/25546871
public class MainActivity extends Activity implements
        ExpandableListView.OnChildClickListener,
        ExpandableListView.OnGroupClickListener,
        OnHeaderUpdateListener, StickyLayout.OnGiveUpTouchEventListener {
    private static final String TAG = "MainActivity";
    private PinnedHeaderExpandableListView expandableListView;
    private StickyLayout stickyLayout;
    private ArrayList<Group> groupList;
    private ArrayList<List<People>> childList;

    private MyExpandableListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        expandableListView = (PinnedHeaderExpandableListView) findViewById(R.id.expandablelist);
        stickyLayout = (StickyLayout)findViewById(R.id.sticky_layout);
        initData();

        adapter = new MyExpandableListAdapter(this, groupList, childList);
        expandableListView.setAdapter(adapter);

        // 展开所有group
        for (int i = 0, count = expandableListView.getCount(); i < count; i++) {
            expandableListView.expandGroup(i);
        }

        expandableListView.setOnHeaderUpdateListener(this);
        expandableListView.setOnChildClickListener(this);
        expandableListView.setOnGroupClickListener(this);
        stickyLayout.setOnGiveUpTouchEventListener(this);
//        stickyLayout.setSticky(false);
//        stickyLayout.requestDisallowInterceptTouchEventOnHeader(true);
    }

    /***
     * InitData
     */
    void initData() {
        groupList = new ArrayList<Group>();
        Group group = null;
        for (int i = 0; i < 3; i++) {
            group = new Group();
            group.setTitle("group-" + i);
            groupList.add(group);
        }

        childList = new ArrayList<List<People>>();
        for (int i = 0; i < groupList.size(); i++) {
            ArrayList<People> childTemp;
            if (i == 0) {
                childTemp = new ArrayList<People>();
                for (int j = 0; j < 13; j++) {
                    People people = new People();
                    people.setName("yy-" + j);
                    people.setAge(30);
                    people.setAddress("sh-" + j);

                    childTemp.add(people);
                }
            } else if (i == 1) {
                childTemp = new ArrayList<People>();
                for (int j = 0; j < 8; j++) {
                    People people = new People();
                    people.setName("ff-" + j);
                    people.setAge(40);
                    people.setAddress("sh-" + j);

                    childTemp.add(people);
                }
            } else {
                childTemp = new ArrayList<People>();
                for (int j = 0; j < 23; j++) {
                    People people = new People();
                    people.setName("hh-" + j);
                    people.setAge(20);
                    people.setAddress("sh-" + j);

                    childTemp.add(people);
                }
            }
            childList.add(childTemp);
        }

    }
    // 点击组的回调
    @Override
    public boolean onGroupClick(final ExpandableListView parent, final View v,
            int groupPosition, final long id) {

        return false;
    }
    // 点击子的回调
    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
            int groupPosition, int childPosition, long id) {
        Toast.makeText(MainActivity.this,
                childList.get(groupPosition).get(childPosition).getName(), Toast.LENGTH_LONG)
                .show();

        return false;
    }

    // 获取固定的头部 View
    // 和 ExpandableListView 的组是一样的布局
    // 属于 OnHeaderUpdateListener 的方法
    @Override
    public View getPinnedHeader() {
        View headerView = (ViewGroup) getLayoutInflater().inflate(R.layout.group, null);
        headerView.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        return headerView;
    }
    // 更新固定的头部 View 的文本内容
    // 属于 OnHeaderUpdateListener 的方法
    @Override
    public void updatePinnedHeader(View headerView, int firstVisibleGroupPos) {
        Group firstVisibleGroup = (Group) adapter.getGroup(firstVisibleGroupPos);
        TextView textView = (TextView) headerView.findViewById(R.id.group);
        textView.setText(firstVisibleGroup.getTitle());
    }

    @Override
    public boolean giveUpTouchEvent(MotionEvent event) {
        Log.d(TAG, "giveUpTouchEvent: expandableListView.getFirstVisiblePosition()="+expandableListView.getFirstVisiblePosition());
        if (expandableListView.getFirstVisiblePosition() == 0) {
            View view = expandableListView.getChildAt(0);
            Log.d(TAG, "giveUpTouchEvent: view="+view + ", view.getTop()=" + view.getTop());
            if (view != null && view.getTop() >= 0) {
                Log.d(TAG, "giveUpTouchEvent: true");
                return true;
            }
        }
        Log.d(TAG, "giveUpTouchEvent: false");
        return false;
    }

}
