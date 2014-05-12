package edu.caltech.ipac.firefly.resbundle.images;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * Images used by paging table
 *
 * @author loi
 */
public interface TableImages extends ClientBundle {

    @Source("icons-2014/16x16_BackwardToEnd.png")
    public ImageResource getFirstPage();

    @Source("page-last-disabled.gif")
    public ImageResource getLastPageDisabled();

    @Source("icons-2014/16x16_ForwardToEnd.png")
    public ImageResource getLastPage();

    @Source("icons-2014/16x16_Forward.png")
    public ImageResource getNextPage();

    @Source("icons-2014/16x16_Backward.png")
    public ImageResource getPrevPage();

    @Source("sort_asc.gif")
    public ImageResource getSortAsc();

    @Source("sort_desc.gif")
    public ImageResource getSortDesc();

    @Source("transparent.gif")
    public ImageResource getTransImage();

//    @Source("table_column.gif")
//    public ImageResource getColumnOptions();

    @Source("icons-2014/24x24_TextView.png")
    public ImageResource getTextViewImage();

    @Source("icons-2014/24x24_TableView.png")
    public ImageResource getTableViewImage();

    @Source("icons-2014/24x24_Save.png")
    public ImageResource getSaveImage();

    @Source("icons-2014/16x16_Filter.png")
    public ImageResource getFilterIn();

    @Source("icons-2014/24x24_Filter.png")
    public ImageResource getFilterImage();

    @Source("icons-2014/24x24_FilterOff_Circle.png")
    public ImageResource getClearFilters();

    public static class Creator  {
        private final static TableImages _instance=
                (TableImages) GWT.create(TableImages.class);
        public static TableImages getInstance() {
            return _instance;
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
