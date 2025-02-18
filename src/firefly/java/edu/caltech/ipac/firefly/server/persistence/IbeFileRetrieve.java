/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
package edu.caltech.ipac.firefly.server.persistence;

/**
 * @author tatianag
 *         $Id: $
 */

import edu.caltech.ipac.astro.ibe.IBE;
import edu.caltech.ipac.astro.ibe.IbeDataParam;
import edu.caltech.ipac.astro.ibe.IbeDataSource;
import edu.caltech.ipac.firefly.data.ServerRequest;
import edu.caltech.ipac.firefly.server.ServerContext;
import edu.caltech.ipac.firefly.server.packagedata.FileInfo;
import edu.caltech.ipac.firefly.server.query.BaseFileInfoProcessor;
import edu.caltech.ipac.firefly.server.query.DataAccessException;
import edu.caltech.ipac.firefly.server.query.ParamDoc;
import edu.caltech.ipac.firefly.server.query.SearchProcessorImpl;
import edu.caltech.ipac.util.FileUtil;
import edu.caltech.ipac.util.cache.Cache;
import edu.caltech.ipac.util.cache.CacheManager;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@SearchProcessorImpl(id = "ibe_file_retrieve", params =
        {@ParamDoc(name = "mission", desc = "mission"),
        })
public class IbeFileRetrieve extends BaseFileInfoProcessor {
    public static final String PROC_ID = IbeFileRetrieve.class.getAnnotation(SearchProcessorImpl.class).id();
    public static final String MISSION = "mission";

    @Override
    protected FileInfo loadData(ServerRequest request) throws IOException, DataAccessException {
        try {
            String mission = request.getParam(MISSION);
            Map<String, String> paramMap = IBEUtils.getParamMap(request.getParams());

            IBE ibe = IBEUtils.getIBE(mission, paramMap);
            IbeDataSource ibeDataSource = ibe.getIbeDataSource();
            IbeDataParam dataParam = ibeDataSource.makeDataParam(paramMap);
            File ofile = makeOutputFile(dataParam);
            ibe.getData(ofile, dataParam);

            // no result found
            if (ofile == null || !ofile.exists() || ofile.length() == 0) {
                return null;
            } else {
                return new FileInfo(ofile.getAbsolutePath(), dataParam.getFileName(), ofile.length());
            }
        } catch (Exception e) {
            throw new DataAccessException("Fail to retrieve file from IBE.", e);
        }
    }

    protected File makeOutputFile(IbeDataParam params) throws IOException {
        String fname = params.getFileName();
        if (fname.contains(".tbl")) {
            return File.createTempFile("IbeFileRetrieve-", "-" + fname, ServerContext.getTempWorkDir());
        } else {
            ;
            return File.createTempFile(fname + "-", "", ServerContext.getVisCacheDir());
        }
    }

    @Override
    public boolean doCache() {
        return true;
    }

}
