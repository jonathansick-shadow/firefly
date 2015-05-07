/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
package edu.caltech.ipac.firefly.server.vispush;
/**
 * User: roby
 * Date: 1/27/15
 * Time: 1:04 PM
 */


import edu.caltech.ipac.firefly.core.background.BackgroundState;
import edu.caltech.ipac.firefly.core.background.BackgroundStatus;
import edu.caltech.ipac.firefly.data.ServerEvent;
import edu.caltech.ipac.firefly.data.ServerParams;
import edu.caltech.ipac.firefly.data.ServerRequest;
import edu.caltech.ipac.firefly.server.ServerContext;
import edu.caltech.ipac.firefly.server.packagedata.BackgroundInfoCacher;
import edu.caltech.ipac.firefly.server.query.BackgroundEnv;
import edu.caltech.ipac.firefly.server.events.ServerEventManager;
import edu.caltech.ipac.firefly.server.events.ServerEventQueue;
import edu.caltech.ipac.firefly.util.event.Name;
import edu.caltech.ipac.firefly.visualize.WebPlotRequest;

import java.util.concurrent.TimeUnit;

/**
 * @author Trey Roby
 */
public class VisPushJob {

//    private static final Map<String,VisPushJob> jobs= new HashMap<String, VisPushJob>(23);

//    public static VisPushJob getJob(String id) { return jobs.get(id); }

//    public static String makeNewJob(String bid) {
//        BackgroundInfoCacher pi= new BackgroundInfoCacher(bid);
//        BackgroundStatus bgStat= pi.getStatus();
//        String retval= bid;
//        if (bgStat==null) {
//            PushWorker worker= new PushWorker();
//            BackgroundEnv.BackgroundProcessor processor=
//                    new BackgroundEnv.BackgroundProcessor(worker, "Push Job", "from push",
//                                                          ServerContext.getRequestOwner(),bid,
//                                                          new ServerEvent.EventTarget(ServerEvent.Scope.CHANNEL));
//            retval= BackgroundEnv.backgroundProcess(1, processor).getID();
//        }
//        return retval;
//    }

    public static boolean pushFits(WebPlotRequest wpr, ServerEvent.EventTarget evtTarget) {
        ServerEvent sevt = new ServerEvent(Name.PUSH_WEB_PLOT_REQUEST, evtTarget,
                    ServerEvent.DataType.STRING, wpr.toString(), ServerContext.getRequestOwner().getEventConnID());
        ServerEventManager.fireEvent(sevt);
        return true;
    }

    public static boolean pushRegion(String fileName, ServerEvent.EventTarget evtTarget) {
        ServerEvent sevt = new ServerEvent(Name.PUSH_REGION_FILE, evtTarget,
                ServerEvent.DataType.STRING, fileName, ServerContext.getRequestOwner().getEventConnID());
        ServerEventManager.fireEvent(sevt);
        return true;
    }

    public static boolean pushExtension(String sreqId,
                                        String plotId,
                                        String extType,
                                        String title,
                                        String image,
                                        String toolTip,
                                        ServerEvent.EventTarget evtTarget) {
        ServerRequest r= new ServerRequest(sreqId);
        r.setParam(ServerParams.EXT_TYPE, extType);
        r.setParam(ServerParams.TITLE, title);
        r.setParam(ServerParams.PLOT_ID, plotId);
        if (image!= null) r.setParam(ServerParams.IMAGE, image);
        if (toolTip!= null) r.setParam(ServerParams.IMAGE, toolTip);

        ServerEvent sevt = new ServerEvent(Name.PUSH_FITS_COMMAND_EXT, evtTarget,
                ServerEvent.DataType.STRING, r.toString(), ServerContext.getRequestOwner().getEventConnID());
        ServerEventManager.fireEvent(sevt);
        return true;
    }

    public static boolean pushTable(String fileName, ServerEvent.EventTarget evtTarget) {
        ServerEvent sevt = new ServerEvent(Name.PUSH_TABLE_FILE, evtTarget,
                ServerEvent.DataType.STRING, fileName, ServerContext.getRequestOwner().getEventConnID());
        ServerEventManager.fireEvent(sevt);
        return true;
    }

//    public static UserResponse queryAction(String id) {
//        BackgroundInfoCacher pi= new BackgroundInfoCacher(id);
//        BackgroundStatus bgStat= pi.getStatus();
//        String retData= null;
//        String retDesc= null;
//        if (bgStat!=null) {
//            bgStat.incRequestCnt();
//            pi.setStatus(bgStat);
//
//            String name= "wait-for-input-" + id;
////            WaitForData worker= new WaitForData(id);
//            WaitForDataEXP worker= new WaitForDataEXP(id);
//            Thread thread= new Thread(worker, name);
//
//            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//                public void uncaughtException(Thread t, Throwable e) {
//                    // do something
//                }
//            });
//
//            thread.setDaemon(true);
//            thread.start();
//
//            try {
//                thread.join(120000);
//                worker.quit();
//                retData= worker.getResult();
//                retDesc= worker.getDesc();
//            } catch (InterruptedException e) {
//                retData= null;
//
//            }
//            bgStat.decRequestCnt();
//            pi.setStatus(bgStat);
//        }
//        return new UserResponse(retData,retDesc);
//    }
//
//
//
//    private static class PushWorker implements BackgroundEnv.Worker {
//
//        public PushWorker() {
//        }
//
//        public BackgroundStatus work(BackgroundEnv.BackgroundProcessor p)  throws Exception {
//            BackgroundStatus bgStat= new BackgroundStatus(p.getBID(), BackgroundStatus.BgType.PERSISTENT,
//                                                          BackgroundState.WAITING);
//            return bgStat;
//        }
//    }
//
//
//    public static class UserResponse {
//        private final String data;
//        private final String desc;
//
//        public UserResponse(String data, String desc) {
//            this.data = data;
//            this.desc = desc;
//        }
//
//        public String getData() {
//            return data;
//        }
//
//        public String getDesc() {
//            return desc;
//        }
//    }
//
//
//    private static class WaitForData implements Runnable {
//
//        private volatile boolean check= true;
//        private volatile String result= null;
//        private volatile String desc= null;
//        private final String id;
//
//
//        public WaitForData(String id) {
//            this.id= id;
//        }
//
//
//        public void run() {
//            BackgroundInfoCacher pi = new BackgroundInfoCacher(id);
//            for (int i = 0; (i < 300 && check); i++) {
//                try {
//                    BackgroundStatus bgStat = pi.getStatus();
//
//                    int max = bgStat.getNumResponseData();
//                    String data = null;
//                    String desc = null;
//                    int j;
//                    for (j = 0; (j < max); j++) {
//                        desc = bgStat.getResponseDesc(j);
//                        data = bgStat.getResponseData(j);
//                        if (data != null) break;
//                    }
//                    bgStat.removeParam(BackgroundStatus.USER_RESPONSE + j);
//                    pi.setStatus(bgStat);
//
//                    if (data != null) {
//                        this.result = data;
//                        this.desc= desc;
//                        check = false;
//                    } else {
//                        TimeUnit.SECONDS.sleep(1);
//                    }
//                } catch (InterruptedException e) {
//                    check = false;
//                }
//            }
//        }
//
//        public void quit() {
//            check= false;
//        }
//
//        public String getResult() {
//            return result;
//        }
//        public String getDesc() {
//            return desc;
//        }
//    }
//
//
//
//    private static class WaitForDataEXP implements Runnable {
//
//        private volatile String result= null;
//        private volatile String desc= null;
//        private final String id;
//        private volatile Thread thread;
//        private volatile ServerEventQueue queue;
//        private volatile boolean threadIsDone= false;
//
//
//        public WaitForDataEXP(String id) {
//            this.id= id;
//        }
//
//
//        public void run() {
//            thread= Thread.currentThread();
//            queue= new ServerEventQueue(ServerEvent.SERVER_CONN_ID, null, new ServerEventQueue.EventConnector() {
//                @Override
//                public void send(String message) {
//                    getResponse(message);
//                }
//
//                @Override
//                public boolean isOpen() {
//                    return true;
//                }
//
//                @Override
//                public void close() {}
//            });
//            ServerEventManager.addEventQueue(queue);
//
//            try {
//                TimeUnit.SECONDS.sleep(300);
//            } catch (InterruptedException e) {
//                // do nothing
//            }
//        }
//
//        public void quit() {
//            threadIsDone= true;
//            this.thread.interrupt();
//        }
//
//        private void getResponse(String message) {
//            BackgroundInfoCacher pi = new BackgroundInfoCacher(id);
//            BackgroundStatus bgStat = pi.getStatus();
//            if (threadIsDone) return;
//
//            int max = bgStat.getNumResponseData();
//            String data = null;
//            String desc = null;
//            int j;
//            for (j = 0; (j < max); j++) {
//                desc = bgStat.getResponseDesc(j);
//                data = bgStat.getResponseData(j);
//                if (data != null) break;
//            }
//            bgStat.removeParam(BackgroundStatus.USER_RESPONSE + j);
//            pi.setStatus(bgStat);
//
//            if (data != null) {
//                this.result = data;
//                this.desc= desc;
//            }
//            ServerEventManager.removeEventQueue(queue);
//            this.thread.interrupt();
//        }
//
//        public String getResult() {
//            return result;
//        }
//        public String getDesc() {
//            return desc;
//        }
//    }
}
