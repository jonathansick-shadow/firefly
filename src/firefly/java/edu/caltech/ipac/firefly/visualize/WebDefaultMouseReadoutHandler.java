/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
package edu.caltech.ipac.firefly.visualize;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.caltech.ipac.astro.CoordException;
import edu.caltech.ipac.firefly.util.WebAssert;
import edu.caltech.ipac.firefly.visualize.conv.CoordUtil;
import edu.caltech.ipac.util.StringUtils;
import edu.caltech.ipac.visualize.plot.CoordinateSys;
import edu.caltech.ipac.visualize.plot.ImagePt;
import edu.caltech.ipac.visualize.plot.Pt;
import edu.caltech.ipac.visualize.plot.WorldPt;
import edu.caltech.ipac.visualize.plot.projection.Projection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: roby
 * Date: Apr 25, 2007
 * Time: 12:34:33 PM
 */


/**
 * @author Trey Roby
 */
public class WebDefaultMouseReadoutHandler implements WebMouseReadoutHandler {

    public static final String FILE_PIXEL_SIZE = "Pixel Size";
    public static final String SCREEN_PIXEL_SIZE = "Screen Pixel Size";
    //row parameter keys
    public static final String TITLE= "TITLE";
    public static final String EQ_J2000= "EQ_J2000";
    public static final String EQ_J2000_DEG = "EQ_J2000_DEG";
    public static final String IMAGE_PIXEL = "IMAGE_PIXEL";
    public static final String GALACTIC= "GALACTIC";
    public static final String EQ_B1950= "EQ_B1950";


    public static final String EQ_J2000_DESC= "EQ J2000 HMS";
    public static final String EQ_J2000_DEG_DESC = "EQ J2000 decimal";
    public static final String IMAGE_PIXEL_DESC = "FITS Image Pixel";
    public static final String GALACTIC_DESC= "Galactic";
    public static final String EQ_B1950_DESC= "EQ B1950";


    public static final Result EMPTY= new Result("","");
    //public static final String FIRST_FLUX= "FIRST_FLUX";
    //public static final String PIXEL_SIZE= "PIXEL_SIZE";

    public static final List<Integer> ROW_WITH_OPTIONS= Arrays.asList(0,1,2);
    private static final List<String> rowOps= Arrays.asList(EQ_J2000_DESC,
                                                            EQ_J2000_DEG_DESC,
                                                            GALACTIC_DESC,
                                                            EQ_B1950_DESC,
                                                            IMAGE_PIXEL_DESC);

    private static final List<String> scaleRowOps= Arrays.asList(FILE_PIXEL_SIZE, SCREEN_PIXEL_SIZE);

    private static final int MAX_TITLE_LEN= 25;


    private static final int MAX_FLUXES= 100;
    private static final int MOUSE_DELAY_MS= 200;
    private static final NumberFormat _nfExp= NumberFormat.getScientificFormat();
    private static final NumberFormat _nfExpFlux= NumberFormat.getFormat("#.######E0");
    private static final NumberFormat _nf   = NumberFormat.getFormat("#.######");
    private static final NumberFormat _nfPix   = NumberFormat.getFormat("#.####");
//    private static final int BASE_ROWS = 6;
    private static final int NEW_BASE_ROWS = 4;
    private static final int MINIMAL_BASE_ROWS = 3;


//    private static final int FIRST_FLUX_ROW = 6;
    private static final int NEW_FIRST_FLUX_ROW = 4;
    private static final int PIXEL_SIZE_OFFSET = 1;
    private static HashMap<Integer, String > DEFAULT_ROW_PARAMS= makeDefaultRowParams();
    private static HashMap<Integer, String > MINIMAL_ROW_PARAMS= makeMinimalRowParams();
    public enum ReadoutMode {HMS, DECIMAL }
    public enum WhichReadout {LEFT, RIGHT }
    public enum WhichDir {LON, LAT, BOTH}

    private ReadoutMode _leftMode= ReadoutMode.HMS;
    private CoordinateSys _leftCoordSys= CoordinateSys.EQ_J2000;

    private ReadoutMode _rightMode= ReadoutMode.HMS;
//    private CoordinateSys _rightCoordSys= CoordinateSys. EQ_J2000;
    private CoordinateSys _rightCoordSys= CoordinateSys.PIXEL;

    private FluxTimer _fluxTimer= new FluxTimer();
    private List<FluxCache> _savedFluxes= new ArrayList<FluxCache>(10);

    public static final Result NO_RESULT=  new Result("","");
    private boolean _attempingCtxUpdate= false;
    private WebPlot _lastPlot= null;
    private long _lastCallID= 0;
    private int _lastFluxRow;

    private HashMap<Integer, String > _rowParams = null;

//======================================================================
//----------------------- Constructors ---------------------------------
//======================================================================

    public WebDefaultMouseReadoutHandler() {
        this(DEFAULT_ROW_PARAMS);
    }

    public WebDefaultMouseReadoutHandler(HashMap<Integer, String> rowParams) {
        setReadOutRowParams(rowParams);
    }

//======================================================================
//----------------------- Public Methods -------------------------------
//======================================================================
    public void setReadOutRowParams(HashMap<Integer, String> rowParams) {
        _rowParams = rowParams;
    }

    public void useDefaultReadOutRowRParams() {
        setReadOutRowParams(DEFAULT_ROW_PARAMS);
    }
    
    public void setMode(WhichReadout which,
                        ReadoutMode mode) {
        if (which==WhichReadout.LEFT ) {
            _leftMode= mode;
        }
        else if (which==WhichReadout.RIGHT ) {
            _rightMode= mode;
        }
        else {
            WebAssert.tst(false);
        }
    }

    public ReadoutMode getMode(WhichReadout which) {
        return (which==WhichReadout.LEFT ) ? _leftMode : _rightMode;
    }

    public void setCoordSystem(WhichReadout which,
                               CoordinateSys coordSys) {
        if (which==WhichReadout.LEFT ) {
            _leftCoordSys= coordSys;
        }
        else if (which==WhichReadout.RIGHT ) {
            _rightCoordSys= coordSys;
        }
        else {
            WebAssert.tst(false);
        }
    }

    public CoordinateSys getCoordSystem(WhichReadout which) {
        return (which==WhichReadout.LEFT ) ? _leftCoordSys : _rightCoordSys;
    }

//=======================================================================
//-------------- Method from MouseReadoutHandler Interface --------------
//=======================================================================

    public int getRows(WebPlot plot) {
        int retval= NEW_BASE_ROWS;
        if (plot!=null) {
            if (WebMouseReadout.isMinimal(plot)) {
                retval= MINIMAL_BASE_ROWS;
            }
            else {
                int bands= plot.getBands().length;
                _lastFluxRow= NEW_FIRST_FLUX_ROW+bands-1;
                retval= NEW_BASE_ROWS +bands-1;
            }
        }
        return retval;
    }





    public void computeMouseValue(WebPlot plot,
                                    Readout readout,
                                    int row,
                                    ImagePt ipt,
                                    ScreenPt screenPt,
                                    long callID) {
        Result retval= null;
        checkPlotChange(plot);


        HashMap<Integer, String> activeParams = _rowParams;


        useDefaultReadOutRowRParams();
        if (plot.getPlotView() != null &&
                plot.getPlotView().containsAttributeKey(WebPlot.READOUT_ROW_PARAMS)) {
            Object o = plot.getPlotView().getAttribute(WebPlot.READOUT_ROW_PARAMS); 
            if (o!=null && o instanceof HashMap) 
                setReadOutRowParams((HashMap<Integer, String>)o);
        } else if (WebMouseReadout.isMinimal(plot)) {
            setReadOutRowParams(MINIMAL_ROW_PARAMS);
        }
        if (activeParams.containsKey(row)) {
            if (activeParams.get(row).equals(TITLE)) {
                showTitle(readout,VisUtil.getBestTitle(plot));
            }
            else if (activeParams.get(row).equals(EQ_J2000)) {
                Projection proj= plot.getProjection();
                if (!proj.isSpecified()) {
                    retval= new Result("Projection:", "none in image");
                }
                else if (!proj.isImplemented()) {
                    retval= new Result("Projection:", "not recognized");
                }
                else {
                    retval= getBoth1(plot, ipt, screenPt);
                }
            }
            else if (activeParams.get(row).equals(EQ_J2000_DEG)) {
                retval= getReadoutByImagePt(plot,
                                          ipt,
                                          screenPt,
                                          WhichDir.BOTH,
                                          ReadoutMode.DECIMAL,
                                          CoordinateSys.EQ_J2000);
            }
            else if (activeParams.get(row).equals(IMAGE_PIXEL)) {
                if (!plot.isBlankImage()) retval= getBoth2(plot, ipt, screenPt);
                else                      retval= EMPTY;
            }
            else if (activeParams.get(row).equals(GALACTIC)) {
                retval= getReadoutByImagePt(plot,
                                            ipt,
                                            screenPt,
                                            WhichDir.BOTH,
                                            ReadoutMode.DECIMAL,
                                            CoordinateSys.GALACTIC);
            }
            else if (activeParams.get(row).equals(EQ_B1950)) {
                retval= getReadoutByImagePt(plot,
                                            ipt,
                                            screenPt,
                                            WhichDir.BOTH,
                                            ReadoutMode.HMS,
                                            CoordinateSys.EQ_B1950);
            }
            else if (activeParams.get(row).equals(FILE_PIXEL_SIZE)) {
                retval= getPixelSize(plot, FILE_PIXEL_SIZE);
            }
            else if (activeParams.get(row).equals(SCREEN_PIXEL_SIZE)) {
                retval= getPixelSize(plot, SCREEN_PIXEL_SIZE);
            }
        } else if (row>= NEW_FIRST_FLUX_ROW && row<=_lastFluxRow) {
            if (!plot.isBlankImage()) {
                if (_lastCallID!=callID) {
                    _lastCallID= callID;
                    Result fluxRes[]= getFlux(plot, readout, ipt);
                    Band bands[]= plot.getBands();
                    int i= 0;
                    for(Result r : fluxRes) {
                        readout.setValue(getBandOffset(plot,bands[i++]),r._label,r._value,r._style);
                    }
                }
                retval=null;
            }
            else {
                retval= EMPTY;
            }
        }

        if (retval!=null) {
            readout.setValue(row,retval._label,retval._value);
        }
    }


    public void computeMouseExitValue(WebPlot plot, Readout readout, int row) {

        HashMap<Integer, String> activeParams = _rowParams;

        if (activeParams.containsKey(row)) {
            if (activeParams.get(row).equals(EQ_J2000)) {
                readout.setValue(row,CoordinateSys.EQ_J2000.getShortDesc() , "");
            }
            else if (activeParams.get(row).equals(EQ_J2000_DEG)) {
                readout.setValue(row,CoordinateSys.EQ_J2000.getShortDesc(), "");
            }
            else if (activeParams.get(row).equals(IMAGE_PIXEL)) {
                readout.setValue(row,CoordinateSys.PIXEL.getShortDesc(), "");
            }
            else if (activeParams.get(row).equals(GALACTIC)) {
                readout.setValue(row,CoordinateSys.GALACTIC.getShortDesc(), "");
            }
            else if (activeParams.get(row).equals(EQ_B1950)) {
                readout.setValue(row,CoordinateSys.EQ_B1950.getShortDesc(), "");
            }
            else if (activeParams.get(row).equals(FILE_PIXEL_SIZE)) {
                readout.setValue(row,FILE_PIXEL_SIZE+":", "");
            }
            else if (activeParams.get(row).equals(SCREEN_PIXEL_SIZE)) {
                readout.setValue(row,SCREEN_PIXEL_SIZE+":", "");
            }
            else {
                readout.setValue(row,"", "");
            }
        }
        else {
            readout.setValue(row,"", "");
        }
    }



    public List<Integer> getRowsWithOptions() { return ROW_WITH_OPTIONS; }
    public List<String> getRowOptions(int row) {
        List<String> retval= null;
        String which= _rowParams.get(row);
        if (which!=null) {
            if (scaleRowOps.contains(which)) {
                return scaleRowOps;
            }
            else if (rowOps.contains(convertConstToOp(which))) {
                return rowOps;
            }
        }
        return retval;
    }
    public void setRowOption(int row, String op) {
        String which= _rowParams.get(row);

        if (scaleRowOps.contains(which)) {
            if (_rowParams!=null) {
                _rowParams.put(row, op);
            }
        }
        else if (rowOps.contains(convertConstToOp(which))) {
            String opConst= convertOpToConst(op);
            if (opConst!=null) {
                if (_rowParams!=null) {
                    _rowParams.put(row, opConst);
                }
            }
        }
    }

    public String getRowOption(int row) {
        String which= _rowParams.get(row);
        String retval= null;
        if (scaleRowOps.contains(which)) {
            retval= _rowParams.get(row);
        }
        else if (rowOps.contains(convertConstToOp(which))) {
            retval= convertConstToOp(_rowParams.get(row));
        }
        return retval;
    }

    private static String convertOpToConst(String op) {
        if      (op.equals(EQ_J2000_DESC))     return EQ_J2000;
        else if (op.equals(EQ_J2000_DEG_DESC)) return EQ_J2000_DEG;
        else if (op.equals(IMAGE_PIXEL_DESC))  return IMAGE_PIXEL;
        else if (op.equals(GALACTIC_DESC))     return GALACTIC;
        else if (op.equals(EQ_B1950_DESC))     return EQ_B1950;
        else return null;
    }

    private static String convertConstToOp(String constStr) {
        if      (constStr.equals(EQ_J2000))     return EQ_J2000_DESC;
        else if (constStr.equals(EQ_J2000_DEG)) return EQ_J2000_DEG_DESC;
        else if (constStr.equals(IMAGE_PIXEL))  return IMAGE_PIXEL_DESC;
        else if (constStr.equals(GALACTIC))     return GALACTIC_DESC;
        else if (constStr.equals(EQ_B1950))     return EQ_B1950_DESC;
        else return null;
    }

    public Result getZoom(WebPlot plot) {
        return new Result("Zoom Level: ", plot.getZoomFact()+"");
    }

    public Result getFileSize(WebPlot plot) {
        long size= plot.getFitsData(Band.NO_BAND).getFitsFileSize();
        return new Result("File Size: ", StringUtils.getSizeAsString(size,true) );
    }

    public Result[] getFlux(WebPlot plot, final Readout readout, ImagePt ipt) {
        Result retval[];
        _fluxTimer.cancel();

        int size= _lastFluxRow-NEW_FIRST_FLUX_ROW + 1;
        retval= new Result[size];
        FluxCache fc= getSavedFlux(plot,ipt);
        Band bands[]= plot.getBands();
        if (fc==null) {
            _fluxTimer.setupCall(ipt, plot,readout);
            _fluxTimer.schedule(MOUSE_DELAY_MS);
            for(int i=0; (i<retval.length);i++) {
                retval[i]= new Result(getFluxLabel(plot, bands[i]), "                ");
            }
        }
        else {
            for(int i=0; (i<retval.length);i++) {
                retval[i]= makeFluxResult(fc.getFlux().get(bands[i]),plot,bands[i]);
            }
        }
        return retval;
    }


    public void setFluxLater(double zValue,
                             ImagePt ipt,
                             Readout readout,
                             WebPlot plot,
                             Band band) {

        Result result= makeFluxResult(zValue,plot,band);
        readout.setValue(getBandOffset(plot, band), result._label, result._value, getColorStyle(band), true, true);
        String fluxUnits= plot.getFitsData(band).getFluxUnits();
        WebMouseReadoutPerm.notifyExternal(plot.getScreenCoords(ipt),ipt,band, zValue, fluxUnits,true,false);
        addFlux(plot,ipt,band,zValue);
    }


    private int getBandOffset(WebPlot plot, Band band) {
        Band bands[]= plot.getBands();
        int i= NEW_FIRST_FLUX_ROW;
        for(Band b : bands) {
            if (b==band) {
                break;
            }
            else {
                i++;
            }
        }
        return i;
    }


    private void showTitle(Readout readout,
                           String title) {
        if (title!=null) {
            if (title.length()> MAX_TITLE_LEN) {
                title= title.substring(0,MAX_TITLE_LEN) + "...";
            }
            readout.setTitle(title,true);
        }
    }

    private String getColorStyle(Band band) {
        String color;
        switch (band) {
            case RED : color= "red-color"; break;
            case GREEN : color= "green-color"; break;
            case BLUE : color= "blue-color"; break;
            case NO_BAND : color= null; break;
            default : color= null; break;
        }
        return color;

    }


    private Result makeFluxResult(double zValue, WebPlot plot, Band band) {
        Result retval;
        String colorStyle= getColorStyle(band);


        if (!Double.isNaN(zValue)) {
            String fstr= formatFlux(zValue,plot, band);
            retval= new Result(getFluxLabel(plot,band), fstr, colorStyle);
        }
        else {
            retval= new Result(getFluxLabel(plot,band), "None", colorStyle);
        }
        return retval;
    }


    private String getFluxLabel(WebPlot plot, Band band)  {
        String label;
        String fluxUnits= plot.getFitsData(band).getFluxUnits();


        String start;
        switch (band) {
            case RED : start= "Red "; break;
            case GREEN : start= "Green "; break;
            case BLUE : start= "Blue "; break;
            case NO_BAND : start= ""; break;
            default : start= ""; break;
        }

        String valStr= start.length()>0 ? "Val: " : "Value: ";

        if (fluxUnits.equalsIgnoreCase("dn")) {
            label= start + valStr;
        }
        else if (fluxUnits.equalsIgnoreCase("frames")) {
            label= start + valStr;
        }
        else if (fluxUnits.equalsIgnoreCase("")) {
            label= start + valStr;
        }
        else {
            label= start + "Flux: ";
        }
        return label;
    }

    public Result getLon1(WebPlot plot, ImagePt ipt, ScreenPt screenPt) {
        return getCoord(plot,ipt,screenPt,
                        WhichDir.LON, WhichReadout.LEFT);
    }

    public Result getLon2(WebPlot plot, ImagePt ipt, ScreenPt screenPt) {
        return getCoord(plot,ipt,screenPt,
                        WhichDir.LON, WhichReadout.RIGHT);
    }

    public static Result getImagePixelSize(WebPlot plot) {
        return getPixelSize(plot, FILE_PIXEL_SIZE);
    }

//    public static Result getScreenPixelSize(WebPlot plot) {
//        Result retval;
//        if (plot != null) {
//            float size= (float)plot.getImagePixelScaleInArcSec() / plot.getZoomFact();
//            retval= new Result("1 Screen Pixel: ", _nfPix.format(size)  + "\"");
//        }
//        else {
//            retval= NO_RESULT;
//        }
//        return retval;
//    }


    public static Result getPixelSize(WebPlot plot, String scaleDisplayOp) {
        Result retval;
        if (plot!=null) {
            String ipStr="";
            if (scaleDisplayOp.equals(FILE_PIXEL_SIZE)) {
                ipStr= _nfPix.format(plot.getImagePixelScaleInArcSec());
            }
            else if (scaleDisplayOp.equals(SCREEN_PIXEL_SIZE)) {
                float size = (float) plot.getImagePixelScaleInArcSec() / plot.getZoomFact();
                ipStr= _nfPix.format(size);
            }
            retval= new Result(scaleDisplayOp+":", ipStr+"\"");
        }
        else {
            retval= new Result(scaleDisplayOp+":", "");
        }
        return retval;
    }




    public Result getLat1(WebPlot plot, ImagePt ipt, ScreenPt screenPt) {
        return getCoord(plot,ipt,screenPt,
                        WhichDir.LAT, WhichReadout.LEFT);
    }
    public Result getLat2(WebPlot plot, ImagePt ipt, ScreenPt screenPt) {
        return getCoord(plot,ipt,screenPt,
                        WhichDir.LAT, WhichReadout.RIGHT);
    }


    public Result getBoth2(WebPlot plot, ImagePt ipt, ScreenPt screenPt) {
        return getCoord(plot,ipt,screenPt,
                WhichDir.BOTH, WhichReadout.RIGHT);
    }

    public Result getBoth1(WebPlot plot, ImagePt ipt, ScreenPt screenPt) {
        return getCoord(plot,ipt,screenPt,
                WhichDir.BOTH, WhichReadout.LEFT);
    }

    private Result getCoord(WebPlot plot,
                            ImagePt ipt,
                            ScreenPt screenPt,
                            WhichDir dir,
                            WhichReadout readout) {

//        double x= screenPt.getIX();
//        double y= screenPt.getIY();
        Result retval= NO_RESULT;
        CoordinateSys coordSys;
        ReadoutMode mode;

        if (readout==WhichReadout.LEFT) {
            coordSys= _leftCoordSys;
            mode= _leftMode;
        }
        else if (readout==WhichReadout.RIGHT) {
            coordSys= _rightCoordSys;
            mode= _rightMode;
        }
        else {
            WebAssert.tst(false);
            coordSys= null;
            mode= null;
        }


        if (coordSys == null)  coordSys= plot.getCoordinatesOfPlot();
        if (plot == null) return NO_RESULT;
//        if (x > plot.getScreenWidth() || y > plot.getScreenHeight()) {
//            retStr= "";
//        }
//        else {
            if (coordSys.equals(CoordinateSys.SCREEN_PIXEL)) {
                if (dir==WhichDir.LON) {
                    retval= getReadoutByPixel(plot, dir, screenPt.getIX());
                }
                else if (dir==WhichDir.LAT) {
                    retval= getReadoutByPixel(plot, dir, screenPt.getIY());
                }
                else {
                    WebAssert.tst(false);
                }
            }
            else if (ipt != null) {
                retval= getReadoutByImagePt(plot, ipt, screenPt, dir,
                                            mode, coordSys);
            }
            else {
                retval= NO_RESULT;
            }
//        } // end else
        return retval;
    }

//    public static HashMap<Integer, String> makeDefaultRowParams() {
//        HashMap<Integer, String> retval = new HashMap<Integer,String> (3);
//        retval.put(0, TITLE);
//        retval.put(1, EQ_J2000);
//        retval.put(2, EQ_J2000_DEG);
//        retval.put(3, GALACTIC);
//        retval.put(4, EQ_B1950);
//        retval.put(5, IMAGE_PIXEL);
//
//        return retval;
//    }


    public static HashMap<Integer, String> makeDefaultRowParams() {
        HashMap<Integer, String> retval = new HashMap<Integer,String> (3);
        retval.put(0, TITLE);
        retval.put(1, EQ_J2000);
        retval.put(2, IMAGE_PIXEL);
        retval.put(3, FILE_PIXEL_SIZE);
        return retval;
    }

    public static HashMap<Integer, String> makeMinimalRowParams() {
        HashMap<Integer, String> retval = new HashMap<Integer,String> (3);
        retval.put(0, TITLE);
        retval.put(1, EQ_J2000);
        retval.put(2, GALACTIC);
        return retval;
    }


    public static Result getReadoutByImagePt(WebPlot plot,
                                       ImagePt ip,
                                       ScreenPt screenPt,
                                       WhichDir dir,
                                       ReadoutMode mode,
                                       CoordinateSys coordSys) {
        Result retval;
        if (plot == null) {
            retval= NO_RESULT;
        }
        else if (coordSys.equals(CoordinateSys.PIXEL)) {
            if (dir==WhichDir.BOTH) {
                retval= getDecimalBoth(ip.getX()-0.5, ip.getY()-0.5 , coordSys.getShortDesc());
            }
            else {
                retval= getDecimalXY(getValue(ip,dir)-0.5 , dir, coordSys);
            }
        }
        else if (coordSys.equals(CoordinateSys.SCREEN_PIXEL)) {
            if (dir==WhichDir.BOTH) {
                retval= getDecimalBoth(screenPt.getIX(), screenPt.getIY(), coordSys.getShortDesc());
            }
            else {
                retval= getDecimalXY(getValue(screenPt,dir), dir,
                        CoordinateSys.SCREEN_PIXEL);
            }
        }
        else {
            WorldPt degPt= plot.getWorldCoords(ip, coordSys);
            if (degPt==null) {
                retval= NO_RESULT;
            }
            else if (mode == ReadoutMode.HMS) {
                if (dir==WhichDir.BOTH) {
                    retval= getHmsBoth(degPt.getLon(),degPt.getLat(), coordSys);
                }
                else {
                    retval= getHmsXY(getValue(degPt,dir), dir, coordSys);
                }
            }
            else if (mode == ReadoutMode.DECIMAL) {
                if (dir==WhichDir.BOTH) {
                    retval= getDecimalBoth(degPt.getX(),degPt.getY(),  coordSys.getShortDesc());
                }
                else {
                    retval= getDecimalXY(getValue(degPt,dir), dir, coordSys);
                }
            }
            else {
                WebAssert.tst(false);
                retval= NO_RESULT;
            }
        }
        return retval;
    }

    private static Result getHmsXY(double val, WhichDir which, CoordinateSys coordSys) {
        Result retval;
        try {
            if (which==WhichDir.LON) {
                retval= new Result(coordSys.getlonShortDesc() ,
                        CoordUtil.convertLonToString(val, coordSys.isEquatorial()));
            }
            else if (which==WhichDir.LAT) {
                retval= new Result(coordSys.getlatShortDesc() ,
                        CoordUtil.convertLatToString(val, coordSys.isEquatorial()));
            }
            else {
                WebAssert.tst(false);
                retval= NO_RESULT;
            }
        } catch (CoordException ce) {
            retval= NO_RESULT;
        }
        return retval;
    }

    public static Result getHmsBoth(double lon, double lat, CoordinateSys coordSys) {
        Result retval;
        try {
            String lonStr= CoordUtil.convertLonToString(lon, coordSys.isEquatorial());
            String latStr= CoordUtil.convertLatToString(lat, coordSys.isEquatorial());
            retval= new Result(coordSys.getShortDesc(), lonStr +", "+latStr);
        } catch (CoordException ce) {
            retval= NO_RESULT;
        }
        return retval;
    }

    private static Result getReadoutByPixel(WebPlot plot, WhichDir which, int val) {
        Result retval;
        if (plot == null) {
            retval= NO_RESULT;
        }
        else {
            retval= getDecimalXY(val, which ,CoordinateSys.SCREEN_PIXEL);
        }
        return retval;
    }


    private static Result getDecimalXY(double val,
                                       WhichDir dir,
                                       CoordinateSys coordSys) {

        String desc= null;
        if (dir==WhichDir.LON) {
            desc= coordSys.getlonShortDesc();
        }
        else if (dir==WhichDir.LAT) {
            desc= coordSys.getlatShortDesc();
        }
        else {
            WebAssert.tst(false);
        }
        return new Result(desc , _nf.format(val));
    }

    private static Result getDecimalBoth(double x,
                                         double y,
                                         String desc) {

        return new Result(desc , _nf.format(x) +", " + _nf.format(y));
    }


    private static double getValue(ScreenPt pt, WhichDir dir) {
        double val;
        if (dir==WhichDir.LON) {
            val= pt.getIX();
        }
        else if (dir==WhichDir.LAT) {
            val= pt.getIY();
        }
        else {
            WebAssert.tst(false);
            val= 0;
        }
        return val;
    }

    private static double getValue(Pt pt, WhichDir dir) {
        double val;
        if (dir==WhichDir.LON) {
            val= pt.getX();
        }
        else if (dir==WhichDir.LAT) {
            val= pt.getY();
        }
        else {
            WebAssert.tst(false);
            val= 0;
        }
        return val;
    }

//    public static String formatReadoutByImagePt(WebDefaultMouseReadoutHandler.WhichReadout which,
//				 WebPlot plot, ImageWorkSpacePt ipt, String separator) {
//        String retval;
//        try {
//            ScreenPt screenPt = null;
//            ReadoutMode readoutMode = ReadoutMode.HMS;
//            CoordinateSys coordSys= CoordinateSys.EQ_J2000;
//            if (coordSys == null)  coordSys= plot.getCoordinatesOfPlot();
//            if (coordSys.equals(CoordinateSys.SCREEN_PIXEL_SIZE)) {
//                screenPt = plot.getScreenCoords(ipt);
//            }
//	    ImagePt ip = new ImagePt(ipt.getX(), ipt.getY());
//            retval = getReadoutByImagePt(plot, ip, screenPt, WhichDir.LON, readoutMode, coordSys) +
//                     separator +
//                     getReadoutByImagePt(plot, ip, screenPt, WhichDir.LAT, readoutMode, coordSys);
//            return retval;
//        } catch (Exception e) {
//            return " ";
//        }
//    }



    private void findFluxTry2(final WebPlot plot,
                              final Readout readout,
                              final ImagePt pt)  {
        if (!_attempingCtxUpdate) {
            _attempingCtxUpdate= true;
            plot.getFlux( pt,new AsyncCallback<double[]>() {
                public void onFailure(Throwable throwable) {
                    _attempingCtxUpdate= false;
                }

                public void onSuccess(double flux[]) {


                    Band bands[]= plot.getBands();
                    _attempingCtxUpdate= false;
                    for(int i= 0; (i<bands.length); i++) {
                        setFluxLater(flux[i],pt, readout,plot, bands[i]);
                    }
                }
            });
        }
    }



    private void findFlux(final WebPlot plot,
                          final Readout readout,
                          final ImagePt ipt)  {
        WebMouseReadoutPerm.notifyExternal(plot.getScreenCoords(ipt),ipt,Band.NO_BAND, Double.NaN, null,false, true);
        plot.getFluxLight( ipt,new AsyncCallback<String[]>() {
            public void onFailure(Throwable throwable) {
                for (Band band : plot.getBands()) {
                    setFluxLater(Double.NaN,ipt,
                                 readout,plot,band);
                }
            }

            public void onSuccess(String[] strFlux) {
                if (strFlux!=null && strFlux.length>0 && !PlotState.NO_CONTEXT.equals(strFlux[0])) {
                    Band bands[]= plot.getBands();
                    for(int i= 0; (i<bands.length); i++) {
                        double val= Double.parseDouble(strFlux[i]);
                        setFluxLater(val,ipt,
                                     readout,plot,bands[i]);
                    }
                }
                else {
                    Band bands[]= plot.getBands();
                    for (int i=0; (i<bands.length); i++) {
                        readout.setValue(NEW_FIRST_FLUX_ROW+i,getFluxLabel(plot,bands[i]),"Reloading...",
                                         getColorStyle(bands[i]));
                    }
                    findFluxTry2(plot,readout,ipt);
                }
            }
        });
    }

    private void checkPlotChange(WebPlot currPlot) {
        if (currPlot!=_lastPlot) {
            _lastPlot= currPlot;
            _savedFluxes.clear();

        }
    }



//======================================================================
//------------------ Private / Protected Methods -----------------------
//======================================================================

    private FluxCache getSavedFlux(WebPlot p, ImagePt pt) {
        FluxCache retval= null;
        for(FluxCache fc : _savedFluxes) {
            if (fc.testMatch(p,pt)) {
                retval= fc;
                break;
            }
        }
        return retval;
    }

    private void addFlux(WebPlot p, ImagePt pt, Band band, double flux) {
        // purge old
        FluxCache min= null;
        if (_savedFluxes.size() >= MAX_FLUXES) {
            for(FluxCache fc : _savedFluxes) {
                if (min==null || min.getTime()>=fc.getTime()) {
                    min= fc;
                }
            }
            WebAssert.tst(min!=null,
                          "Did not find a saved flux to delete");
            _savedFluxes.remove(min);
        }


        // add new
        FluxCache newFC= getSavedFlux(p,pt);
        if (newFC==null) {
            newFC= new FluxCache(p,pt);
            _savedFluxes.add(newFC);
        }
        newFC._fluxMap.put(band,flux);


    }

// =====================================================================
// -------------------- Inner classes --------------------------------
// =====================================================================

    private class FluxTimer extends Timer {
        private ImagePt _pt;
        private WebPlot _plot;
        private Readout _readout;

        public void run() { findFlux(_plot, _readout, _pt); }

        public void setupCall(ImagePt pt,
                              WebPlot plot,
                              Readout readout) {
            _pt= pt;
            _plot= plot;
            _readout= readout;
        }
    }

    private static class FluxCache {
        private final WebPlot _plot;
        private final Map<Band,Double> _fluxMap= new HashMap<Band,Double>(3);
        private final ImagePt _pt;
        private final long _date;

        public FluxCache(WebPlot plot, ImagePt pt) {
            _plot= plot;
            _pt= pt;
            _date= new Date().getTime();
        }
        public boolean equals(Object o) {
            boolean retval= false;
            if (o instanceof FluxCache) {
                FluxCache other= (FluxCache)o;
                retval= (_plot==other._plot) && other._pt.equals(_pt);
            }
            return retval;
        }


        public boolean testMatch(WebPlot plot, ImagePt pt) {
            return (_plot==plot) && pt.equals(_pt);
        }

        public Map<Band,Double> getFlux() {return _fluxMap; }
        public long getTime() { return _date;}
    }


    /**
     * Return the formatted value for this flux value
     * @param value the flux value
     * @param plot the plot the value is associated with
     * @param band the  color band
     * @return formatted flux with units
     */
    public static String formatFlux(double value, WebPlot plot, Band band) {
        String fluxUnits= plot.getFitsData(band).getFluxUnits();
        return formatFlux(value)+ " " + fluxUnits;

    }

    /**
     * Return the formatted value for this flux value
     * @param value the flux value
     * @return formatted flux
     */
    public static String formatFlux(double value) {
        String fstr;
        double absV= Math.abs(value);
        if (absV < 0.01 || absV >= 1000.) {
            fstr= _nfExpFlux.format(value);
        }
        else {
            fstr= _nf.format(value);
        }
        return fstr;

    }


    public static class Result {
        public  final String _label;
        public  final String _value;
        private final String _style;

        public Result( String label, String value) {
            this(label,value,null);
        }

        public Result( String label, String value, String style) {
            _label= label;
            _value= value;
            _style = style;
        }

    }



}

