package com.amazonaws.samples;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class Twitter {

	public static void main(String args[]) throws TwitterException, IOException {

		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setDebugEnabled(true).setOAuthConsumerKey("7HVJ84Cqpc4gWvMjSimUUQXxp")
				.setOAuthConsumerSecret("1Rw9vhXTKD0p3H4HBA61yeweUy4KeFUMs4ZripOvrzdiaP7Vl3")
				.setOAuthAccessToken("1414458680888623108-6f54bTxZUQRaeW9FaOsYUc0kN7zjXU")
				.setOAuthAccessTokenSecret("Z7Qw8lHhuNsvzoKF0E8IIgD4DEoZo1pEmpsXLDyD8N6tu");

		TwitterFactory tf = new TwitterFactory(configurationBuilder.build());
		twitter4j.Twitter twitter = tf.getInstance();

		// aws s3 storage

		Regions clientRegion = Regions.AP_SOUTH_1;
		String bucketName = "trial-image-bucket";
		

//	//getting my home timeline
//	List<Status> status = twitter.getHomeTimeline();
//	
//	for(Status s: status) {
//	System.out.println(s.getUser().getName()+ " ----> "+ s.getText());
//}

		// getting a user's timeline of tweets
		Scanner in = new Scanner(System.in);
		System.out.println("enter user name");
		String user = in.nextLine();
		in.close();

		List<Status> userStatus = twitter.getUserTimeline(user);
		List<String> userPhotos = new ArrayList<String>();

		System.out.println("Showing @" + user + "'s user timeline.");
		for (Status stObj : userStatus) {
			if (stObj.isRetweet())
				continue;
			else {
				MediaEntity[] media = stObj.getMediaEntities();
				for (MediaEntity m : media) {
					userPhotos.add(m.getMediaURL());
				}

				System.out.println("@" + stObj.getUser().getScreenName() + " - " + stObj.getText());

			}
		}
		for (String str : userPhotos) {
			URL url = new URL(str);
			String fileName = url.getFile();
			BufferedImage img = ImageIO.read(url);
			File file = new File(
					System.getProperty("user.dir") + "\\pictures" + fileName.substring(fileName.lastIndexOf("/")));
			ImageIO.write(img, "jpg", file);
			
			String filePath = file.getAbsolutePath();
			String fileObjKeyName = fileName.substring(fileName.lastIndexOf("/"));
			System.out.println(filePath);
			

			try {

				AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(clientRegion).build();
				
				PutObjectRequest request = new PutObjectRequest(bucketName, fileObjKeyName.substring(1), new File(filePath));
				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentType("image/jpeg");
				metadata.addUserMetadata("title", fileName.substring(fileName.lastIndexOf("/")));
				request.setMetadata(metadata);
				s3Client.putObject(request);

			} catch (AmazonServiceException e) {
				// The call was transmitted successfully, but Amazon S3 couldn't process
				// it, so it returned an error response.
				e.printStackTrace();
			} catch (SdkClientException e) {
				// Amazon S3 couldn't be contacted for a response, or the client
				// couldn't parse the response from Amazon S3.
				e.printStackTrace();
			}
		}
		System.out.println("done, and uploaded to bucket");

	}

}
