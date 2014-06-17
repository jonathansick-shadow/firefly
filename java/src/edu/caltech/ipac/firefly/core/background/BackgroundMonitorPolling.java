package edu.caltech.ipac.firefly.core.background;

import com.google.gwt.storage.client.StorageEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.caltech.ipac.firefly.rpc.SearchServices;
import edu.caltech.ipac.firefly.rpc.SearchServicesAsync;
import edu.caltech.ipac.firefly.ui.background.BackgroundTask;
import edu.caltech.ipac.firefly.util.BrowserCache;
import edu.caltech.ipac.firefly.util.WebAssert;
import edu.caltech.ipac.firefly.util.event.Name;
import edu.caltech.ipac.firefly.util.event.WebEvent;
import edu.caltech.ipac.firefly.util.event.WebEventManager;
import edu.caltech.ipac.util.ComparisonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * User: roby
 * Date: Oct 27, 2008
 * Time: 4:09:07 PM
 */


/**
 * @author Trey Roby
 */
public class BackgroundMonitorPolling implements BackgroundMonitor {


    private static final String STATE_KEY = "PackagerControlKeys";
    private static final long TWO_WEEKS_IN_SECS= 60L * 60L  * 24L * 14L ;   //  1 hour * 24 hours * 14 days = 2 weeks
    private final Map<String, Monitor> _monitorMap = new HashMap<String, Monitor>();
    private final Set<String> _deletedItems= new HashSet<String>(10);
    private boolean mustReadFromCache = true;
    public static final int WAITS[]=
                                    {1,1,1,2,2,2,2,5,5,5,5,
                                     10,4,10,10,6,10,4,10,6,
                                     10,4,10,10,6,10,4,10,10};

//======================================================================
//----------------------- Constructors ---------------------------------
//======================================================================

    public BackgroundMonitorPolling() {
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                if (BrowserCache.isPerm()) {
                    BrowserCache.addHandlerForKey(STATE_KEY, new StorageEvent.Handler() {
                        public void onStorageChange(StorageEvent ev) {
                            syncWithCache();
                        }
                    });
                }
            }
        });

    }

    public int getCount() { return _monitorMap.size(); }

    public void addItem(MonitorItem item) {
        Monitor monitor= new Monitor(item);
        _monitorMap.put(item.getID(),monitor);
        monitor.startMonitoring();
        syncWithCache();
    }

    public void removeItem(MonitorItem item) {
        Monitor monitor= _monitorMap.get(item.getID());
        if (monitor!=null) {
            _monitorMap.remove(item.getID());
            _deletedItems.add(item.getID());
            monitor.stop();
        }

        syncWithCache();
        WebEvent<MonitorItem> ev= new WebEvent<MonitorItem>(this, Name.MONITOR_ITEM_REMOVED, item);
        WebEventManager.getAppEvManager().fireEvent(ev);
    }

    public MonitorItem getItem(String id) {
        MonitorItem retval= null;
        Monitor wrapper= _monitorMap.get(id);
        if (wrapper!=null) retval= wrapper.getMonitorItem();
        return retval;
    }

    public void setStatus(BackgroundStatus bgStat) {
        Monitor m= _monitorMap.get(bgStat.getID());
        if (m!=null) m.updateStatus(bgStat);
    }

    public void forceUpdates() {
        for(Monitor m :  _monitorMap.values()) m.forceMonitoring();
    }

    public boolean isDeleted(String id) { return _deletedItems.contains(id); }
    public boolean isMonitored(String id) { return _monitorMap.containsKey(id); }



    public void pollAll() {
        for(Monitor mon : _monitorMap.values()) {
            mon.check();
        }
    }


//======================================================================
//------------------ StatefulWidget Methods ----------------------------
//======================================================================


    public void syncWithCache() {
        if (mustReadFromCache)  {
            String serState= BrowserCache.get(STATE_KEY);
            if (serState!=null)  MonitorRecoveryFunctions.deserializeAndLoadMonitor(this, serState);
            mustReadFromCache= false;
        }

        List<MonitorItem> itemList= new ArrayList<MonitorItem>(_monitorMap.size());
        for(Monitor m : _monitorMap.values())  itemList.add(m.getMonitorItem());
        BrowserCache.put(STATE_KEY, MonitorRecoveryFunctions.serializeMonitorList(itemList), TWO_WEEKS_IN_SECS);
    }



//======================================================================
//------------------ Inner Classes -------------------------------------
//======================================================================

    public static class Monitor extends Timer implements CanCancel {
        private final MonitorItem _monItem;
        private int _waitIdx= 0;
        private boolean _enabled= true;

        private Monitor(MonitorItem item) {
            _monItem= item;
            if (_monItem.getCanceller()==null) {
                _monItem.setCanceller(this);
            }
        }

        private void startMonitoring() {
            scheduleCheck(false);
        }

        private void forceMonitoring() {
            scheduleCheck(true);
        }

        private MonitorItem getMonitorItem() { return _monItem;}

        public void run() {
            if (_enabled) {
                check();
            }
        }

        public void check() {
            if (_monItem.isComposite()) {
                for(MonitorItem mi : _monItem.getCompositeList()) {
                    checkStatus(mi.getStatus());
                }
            }
            else {
                checkStatus(_monItem.getStatus());
            }
        }


        private void checkStatus(final BackgroundStatus bgStat ) {

            SearchServicesAsync dserv= SearchServices.App.getInstance();
            dserv.getStatus(bgStat.getID(), new AsyncCallback<BackgroundStatus>() {
                public void onFailure(Throwable caught) {
                    updateStatus(bgStat.cloneWithState(BackgroundState.FAIL));
                }

                public void onSuccess(BackgroundStatus bgStat) {
                    updateStatus(bgStat);
                }
            });
        }


        public void updateStatus(BackgroundStatus bgStat) {
            if (_monItem.isComposite()) {
                boolean found= false;
                for(MonitorItem mi : _monItem.getCompositeList()) {
                    if (ComparisonUtil.equals(mi.getID(), bgStat.getID())) {
                        found= true;
                        mi.setStatus(bgStat);
                        CompositeJob cR= _monItem.getCompositeJob();
                        CompositeJob newComposite= cR.makeDeltaJob(bgStat);
                        _monItem.setCompositeJob(newComposite);
                        break;
                    }
                }
                if (!found) {
                    WebAssert.tst(false,
                                  "id could not be found in composite monitor item, id: " + bgStat.getID());

                }
            }
            else {
                WebAssert.tst(ComparisonUtil.equals(_monItem.getID(), bgStat.getID()),
                              "id not same on update: old id: " + _monItem.getID() +
                                      " new id: " + bgStat.getID());
                _monItem.setStatus(bgStat);
            }

            scheduleCheck(false);
        }


        private void cancelDownload() {

            cancel();
            if (_monItem.isComposite()) {
                for(MonitorItem mi : _monItem.getCompositeList()) {
                    BackgroundTask.cancel(mi.getID());
                }
            }
            else {
                BackgroundTask.cancel(_monItem.getID());
            }
            BackgroundStatus bgStat= _monItem.getStatus();
            bgStat= bgStat.cloneWithState(BackgroundState.USER_ABORTED);
            _monItem.setStatus(bgStat);
        }


        private void scheduleCheck(boolean force) {
            cancel();
            if (force || !_monItem.isDone()) {
                schedule( WAITS[_waitIdx] * 1000);
                if (_waitIdx<WAITS.length-1) _waitIdx++;
            }
        }

        public void cancelTask() { cancelDownload(); }

        public void stop() {
            _enabled= false;
            this.cancel();
        }
    }


}

/*
 * THIS SOFTWARE AND ANY RELATED MATERIALS WERE CREATED BY THE CALIFORNIA 
 * INSTITUTE OF TECHNOLOGY (CALTECH) UNDER A U.S. GOVERNMENT CONTRACT WITH 
 * THE NATIONAL AERONAUTICS AND SPACE ADMINISTRATION (NASA). THE SOFTWARE 
 * IS TECHNOLOGY AND SOFTWARE PUBLICLY AVAILABLE UNDER U.S. EXPORT LAWS 
 * AND IS PROVIDED AS-IS TO THE RECIPIENT WITHOUT WARRANTY OF ANY KIND, 
 * INCLUDING ANY WARRANTIES OF PERFORMANCE OR MERCHANTABILITY OR FITNESS FOR 
 * A PARTICULAR USE OR PURPOSE (AS SET FORTH IN UNITED STATES UCC 2312- 2313) 
 * OR FOR ANY PURPOSE WHATSOEVER, FOR THE SOFTWARE AND RELATED MATERIALS, 
 * HOWEVER USED.
 * 
 * IN NO EVENT SHALL CALTECH, ITS JET PROPULSION LABORATORY, OR NASA BE LIABLE 
 * FOR ANY DAMAGES AND/OR COSTS, INCLUDING, BUT NOT LIMITED TO, INCIDENTAL 
 * OR CONSEQUENTIAL DAMAGES OF ANY KIND, INCLUDING ECONOMIC DAMAGE OR INJURY TO 
 * PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER CALTECH, JPL, OR NASA BE 
 * ADVISED, HAVE REASON TO KNOW, OR, IN FACT, SHALL KNOW OF THE POSSIBILITY.
 * 
 * RECIPIENT BEARS ALL RISK RELATING TO QUALITY AND PERFORMANCE OF THE SOFTWARE 
 * AND ANY RELATED MATERIALS, AND AGREES TO INDEMNIFY CALTECH AND NASA FOR 
 * ALL THIRD-PARTY CLAIMS RESULTING FROM THE ACTIONS OF RECIPIENT IN THE USE 
 * OF THE SOFTWARE. 
 */
