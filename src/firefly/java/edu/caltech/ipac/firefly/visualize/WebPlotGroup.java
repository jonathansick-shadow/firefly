/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
package edu.caltech.ipac.firefly.visualize;


import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import edu.caltech.ipac.firefly.util.Dimension;
import edu.caltech.ipac.firefly.util.WebAssert;
import edu.caltech.ipac.firefly.util.event.Name;
import edu.caltech.ipac.firefly.util.event.WebEvent;
import edu.caltech.ipac.firefly.visualize.task.VisTask;
import edu.caltech.ipac.firefly.visualize.task.ZoomTask;
import edu.caltech.ipac.visualize.plot.ImageWorkSpacePt;

import java.util.List;

/**
 * This class manages groups of plots.  This is primarily necessary for overlays.
 * There is a base plot that the overlays are projected onto.  A PlotGroup is a
 * plot and all its overlays.
 */
public class WebPlotGroup  {

    private Dimension _imageSize= new Dimension(0,0); // image size of the mosaic of images
    private Dimension _screenSize= new Dimension(0,0); // screen size of the mosaic of images

    public static final int ZOOM_WAIT_MS= 2000; // 2 seconds

            // keep the mosaic image min/max
    private int _minX;     // image min x
    private int _maxX;     // image max x
    private int _minY;     // image min y
    private int _maxY;     // image max y

    private float _zLevel= 1.0F;

    private final WebPlot   _basePlot;
    private final ZoomTimer _zoomTimer= new ZoomTimer();
    private WebPlotView     _plotView;
    private ZoomTask        _activeZoomTask;


    public WebPlotGroup(WebPlot basePlot,float initialZoomLevel) {
       _basePlot= basePlot;
       _zLevel= initialZoomLevel;
    }

    /**
     * Get the PlotView.
     * A PlotGroup contains a reference to the PlotView that contains it.
     * A PlotGroup may be in only one PlotView.
     * @return PlotView the PlotView this plot is in.
     */
    public WebPlotView getPlotView() { return _plotView; }

    /**
     * Return the base plot for the plot group.  The base plot is the plot that
     * defines the projection all other plot are reprojected to.
     * @return a Plot
     */
    public WebPlot getBasePlot() { return _basePlot; }
    

    /**
     * Remove the PlotView.
     * @param pv the PlotView to remove
     */
    public void removePlotView(WebPlotView pv) {
       WebAssert.tst(_plotView == pv);
       _plotView= null;
    }


    public Dimension getScreenSize() { return _screenSize;  }
    public Dimension getImageSize()  { return _imageSize;   }

    public int getGroupImageXMin()   { return _minX; }
    public int getGroupImageXMax()   { return _maxX; }
    public int getGroupImageYMin()   { return _minY; }
    public int getGroupImageYMax()   { return _maxY; }


    /**
     * called right before the listeners are called
     */

    /**
     * Determine if a image point is in the plot boundaries.
     * @param ipt image point to test
     * @return boolean true if it is in the boundaries, false if not.
     */
    public boolean pointInPlot( ImageWorkSpacePt ipt) {
        if (ipt==null) return false;
        double x= ipt.getX();
        double y= ipt.getY();
        return (x >= _minX && x <= _maxX && y >= _minY && y <= _maxY );
    }



    public void postZoom(PlotImages images) {
        _basePlot.refreshWidget(images);
        _activeZoomTask= null;
        fireReplotEvent(ReplotDetails.Reason.ZOOM_COMPLETED);
    }

    void refreshWidget(PlotImages images) {
        _basePlot.refreshWidget(images);
    }

    public boolean isZoomProcessing() { return _activeZoomTask!=null; }

    public float getZoomFact() { return _zLevel; }


  // ------------------------------------------------------------
  // ================= Package methods ==========================
  // ------------------------------------------------------------


    /**
     * set the WebPlotView obj
     * @param plotView the WebPlotView obj
     */
    void setPlotView(WebPlotView plotView) {
        _plotView= plotView;
        _basePlot.getTileDrawer().setPlotView(plotView);
    }

    /**
     * should only be called from WebPlotView
     * zoom to a certain level.  The value can be any positive number. 1.0 is original size,
     * 2.0 is double size, 0.5 is half size.  You can even do something such as 3.24.
     * @param level the zoom level
     * @param  isFullScreen a hint to the server about how to generate the image.  when true the server will not tile
     * but will generate one image
     */
    void activateDeferredZoom(final float level,
                              boolean isFullScreen,
                              boolean useDeferredDelay,
                              final List<WebPlot> overlayPlots) {
        if (_activeZoomTask!=null)  {
            _activeZoomTask.cancel();
            _activeZoomTask= null;
        }

        _zoomTimer.cancel();
        _zoomTimer.setupCall(level, isFullScreen, overlayPlots);
        _zoomTimer.schedule(useDeferredDelay ? ZOOM_WAIT_MS : 5);

        final float oldLevel= _zLevel;
        _zLevel= level;
        computeMinMax();

        if (overlayPlots!=null) {
            for (WebPlot p : overlayPlots) {
                p.getPlotGroup()._zLevel= level;
                p.getPlotGroup().computeMinMax();
            }
        }


        final PlotImages im= _basePlot.getTileDrawer().getImages();
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                _basePlot.getTileDrawer().scaleImagesIfMatch(oldLevel, level,im);
                if (overlayPlots!=null) {
                    for(WebPlot p : overlayPlots) {
                        PlotImages overIm= p.getTileDrawer().getImages();
                        p.getTileDrawer().scaleImagesIfMatch(oldLevel, level,overIm);
                    }
                }
            }
        });
        fireReplotEvent(ReplotDetails.Reason.ZOOM);
    }

    void cancelPendingZooms() {
        _zoomTimer.cancel();
    }



    /**
     * this method will coordinate between all the plots setting up
     * the transform and the width and height as new plots are added.
     * should this be merge with addPlot() ????
     */
     void computeMinMax() {
         _minX= 0;
         _maxX= _basePlot.getImageDataWidth();
         _minY= 0;
         _maxY= _basePlot.getImageDataHeight();

         int iw= Math.abs(_minX) + Math.abs(_maxX);
         int ih= Math.abs(_minY) + Math.abs(_maxY);
         _imageSize= new Dimension(iw,ih);

         int sw= (int)((Math.abs(_minX) + Math.abs(_maxX))  * _zLevel);
         int sh= (int)((Math.abs(_minY) + Math.abs(_maxY))  * _zLevel);
         _screenSize= new Dimension(sw,sh);
    }

    private void fireReplotEvent(ReplotDetails.Reason reason) {
        fireReplotEvent(reason,_basePlot, Band.NO_BAND);
    }


    static void fireReplotEvent(ReplotDetails.Reason reason, WebPlot plot) {
        fireReplotEvent(reason,plot, Band.NO_BAND);
    }

    static void fireReplotEvent(ReplotDetails.Reason reason, WebPlot plot, Band band) {
        if (plot==null) return;
        WebPlotView pv= plot.getPlotView();
        if (pv!=null && plot.isAlive()) {
            WebPlotGroup wpg= plot.getPlotGroup();
            ReplotDetails details= new ReplotDetails(wpg,plot,reason,band);
            WebEvent ev= new WebEvent(wpg, Name.REPLOT,details);
            pv.fireEvent(ev);
        }
    }

    private class ZoomTimer extends Timer {

        private float level;
        private boolean isFullScreen;
        private List<WebPlot> overlayPlots;

        public void run() { _activeZoomTask= VisTask.getInstance().zoom(WebPlotGroup.this, level,
                isFullScreen, overlayPlots); }

        public void setupCall(float level, boolean isFullScreen, List<WebPlot> overlayPlots) {
            this.level = level;
            this.isFullScreen = isFullScreen;
            this.overlayPlots= overlayPlots;
        }
    }



}
