package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.runtime.transformer.Transformer
import ml.combust.mleap.runtime.transformer.builder.TransformBuilder

import scala.util.Try

/**
  * Created by mikhail on 10/16/16.
  */
case class Binarizer(uid: String = Transformer.uniqueName("binarizer"),
                     inputCol: String,
                     outputCol: String,
                     model: Binarizer) extends Transformer {
  override def transform[TB <: TransformBuilder[TB]](builder: TB): Try[TB] = {

  }
}
