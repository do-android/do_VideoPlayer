package dotest.module.activity;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import core.DoServiceContainer;
import core.object.DoInvokeResult;
import doext.implement.do_VideoPlayer_Model;
import dotest.module.frame.debug.DoService;

/**
 * webview组件测试样例
 */
public class WebViewSampleTestActivty extends DoTestActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void initModuleModel() throws Exception {
		this.model = new do_VideoPlayer_Model();
	}

	@Override
	protected void initUIView() throws Exception {
//		Do_WebView_View view = new Do_WebView_View(this);
//		((DoUIModule) this.model).setCurrentUIModuleView(view);
//		((DoUIModule) this.model).setCurrentPage(currentPage);
//		view.loadView((DoUIModule) this.model);
//		LinearLayout uiview = (LinearLayout) findViewById(R.id.uiview);
//		uiview.addView(view);
	}

	@Override
	public void doTestProperties(View view) {
		DoService.setPropertyValue(this.model, "url", "https://www.baidu.com");
	}

	@Override
	protected void doTestSyncMethod() {
		Map<String, String> _paras_back = new HashMap<String, String>();
		String url = "http://200024860.vod.myqcloud.com/200024860_a6c772b664cb11e6b78b5788a0237c9a.f20.mp4";
		_paras_back.put("path", url);
		DoService.syncMethod(this.model, "play", _paras_back);
	}

	@Override
	protected void doTestAsyncMethod() {
		Map<String, String> _paras_loadString = new HashMap<String, String>();
		_paras_loadString.put("text", "<b>百度</b>");
		DoService.asyncMethod(this.model, "loadString", _paras_loadString, new DoService.EventCallBack() {
			@Override
			public void eventCallBack(String _data) {// 回调函数
				DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
			}
		});
	}

	@Override
	protected void onEvent() {
		// 系统事件订阅
		DoService.subscribeEvent(this.model, "loaded", new DoService.EventCallBack() {
			@Override
			public void eventCallBack(String _data) {
				DoServiceContainer.getLogEngine().writeDebug("系统事件回调：name = loaded, data = " + _data);
				Toast.makeText(WebViewSampleTestActivty.this, "系统事件回调：loaded", Toast.LENGTH_LONG).show();
			}
		});
		// 自定义事件订阅
		DoService.subscribeEvent(this.model, "_messageName", new DoService.EventCallBack() {
			@Override
			public void eventCallBack(String _data) {
				DoServiceContainer.getLogEngine().writeDebug("自定义事件回调：name = _messageName, data = " + _data);
				Toast.makeText(WebViewSampleTestActivty.this, "自定义事件回调：_messageName", Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void doTestFireEvent(View view) {
		// fire 自定义事件
		DoInvokeResult invokeResult = new DoInvokeResult(this.model.getUniqueKey());
		this.model.getEventCenter().fireEvent("_messageName", invokeResult);
	}

}
