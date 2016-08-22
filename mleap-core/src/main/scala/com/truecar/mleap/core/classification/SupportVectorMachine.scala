package com.truecar.mleap.core.classification

import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.linalg.mleap.BLAS

/**
  * Created by hollinwilkins on 4/14/16.
  */
case class SupportVectorMachine(coefficients: Vector,
                                intercept: Double,
                                threshold: Option[Double] = None) {
  def apply(features: Vector): Double = {
    val margin = BLAS.dot(coefficients, features) + intercept

    threshold match {
      case Some(t) => if (margin > t) 1.0 else 0.0
      case None => margin
    }
  }
}
