package osmdatahandler;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.amazonaws.services.securitytoken.model.GetSessionTokenResult;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Scanner;

/** Downloading from AWS */
public class AWSDataLoader {

  private static final String DURATION_AWS_KEY = "3600";
  private static final String VERSION = "2019-8";
  private static final String OSM_FOLDER[] = {"BKK_4W", "CGK_4W", "MNL_4W"};
  private static final String DIRECTORY = "/Users/abhinav.sunderrajan/" + VERSION + "/";

  private static BasicAWSCredentials getCredentials(String fileName) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    BasicAWSCredentials awsCreds = null;
    while (br.ready()) {
      String line = br.readLine();
      if (line.contains("access")) continue;
      String split[] = line.split(",");
      awsCreds = new BasicAWSCredentials(split[0], split[1]);
    }
    br.close();
    return awsCreds;
  }

  public static void main(String[] args) throws IOException {

    String pathToAWSKeyFile = args[0];
    BasicAWSCredentials basicCredentials = getCredentials(pathToAWSKeyFile);

    // for getting the MFA token.
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter MFA code: ");
    String MFA_Code = scanner.next();
    scanner.close();

    AWSSecurityTokenServiceClientBuilder stsBuilder =
        AWSSecurityTokenServiceClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(basicCredentials))
            .withRegion(Regions.AP_SOUTHEAST_1);

    AWSSecurityTokenService sts = stsBuilder.build();

    GetSessionTokenRequest tokenRequest =
        new GetSessionTokenRequest()
            .withTokenCode(MFA_Code)
            .withDurationSeconds(Integer.parseInt(DURATION_AWS_KEY))
            .withSerialNumber("serial-number-from-s3-iam");

    GetSessionTokenResult t = sts.getSessionToken(tokenRequest);

    BasicSessionCredentials basicSessionCredentials =
        new BasicSessionCredentials(
            t.getCredentials().getAccessKeyId(),
            t.getCredentials().getSecretAccessKey(),
            t.getCredentials().getSessionToken());

    AmazonS3 s3Client =
        AmazonS3ClientBuilder.standard()
            .withRegion(Regions.AP_SOUTHEAST_1)
            .withCredentials(new AWSStaticCredentialsProvider(basicSessionCredentials))
            .build();

    for (String osm : OSM_FOLDER) {
      ListObjectsV2Result result = s3Client.listObjectsV2("bucket", "prefix" + VERSION + "/" + osm);
      List<S3ObjectSummary> objects = result.getObjectSummaries();
      for (S3ObjectSummary os : objects) {
        String filePath = os.getKey();
        // download only the binary
        if (filePath.contains(".pbf")) {
          S3Object o = s3Client.getObject("bucket", filePath);
          InputStream reader = new BufferedInputStream(o.getObjectContent());
          String fileName[] = filePath.split("/");
          System.out.println("downloading " + fileName[fileName.length - 1]);
          File directory = new File(DIRECTORY);
          if (!directory.exists()) directory.mkdir();

          File file = new File(DIRECTORY + fileName[fileName.length - 1]);
          OutputStream writer = new BufferedOutputStream(new FileOutputStream(file));
          int read = -1;
          while ((read = reader.read()) != -1) {
            writer.write(read);
          }
          writer.flush();
          writer.close();
          reader.close();
          break;
        }
      }
    }
  }
}
