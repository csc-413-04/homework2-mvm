package main.java;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import spark.Request;
import spark.Response;

import java.sql.Timestamp;
import java.util.Date;
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
            long msDate1 =  new Date().getTime();
            Timestamp ts = new Timestamp(msDate1);
            long msDate2 = ts.getTime();
            Date date = new Date( msDate2 );
//            System.out.println("msDate1 = " + msDate1 );
//            System.out.println("ts = " + ts );
//            System.out.println("msDate2 = " + msDate2 );
//            System.out.println("date = " + date );
            if (searchUser != null) {
                String pass = req.queryParams("password");      //password from the request
                String mPass = searchUser.getString("password");        //password from the collection
                //System.out.println("mPASS: " + mPass);
                if (mPass.equalsIgnoreCase(pass)) {
                    Object token = searchUser.get("_id");
                    // create a new document
                    Document authDoc = new Document("token", token)
                            .append("time", date);
                    // insert document into collection
                    authCollection.insertOne(authDoc);
                    return token + " " + date;
                }
            }
//            System.out.println("search result: " + searchUser);
            return "login_failed";
        });

        get("/addfriend", (req, res) -> {
            processRoute(req, res);
            String token = req.queryParams("token");
            ObjectId newToken = new ObjectId(token);
            Document searchID = userCollection.find(eq("_id", newToken)).first();
            System.out.println("User: " + searchID);
            if (searchID != null) {
                String friendToken = req.queryParams("friend");
                ObjectId newFriendToken = new ObjectId(friendToken);
                Document searchFriendID = userCollection.find(eq("_id", newFriendToken)).first();
                if (searchFriendID != null) {
                    System.out.println("Friend: " + searchFriendID);
                    Bson filter = new Document("_id", newToken);
                    Bson newFriend = new Document("friend", newFriendToken);
                    Bson updateDoc = new Document("$push", newFriend);
                    userCollection.updateOne(filter, updateDoc);
                    return "okay";
                }
            }
                return "failed_authentication";
        });

        get("/friends", (req, res) -> {
            processRoute(req, res);
            String token = req.queryParams("token");
            ObjectId newToken = new ObjectId(token);
            Document searchID = userCollection.find(eq("_id", newToken)).first();
            if (searchID != null) {
                Object friend = searchID.get("friend");
                return friend;
            }
            return "wrong token";
        });
    }
}


