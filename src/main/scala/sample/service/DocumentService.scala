package sample.service

import org.joda.time.DateTime
import com.google.common.collect.MapMaker
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s._
//import org.json4s.jackson.Serialization
import scala.util.control._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable
import scala.util.control.Breaks._
import java.util.UUID
import java.util.Calendar
import java.util.ArrayList
import java.net.{ HttpURLConnection, URL }
import java.io._

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.S3Object;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploader.UploadState._
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model._
import com.google.api.services.youtube.model.Video
import com.google.common.collect.Lists;

import sample.entity._;
import sample.util._;

object DocumentService {
  val logger = LoggerFactory.getLogger(this.getClass)
  
  val bucketName = "104careu-youtube-usertemp-dev";
  
  implicit val formats = DefaultFormats
  
  val VIDEO_FILE_FORMAT = "video/*"

  
  def getS3PreSignedUrl() = {
    val s3client = new AmazonS3Client(new ProfileCredentialsProvider())
    var objectKey  =  "*** Provide an object key ***"
		try {
			System.out.println("Generating pre-signed URL.")
			var expiration = new java.util.Date()
			var milliSeconds = expiration.getTime()
			milliSeconds += 1000 * 60 * 60 // Add 1 hour.
			expiration.setTime(milliSeconds)

			objectKey  = UUID.randomUUID().toString()
			
			var generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey)
			generatePresignedUrlRequest.setMethod(HttpMethod.PUT)
			generatePresignedUrlRequest.setExpiration(expiration)
			generatePresignedUrlRequest.setContentType("binary/octet-stream")
			
			var preSigned = s3client.generatePresignedUrl(generatePresignedUrlRequest)

			//System.out.println("Pre-Signed URL = " + preSigned)
			logger.info(s"Pre-Signed URL > $preSigned")
      var returnUrl = s"""{"S3PreSignedUrl": "$preSigned","objectKey":"$objectKey"}"""
			returnUrl
			
		} catch{
		  case exception:AmazonServiceException =>
  		  System.out.println("Caught an AmazonServiceException, " +
  					"which means your request made it " +
  					"to Amazon S3, but was rejected with an error response " +
  			"for some reason.");
  			System.out.println("Error Message: " + exception.getMessage());
  			System.out.println("HTTP  Code: "    + exception.getStatusCode());
  			System.out.println("AWS Error Code:" + exception.getErrorCode());
  			System.out.println("Error Type:    " + exception.getErrorType());
  			System.out.println("Request ID:    " + exception.getRequestId());
  			
  			
  			var returnUrl = s"""{"aws_exception": $exception.getErrorMessage()}"""
			  returnUrl
  			
		  case ace:AmazonClientException =>
		    System.out.println("Caught an AmazonClientException, " +
					"which means the client encountered " +
					"an internal error while trying to communicate" +
					" with S3, " +
			    "such as not being able to access the network.");
			  System.out.println("Error Message: " + ace.getMessage());
			  
			  var returnUrl = s"""{"aws_client_exception": $ace.getMessage()}"""
			  returnUrl
			  
		} 
  }
  /*
  def updateYoutubeInfo(s3ObjectKeyOrYoutubeId:String, title:String, description:String, tags:ArrayList[String]) :com.e104.happiness.entity.Video= {
      val s3ObjectKeys: List[String] = List(s3ObjectKeyOrYoutubeId)
      //val getYoutube = youtubeDao.findById("5820a3648d6a9b8f6fde8d45").getOrElse(throw NoData("Youtube video not found!!"))
      var getFromMongo = VideoDAO.findInKeys("s3ObjectKey", s3ObjectKeys)
      if(getFromMongo.isEmpty){
          getFromMongo = VideoDAO.findInKeys("videoId", s3ObjectKeys)
          if(getFromMongo.isEmpty){
              return throw NoData("Youtube video not found!!")
          }
      }
      
      val getYoutube = getFromMongo.toSeq.last
      logger.info(s"get document > $getYoutube")
      println(getYoutube._id.toString())
      println(getYoutube.videoId.toString())
           
      var youtube = getYoutubeObj
      
      // Call the YouTube Data API's youtube.videos.list method to
      // retrieve the resource that represents the specified video.
      var listVideosRequest = youtube.videos().list("snippet").setId(getYoutube.videoId);
      var listResponse = listVideosRequest.execute();

      // Since the API request specified a unique video ID, the API
      // response should return exactly one video. If the response does
      // not contain a video, then the specified video ID was not found.
      var videoList = listResponse.getItems();
      if (videoList.isEmpty()) {
          System.out.println("Can't find a video with ID: " + getYoutube.videoId);
          return throw NoData("Youtube video not found!!")
      }

      // Extract the snippet from the video resource.
      var video = videoList.get(0);
      
      // Most of the video's metadata is set on the VideoSnippet object.
      var snippet = video.getSnippet();

      // This code uses a Calendar instance to create a unique name and
      // description for test purposes so that you can easily upload
      // multiple files. You should remove this code from your project
      // and use your own standard names instead.
      snippet.setTitle(title);
      snippet.setDescription(description);         
      
      // Set the keyword tags that you want to associate with the video.
      if(!tags.isEmpty()){
          snippet.setTags(tags);
      }

      // Add the completed snippet object to the video resource.
      video.setSnippet(snippet);
      
      var updateYoutube = youtube.videos().update("snippet", video)
      
      var updateYoutubeReturn = updateYoutube.execute()
      
      println("updateYoutubeReturn: " + updateYoutubeReturn)
      /*
      var returnYoutube = com.e104.happiness.entity.Video(
    		                _id = getYoutube._id,
    		                id = getYoutube.id,
    		                s3ObjectKey = getYoutube.s3ObjectKey,
    		                videoId = getYoutube.videoId,
    		                uploadStatus = getYoutube.uploadStatus,
    		                youtubeReturn = updateYoutubeReturn.toString(),
    		                memberId = null,
                        channel = updateYoutubeReturn.getSnippet.getChannelTitle,
                        title = updateYoutubeReturn.getSnippet.getTitle,
                        snapImage = getYoutube.snapImage,
                        iFrameCode = getYoutube.iFrameCode,
                        orderBy = 99
    		            )
    		            
    	VideoDAO.updateThis(returnYoutube)
    	* */
    	
    	returnYoutube
  }
  * 
  * */
  
  
  def getYoutubeInfo(s3ObjectKeyOrYoutubeId:String) : String = {
      val s3ObjectKeys: List[String] = List(s3ObjectKeyOrYoutubeId)
      //val getYoutube = youtubeDao.findById("5820a3648d6a9b8f6fde8d45").getOrElse(throw NoData("Youtube video not found!!"))
//      var getFromMongo = VideoDAO.findInKeys("s3ObjectKey", s3ObjectKeys)
//      if(getFromMongo.isEmpty){
//          getFromMongo = VideoDAO.findInKeys("videoId", s3ObjectKeys)
//          if(getFromMongo.isEmpty){
//              return throw NoData("Youtube video not found!!")
//          }
//      }
      
//      val getYoutube = getFromMongo.toSeq.last
//      logger.info(s"get document > $getYoutube")
//      println(getYoutube._id.toString())
//      // The status maybe uploaded, rejected, processed.
//      // if status is uploaded, call youtube api for reflash info
//      if(!getYoutube.uploadStatus.equals("uploaded")){
//          getYoutube      
//      }else{
          println("reflash info")
          var youtube = getYoutubeObj
          var videoList = youtube.videos.list("snippet,processingDetails,player,status")      
          //videoList.setId(getYoutube.videoId)
          videoList.setId(s3ObjectKeyOrYoutubeId)
          var returnedVideo = videoList.execute()
          var returnedItemObj = parse(returnedVideo.getItems.toString)
          var state = (returnedItemObj\("status")\("uploadStatus")).values.toString()
          var returnString = (returnedItemObj \ ("player") \ ("embedHtml")).values.toString()
          println(returnString)
//          state match{
//                case "processed" =>
//                    //println("processed")
//                    var returnYoutube = com.e104.happiness.entity.Video(
//                        _id = getYoutube._id,
//    		                s3ObjectKey = getYoutube.s3ObjectKey,
//    		                videoId = getYoutube.videoId,
//    		                uploadStatus = state.toString(),
//    		                youtubeReturn = returnedVideo.toString(),
//                        channel = (returnedItemObj\("snippet")\("channelTitle")).values.toString,
//                        title = (returnedItemObj\("snippet")\("title")).values.toString,
//                        snapImage = (returnedItemObj\("snippet")\("thumbnails")\("medium")\("url")).values.toString,
//                        iFrameCode = (returnedItemObj\("player")\("embedHtml")).values.toString,
//                        orderBy = 99
//    		            )
//    		            
//    		            println(VideoDAO.updateThis(returnYoutube))
//    		            
//    		            returnYoutube
//                case "rejected" =>
//                    //println("rejected")
//                    var returnYoutube = com.e104.happiness.entity.Video(
//                        _id = getYoutube._id,
//    		                s3ObjectKey = getYoutube.s3ObjectKey,
//    		                videoId = getYoutube.videoId,
//    		                uploadStatus = state.toString(),
//    		                youtubeReturn = returnedVideo.toString()
//    		            )
//    		            
//    		            VideoDAO.updateThis(returnYoutube)
//    		            
//    		            returnYoutube
//    		         case everythingElse =>
//    		            var returnYoutube = com.e104.happiness.entity.Video(
//    		                _id = getYoutube._id,
//    		                s3ObjectKey = getYoutube.s3ObjectKey,
//    		                videoId = getYoutube.videoId,
//    		                uploadStatus = state.toString(),
//    		                youtubeReturn = returnedVideo.toString(),
//                        channel = (returnedItemObj\("snippet")\("channelTitle")).values.toString(),
//                        title = (returnedItemObj\("snippet")\("title")).values.toString(),
//                        snapImage = (returnedItemObj\("snippet")\("thumbnails")\("medium")\("url")).values.toString(),
//                        iFrameCode = (returnedItemObj\("player")\("embedHtml")).values.toString(),
//                        orderBy = 99 
//    		            )
//    		            
//    		            VideoDAO.updateThis(returnYoutube)
//    		            
//    		            returnYoutube
            //}
        //}   
       returnString
  }

  
  def getS3Object(bucketName: String, objectKey:String) : downloadResult = {
    var s3client = new AmazonS3Client(new ProfileCredentialsProvider())
		try{
			  logger.info(s"Get S3 Object: $objectKey from $bucketName")

        var s3object = s3client.getObject(new GetObjectRequest(bucketName, objectKey));
        //println("Content-Type: "  +  s3object.getObjectMetadata().getContentType());
        //println("Content-Disposition: "  +  s3object.getObjectMetadata().getContentDisposition());
        var fileName = ""
        var contentDisposition =  s3object.getObjectMetadata().getContentDisposition().split(" ")
        for(str <- contentDisposition){
          if(str.indexOf("filename") != -1)
            fileName = str.substring(str.indexOf("=")+1, str.length())
        }
        
        val file = new File(fileName)
        if(file.exists()){
          file.delete()
        }
        
        inputToFile(s3object.getObjectContent(), file)
        //displayTextInputStream(s3object.getObjectContent());
        //println(downloadResult)
		} catch{
		  case exception:AmazonServiceException =>
  		  System.out.println("Caught an AmazonServiceException, " +
  					"which means your request made it " +
  					"to Amazon S3, but was rejected with an error response " +
  			"for some reason.");
  			System.out.println("Error Message: " + exception.getMessage());
  			System.out.println("HTTP  Code: "    + exception.getStatusCode());
  			System.out.println("AWS Error Code:" + exception.getErrorCode());
  			System.out.println("Error Type:    " + exception.getErrorType());
  			System.out.println("Request ID:    " + exception.getRequestId());
  			
  			
  			var returnUrl = s"""{"aws_exception": $exception.getErrorMessage()}"""
			  downloadResult(false, "", "")
  			
		  case ace:AmazonClientException =>
		    System.out.println("Caught an AmazonClientException, " +
					"which means the client encountered " +
					"an internal error while trying to communicate" +
					" with S3, " +
			    "such as not being able to access the network.");
			  System.out.println("Error Message: " + ace.getMessage());
			  
			  var returnUrl = s"""{"aws_client_exception": $ace.getMessage()}"""
			  downloadResult(false, "", "")
			  
		} 
  }
  
  def uploadToYoutube(videoPath: String, VIDEO_ID: String) :Video= {
      var returnObj = new com.google.api.services.youtube.model.Video
      try{
          var youtube = getYoutubeObj
          
          logger.info("Uploading: " + videoPath);
          
          // Add extra information to the video before uploading.
          var videoObjectDefiningMetadata = new Video();
  
          // Set the video to be publicly visible. This is the default
          // setting. Other supporting settings are "unlisted" and "private."
          var status = new VideoStatus();
          status.setPrivacyStatus("public");
          videoObjectDefiningMetadata.setStatus(status);
          
          // Most of the video's metadata is set on the VideoSnippet object.
          var snippet = new VideoSnippet();
          
          // This code uses a Calendar instance to create a unique name and
          // description for test purposes so that you can easily upload
          // multiple files. You should remove this code from your project
          // and use your own standard names instead.
          var cal = Calendar.getInstance();
          snippet.setTitle("Test Upload via Java on " + cal.getTime());
          snippet.setDescription("Video uploaded via YouTube Data API V3 using the Java library " + "on " + cal.getTime());
          
          // Set the keyword tags that you want to associate with the video.
          var tags = new ArrayList[String];
          tags.add("test");
          tags.add("example");
          tags.add("java");
          tags.add("YouTube Data API V3");
          tags.add("erase me");
          snippet.setTags(tags);
  
          // Add the completed snippet object to the video resource.
          videoObjectDefiningMetadata.setSnippet(snippet);
  
          var initialFile = new File(videoPath);
          var fileInputStream = new FileInputStream(initialFile);
          var mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT, fileInputStream);
  
          // Insert the video. The command sends three arguments. The first
          // specifies which information the API request is setting and which
          // information the API response should return. The second argument
          // is the video resource that contains metadata about the new video.
          // The third argument is the actual video content.
          var videoInsert = youtube.videos()
                .insert("snippet,statistics,status", videoObjectDefiningMetadata, mediaContent);

          // Set the upload type and add an event listener.
          var uploader = videoInsert.getMediaHttpUploader();
  
          // Indicate whether direct media upload is enabled. A value of
          // "True" indicates that direct media upload is enabled and that
          // the entire media content will be uploaded in a single request.
          // A value of "False," which is the default, indicates that the
          // request will use the resumable media upload protocol, which
          // supports the ability to resume an upload operation after a
          // network interruption or other transmission failure, saving
          // time and bandwidth in the event of network failures.
          uploader.setDirectUploadEnabled(false);
          
          @throws(classOf[IOException])
          var progressListener = new MediaHttpUploaderProgressListener() {
              def progressChanged(uploader: MediaHttpUploader) {
                uploader.getUploadState() match{ 
                    case INITIATION_STARTED =>
                        System.out.println("  - Initiation Started");
                    case INITIATION_COMPLETE =>
                        System.out.println("  - Initiation Completed");
                    case MEDIA_IN_PROGRESS =>
                        System.out.println("  - Upload in progress");
                        System.out.println("  - Upload percentage: " + uploader.getProgress());
                    case MEDIA_COMPLETE =>
                        System.out.println("  - Upload Completed!");
                    case NOT_STARTED =>
                        System.out.println("  - Upload Not Started!");
                }
                
              }
           };
           
          uploader.setProgressListener(progressListener);

          // Call the API and upload the video.
          var returnedVideo = videoInsert.execute();
//  Print data about the newly inserted video from the API response.
//          System.out.println("\n================== Returned Video ==================\n");
//          System.out.println("  - Id: " + returnedVideo.getId());
//          System.out.println("  - Title: " + returnedVideo.getSnippet().getTitle());
//          System.out.println("  - Tags: " + returnedVideo.getSnippet().getTags());
//          System.out.println("  - Privacy Status: " + returnedVideo.getStatus().getPrivacyStatus());
//          System.out.println("  - Video Count: " + returnedVideo.getStatistics().getViewCount());
//          System.out.println("  - Status: " + returnedVideo.getStatus);
//          System.out.println("  - Youtube return: " + returnedVideo.toString());
          logger.info("Youtube return: " + returnedVideo.toString() +"\n")  
          returnedVideo
          
      }catch{
             case e:GoogleJsonResponseException =>
               System.err.println("GoogleJsonResponseException code: " + e.getMessage)
               System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
                        + e.getDetails().getMessage());
               returnObj
               //e.printStackTrace();
                
             case e:IOException =>
                System.err.println("IOException: " + e.getMessage());
                returnObj
                //e.printStackTrace();
                
             case t:Throwable =>
                System.err.println("Throwable: " + t.getMessage());
                returnObj
                //t.printStackTrace();
       }
  }
  
  def getYoutubeObj() :YouTube= {
      var auth = new GAuth2
      var scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");
      
      // Authorize the request.
      //var credential = auth.authorize(scopes, "uploadvideo");
      var credential = auth.authorize();
          
      // This object is used to make YouTube Data API requests. 
      var youtube = new YouTube.Builder(auth.HTTP_TRANSPORT, auth.JSON_FACTORY, credential)
                    .setApplicationName("youtube-104happiness").build();
      
      youtube
  }
  
  def inputToFile(is: java.io.InputStream, f: java.io.File): downloadResult ={
      //val in = scala.io.Source.fromInputStream(is)
      val fos = new java.io.FileOutputStream(f)
      
      try {
        val bytes = new Array[Byte](1024)
        Iterator.continually (is.read).takeWhile (-1 !=).foreach (fos.write)
        //in.foreach(fos.write(_))
        //fos.write(Stream.continually(in.reader()).takeWhile(-1 !=).map(_.).toArray)
        downloadResult(true, f.getCanonicalPath, f.getName)
      }catch{
        case io:IOException =>
          System.out.println("Error Message: " + io.getMessage());  
          downloadResult(false, "", f.getName)
      }finally{ 
        fos.close

      }
  }
  
  def displayTextInputStream(input: InputStream ){
      try{
      	// Read one text line at a time and display.
          var reader = new BufferedReader(new InputStreamReader(input));
          breakable{
            while (true){
              var line = reader.readLine();
              if (line == null) break
              System.out.println("    " + line);
            }
          }
          System.out.println();
      }catch{
        case io:IOException =>
          System.out.println("Error Message: " + io.getMessage());  
      } 
   }
}

//object abc extends App {
//  //val tags = List("App","WebThumbnail","WTT", "Web")
//  //val doc = new DocumentService().getDocumentUrl("d8012a7f9af34293ac435bcdde44b9bc11",tags)
//  //val doc = new DocumentService().swapUploadStatus("d8012a7f9af34293ac435bcdde44b9bc11", true)
//  //val doc = new DocumentService().updateDoucmentExpiration("6a29384675c4459cac76a60c4c0be2ac11")
//  //val doc = new DocumentService().getDocumentTeachingMaterial("452345235435235")
//  //val doc = new DocumentService().updateDoucmentExpiration("a95556635f714a62af4d7c343a70879711")
//  //val doc = new DocumentService().delDocument("6fdf0f26c9e242ad99894b50c5c567f411")
//  val doc = new DocumentService().uploadFileToS3("test01")
//  val doc = new DocumentService().getFileFormS3("test01")
//  //println("executed")
//}
