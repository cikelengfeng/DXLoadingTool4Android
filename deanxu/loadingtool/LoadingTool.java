package deanxu.loadingtool;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于给任意一个view添加loading效果
 * Created by xudong on 13-6-27.
 */
public final class LoadingTool {

    private static final String LOADING_VIEW_TAG = "com.deanxu.loadingtool.loading_tool.tag";

    private static final Map<String, CallbackInfo> infoMap = new HashMap<String, CallbackInfo>();

    private static final class CallbackInfo {
        private Runnable timeoutCallback;
        private LoadingToolInterceptor<View> interceptor;
        private long deadLineTime;//in millisecond
        private WeakReference<View> targetView;
        private View getTargetView() {
            return targetView == null ? null : targetView.get();
        }
        private void setTargetView(View view) {
            if (view == null) {
                targetView = null;
            }else {
                targetView = new WeakReference<View>(view);
            }
        }
    }

    /**
     * 默认实现的拦截器，loading时将target替换为一个progressBar
     */
    private static LoadingToolInterceptor<View> defaultInterceptor = new LoadingToolInterceptor<View>() {
        @Override
        public void makeViewLoading(View target) {
            LoadingTool.invalidateView(target);
        }

        @Override
        public void makeViewIdle(View target) {
            LoadingTool.validateView(target);
        }
    };

    /**
     * 不断地播放缩放动画， 以自身中点为中心 scale 1 -> 0.9 -> 1
     */
    public static final LoadingToolInterceptor<View> LOADING_SCALE_INTERCEPTOR = new LoadingToolInterceptor<View>() {
        @Override
        public void makeViewLoading(View target) {
            Animation animationStep1 = new ScaleAnimation(1, 0.9f, 1, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animationStep1.setDuration(300);
            animationStep1.setRepeatCount(Animation.INFINITE);
            animationStep1.setRepeatMode(Animation.REVERSE);
            target.startAnimation(animationStep1);
        }

        @Override
        public void makeViewIdle(View target) {
            target.clearAnimation();
        }
    };

    /**
     * 不断地播放alpha动画， 以自身中点为中心 alpha 1 -> 0.7 -> 1
     */
    public static final LoadingToolInterceptor<View> LOADING_ALPHA_INTERCEPTOR = new LoadingToolInterceptor<View>() {
        @Override
        public void makeViewLoading(View target) {
            Animation animationStep1 = new AlphaAnimation(1, 0.7f);
            animationStep1.setDuration(300);
            animationStep1.setRepeatCount(Animation.INFINITE);
            animationStep1.setRepeatMode(Animation.REVERSE);
            target.startAnimation(animationStep1);
        }

        @Override
        public void makeViewIdle(View target) {
            target.clearAnimation();
        }
    };

    /**
     * 不断地播放旋转动画， 以自身中点为中心 rotate -20 -> 20 -> -20
     */
    public static final LoadingToolInterceptor<View> LOADING_ROTATE_INTERCEPTOR = new LoadingToolInterceptor<View>() {
        @Override
        public void makeViewLoading(View target) {
            Animation animationStep1 = new RotateAnimation(-20, 20, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animationStep1.setDuration(300);
            animationStep1.setRepeatCount(Animation.INFINITE);
            animationStep1.setRepeatMode(Animation.REVERSE);
            target.startAnimation(animationStep1);
        }

        @Override
        public void makeViewIdle(View target) {
            target.clearAnimation();
        }
    };

    /**
     * 设置默认拦截器,以便修改全局的loading效果
     *
     * @param defaultInterceptor 如果是null，则不会替换旧的拦截器
     */
    public static void setDefaultInterceptor(LoadingToolInterceptor<View> defaultInterceptor) {
        if (defaultInterceptor != null) {
            LoadingTool.defaultInterceptor = defaultInterceptor;
        }
    }

    /**
     * 用于定制loading效果的拦截器
     */
    public static interface LoadingToolInterceptor<T extends View> {
        /**
         * 使view变为loading状态，e.g.开始一个loading动画 或者什么也不做
         *
         * @param target 需要设置loading效果的view
         */
        abstract void makeViewLoading(final T target);

        /**
         * 使view恢复为idle状态
         *
         * @param target 需要设置loading效果的view
         */
        abstract void makeViewIdle(final T target);
    }

    private LoadingTool() {
    }//没有必要实例化该类

    /**
     * 为一个view显示loading效果，使用一个progressBar替换该target，在恢复时将该view显示出来，默认超时时间是Integer.MAX_VALUE
     *
     * @param target 需要设置loading效果的view，不能为null
     */
    public static void showLoadingInDefaultModeForView(final View target, final String id) {
        showLoadingInDefaultModeForView(target, id, Integer.MAX_VALUE);
    }

    public static void showLoadingInDefaultModeForView(final View target, final String id,int timeout) {
        showLoading(target, id, defaultInterceptor, timeout);
    }

    /**
     * 为一个view显示loading效果，使用scale拦截器{@link #LOADING_SCALE_INTERCEPTOR} 超时时间为Integer.MAX_VALUE
     *
     * @param view 需要设置loading效果的view，不能为null
     */
    public static void showLoadingInScaleModeForView(final View view, final String id) {
        showLoading(view, id, LOADING_SCALE_INTERCEPTOR, Integer.MAX_VALUE);
    }

    /**
     * 为一个view显示loading效果，使用rotate拦截器{@link #LOADING_ROTATE_INTERCEPTOR} 超时时间为Integer.MAX_VALUE
     *
     * @param view 需要设置loading效果的view，不能为null
     */
    public static void showLoadingInRotateModeForView(final View view, final String id) {
        showLoading(view, id, LOADING_ROTATE_INTERCEPTOR, Integer.MAX_VALUE);
    }

    /**
     * 为一个view显示loading效果，使用alpha拦截器{@link #LOADING_ALPHA_INTERCEPTOR} 超时时间为Integer.MAX_VALUE
     *
     * @param view 需要设置loading效果的view，不能为null
     */
    public static void showLoadingInAlphaModeForView(final View view, final String id) {
        showLoading(view, id, LOADING_ALPHA_INTERCEPTOR, Integer.MAX_VALUE);
    }

    /**
     * 为一个view显示loading效果，效果取决于调用者提供的拦截器
     *
     * @param target          需要设置loading效果的view，不能为null
     * @param userInterceptor 拦截器
     * @param timeOut         loading效果的超时时间，超时后自动取消loading效果
     */
    public static void showLoading(final View target, final String id, LoadingToolInterceptor<View> userInterceptor, int timeOut) {
        Log.d("loadingtool", "id " + id + " need to show");
        if (infoMap.containsKey(id) && infoMap.get(id).getTargetView()!= null && infoMap.get(id).getTargetView().equals(target)) {//is loading
            return;
        }
        if (userInterceptor != null) {
            CallbackInfo info = new CallbackInfo();
            Runnable callback = new Runnable() {
                @Override
                public void run() {
                    hideLoading(id);
                }
            };
            info.timeoutCallback = callback;
            info.interceptor = userInterceptor;
            info.setTargetView(target);
            long currentMill = System.currentTimeMillis();
            info.deadLineTime = Integer.MAX_VALUE - timeOut > currentMill ? currentMill + timeOut : Integer.MAX_VALUE;
            userInterceptor.makeViewLoading(target);
            putCallbackInfo(id,info);
            target.postDelayed(callback, timeOut);
        }else {
            Log.d("loadingtool", "userInterceptor is null,do nothing");
        }
    }

    /**
     * 取消target的loading状态
     *
     * @param id 需要取消loading状态的id
     */
    public static void hideLoading(final String id) {
        Log.d("loadingtool", "id "+id + " need to hide");
        CallbackInfo info = infoMap.get(id);
        if (info != null) {
            View target = info.getTargetView();
            if (target != null) {
                info.interceptor.makeViewIdle(info.getTargetView());
                target.removeCallbacks(info.timeoutCallback);
                removeCallbackInfoSafely(id);
            }else {
                Log.d("loadingtool", "target is null,this id may be in Pasued state,please call resume instead");
            }
        }else {
            Log.d("loadingtool", "callbackinfo is null,do nothing");
        }

    }

    /**
     * 暂时取消view的loading状态，该方法和{@link #resume(android.view.View, String)} 适合用在listView中，当view离开屏幕被listView保存以便复用时，调用该方法。以免复用该view时仍然处于loading状态
     * <p><strong>如果该view不是loading状态，则什么也不做</strong></p>
     * <P>还在测试状态</P>
     *
     * @param id   用于恢复的标志,id应该是整个程序上下文中唯一的值
     */
    public static void pause(final String id) {
        Log.d("loadingtool", "id "+id + " need to pause");
        if (infoMap.containsKey(id)) {
            CallbackInfo info = infoMap.get(id);
            View target = info.getTargetView();
            if (target != null) {
                info.interceptor.makeViewIdle(target);
            }else {
                Log.d("loadingtool", "but the id's target view is null");
            }
            info.setTargetView(null);
        }else {
            Log.d("loadingtool", "id is not in infomap,it may has been removed by hideloading call or never be in loading state");
        }
    }

    /**
     * 恢复loading状态，当列表项被重建时调用，以便恢复曾经被暂时取消的loading状态
     * <p><strong>如果调用{@link #pause(String)}时flag对应的view不是pause状态，则什么也不做</strong></p>
     * <P>还在测试状态</P>
     *
     * @param target 需要显示loading状态的view，和pause中传递的view应该是实现相同功能的，但是未必是同一个view：它有可能是其它被复用的view
     * @param id   用于恢复的标志,id应该是整个程序上下文中唯一的值
     */
    public static void resume(final View target, final String id) {
        Log.d("loadingtool", "id "+id + " need to resume");
        CallbackInfo info = infoMap.get(id);
        if (info != null && info.getTargetView() == null) {
            info.interceptor.makeViewLoading(target);
            info.setTargetView(target);
        }else {
            Log.d("loadingtool", "this id is not in paused state,make showLoading call instead");
        }
    }

    public static boolean isIDPausedLoading(final String id) {
        CallbackInfo info = infoMap.get(id);
        return !TextUtils.isEmpty(id) && info != null && info.getTargetView() == null;
    }

    private static ViewGroup findViewContainer(final View target) {
        if (target == null) {
            return null;
        }
        final ViewGroup targetParent;
        try {
            targetParent = (ViewGroup) target.getParent();
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
        return targetParent;
    }

    private static void invalidateView(final View target) {
        ViewGroup targetParent = findViewContainer(target);
        if (targetParent == null) {
            return;
        }
        Context viewContext = target.getContext();
        if (viewContext == null) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = target.getLayoutParams();
        final ProgressBar loadingBar = new ProgressBar(viewContext);
        loadingBar.setTag(LOADING_VIEW_TAG);
        loadingBar.setLayoutParams(layoutParams);

        target.setVisibility(View.INVISIBLE);
        targetParent.addView(loadingBar);
    }

    private static void validateView(final View target) {
        ViewGroup targetParent = findViewContainer(target);
        if (targetParent == null) {
            return;
        }
        View loadingBar = targetParent.findViewWithTag(LOADING_VIEW_TAG);
        if (loadingBar == null) {
            return;
        }
        targetParent.removeView(loadingBar);
        target.setVisibility(View.VISIBLE);
    }

    //以下四个方法主要用于在调试时添加log，所以不要因为里面只有一行代码就重构掉
    private static void putCallbackInfo(String id,CallbackInfo info) {
        infoMap.put(id, info);
    }

    private static void removeCallbackInfoSafely(String id) {
            if (infoMap.containsKey(id)) {
            infoMap.remove(id);
        }
    }

}
