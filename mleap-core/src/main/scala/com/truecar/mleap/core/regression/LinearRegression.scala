package com.truecar.mleap.core.regression

import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.linalg.mleap.BLAS

/**
 * Created by hwilkins on 11/5/15.
 */

case class LinearRegression(coefficients: Vector,
                            intercept: Double) extends Serializable {
  def apply(features: Vector): Double = {
    BLAS.dot(features, coefficients) + intercept
  }
}
