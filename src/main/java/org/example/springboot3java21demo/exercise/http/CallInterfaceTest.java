package org.example.springboot3java21demo.exercise.http;

import org.junit.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * GET、POST 工具类
 */
public class CallInterfaceTest {
    /**
     * 设置 https 请求
     *
     * @throws Exception 抛出异常
     */
    public static void trustAllHttpsCertificates() throws Exception {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String str, SSLSession session) {
                return true;
            }
        });
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new MITM();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    /**
     * 设置 https 请求证书
     */
    static class MITM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }

        @Override
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        @Override
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }


    }

    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url   发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            //ssl
            trustAllHttpsCertificates();
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            /*
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }*/
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * post请求
     *
     * @param json 前端请求
     * @return 返回值
     * @throws Exception 返回异常
     */
    public static String sendPost(String address, String json) throws Exception {
        //创建URL对象,设置请求url
        URL url = new URL(address);
        trustAllHttpsCertificates();
        //调用URL对象的openConnection( )来获取HttpURLConnection对象实例
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //请求方法为POST
        conn.setRequestMethod("POST");
        //设置连接超时为5秒
        conn.setConnectTimeout(5000);
        //允许输入输出
        conn.setDoInput(true);
        conn.setDoOutput(true);
        //不能缓存
        conn.setUseCaches(false);
        //至少要设置的两个请求头
        //设置头部信息
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
        //一定要设置 Content-Type 要不然服务端接收不到参数
        conn.setRequestProperty("Content-Type", "application/Json; charset=UTF-8");
        //输出流包含要发送的数据,要注意数据格式编码
        OutputStream op = conn.getOutputStream();
        op.write(json.getBytes());
        //服务器返回东西了，先对响应码判断
        String result = "";
        if (conn.getResponseCode() == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line = "";
            while (null != (line = br.readLine())) {
                result += line;
            }
            br.close();
            conn.disconnect();
            return result;
        }
        return result;
    }

    @Test
    public void sedGetHttpTest() throws Exception {
        String address = "http://172.16.26.54:8700/rest/dig-buw-monitor/workflows/configuration/getStatusConfiguration";
        String param = "";
        int count = 1;
        for (int i = 0; i < count; i++) {
            String result = sendGet(address, param);
            System.out.println("第" + (i + 1) + "次调用:" + result);
        }
    }


    @Test
    public void sedGetHttpsTest() throws Exception {
//        String address = "https://172.16.26.46:9444/rest/dig-buw-monitor/workflows/configuration/getStatusConfiguration";
//        String address = "https://www.ahdataex.com/portal/pure/SaveTenant!SaveTenant.action";
//        String address= "https://blog.csdn.net/weixin_34092370/article/details/91853603";
        String address = "https://www.baidu.com";
        String param = "";
        int count = 1000;
        for (int i = 0; i < count; i++) {
            String result = sendGet(address, param);
            System.out.println("第" + (i + 1) + "次调用:" + result);
        }
    }

    @Test
    public void sendPostHttpTest() throws Exception {
        //请求地址
        String address = "http://172.16.26.54:8700/rest/dig-buw-manager/taskServiceForRest/getTaskDefineForExecutor";
        //将对象转换为json，然后发送；
        String json = "{\n" +
                "  \"objectInfo\": {\n" +
                "    \"nodeKey\": \"4bbb27d384844d8cb83de1307dcc33d6\",\n" +
                "    \"taskType\": \"m3221\",\n" +
                "    \"tenantId\": \"tenant_system\"\n" +
                "  },\n" +
                "  \"token\":1\n" +
                "}";
        String result = "";
        int count = 1000;
        for (int i = 0; i < count; i++) {
            result = sendPost(address, json);
            System.out.println("第" + (i + 1) + "次调用：" + result);
        }
    }

    @Test
    public void sendPostHttpsTest() throws Exception {
        //请求地址
        String address = "http://172.16.11.151:8207/dig-dp/taskServiceForRest/getTaskDefineForExecutor";
        //将对象转换为json，然后发送；
        String json = "{\n" +
                "  \"objectInfo\": {\n" +
                "    \"nodeKey\": \"d63975e2c2f543aaac8e1bebb7eaac48\",\n" +
                "    \"taskType\": \"m3221\",\n" +
                "    \"tenantId\": \"cs\"\n" +
                "  },\n" +
                "  \"token\":1\n" +
                "}";
        String result = "";
        int count = 5000;
        for (int i = 0; i < count; i++) {
            result = sendPost(address, json);
            System.out.println("第" + (i + 1) + "次调用：" + result);
        }
    }

    public static void main(String[] args) throws Exception {
        String address = "https://172.16.26.78/dpsjy/rest/WorkflowServiceForRest/bdiEtlMonitorWfDefNew";
        String json = "{\n" +
                "  \"objectInfo\": {\n" +
                "    \"key\": \"\",\n" +
                "    \"startType\": \"\",\n" +
                "    \"versionType\": 0,\n" +
                "    \"hasCheckIn\": \"\",\n" +
                "    \"objType\": \"m35\",\n" +
                "    \"wfName\": \"\",\n" +
                "    \"wfCode\": \"\",\n" +
                "    \"oid\": \"\",\n" +
                "    \"treeNode\": \"\",\n" +
                "    \"treeLevel\": 1,\n" +
                "    \"sort\": \"lastSaveTime\",\n" +
                "    \"dir\": \"desc\",\n" +
                "    \"limit\": 20,\n" +
                "    \"start\": 0\n" +
                "  },\n" +
                "  \"token\":101010\n" +
                "}";
        String result = sendPost(address, json);
        System.out.println(result);
    }
}
