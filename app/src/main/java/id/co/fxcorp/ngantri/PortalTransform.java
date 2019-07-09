package id.co.fxcorp.ngantri;

import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class PortalTransform implements ViewPager.OnPageChangeListener, ViewPager.PageTransformer {

        private ViewPager viewPager;
        private FragmentStatePagerAdapter mAdapter;
        private float lastOffset;
        private boolean scalingEnabled;

        public PortalTransform(ViewPager viewPager, FragmentStatePagerAdapter adapter) {
            this.viewPager = viewPager;
            viewPager.addOnPageChangeListener(this);
            mAdapter = adapter;
        }

        public void enableScaling(boolean enable) {
            scalingEnabled = enable;
        }

        @Override
        public void transformPage(View page, float position) {

        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int realCurrentPosition;
            int nextPosition;
            float realOffset;
            boolean goingLeft = lastOffset > positionOffset;

            // If we're going backwards, onPageScrolled receives the last position
            // instead of the current one
            if (goingLeft) {
                realCurrentPosition = position + 1;
                nextPosition = position;
                realOffset = 1 - positionOffset;
            } else {
                nextPosition = position + 1;
                realCurrentPosition = position;
                realOffset = positionOffset;
            }

            // Avoid crash on overscroll
            if (nextPosition > mAdapter.getCount() - 1
                    || realCurrentPosition > mAdapter.getCount() - 1) {
                return;
            }

            View currentCard = mAdapter.getItem(realCurrentPosition).getView();

            // This might be null if a fragment is being used
            // and the views weren't created yet
            if (currentCard != null) {
                if (scalingEnabled) {
                    float scale = (float) (1 + 0.1 * (1 - realOffset));
                    if (currentCard.getScaleX() != scale) {
                        currentCard.setScaleX(scale);
                        currentCard.setScaleY(scale);
                    }
                }
            }

            View nextCard = mAdapter.getItem(nextPosition).getView();

            // We might be scrolling fast enough so that the next (or previous) card
            // was already destroyed or a fragment might not have been created yet
            if (nextCard != null) {
                if (scalingEnabled) {
                    float scale = (float) (1 + 0.1 * (realOffset));
                    if (nextCard.getScaleX() != scale) {
                        nextCard.setScaleX(scale);
                        nextCard.setScaleY(scale);
                    }
                }
            }

            lastOffset = positionOffset;
        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
}
