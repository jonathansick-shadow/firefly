/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */

/*global window*/

import {parseWorldPt, makeProjectionPt} from './Point.js';



export const makeProjection= function(gwtProjStr) {
   if (!window.ffgwt) return null;
   var gwtProj= window.ffgwt.Visualize.ProjectionSerializer.deserializeProjection(gwtProjStr);
   return {
      isWrappingProjection() {
         gwtProj.isWrappingProjection();
      },
      getPixelWidthDegree() {
         gwtProj.getPixelWidthDegree();
      },
      getPixelHeightDegree() {
         gwtProj.getPixelHeightDegree();
      },
      getPixelScaleArcSec() {
         gwtProj.getPixelScaleArcSec();
      },
      isWrappingProjection() {
         gwtProj.isWrappingProjection();
      },
      getImageCoords(x,y) {
         var pt= gwtProj.getImageCoordsSilent(x,y);
         return pt ? makeProjectionPt(pt.getX(), pt.getY()) : null;
      },
      getWorldCoords(x,y) {
         var wpt= gwtProj.getWorldCoordsSilent(x,y);
         return wpt ? parseWorldPt(wpt.serialize()) : null;
      }
   };
};

