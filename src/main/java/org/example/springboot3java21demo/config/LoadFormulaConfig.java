package org.example.springboot3java21demo.config;

import com.google.common.io.Resources;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class LoadFormulaConfig {

    private static Map<String, FormulaConfig> config = null;
    private static LoadFormulaConfig loadFormularConfig;

    public static LoadFormulaConfig getInstance() {
        if (null == loadFormularConfig) {
            loadFormularConfig = new LoadFormulaConfig();
        }
        return loadFormularConfig;
    }

    private LoadFormulaConfig() {
        try {
            XStream x = new XStream();
            x.addPermission(AnyTypePermission.ANY);
            x.autodetectAnnotations(true);
            x.processAnnotations(FormulaConfig.class);
            x.processAnnotations(FormulaVariable.class);
            x.processAnnotations(FormulaDataSet.class);
            x.processAnnotations(ContentCreatorClass.class);
            x.alias("parameter", String.class);
            x.alias("items", List.class);
            URL resource = Resources.getResource("formula/waschemeitem_variable.xml");
            List<FormulaConfig> result = (List<FormulaConfig>) x.fromXML(resource);
            config = new HashMap<>();
            for (FormulaConfig c : result) {
                config.put(c.getItemflag(), c);
            }
        } catch (Exception e) {
            log.error("Load xml error :", e);
        }
    }

    public String getBeanName(String itemflag) {
        if (null == config) return null;
        switch (itemflag) {
            case "changeDoc":
                return "changeDocDataSetImpl";
            case "inputData":
                return "inputDataSetImpl";
            default: {
                if (!config.containsKey(itemflag)) return null;
                return config.get(itemflag).getDataset().getContentCreatorClass().getBeanId();
            }
        }
    }

    public String getConvertBeanName(String itemflag) {
        if (null == config) return null;
        log.error("config.size():{}", config.size());
        if (!config.containsKey(itemflag)) return null;
        FormulaVariable variable = config.get(itemflag).getVariable();
        log.error("FormulaAriable:{}", variable);
        if (variable == null) return null;
        ContentCreatorClass contentCreatorClass = variable.getContentCreatorClass();
        if (contentCreatorClass == null) {
            return null;
        }
        return contentCreatorClass.getBeanId();
    }
}
