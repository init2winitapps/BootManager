package com.drx2.bootmanager.page.adapter;
import java.util.ArrayList;

import com.drx2.bootmanager.page.adapter.PagerHeader.OnHeaderClickListener;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;


public class PageAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener, OnHeaderClickListener {

	private final Context mContext;
	private final ViewPager mPager;
	private final ArrayList<PageInfo> mPages = new ArrayList<PageInfo>();
	private final PagerHeader mHeader;
	
	public static PageChangeListener onPageChange;
	
	public PageAdapter(FragmentActivity activity, ViewPager pager, PagerHeader head) {
		super(activity.getSupportFragmentManager());
		mContext = activity;
		mPager = pager;
		mHeader = head;
		
		mHeader.setOnHeaderClickListener(this);
		mPager.setOnPageChangeListener(this);
		mPager.setAdapter(this);
	}
	
	public static void setPageChangeListener(PageChangeListener page) {
		onPageChange = page;
	}
	
	public void addPage(Class<?> clss, Bundle args, String title) {
		PageInfo info = new PageInfo(clss, args, title);
		mPages.add(info);
		mHeader.add(0, title);
		notifyDataSetChanged();
	}
	
	public void removePage(int page){
		mPages.remove(page);
		notifyDataSetChanged();
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
		
	}
	
	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		mHeader.setPosition(position, positionOffset, positionOffsetPixels);
	}
	
	@Override
	public void onPageSelected(int index) {
		mHeader.setDisplayedPage(index);
		if(onPageChange != null) {
			onPageChange.onPageChange(index);
		}
	}
	
	@Override
	public Fragment getItem(int index) {
		PageInfo info = mPages.get(index);
		return Fragment.instantiate(mContext, info.clss.getName(), info.bundle);
	}
	
	@Override
	public int getCount() {
		return mPages.size();
	}

	@Override
	public void onHeaderClicked(int position) {
		
	}

	@Override
	public void onHeaderSelected(int position) {
		onPageChange.onPageChange(position);
		mPager.setCurrentItem(position);			
	}

	
}