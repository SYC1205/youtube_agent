package controllers

import java.util.UUID

import play.api._
import play.api.mvc._
import play.api.libs.json._

import org.json4s.jackson.Serialization
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s._


import sample.service._

class Application extends Controller {

  def index = Action {
    //Ok(views.html.index("Your new application is ready."))
    //Ok("Hello world")   
    Ok(views.html.react())
  }
  
  // The json keys. The 'id' field was added as without it we would get a warning like this:
  // Warning: Each child in an array or iterator should have a unique "key" prop. Check the render method of CommentList. See https://fb.me/react-warning-keys for more information.
  val JSON_KEY_COMMENTS = "comments"
  val JSON_KEY_AUTHOR = "author"
  val JSON_KEY_TEXT = "text"
  val JSON_KEY_ID = "id"

  // Initialise the comments list
  var commentsJson: JsArray = Json.arr(
    Json.obj(JSON_KEY_ID -> UUID.randomUUID().toString, JSON_KEY_AUTHOR -> "Pete Hunt", JSON_KEY_TEXT -> "This is one comment"),
    Json.obj(JSON_KEY_ID -> UUID.randomUUID().toString, JSON_KEY_AUTHOR -> "Jordan Walke", JSON_KEY_TEXT -> "This is *another* comment")
  )

  // Returns the comments list
  def comments = Action {
    Ok(commentsJson)
  }

  // Adds a new comment to the list and returns it
  def comment(author: String, text: String) = Action {
    val newComment = Json.obj(
      JSON_KEY_ID -> UUID.randomUUID().toString,
      JSON_KEY_AUTHOR -> author,
      JSON_KEY_TEXT -> text)
    commentsJson = commentsJson :+ newComment
    Ok(newComment)
  }
  
  def getYoutube(s3ObjectKeyOrYoutubeId: String) = Action {
    implicit val formats = DefaultFormats
    
    val getYoutube = DocumentService.getYoutubeInfo(s3ObjectKeyOrYoutubeId)
    
    Ok(getYoutube)
  }
  
  def getS3PreSignedUrl = Action {
    var getS3PreSignedUrl = DocumentService.getS3PreSignedUrl()
     Ok(getS3PreSignedUrl)
  }

}
