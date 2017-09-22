package com.ttdt.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.ttdt.R;

public class WelcomeActivity extends Activity {

	//声明控件
	private ImageView imgWelcome;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		//初始化控件
		setupView();
	}
	private void setupView() {
		// TODO Auto-generated method stub
		imgWelcome = (ImageView) findViewById(R.id.img_welcome01);
		//拿到动画
		Animation anim =
				AnimationUtils.loadAnimation(this, R.anim.alpha);
		//给图片添加动画
		imgWelcome.startAnimation(anim);
		//给动画添加监听器
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				//跳转界面
				Intent intent =
						new Intent(WelcomeActivity.this,MainActivity.class);
				//启动跳转意图
				startActivity(intent);
				finish();
			}
		});
	}


}

