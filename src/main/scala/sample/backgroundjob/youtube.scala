package sample.backgroundjob

import java.io._
import org.slf4j.LoggerFactory
import org.json4s._
import org.json4s.native.JsonMethods._
import scala.collection.JavaConversions._
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

//import sample.util.E104Util._
import sample.service.DocumentService

class Aws_sqs{
  implicit val formats = DefaultFormats // Brings in default date formats etc.
  val logger = LoggerFactory.getLogger(this.getClass)
  val sqs = new AmazonSQSClient();
  val tokyo =  Region.getRegion(Regions.AP_NORTHEAST_1);
  sqs.setRegion(tokyo);

  val s3UploadEventQueueUrl = "https://sqs.ap-northeast-1.amazonaws.com/574562031426/S3_SNS_youtubeUpload"
  var receiveMessageRequest = new ReceiveMessageRequest(s3UploadEventQueueUrl);
      receiveMessageRequest.setWaitTimeSeconds(10)
      /* 
       * Gets and sets the MaxNumberOfMessages property. Maximum number of messages to return. 
       * SQS never returns more messages than this value but might return fewer. 
       * Not necessarily all the messages in the queue are returned (for more information,
       * see the preceding note about machine sampling). 
       * Values can be from 1 to 10. Default is 1.
       * 
       * println(receiveMessageRequest.getMaxNumberOfMessages)
      */
    
	def  receiveMessage(n: Int): Int = {
	  logger.info("Wait messages from MyQueue.");
    var messages = sqs.receiveMessage(receiveMessageRequest).getMessages 
		for (msg <- messages) {
		  var msgObj = parse(parse(msg.getBody).extract[Message].Message)
//			  println(msgObj)
//			  Sample: JObject(List((Type,JString(Notification)), (MessageId,JString(81ce85d6-ff9a-5a51-99c4-d5422aaf02b2)), (TopicArn,JString(arn:aws:sns:ap-northeast-1:574562031426:youtube_source_upload)), (Subject,JString(Amazon S3 Notification)), (Message,JString({"Records":[{"eventVersion":"2.0","eventSource":"aws:s3","awsRegion":"ap-northeast-1","eventTime":"2016-11-03T06:02:29.348Z","eventName":"ObjectCreated:Put","userIdentity":{"principalId":"AWS:AIDAI4NOKS5YHXA3H3B3M"},"requestParameters":{"sourceIPAddress":"61.221.166.143"},"responseElements":{"x-amz-request-id":"FFD64A1EFF640E1E","x-amz-id-2":"gTPqAUyervPVM2G+HhdyYXNJ9iEEUeW9RU8iRTpVFnImDWM8gj5lxUaD4huoA8d9p8jAdRAW8jg="},"s3":{"s3SchemaVersion":"1.0","configurationId":"youtube_source_upload","bucket":{"name":"104careu-youtube-usertemp-dev","ownerIdentity":{"principalId":"A3TI17FU0PPH8U"},"arn":"arn:aws:s3:::104careu-youtube-usertemp-dev"},"object":{"key":"8ee17197-d96b-47d9-935d-26fba0e9eaea","size":97,"eTag":"dc0e1335af8a4945ba0f463d8e79f130","sequencer":"00581AD2F53D46B8D7"}}}]})), (Timestamp,JString(2016-11-03T06:02:29.439Z)), (SignatureVersion,JString(1)), (Signature,JString(OTxHuTDu2zugUL1ItLvCdttvafj83s5MlEBWXo99FBxRDTXb83tgj4J/Hp5T38yn/or4j052ntn7P0ITnHqkRYxoDFbqJ4h0RC7Qmvd4+4izRp+V49oterrk0O4sEw3yuhHgXT0In2CTDqdNYrSVWWv79dUUp8TgOHTSwAfpgSBMbUsAwMCXcjsnSmkQHJJYpap8aG29PZI2m/jfy0S95klJbuVcYeYgoce2iGxfC/f/jpzaMxWBw0jfaqISKBfSGEcTdwYtSJCnOAGXAEdzyjU3DmZcWDBv6BiZdsfBsT8X6zbFc9oydvfb+r/UH/2bLyZHqWVFgIphtOGOC/0TGg==)), (SigningCertURL,JString(https://sns.ap-northeast-1.amazonaws.com/SimpleNotificationService-b95095beb82e8f6a046b3aafc7f4149a.pem)), (UnsubscribeURL,JString(https://sns.ap-northeast-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:ap-northeast-1:574562031426:youtube_source_upload:3e86c902-34aa-4d8c-98d2-243cc3ff8839))))
		  
		  var objectKey = (msgObj\("Records")\("s3")\("object")\("key")).values
		  var bucket = (msgObj\("Records")\("s3")\("bucket")\("name")).values
		  logger.info(s"New object: $objectKey in bucket: $bucket")
		  
		  var downloadResult = DocumentService.getS3Object(bucket.toString(), objectKey.toString())
		  if(downloadResult.success){
		    logger.info("will upload to yotube")
		    var youtubeReturn = DocumentService.uploadToYoutube(downloadResult.fileFullPath, objectKey.toString())
		    if(youtubeReturn.getId != null){
		        //data to mongo and kill queue
//		        VideoDAO.insert(
//		            Video(
//		                s3ObjectKey = objectKey.toString(), 
//		                videoId = youtubeReturn.getId.toString(), 
//		                uploadStatus = youtubeReturn.getStatus.getUploadStatus(),
//		                youtubeReturn = youtubeReturn.getStatus.toString()
//		            )
//		        )
		        
		        new File(downloadResult.fileFullPath).delete()     
		        //sqs.deleteMessage(new DeleteMessageRequest().withQueueUrl(s3UploadEventQueueUrl).withReceiptHandle(msg.getReceiptHandle));
		    }
		  }else{
		    logger.error("Down fail !!!")
		  }
		  //println(msg.getReceiptHandle)

		}
    
    return n * receiveMessage(n+1)
  }  	 
}


case class Message(Message: String)

object Main extends App {
   val logger = LoggerFactory.getLogger(this.getClass)
   logger.info(s"<<< Job Start >>>")
   
   val aws_sqs = new Aws_sqs  
   aws_sqs.receiveMessage(0);   
}

/*
 * This is sqs sample
 * 
    {
      "Records":[
        {
          "eventVersion":"2.0",
          "eventSource":"aws:s3",
          "awsRegion":"ap-northeast-1",
          "eventTime":"2016-11-03T06:02:29.348Z",
          "eventName":"ObjectCreated:Put",
          "userIdentity":{
          	"principalId":"AWS:AIDAI4NOKS5YHXA3H3B3M"
        	},
      		"requestParameters":{
        		"sourceIPAddress":"61.221.166.143"
        	},
        	"responseElements":{
        		"x-amz-request-id":"FFD64A1EFF640E1E",
        		"x-amz-id-2":"gTPqAUyervPVM2G+HhdyYXNJ9iEEUeW9RU8iRTpVFnImDWM8gj5lxUaD4huoA8d9p8jAdRAW8jg="
        	},
        	"s3":{
        		"s3SchemaVersion":"1.0",
        		"configurationId":"youtube_source_upload",
        		"bucket":{
        			"name":"104careu-youtube-usertemp-dev",
        			"ownerIdentity":{
        				"principalId":"A3TI17FU0PPH8U"
        			},
        			"arn":"arn:aws:s3:::104careu-youtube-usertemp-dev"
        		},
        	"object":{
        		"key":"8ee17197-d96b-47d9-935d-26fba0e9eaea",
        		"size":97,
        		"eTag":"dc0e1335af8a4945ba0f463d8e79f130",
        		"sequencer":"00581AD2F53D46B8D7"
        	}
        	}
        }
      ]
    }
*/