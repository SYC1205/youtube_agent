package sample.entity

//import org.bson.types.ObjectId
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime


case class downloadResult(
    success: Boolean,
    fileFullPath: String,
    fileName: String
    )