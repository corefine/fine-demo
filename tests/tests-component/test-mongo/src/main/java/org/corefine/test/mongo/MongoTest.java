package org.corefine.test.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * @author Fe by 2022/1/6 11:22
 */
public class MongoTest {

    public void test() {
        MongoClient client = MongoClients.create("mongodb://172.35.88.77/admin");
        MongoDatabase db = client.getDatabase("lego_workflow-1");
        Document command = new Document();
        command.put("explain", Document.parse("{\"find\": \"processExpandBO\", \"filter\": {\"eventId\": {\"$in\": [\"O18421\"]}, \"companyId\": \"4191\", \"isDeleted\": \"0\"}, \"$db\": \"lego_workflow-1\"}"));
        System.out.println(command.toJson());
        Document result = db.runCommand(command);
        System.out.println(result);

//        MongoCollection<Document> collection =  database.getCollection("processExpandBO");
//        Document query = new Document();
//        query.put("$explain", 1);
//        FindIterable<Document> findIterable = collection.find(query);
//        System.out.println(findIterable.first());

//        Document document = new Document();
//        document.put("explain", 1);
//        FindIterable<Document> projection = findIterable.projection(document);
//        System.out.println(projection.first());
        //{"find": "processExpandBO", "filter": {"eventId": {"$in": ["O18421"]}, "companyId": "4191", "isDeleted": "0"}, "$db": "lego_workflow-1"}
//        String command = "{\"find\":\"processExpandBO\",\"filter\":{\"companyId\":\"4191\",\"isDeleted\":\"0\"}, \"$explain\": 1}";

//        Document document = database.runCommand(Document.parse(command));
//
//        System.out.println();
//        System.out.println(document);
    }

    public static void main(String[] args) {
        new MongoTest().test();
    }
}
