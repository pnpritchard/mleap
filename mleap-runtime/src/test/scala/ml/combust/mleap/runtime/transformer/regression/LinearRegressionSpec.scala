package ml.combust.mleap.runtime.transformer.regression

import ml.combust.mleap.core.regression.LinearRegressionModel
import ml.combust.mleap.runtime.{LeapFrame, LocalDataset, Row}
import ml.combust.mleap.runtime.types.{StructField, StructType, TensorType}
import org.apache.spark.mllib.linalg.Vectors
import org.scalatest.FunSpec

/**
  * Created by hollinwilkins on 9/15/16.
  */
class LinearRegressionSpec extends FunSpec {
  val schema = StructType(Seq(StructField("features", TensorType.doubleVector()))).get
  val dataset = LocalDataset(Seq(Row(Vectors.dense(Array(20.0, 10.0, 5.0)))))
  val frame = LeapFrame(schema, dataset)
  val linearRegression = LinearRegression(featuresCol = "features",
    predictionCol = "prediction",
    model = LinearRegressionModel(coefficients = Vectors.dense(Array(1.0, 0.5, 5.0)),
      intercept = 73.0))

  describe("LinearRegression") {
    describe("#transform") {
      it("executes the linear regression model and outputs a prediction") {
        val frame2 = linearRegression.transform(frame).get
        val prediction = frame2.dataset(0).getDouble(1)

        assert(prediction == 123.0)
      }

      describe("with invalid features input") {
        it("returns a Failure") {
          val frame2 = linearRegression.copy(featuresCol = "bad_features").transform(frame)

          assert(frame2.isFailure)
        }
      }
    }
  }
}
