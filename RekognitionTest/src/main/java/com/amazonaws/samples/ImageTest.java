package com.amazonaws.samples;

import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class ImageTest {

	public static void main(String[] args) throws Exception {

		
		String bucket = "trial-image-bucket";
		Regions clientRegion = Regions.AP_SOUTH_1;
	

		AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(clientRegion).build();
		
		AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion(clientRegion)
				.build();

		S3Objects.inBucket(s3, bucket).forEach((S3ObjectSummary objectSummary) -> {
			String photo = objectSummary.getKey();
			
	
			
			DetectLabelsRequest request = new DetectLabelsRequest()
					.withImage(new Image().withS3Object(new S3Object().withName(photo).withBucket(bucket)))
					.withMaxLabels(10).withMinConfidence(75F);
			
			try {
				DetectLabelsResult result = rekognitionClient.detectLabels(request);
				List<Label> labels = result.getLabels();

				System.out.println("Detected labels for " + photo);
				for (Label label : labels) {

					// TODO: if gliding and confidence found is higher than 90 - validate

					System.out.println(label.getName() + ": " + label.getConfidence().toString());
				}

			} catch (AmazonRekognitionException e) {
				e.printStackTrace();
			}
			finally {
				System.out.println("");
			}

		});

		
	
	}
}
