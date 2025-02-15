package com.The032solutions.MouTCV.Activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.viewpager.widget.PagerAdapter;

import com.The032solutions.MouTCV.R;

public class ItemPagerAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    final int[] mItems;
    View itemView;
    ImageView imageView;

    public ItemPagerAdapter(Context context, int[] items) {
        this.mContext = context;
        this.mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mItems = items;
    }

    @Override
    public int getCount() {
        return mItems.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);
        imageView = (ImageView) itemView.findViewById(R.id.imageView);
        imageView.setImageResource(mItems[position]);
        container.addView(itemView);
        return itemView;
    }

    public void replaceImage(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
