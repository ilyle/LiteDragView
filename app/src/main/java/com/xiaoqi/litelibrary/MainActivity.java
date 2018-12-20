package com.xiaoqi.litelibrary;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaoqi.litedragview.LiteDragHelper;
import com.xiaoqi.litedragview.LiteDragView;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView mTextView = findViewById(R.id.tv);
        mContext = this;
        LiteDragHelper.bind(mContext, mTextView, Color.BLUE);
    }
}
