/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
package edu.caltech.ipac.firefly.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.caltech.ipac.firefly.visualize.Band;
import edu.caltech.ipac.firefly.visualize.FileAndHeaderInfo;
import edu.caltech.ipac.firefly.visualize.PlotState;
import edu.caltech.ipac.firefly.visualize.StretchData;
import edu.caltech.ipac.firefly.visualize.WebPlotRequest;
import edu.caltech.ipac.firefly.visualize.WebPlotResult;
import edu.caltech.ipac.firefly.visualize.draw.StaticDrawInfo;
import edu.caltech.ipac.visualize.plot.ImagePt;

import java.util.ArrayList;
import java.util.List;

/**
 * User: roby
 * Date: Feb 25, 2008
 * Time: 1:22:01 PM
 */
public interface PlotServiceAsync {


//    public void getWebPlotBatch(WebPlotRequest request[],
//                                AsyncCallback<PlotCreationResult[]> async );

    public void getWebPlot(WebPlotRequest request,
                           AsyncCallback<WebPlotResult> async );

    public void getWebPlotGroup(List<WebPlotRequest> requestList,
                                String progressKey,
                               AsyncCallback<WebPlotResult[]> async);

    public void getOneFileGroup(List<WebPlotRequest> requestList,
                                String progressKey,
                                AsyncCallback<WebPlotResult[]> async);



    public void getWebPlot(WebPlotRequest redRequest,
                           WebPlotRequest greenRequest,
                           WebPlotRequest blueRequest,
                           AsyncCallback<WebPlotResult> async);

    public void getTableData(WebPlotRequest request,
                             AsyncCallback<WebPlotResult> async);


    public void deleteColorBand(PlotState state,
                                Band band,
                                AsyncCallback<WebPlotResult> async);

    public void addColorBand(PlotState      state,
                             WebPlotRequest bandRequest,
                             Band band,
                             AsyncCallback<WebPlotResult> async);


    public void getFileFlux(FileAndHeaderInfo fileAndHeader[],
                            ImagePt inIpt,
                            AsyncCallback<String[]> async);

    public void getFlux(PlotState request,
                        ImagePt inIpt,
                        AsyncCallback<WebPlotResult> async);
    public void setZoomLevel(PlotState state[],
                             float level,
                             boolean isFullScreen,
                             AsyncCallback<WebPlotResult> async);
    public void deletePlot(String ctxStr,
                           AsyncCallback<Boolean> async);
                                                                                            
    public void recomputeStretch(PlotState request,
                                 StretchData[] stretchData,
                                 AsyncCallback<WebPlotResult> async);
    public void crop(PlotState stateAry[],
                     ImagePt corner1,
                     ImagePt corner2,
                     boolean cropMultiAll,
                     AsyncCallback<WebPlotResult> async);

    public void rotateNorth(PlotState state[], boolean north, float newZoomLevel, AsyncCallback<WebPlotResult> async);
    public void rotateToAngle(PlotState state[],
                              boolean rotate,
                              double angle,
                              float newZoomLevel,
                              AsyncCallback<WebPlotResult> async);

    public void flipImageOnY(PlotState state[], AsyncCallback<WebPlotResult> async);

    public void changeColor(PlotState request,
                            int colorTableId,
                            AsyncCallback<WebPlotResult> async);


    public void getFitsHeaderInfo(PlotState request, AsyncCallback<WebPlotResult> async);

    public void getAreaStatistics(PlotState request, ImagePt pt1, ImagePt pt2, ImagePt pt3, ImagePt pt4, AsyncCallback<WebPlotResult> async);

    public void getColorHistogram(PlotState request,
                                  Band      band,
                                  int       width,
                                  int       height,
                                  AsyncCallback<WebPlotResult> async);

    public void getImagePng(PlotState state,
                            ArrayList<StaticDrawInfo> drawInfoList,
                            AsyncCallback<WebPlotResult> async);

    public void checkPlotProgress(String progressKey, AsyncCallback<WebPlotResult> async);

    public void getDS9Region(String key, AsyncCallback<WebPlotResult> async);
    public void saveDS9RegionFile(String regionData, AsyncCallback<WebPlotResult> async);


    public void addSavedRequest(String saveKey, WebPlotRequest request, AsyncCallback<Boolean> async);
    public void getAllSavedRequest(String saveKey, AsyncCallback<WebPlotResult> async);

}

