/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api;

/**
 *
 * @author izabela.borges
 */

    
    
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analytics.model.Accounts;
import com.google.api.services.analytics.model.GaData;
import com.google.api.services.analytics.model.McfData;
import com.google.api.services.analytics.model.Profiles;
import com.google.api.services.analytics.model.Webproperties;
import java.io.BufferedWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A simple example of how to access the Google Analytics API using a service
 * account.
 */
public class HelloAnalytics {

  private static final String APPLICATION_NAME = "Hello Analytics";
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static final String KEY_FILE_LOCATION = "D:\\Users\\izabela.borges\\Documents\\NetBeansProjects\\API\\src\\api\\client_secrets.p12";
  private static final String SERVICE_ACCOUNT_EMAIL = "642452945-compute@developer.gserviceaccount.com";
  private static final String Saida_Dados  = "D:\\Users\\izabela.borges\\Desktop\\arquivo.json";
  public static void main(String[] args) {
    
      

        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            fw = new FileWriter(Saida_Dados); // A classe FileWriter é usada para escrever o arquivo em json.
            bw = new BufferedWriter(fw); // O construtor BufferedWriter recebe o objeto fw como parâmetro. Assim podemos usar o método .write

            Analytics analytics = initializeAnalytics();
            String profile = getFirstProfileId(analytics);
            System.out.println("First Profile Id: " + profile);
            printResults(bw, getResults(bw, analytics, profile));

          
            bw.flush();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



  private static Analytics initializeAnalytics() throws Exception {
    // Initializes an authorized analytics service object.

    // Construct a GoogleCredential object with the service account email
    // and p12 file downloaded from the developer console.
    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    GoogleCredential credential = new GoogleCredential.Builder()
        .setTransport(httpTransport)
        .setJsonFactory(JSON_FACTORY)
        .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
        .setServiceAccountPrivateKeyFromP12File(new File(KEY_FILE_LOCATION))
        .setServiceAccountScopes(AnalyticsScopes.all())
        .build();

    // Construct the Analytics service object.
    return new Analytics.Builder(httpTransport, JSON_FACTORY, credential)
        .setApplicationName(APPLICATION_NAME).build();
  }

  private static String getFirstProfileId(Analytics analytics) throws IOException {
    // Get the first view (profile) ID for the authorized user.
    String profileId = null;

    // Query for the list of all accounts associated with the service account.
    Accounts accounts = analytics.management().accounts().list().execute();

    if (accounts.getItems().isEmpty()) {
      System.err.println("No accounts found");
    } else {
      String firstAccountId = accounts.getItems().get(0).getId();

      // Query for the list of properties associated with the first account.
      Webproperties properties = analytics.management().webproperties()
          .list(firstAccountId).execute();

      if (properties.getItems().isEmpty()) {
        System.err.println("No Webproperties found");
      } else {
        String firstWebpropertyId = properties.getItems().get(0).getId();

        // Query for the list views (profiles) associated with the property.
        Profiles profiles = analytics.management().profiles()
            .list(firstAccountId, firstWebpropertyId).execute();

        if (profiles.getItems().isEmpty()) {
          System.err.println("No views (profiles) found");
        } else {
          // Return the first (view) profile associated with the property.
          profileId = profiles.getItems().get(0).getId(); // Define qual perfil do analytics, caso você tenha mais de um perfil. Vá alterando o valor do get(0), para get(1), get(2)..
        }
      }
    }
    return profileId;
  }
  

  private static GaData getResults(Analytics analytics, String profileId) throws IOException {
    // Query the Core Reporting API for the number of sessions
    // in the past seven days.
    return analytics.data().ga()
        .get("ga:" + profileId, "7daysAgo", "today", "ga:sessions")
        .execute();
  }

  private static void printResults(GaData results) {
    // Parse the response from the Core Reporting API for
    // the profile name and number of sessions.
    if (results != null && !results.getRows().isEmpty()) {
      System.out.println("View (Profile) Name: "
        + results.getProfileInfo().getProfileName());
      System.out.println("Total Sessions: " + results.getRows().get(0).get(0));
    } else {
      System.out.println("No results found");
    }
  }
  
   public static McfData getResults(BufferedWriter bw, Analytics analytics, String tableId) throws IOException {

        return analytics.data().mcf()
                .get("ga:" + tableId,
                        "28daysAgo",
                        "yesterday",
                        "mcf:assistedConversions") //informe as métricas
                .setDimensions("mcf:sourceMediumPath,mcf:transactionId,mcf:conversionDate") // as dimensões
                .setSort("mcf:conversionDate") //Caso queira usar um filtro só colocar .setFilters("")
                .setMaxResults(3000).execute();

    }

    private static void printResults(BufferedWriter bw, McfData results) throws IOException {
         System.out.println(" " + results);
        bw.write(results.toPrettyString());
    }
}

