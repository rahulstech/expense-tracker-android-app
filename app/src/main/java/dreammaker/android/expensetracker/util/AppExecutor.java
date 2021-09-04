package dreammaker.android.expensetracker.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutor {
    private static ExecutorService diskOperationsExecutor = null;
    private static MainThreadExecutor mainThreadExecutor;

    public static ExecutorService getDiskOperationsExecutor(){
        if(null == diskOperationsExecutor){
            diskOperationsExecutor = Executors.newFixedThreadPool(3);
        }
        return diskOperationsExecutor;
    }

    public static Executor getMainThreadExecutor(){
        if(null == mainThreadExecutor){
            mainThreadExecutor = new MainThreadExecutor();
        }
        return mainThreadExecutor;
    }
    
    private static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());
        
        @Override
        public void execute(Runnable task)
        {
            handler.post(task);
        }
    }
}
