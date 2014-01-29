package libraries;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;

import java.net.UnknownHostException;
import java.util.Arrays;

public class MongoConnection {

    private MongoClient mongoClient;
    public MongoConnection() throws UnknownHostException {
        mongoClient = new MongoClient( "localhost" , 27017 );

        //NOTE: WHEN WE PUT MONGO ON AWS, WE WILL NEED TO EDIT THIS CONSTRUCTOR
    }
    public DB getDB(String database) {
        return mongoClient.getDB(database);
    }
    public void closeConnection() {
        mongoClient.close();
    }

}