package com.drx2.bootmanager.page.adapter;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.drx2.bootmanager.R;
	
public class FragmentAdapter extends FragmentActivity {
	
	private FragmentActivity mActivity;
	private ViewPager mPager;
	private PageAdapter mAdapter;
	
	public FragmentAdapter(FragmentActivity fragemnt_activity, ViewPager view_pager) {
		this.mActivity = fragemnt_activity;
		this.mPager = view_pager;
		this.mAdapter = new PageAdapter(mActivity, mPager, (PagerHeader)findViewById(R.id.pager_header));
	}
	
	public ViewPager getCurrentViewPager() {
		return this.mPager;
	}
	
	public void addPage(Class<?> fragment_class, Bundle bundle, String title) {
		mAdapter.addPage(fragment_class, bundle, title);
	}
}