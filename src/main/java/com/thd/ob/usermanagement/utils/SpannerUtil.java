package com.thd.ob.usermanagement.utils;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.spanner.*;
import com.google.spanner.admin.database.v1.CreateDatabaseMetadata;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SpannerUtil {

    Spanner spanner;
    SpannerOptions options;

    @Value("classpath:oauth/svc-acct.json")
    private Resource resource;

    @PostConstruct
    public void init() throws IOException{
        //Resource resource = UsermanagementApplication.getApplicationContext().getResource("classpath:oauth/svc-acct.json");

        options = SpannerOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(resource.getInputStream()))
                .setProjectId("centering-rex-212817")
                .build();
//        ServiceAccountCredentials.fromStream(new FileInputStream("/path/to/my/key.json")))
        System.out.println("creds: "+options.getCredentials());
        spanner = options.getService();
        System.out.println("#### Spanner project: "+spanner.getOptions().getProjectId());
    }


    public boolean createDatabase() {
        DatabaseId db = DatabaseId.of(options.getProjectId(),"spanner-test","test-db");
        DatabaseClient dbClient = spanner.getDatabaseClient(db);
        DatabaseAdminClient dbAdminClient = spanner.getDatabaseAdminClient();

        Operation<Database, CreateDatabaseMetadata> op = dbAdminClient
                .createDatabase(
                        db.getInstanceId().getInstance(),
                        db.getDatabase(),
                        Arrays.asList(
                                "CREATE TABLE Singers (\n"
                                        + "  SingerId   INT64 NOT NULL,\n"
                                        + "  FirstName  STRING(1024),\n"
                                        + "  LastName   STRING(1024),\n"
                                        + "  SingerInfo BYTES(MAX)\n"
                                        + ") PRIMARY KEY (SingerId)",
                                "CREATE TABLE Albums (\n"
                                        + "  SingerId     INT64 NOT NULL,\n"
                                        + "  AlbumId      INT64 NOT NULL,\n"
                                        + "  AlbumTitle   STRING(MAX)\n"
                                        + ") PRIMARY KEY (SingerId, AlbumId),\n"
                                        + "  INTERLEAVE IN PARENT Singers ON DELETE CASCADE"));
        Database db2 = op.waitFor().getResult();
        System.out.println("Created database [" + db2.getId() + "]");
        return true;
    }

    public void insertUser(String userName , String password) {
        List<Mutation> mutations = new ArrayList<>();
        mutations.add(
                Mutation.newInsertBuilder("user_credentials")
                .set("username")
                .to(userName)
                .set("password")
                .to(password)
                .build()
        );
        DatabaseId db = DatabaseId.of(options.getProjectId(),"test-instance","testdb");
        DatabaseClient dbClient = spanner.getDatabaseClient(db);
        dbClient.write(mutations);

    }

    public JSONArray query() {
        // singleUse() can be used to execute a single read or query against Cloud Spanner.
        DatabaseId db = DatabaseId.of(options.getProjectId(),"test-instance","testdb");
        DatabaseClient dbClient = spanner.getDatabaseClient(db);
        ResultSet resultSet =
                dbClient
                        .singleUse()
                        .executeQuery(Statement.of("SELECT username,password  FROM user_credentials"));
        System.out.println("------QUERY------");
        JSONArray results = new JSONArray();

        while (resultSet.next()) {

            results.put(new JSONObject("{\"UserName\":\""+resultSet.getString(0)+"\", \"Password\":\""+resultSet.getString(1)+"\"}"));
            System.out.printf(
                    "User Name: %s ;  Password: %s\n", resultSet.getString(0), resultSet.getString(1));
        }
        System.out.println("-----------------");
        return results;

    }

    public void delete(String user) {
        // singleUse() can be used to execute a single read or query against Cloud Spanner.
        DatabaseId db = DatabaseId.of(options.getProjectId(),"test-instance","testdb");
        DatabaseClient dbClient = spanner.getDatabaseClient(db);
        dbClient.write(Arrays.asList(Mutation.delete("user_credentials", Key.of(user))));

//        ResultSet resultSet =
//                dbClient
//                        .singleUse()
//                        .executeQuery(Statement.of("DELETE FROM Singers where SingerId = 2"));
//        while (resultSet.next()) {
//            System.out.printf(
//                    "First Name: %s ;  Last Name: %s\n", resultSet.getString(0), resultSet.getString(1));
//        }
    }

    public String readbyId(String user) {
        DatabaseId db = DatabaseId.of(options.getProjectId(),"test-instance","testdb");
        DatabaseClient dbClient = spanner.getDatabaseClient(db);
        Struct resultSet =
                dbClient
                        .singleUse()
                        .readRow("user_credentials",
                                // KeySet.all() can be used to read all rows in a table. KeySet exposes other
                                // methods to read only a subset of the table.
                                Key.of(user),
                                Arrays.asList( "Username", "Password"));
//        while (resultSet.next()) {
//            System.out.printf(
//                    "%d %d %s\n", resultSet.getLong(0), resultSet.getLong(1), resultSet.getString(2));
//        }

            if(resultSet == null || resultSet.isNull(0)){
                return null;
            }

           return resultSet.getString(0) + " : " +resultSet.getString(1);
    }
}
