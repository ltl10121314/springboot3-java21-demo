package org.example.springboot3java21demo.exercise.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.List;

public class JsonTest {

    public static void main(String[] args) {

    }

    @Test
    public void jsonToObj(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            List list = mapper.readValue(createString(), List.class);
            list.forEach(System.out::println);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private  String createString() {
        String str = "[\n" +
                "  {\n" +
                "    \"type\": \"org\",\n" +
                "    \"scopes\": [\n" +
                "      {\n" +
                "        \"name\": \"二级公司\",\n" +
                "        \"id\": \"2680312687628544\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"pcategory\",\n" +
                "    \"scopes\": []\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"jobgrade\",\n" +
                "    \"scopes\": []\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"jobrank\",\n" +
                "    \"scopes\": []\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"staffstate\",\n" +
                "    \"scopes\": []\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"changetype\",\n" +
                "    \"scopes\": []\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"jobtype\",\n" +
                "    \"scopes\": []\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"wagegroup\",\n" +
                "    \"scopes\": [\n" +
                "      {\n" +
                "        \"name\": \"100001\",\n" +
                "        \"id\": \"1451367116933955587\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"testGroup31\",\n" +
                "        \"id\": \"1451341080830148611\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"wageGroup4\",\n" +
                "        \"id\": \"1456808067389194244\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"job\",\n" +
                "    \"scopes\": []\n" +
                "  }\n" +
                "]";
        return str;
    }
}
