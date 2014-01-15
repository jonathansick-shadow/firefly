package edu.caltech.ipac.firefly.fftools;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import edu.caltech.ipac.firefly.core.Application;
import edu.caltech.ipac.firefly.core.TableLoadHandler;
import edu.caltech.ipac.firefly.data.JscriptRequest;
import edu.caltech.ipac.firefly.data.ServerParams;
import edu.caltech.ipac.firefly.data.SortInfo;
import edu.caltech.ipac.firefly.data.TableServerRequest;
import edu.caltech.ipac.firefly.ui.creator.PrimaryTableUI;
import edu.caltech.ipac.firefly.ui.creator.TablePanelCreator;
import edu.caltech.ipac.firefly.ui.creator.WidgetFactory;
import edu.caltech.ipac.firefly.ui.table.EventHub;
import edu.caltech.ipac.firefly.ui.table.TablePanel;
import edu.caltech.ipac.firefly.ui.table.builder.PrimaryTableUILoader;
import edu.caltech.ipac.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Usage: firefly.showTable(parameters, div)

 * parameters is an object attributes.  div is the div to load the table into.
 * below is a list of all possible parameters.
 *
 * parameters:
 * source      : required; location of the ipac table.  url or file path.
 * type        : basic, selectable, or chart; defaults to basic if not given
 * filters
 * sortInfo
 * pageSize
 * startIdx
 * tableOptions
 *
 * tableOptions:  option=true|false [,option=true|false]*
 * show-filter
 * show-popout
 * show-title
 * show-toolbar
 * show-options
 * show-paging
 * show-save
 * @version $Id: TableJSInterface.java,v 1.8 2012/12/11 21:10:01 tatianag Exp $
 */
public class TableJSInterface {
    public static final String TBL_TYPE = "type";
    public static final String TBL_SOURCE = "source";
    public static final String TBL_ALT_SOURCE = "alt_source";
    public static final String TBL_SORT_INFO = TableServerRequest.SORT_INFO;
    public static final String TBL_FILTER_BY = TableServerRequest.FILTERS;
    public static final String TBL_PAGE_SIZE = TableServerRequest.PAGE_SIZE;
    public static final String TBL_START_IDX = TableServerRequest.START_IDX;
    public static final String FIXED_LENGTH = TableServerRequest.FIXED_LENGTH;

    public static final String TBL_OPTIONS = "tableOptions";    // refer to TablePanelCreator for list of options

    public static final String TYPE_SELECTABLE = "selectable";
    public static final String TYPE_BASIC  = "basic";
    public static final String SEARCH_PROC_ID = "IpacTableFromSource";

//============================================================================================
//------- Methods take the JSPlotRequest, called from javascript, converts then calls others -
//============================================================================================

    public static void showTable(JscriptRequest jspr, String div) {
        showTable(jspr, div, false);
    }

    public static void showTable(JscriptRequest jspr, String div, boolean doCache) {

        EventHub hub= FFToolEnv.getHub();
        TableServerRequest req = convertToRequest(jspr);
        Map<String, String> params = extractParams(jspr, TBL_OPTIONS);
        if (!doCache) {
            req.setParam("rtime", String.valueOf(System.currentTimeMillis()));
        }
        if (!params.containsKey(TablePanelCreator.QUERY_SOURCE)) {
            params.put(TablePanelCreator.QUERY_SOURCE,div);
        }

        if (req == null) return;

        String type = jspr.getParam(TBL_TYPE);
        String tblType = (!StringUtils.isEmpty(type) && type.equals(TYPE_SELECTABLE)) ? WidgetFactory.TABLE : WidgetFactory.BASIC_TABLE;

        if (req.containsParam(ServerParams.SOURCE)) {
            req.setParam(ServerParams.SOURCE, FFToolEnv.modifyURLToFull(req.getParam(ServerParams.SOURCE)));
        }

        final PrimaryTableUI table = Application.getInstance().getWidgetFactory().createPrimaryUI(tblType, req, params);


        RootPanel rp= FFToolEnv.getRootPanel(div);
        if (rp == null) {
            rp= FFToolEnv.getRootPanel(null);
        }

        PrimaryTableUILoader loader = new PrimaryTableUILoader(new TableLoadHandler() {
            public Widget getMaskWidget() {
                return table.getDisplay();
            }
            public void onLoad() {}
            public void onError(PrimaryTableUI table, Throwable t) {}
            public void onLoaded(PrimaryTableUI table) {}
            public void onComplete(int totalRows) {}
        });

        loader.addTable(table);
        loader.loadAll();

        Widget w;
        for (int i = 0; i < rp.getWidgetCount(); i++) {
            w= rp.getWidget(i);
            if (w instanceof TablePanel) {
                w.removeFromParent();
                i = 0;
            }
        }
        rp.add(table.getDisplay());
        if (table.getDisplay() instanceof TablePanel) {
            hub.bind((TablePanel)table.getDisplay());
        }

    }


    private static Map<String, String> extractParams(JscriptRequest jspr, String paramName) {
        HashMap<String, String> params = new HashMap<String, String>();
        String optStr = jspr.getParam(paramName);
        if (!StringUtils.isEmpty(optStr)) {
            String[] options = StringUtils.split(optStr, ",");
            for (String s : options) {
                String[] parts = StringUtils.split(s, "=", 2);
                params.put(parts[0], parts[1]);
            }
        }
        return params;
    }

    public static void showExpandedTable(JscriptRequest jspr) {
        TableServerRequest req = convertToRequest(jspr);
    }

//======================================================================
//------------------ Private / Protected Methods -----------------------
//======================================================================

    public static TableServerRequest convertToRequest(JscriptRequest jspr) {

        TableServerRequest dataReq= new TableServerRequest(SEARCH_PROC_ID);

        Map<String, String> params = jspr.asMap();

        for (String key : params.keySet()) {
            if (StringUtils.isEmpty(key)) continue;

            String val = params.get(key);

            if (key.equals(TBL_SOURCE)) {
                String url = FFToolEnv.modifyURLToFull(val);
                dataReq.setParam(key, url);
            } else if (key.equals(TBL_FILTER_BY) && !StringUtils.isEmpty(val)) {
                dataReq.setFilters(StringUtils.asList(val, ","));
            } else if (key.equals(TBL_SORT_INFO) && !StringUtils.isEmpty(val)) {
                dataReq.setSortInfo(SortInfo.parse(val));
            } else if (key.equals(TBL_START_IDX) && !StringUtils.isEmpty(val)) {
                dataReq.setStartIndex(Integer.parseInt(val));
            } else if (key.equals(TBL_PAGE_SIZE) && !StringUtils.isEmpty(val)) {
                dataReq.setPageSize(Integer.parseInt(val));
            } else if (!StringUtils.isEmpty(val)) {
                dataReq.setParam(key, val);
            }

        }
        return dataReq;
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
