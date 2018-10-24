package main.java;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import spark.Request;
import spark.Response;

import java.sql.Timestamp;
import java.util.Set;

import static com.mongodb.client.model.Filters.eq;
import static spark.Spark.*;

public class Main {
    public static String processRoute(Request req, Response res) {
        Set<String> params = req.queryParams();
        //System.out.println("Parameters: " + params);

        // possible for query param to be an array
        for (String param : params) {
            System.out.println(param + " : " + req.queryParamsValues(param)[0]);
        }
        return "done!";
    }

    public static void main(String[] args) {
        // open connection
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        // get ref to database
        MongoDatabase db = mongoClient.getDatabase("REST2");
        // get ref to collection
        MongoCollection<Document> userCollection = db.getCollection("users");
        MongoCollection<Document> authCollection = db.getCollection("auth");

        /*
        // create a new document
        Document userDoc = new Document("username", "user2")
                .append("password", "5678");
        // insert document into collection
        userCollection.insertOne(userDoc);
*/

        staticFiles.externalLocation("public");
        // http://sparkjava.com/documentation
        port(27017);

        // calling get will make your app start listening for the GET path with the /hello endpoint
        get("/newuser",(req, res) -> {
            processRoute(req, res);
            // create a new document
            // insert document into collection
            String name = req.queryParams("username");
            String pass = req.queryParams("password");
            //System.out.println("Param " +  name + " & " + pass);
            Document userDoc = new Document("username", name)
                    .append("password", pass);
            userCollection.insertOne(userDoc);
            return "okay";
        });

        get("/login", (req, res) -> {
            processRoute(req, res);
            String name = req.queryParams("username");
            Document searchUser = userCollection.find(eq("username", name)).first();
            if (searchUser != null) {
                String pass = req.queryParams("password");
                String mPass = searchUser.getString("password");
                //System.out.println("mPASS: " + mPass);
                if (mPass.equalsIgnoreCase(pass)) {
                    Object token = searchUser.get("_id");
                    Timestamp time = new Timestamp(System.currentTimeMillis());
                    //System.out.println( "Time: " + time);
                    return token + " " + time;
                }
            }
//            System.out.println("search result: " + searchUser);
            return "login_failed";
        });

        get("/addfriend", (req, res) -> {
            processRoute(req, res);

            //if bad token and friends user ID then fail
            if (true) {
                return "failed_authentication";
                //if token and friends user id correct then ok
            } else
                return "okay";
        });

        get("/friends", (req, res) -> {
            processRoute(req, res);
            return "otherfriendsuserid";
        });

        // fetching a value from a search
//        Document search = userCollection.find(eq("username", "leon")).first();
//        System.out.println("serach result: " +search.getString("password"));
    }
}


