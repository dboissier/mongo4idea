package org.codinjutsu.tools.mongo.utils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.codinjutsu.tools.mongo.view.model.JsonDataType;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class MongoUtilsTest {
    @Test
    public void testParseValue() throws Exception {
        assertNull(MongoUtils.parseValue(JsonDataType.NULL, null));

        assertEquals("pouet", MongoUtils.parseValue(JsonDataType.STRING, "pouet"));
        assertEquals(Boolean.TRUE, MongoUtils.parseValue(JsonDataType.BOOLEAN, "true"));
        assertEquals(Boolean.FALSE, MongoUtils.parseValue(JsonDataType.BOOLEAN, "truie"));
        assertEquals(Boolean.FALSE, MongoUtils.parseValue(JsonDataType.BOOLEAN, "false"));

        assertEquals(1.1d, MongoUtils.parseValue(JsonDataType.NUMBER, "1.1"));
        assertEquals(1, MongoUtils.parseValue(JsonDataType.NUMBER, "1"));


        assertEquals(new BasicDBObject("key", "val"), MongoUtils.parseValue(JsonDataType.OBJECT, "{ 'key': 'val'}"));

        BasicDBList dbList = new BasicDBList();
        dbList.add("pouet");
        dbList.add(1.0d);
        dbList.add(Boolean.TRUE);

        assertEquals(dbList, MongoUtils.parseValue(JsonDataType.ARRAY, "[ 'pouet', 1.0, true ]"));
    }
}
