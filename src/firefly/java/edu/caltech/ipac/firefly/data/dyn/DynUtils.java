/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
package edu.caltech.ipac.firefly.data.dyn;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import edu.caltech.ipac.firefly.core.Application;
import edu.caltech.ipac.firefly.data.DownloadRequest;
import edu.caltech.ipac.firefly.data.Param;
import edu.caltech.ipac.firefly.data.Request;
import edu.caltech.ipac.firefly.data.ServerRequest;
import edu.caltech.ipac.firefly.data.dyn.xstream.AccessTag;
import edu.caltech.ipac.firefly.data.dyn.xstream.DownloadTag;
import edu.caltech.ipac.firefly.data.dyn.xstream.FieldGroupTag;
import edu.caltech.ipac.firefly.data.dyn.xstream.FormTag;
import edu.caltech.ipac.firefly.data.dyn.xstream.ParamTag;
import edu.caltech.ipac.firefly.data.dyn.xstream.QueryTag;
import edu.caltech.ipac.firefly.data.dyn.xstream.SearchFormParamTag;
import edu.caltech.ipac.firefly.data.userdata.RoleList;
import edu.caltech.ipac.firefly.ui.DynDownloadSelectionDialog;
import edu.caltech.ipac.firefly.ui.Form;
import edu.caltech.ipac.firefly.ui.FormUtil;
import edu.caltech.ipac.firefly.ui.GwtUtil;
import edu.caltech.ipac.firefly.ui.creator.PrimaryTableUI;
import edu.caltech.ipac.firefly.ui.input.InputField;
import edu.caltech.ipac.firefly.ui.input.InputFieldGroup;
import edu.caltech.ipac.firefly.ui.table.DownloadSelectionIF;
import edu.caltech.ipac.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DynUtils {

    static public final String HYDRA_COMMAND_NAME_PREFIX = "Hydra_";

    static public final String HYDRA_APP_DATA = "hydraData";
    static public final String HYDRA_PROJECT_ID = "projectId";
    static public final String QUERY_ID = "queryId";
    static public final String SEARCH_NAME = "searchName";

    static public final String DEFAULT_DOWNLOAD_TITLE = "Download Options";
    static public final String DEFAULT_FILE_PREFIX = "";
    static public final String DEFAULT_TITLE_PREFIX = "";
    static public final String DEFAULT_EVENT_WORKER_TYPE = "artifacts";
    static public final String DEFAULT_FIELD_GROUP_TYPE = "default";
    static public final String DEFAULT_FIELD_GROUP_DIRECTION = "vertical";
    static public final int DEFAULT_FIELD_GROUP_LABEL_WIDTH = 100;
    static public final String DEFAULT_FIELD_GROUP_ALIGN = "right";
    static public final String DEFAULT_FIELD_GROUP_TITLE = "";
    static public final String DEFAULT_FIELD_GROUP_TOOLTIP = "";
    static public final double DEFAULT_LAYOUT_AREA_WIDTH = 100;
    static public final double DEFAULT_LAYOUT_AREA_HEIGHT = 100;
    static public final String DEFAULT_PREVIEW_TYPE = "imageViewer";
    static public final String DEFAULT_PREVIEW_ALIGN = "left";
    static public final String DEFAULT_PREVIEW_FRAME_TYPE = "frameOnly";
    static public final String DEFAULT_PROJECT_ITEM_ACTIVE_FLAG = "true";
    static public final String DEFAULT_PROJECT_ITEM_IS_COMMAND_FLAG = "false";
    static public final String DEFAULT_TABLE_TYPE = "selectableTable";
    static public final String DEFAULT_TABLE_ALIGN = "left";

    private static final String INFO_MSG_STYLE = "info-msg";


    static public Request makeHydraRequest(Request req, String projectId) {
        req.setParam(HYDRA_PROJECT_ID, projectId);
        return req;
    }

    static public boolean checkRoleAccess(AccessTag at) {
        boolean accessFlag = true;

        if (at != null) {
            RoleList rList = Application.getInstance().getLoginManager().getLoginInfo().getRoles();

            String accessIncludes = at.getIncludes();
            if (accessIncludes.length() > 0) {
                accessFlag = accessFlag && rList.hasAccess(accessIncludes.split(","));
            }

            String accessExcludes = at.getExcludes();
            if (accessExcludes.length() > 0) {
                accessFlag = accessFlag && !rList.hasAccess(accessExcludes.split(","));
            }
        }

        return accessFlag;
    }

    static public List<ParamTag> convertParams(Map<String, String> pMap) {
        ArrayList<ParamTag> pList = new ArrayList<ParamTag>();

        for (Map.Entry<String, String> e : pMap.entrySet()) {
            pList.add(new ParamTag(e.getKey(), e.getValue()));
        }

        return pList;
    }

    static public Map<String, String> convertParams(List<ParamTag> pList) {
        HashMap map = new HashMap<String, String>(0);

        for (ParamTag p : pList) {
            String key = p.getKey();
            String value = p.getValue();

            if (map.containsKey(key)) {
                String oldValue = (String) map.get(key);
                // if old value does not contain "=", assume value replacement
                if (!oldValue.contains("=")) {
                    map.put(key, value);

                }
                
                // replace only the inner keyword/value - keep all others as-is
                else {
                    HashMap tmpMap = new HashMap<String, String>(0);

                    // add all old values to tmp map
                    String[] oldArr = oldValue.split(",");
                    for (String oldItem : oldArr) {
                        String[] oldItem2 = oldItem.split("=");
                        if (oldItem2.length == 1) {
                            tmpMap.put(oldItem2[0], null);
                        } else {
                            tmpMap.put(oldItem2[0], oldItem2[1]);
                        }
                    }

                    // add all new values to tmp map
                    String[] newArr = value.split(",");
                    for (String newItem : newArr) {
                        String[] newItem2 = newItem.split("=");
                        if (newItem2.length == 1) {
                            tmpMap.put(newItem2[0], null);
                        } else {
                            tmpMap.put(newItem2[0], newItem2[1]);
                        }
                    }

                    // create new unique string
                    String finalStr = "";
                    Set<String> finalKeys = tmpMap.keySet();
                    int itemCnt = 0;
                    for (String finalKey : finalKeys) {
                        itemCnt++;
                        if (itemCnt > 1) {
                            finalStr += ",";
                        }

                        String finalValue = (String) tmpMap.get(finalKey);
                        if (finalValue == null) {
                            finalStr += finalKey;
                        } else {
                            finalStr += finalKey + "=" + finalValue;
                        }
                    }

                    map.put(key, finalStr);
                }

            } else {
                map.put(key, value);
            }
        }

        return map;
    }

    static public List<Param> convertToParamList(List<ParamTag> pList) {
        ArrayList<Param> paramList = new ArrayList<Param>();

        for (ParamTag p : pList) {
            paramList.add(new Param(p.getKey(), p.getValue()));
        }

        return paramList;
    }

    static public void PopupMessage(String title, String message) {
        final DialogBox p = new DialogBox(false, false);
        //p.setStyleName(INFO_MSG_STYLE);

        if (title != null) {
            p.setTitle(title);
        }

        VerticalPanel vp = new VerticalPanel();
        vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        vp.setSpacing(10);
        vp.add(new HTML(message));
        vp.add(new HTML(""));

        Button b = new Button("Close");
        vp.add(b);

        p.setWidget(vp);
        p.center();

        b.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                p.hide();
                p.clear();
            }

        });

        p.show();
    }

    // obtain project id from URL instead of from DynRequestHandler - safer
    public static String getProjectIdFromUrl() {
        String urlHash = Window.Location.getHash();
        if (!StringUtils.isEmpty(urlHash)) {
            List<String> hashList = StringUtils.asList(urlHash, "[#&]");
            for (String hashItem : hashList) {
                if (hashItem.contains("=")) {
                    String[] param = hashItem.split("=");
                    if (param[0].equals(DynUtils.HYDRA_PROJECT_ID)) {
                        return param[1];
                    }
                }
            }
        }

        return "";
    }


    public static DynDownloadSelectionDialog makeDownloadDialog(DownloadTag dlTag, Form form) {
        // check for downloadTag options
        DynDownloadSelectionDialog ddsd = null;
        if (dlTag != null) {
            ddsd = new DynDownloadSelectionDialog(dlTag.getTitle());
            String maxRows = dlTag.getMaxRows();
            if (!StringUtils.isEmpty(maxRows)) {
                DownloadSelectionIF.MinMaxValidator validator = new DownloadSelectionIF.MinMaxValidator(
                        ddsd, 1, Integer.parseInt(maxRows));
                ddsd.setValidator(validator);
            }

            FormTag formTag = dlTag.getFormTag();
            if (formTag != null) {
                List<FieldGroupTag> dlFg = formTag.getFieldGroups();
                if (dlFg != null) {
                    Form dlform = GwtUtil.createForm(false, formTag, null, form, null);
                    ddsd.addFieldDefPanel(dlform);
                }
            }

        }
        return ddsd;
    }

    public static void evaluateSearchFormParam(Form searchForm, SearchFormParamTag t, List<ParamTag> pList) {
        String keyName = t.getKeyName();
        String keyValue = t.getKeyValue();
        String createParams = t.getCreateParams();

        InputField inF = searchForm.getField(keyName);
        if (inF == null) {
            // see if it a fieldgroup
            // TODO

        } else if (inF.isVisible()) {
            String fieldDefValue = inF.getValue();
            if (keyValue.equals(fieldDefValue) || (keyValue.equals("*") && fieldDefValue.length() > 0)) {
                String[] createParamArr = createParams.split(",");
                for (String createParam : createParamArr) {
                    InputField inF2 = searchForm.getField(createParam);
                    if (inF2 == null) {
                        // see if it is within a fieldgroup
                        String val = getGroupValueFromForm(searchForm, createParam);
                        if (val != null) {
                            ParamTag pt = new ParamTag(createParam, val);
                            pList.add(pt);
                        }

                    } else {
                        String val = inF2.getValue();
                        if (val != null) {
                            ParamTag pt = new ParamTag(createParam, val);
                            pList.add(pt);
                        }
                    }
                }
            }
        }
    }

    private static String getGroupValueFromForm(Form f, String key) {
        String val = null;
        List<InputFieldGroup> groups = new ArrayList<InputFieldGroup>();

        FormUtil.getAllChildGroups(f, groups);
        boolean found = false;
        for (InputFieldGroup ifG : groups) {
            List<Param> pL = ifG.getFieldValues();
            for (Param _p : pL) {
                if (_p.getName().equals(key)) {
                    val = _p.getValue();
                    found = true;
                    break;
                }
            }

            if (found) {
                break;
            }
        }

        return val;
    }



}

