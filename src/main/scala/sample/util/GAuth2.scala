package sample.util

import play.api._
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

class GAuth2 {
    /**
     * Define a global instance of the HTTP transport.
     */
    val HTTP_TRANSPORT = new NetHttpTransport();

    /**
     * Define a global instance of the JSON factory.
     */
    val JSON_FACTORY = new JacksonFactory();

    /**
     * This is the directory that will be used under the user's home directory where OAuth tokens will be stored.
     */
    val CREDENTIALS_DIRECTORY = ".oauth-credentials";

    /**
     * Authorizes the installed application to access user's protected data.
     *
     * @param scopes              list of scopes needed to run youtube upload.
     * @param credentialDatastore name of the credential datastore to cache OAuth tokens
     */
    @throws(classOf[IOException])
    def authorize(scopes: List[String], credentialDatastore: String) :Credential = {
            var clientSecrets = getClientSecret
            // This creates the credentials datastore at ~/.oauth-credentials/${credentialDatastore}
            //var fileDataStoreFactory = new FileDataStoreFactory(new File(System.getProperty("user.home") + "/" + CREDENTIALS_DIRECTORY));
            //var datastore = fileDataStoreFactory.getDataStore(credentialDatastore);
    
            var flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scopes).setAccessType("offline")
                    .build();
    
            // Build the local server and bind it to port 8080
            var localReceiver = new LocalServerReceiver.Builder().setPort(5000).build();
    
            // Authorize.
            var credential = new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
            println("AccessToken: " + credential.getAccessToken.toString() + ", RefreshToken: "+credential.getRefreshToken + ", timeout: " + credential.getExpiresInSeconds)
            return credential
    }
    
    @throws(classOf[TokenResponseException])
    def authorize() :Credential = {
      var clientSecrets = getClientSecret
      var refreshToken = "1/9z9pBq-6GjQxROEgwYY_ZVMxxfedbeX21VBnxIlLt6M"
      var googleRefreshTokenresponse = new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
          refreshToken, clientSecrets.getDetails.getClientId, clientSecrets.getDetails().getClientSecret).execute();
      var credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(googleRefreshTokenresponse.getAccessToken)
      credential
    }
    
    @throws(classOf[IOException])
    def getClientSecret() :GoogleClientSecrets = {
        // Load client secrets.
        var clientSecretReader = new InputStreamReader(getClass.getResourceAsStream("/credentials.json"));
        var clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretReader);

        // Checks that the defaults have been replaced (Default = "Enter X here").
        if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println(
                    "Enter Client ID and Secret from https://console.developers.google.com/project/_/apiui/credential "
                            + "into src/main/resources/credentials.json");
            System.exit(1);
            clientSecrets
        }else{
          clientSecrets
        }
    }
    
  
}