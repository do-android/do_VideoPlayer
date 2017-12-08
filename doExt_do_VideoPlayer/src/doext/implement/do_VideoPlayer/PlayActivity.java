package doext.implement.do_VideoPlayer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import core.helper.DoResourcesHelper;
import core.helper.DoScriptEngineHelper;
import core.interfaces.DoIModuleTypeID;
import core.object.DoSingletonModule;
import doext.implement.do_VideoPlayer.orientation.OrientationSensorUtils;
import doext.implement.do_VideoPlayer.videoview.MyVideoPlayer;
import doext.implement.do_VideoPlayer.videoview.MyVideoPlayer.OnFullScreenClickListener;
import doext.implement.do_VideoPlayer.videoview.MyVideoPlayer.OnVideoPlayerCloseLoadingListener;
import doext.implement.do_VideoPlayer.videoview.MyVideoPlayer.OnVideoPlayerPreparedListener;

public class PlayActivity extends Activity implements OnVideoPlayerPreparedListener, OnFullScreenClickListener, DoIModuleTypeID, OnVideoPlayerCloseLoadingListener {
	private int width = -1;
	private int height = -1;

	public static final int SCREEN_LANDSCAPE = 0;
	public static final int SCREEN_PORTRAIT = 1;

	private MyVideoPlayer videoPlayer;
//	private String playUrl = Environment.getExternalStorageDirectory() + "/seeyou.MP4";
	private String playUrl = "http://200024860.vod.myqcloud.com/200024860_a6c772b664cb11e6b78b5788a0237c9a.f20.mp4";
//	private String playUrl = "http://oj8so80jf.bkt.clouddn.com/1.mp4";
	private int palyTime;

	private FrameLayout rootView;
	private View videoView;

	private OrientationSensorUtils mOrientationSensorUtils;
	private boolean isStart;
	private ImageView operation;

	private Bitmap startBmp;
	private Bitmap pauseBmp;
	private DoSingletonModule model;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onCreate(savedInstanceState);
		rootView = new FrameLayout(this);
		setContentView(rootView);

		playUrl = getIntent().getStringExtra("path");
		palyTime = getIntent().getIntExtra("point", 0);
		String modelAddress = getIntent().getStringExtra("address");

		try {
			model = DoScriptEngineHelper.parseSingletonModule(null, modelAddress);
		} catch (Exception e) {
			e.printStackTrace();
		}

		int do_videoview_id = DoResourcesHelper.getIdentifier("player_activity", "layout", this);
		videoView = View.inflate(this, do_videoview_id, null);

		int sv_id = DoResourcesHelper.getIdentifier("sv", "id", this);
		SurfaceView surfaceView = (SurfaceView) videoView.findViewById(sv_id);

		int iv_id = DoResourcesHelper.getIdentifier("iv", "id", this);
		ImageView ivFullscreen = (ImageView) videoView.findViewById(iv_id);

		int media_ll_id = DoResourcesHelper.getIdentifier("media_ll", "id", this);
		LinearLayout mediaLayout = (LinearLayout) videoView.findViewById(media_ll_id);

		int sb_id = DoResourcesHelper.getIdentifier("sb", "id", this);
		SeekBar sb = (SeekBar) videoView.findViewById(sb_id);

		int time_current_id = DoResourcesHelper.getIdentifier("time_current", "id", this);
		TextView mCurrentTime = (TextView) videoView.findViewById(time_current_id);

		int time_id = DoResourcesHelper.getIdentifier("time", "id", this);
		TextView mEndTime = (TextView) videoView.findViewById(time_id);

		int videoview_start_id = DoResourcesHelper.getIdentifier("videoview_start", "drawable", this);
		startBmp = BitmapFactory.decodeResource(getResources(), videoview_start_id);
		int videoview_pause_id = DoResourcesHelper.getIdentifier("videoview_pause", "drawable", this);
		pauseBmp = BitmapFactory.decodeResource(getResources(), videoview_pause_id);

		int operation_id = DoResourcesHelper.getIdentifier("operation", "id", this);
		operation = (ImageView) videoView.findViewById(operation_id);
		operation.setVisibility(View.VISIBLE);
		operation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (videoPlayer != null) {
					if (isStart) {
						operation.setImageBitmap(pauseBmp);
						videoPlayer.show(MyVideoPlayer.sDefaultTimeout);
						int pos = videoPlayer.getCurrentPosition();
						videoPlayer.start();
						videoPlayer.seekTo(pos);
						isStart = false;
					} else {
						operation.setImageBitmap(startBmp);
						videoPlayer.pause();
						isStart = true;
					}
				}
			}
		});

		videoPlayer = new MyVideoPlayer(this, surfaceView, mediaLayout, ivFullscreen, mCurrentTime, mEndTime, sb);
		rootView.addView((View) videoView, computeContainerSize(this, 16, 9));
		videoPlayer.setOnFullScreenClickListener(this);
		videoPlayer.setOnVideoPlayerPreparedListener(this);
		videoPlayer.setOnVideoPlayerCloseLoadingListener(this);

		mOrientationSensorUtils = new OrientationSensorUtils(this, mHandler);
		mOrientationSensorUtils.onResume();
		iniProgressBar();
	}

	@Override
	protected void onPause() {
		super.onPause();
		palyTime = videoPlayer.getCurrentPosition();
		videoPlayer.pause();
		operation.setImageBitmap(startBmp);
		isStart = true;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.e("mediaPlayer", "onRestart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e("mediaPlayer", "onResume");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (videoPlayer != null) {
			videoPlayer.stopPlayback();
		}
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case OrientationSensorUtils.ORIENTATION_8:// 反横屏
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
				break;
			case OrientationSensorUtils.ORIENTATION_9:// 反竖屏
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
				break;
			case OrientationSensorUtils.ORIENTATION_0:// 正横屏
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				break;
			case OrientationSensorUtils.ORIENTATION_1:// 正竖屏
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (width == -1 || height == -1) {
			width = videoView.getLayoutParams().width;
			height = videoView.getLayoutParams().height;
		}
		if (ScreenUtils.getOrientation(this) == Configuration.ORIENTATION_PORTRAIT) {
			videoView.getLayoutParams().height = height;
			videoView.getLayoutParams().width = width;
			ScreenUtils.showFullScreen(this, false);
			setRequestedOrientation(SCREEN_PORTRAIT);
		} else {
			videoView.getLayoutParams().height = ScreenUtils.getHeight(this);
			videoView.getLayoutParams().width = ScreenUtils.getWight(this);
			ScreenUtils.showFullScreen(this, true);
			setRequestedOrientation(SCREEN_LANDSCAPE);
		}
		super.onConfigurationChanged(newConfig);
	}

//    @Override
//    protected void onAttachedToWindow() {
//        if (mOrientationSensorUtils == null) {
//            mOrientationSensorUtils = new OrientationSensorUtils(this, mHandler);
//            mLargeMediaController.setOrientationSensorUtils(mOrientationSensorUtils);
//            mSmallMediaController.setOrientationSensorUtils(mOrientationSensorUtils);
//        }
//        mOrientationSensorUtils.onResume();
//        super.onAttachedToWindow();
//    }
//
//    @Override
//    protected void onDetachedFromWindow() {
//        if (mOrientationSensorUtils != null) {
//            mOrientationSensorUtils.onPause();
//        }
//        mHandler.removeCallbacksAndMessages(null);
//        super.onDetachedFromWindow();
//    }

	private FrameLayout.LayoutParams computeContainerSize(Context context, int mWidth, int mHeight) {
		int width = getScreenWidth(context);
		int height = width * mHeight / mWidth;
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		params.width = width;
		params.height = height;
//		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		params.gravity = Gravity.CENTER;
		return params;
	}

	private int getScreenWidth(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Point outSize = new Point();
		wm.getDefaultDisplay().getSize(outSize);
		return outSize.x;
	}

	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {
//		videoPlayer.setOnVideoPlayerPreparedListener(null); 
		if (!TextUtils.isEmpty(playUrl)) {
			if (isStart) {
				operation.setImageBitmap(pauseBmp);
				videoPlayer.show(MyVideoPlayer.sDefaultTimeout);
				isStart = false;
			}

			videoPlayer.playUrl(playUrl);
			if (!videoPlayer.isPlaying()) {
				// 按照初始位置播放
				videoPlayer.seekTo(palyTime);
				videoPlayer.start();
			}
		}
		Log.e("mediaPlayer", "PlayActivity onPrepared ");
	}

	@Override
	public void onFullScreenClick(View view) {
		if (ScreenUtils.getOrientation(this) == Configuration.ORIENTATION_LANDSCAPE) {
			// mLetvUIListener.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			mOrientationSensorUtils.setOrientation(OrientationSensorUtils.ORIENTATION_1);
		} else {
			mOrientationSensorUtils.setOrientation(OrientationSensorUtils.ORIENTATION_0);
		}
		if (mOrientationSensorUtils != null) {
			mOrientationSensorUtils.getmOrientationSensorListener().lockOnce(getRequestedOrientation());
		}

	}

	@Override
	public String getTypeID() {
		if (model != null) {
			return model.getTypeID();
		}
		return "";
	}

	ProgressDialog loadDialog;

	private void iniProgressBar() {
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		int do_videoview_id = DoResourcesHelper.getIdentifier("videoplayer_loading", "layout", this);
		View view = layoutInflater.inflate(do_videoview_id, null);// 得到加载view
		// 获取view对象中的ImageView
		int videoplayer_loading_img_id = DoResourcesHelper.getIdentifier("videoplayer_loading_img", "id", this);
		ImageView imageView = (ImageView) view.findViewById(videoplayer_loading_img_id);
		// 加载动画
		Animation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(1500);
		animation.setRepeatMode(Animation.RESTART);
		animation.setRepeatCount(-1);
		animation.setStartOffset(-1);
		LinearInterpolator lin = new LinearInterpolator();
		animation.setInterpolator(lin);
		// 设置动画
		imageView.startAnimation(animation);
		loadDialog = new ProgressDialog(this);// 创建自定义样式dialog
		loadDialog.setCancelable(false);// 不可以用“返回键”取消
		loadDialog.show();
		// 设置布局
		WindowManager.LayoutParams params = loadDialog.getWindow().getAttributes();
		params.dimAmount = 0f;// 控制弹出后后面背景底色
		loadDialog.getWindow().setGravity(Gravity.CENTER);
		loadDialog.getWindow().setLayout(100, 100);
		loadDialog.setContentView(view, params);
	}

	//关闭加载进度条
	@Override
	public void onCloseLoading() {
		if (null != loadDialog) {
			loadDialog.dismiss();
		}
	}
}
