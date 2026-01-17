package org.example.springboot3java21demo.exercise.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;


/**
 * 
 */
public class TestJMX {
    final static Logger log = LoggerFactory.getLogger(TestJMX.class);

    public static void main(String[] args) {
        log.info(TestJMX.getServer());
    }

    public static String getServer() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ArrayList<String> endPoints = new ArrayList<String>();
        try {
            Set<ObjectName> objs = mbs.queryNames(new ObjectName("*:type=Connector,*"), Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
            String hostname = InetAddress.getLocalHost().getHostName();
            InetAddress[] addresses = InetAddress.getAllByName(hostname);
            for (Iterator<ObjectName> i = objs.iterator(); i.hasNext(); ) {
                ObjectName obj = i.next();
                String scheme = mbs.getAttribute(obj, "scheme").toString();
                String port = obj.getKeyProperty("port");
                for (InetAddress addr : addresses) {
                    String host = addr.getHostAddress();
                    String ep = scheme + "://" + host + ":" + port;
                    endPoints.add(ep);
                }
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            return "";
        }
        if (endPoints.size() > 0) {
            return endPoints.get(0);
        } else {
            return "";
        }
    }
}
