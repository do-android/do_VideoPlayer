package doext.define;

import org.json.JSONObject;

import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;

/**
 * 声明自定义扩展组件方法
 */
public interface do_VideoPlayer_IMethod {
	void play(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception;
}