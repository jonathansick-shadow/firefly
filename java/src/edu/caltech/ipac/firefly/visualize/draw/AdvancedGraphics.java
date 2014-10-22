package edu.caltech.ipac.firefly.visualize.draw;
/**
 * User: roby
 * Date: 5/2/14
 * Time: 2:50 PM
 */


import edu.caltech.ipac.firefly.visualize.ScreenPt;

/**
 * @author Trey Roby
 */
public interface AdvancedGraphics extends Graphics {


    public void setShadowPerm(Shadow s);
    public void setShadowForNextDraw(Shadow s);
    public void clearShadow();

    public void setTranslationPerm(ScreenPt pt);
    public void setTranslationForNextDraw(ScreenPt pt);
    public void clearTranslation();


    public void copyAsImage(AdvancedGraphics g);
    public CanvasPanel getCanvasPanel();


    public static class Shadow {
        private final double blur;
        private final double offX;
        private final double offY;
        private final String color;

        public Shadow(double blur, double offX, double offY, String color) {
            this.blur = blur;
            this.offX = offX;
            this.offY = offY;
            this.color = color;
        }

        public double getBlur() { return blur; }
        public double getOffX() { return offX; }
        public double getOffY() { return offY; }
        public String getColor() { return color; }
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
