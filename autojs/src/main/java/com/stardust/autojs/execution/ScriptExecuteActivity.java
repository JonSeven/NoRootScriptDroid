package com.stardust.autojs.execution;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.stardust.autojs.R;
import com.stardust.autojs.ScriptEngineService;
import com.stardust.autojs.engine.ScriptEngine;
import com.stardust.autojs.runtime.ScriptRuntime;
import com.stardust.autojs.script.ScriptSource;

/**
 * Created by Stardust on 2017/2/5.
 */

public class ScriptExecuteActivity extends AppCompatActivity {


    private static ActivityScriptExecution execution;
    private Object mResult;
    private ScriptEngine mScriptEngine;
    private ScriptExecutionListener mExecutionListener;
    private ScriptSource mScriptSource;

    public static ActivityScriptExecution execute(Context context, ScriptEngineService service, ScriptExecutionTask task) {
        if (execution != null) {
            return null;
        }

        execution = new ActivityScriptExecution(service, task);
        context.startActivity(new Intent(context, ScriptExecuteActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        return execution;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (execution == null) {
            finish();
            return;
        }
        mScriptSource = execution.getSource();
        mScriptEngine = execution.getEngine();
        mExecutionListener = execution.getListener();
        runScript();
    }

    private void runScript() {
        try {
            prepare();
            doExecution();
        } catch (Exception e) {
            mExecutionListener.onException(execution, e);
            super.finish();
        }
    }

    private void doExecution() {
        mScriptEngine.setTag("script", mScriptSource);
        mExecutionListener.onStart(execution);
        mResult = mScriptEngine.execute(mScriptSource);
    }

    private void prepare() {
        mScriptEngine.put("activity", this);
        mScriptEngine.put("__runtime__", execution.getRuntime());
        mScriptEngine.setTag("__require_path__", execution.getConfig().getRequirePath());
        mScriptEngine.init();
    }

    @Override
    public void finish() {
        mExecutionListener.onSuccess(execution, mResult);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScriptEngine.put("activity", null);
        mScriptEngine.destroy();
        execution = null;
    }

    private static class ActivityScriptExecution extends ScriptExecution.AbstractScriptExecution {

        private ScriptEngine mScriptEngine;
        private ScriptRuntime mScriptRuntime;
        private ScriptEngineService mScriptEngineService;

        ActivityScriptExecution(ScriptEngineService service, ScriptExecutionTask task) {
            super(task);
            mScriptEngineService = service;
        }

        @Override
        public ScriptEngine getEngine() {
            if (mScriptEngine == null) {
                mScriptEngine = mScriptEngineService.createScriptEngine();
            }
            return mScriptEngine;
        }

        @Override
        public ScriptRuntime getRuntime() {
            if (mScriptRuntime == null) {
                mScriptRuntime = mScriptEngineService.createScriptRuntime();
            }
            return mScriptRuntime;
        }

    }


}
