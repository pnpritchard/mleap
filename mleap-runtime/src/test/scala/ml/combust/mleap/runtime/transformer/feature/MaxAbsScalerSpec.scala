package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.MaxAbsScalerModel
import ml.combust.mleap.runtime.{LeapFrame, Row, LocalDataset}
import ml.combust.mleap.runtime.types.{TensorType, StructField, StructType}
import org.apache.spark.mllib.linalg.Vectors
import org.scalatest.FunSpec

/**
  * Created by mikhail on 9/25/16.
  */
class MaxAbsScalerSpec extends FunSpec{
  val schema = StructType(Seq(StructField("test_vec", TensorType.doubleVector()))).get
  val dataset = LocalDataset(Seq(Row(Vectors.dense(Array(0.0, 20.0, 20.0)))))
  val frame = LeapFrame(schema, dataset)

  val maxAbsScaler = MaxAbsScaler(inputCol = "test_vec",
    outputCol = "test_normalized",
    model = MaxAbsScalerModel(Vectors.dense(Array(10.0, 20.0, 40.0))))

  describe("#transform") {
    it("scales the input data by maximum value vector") {
      val frame2 = maxAbsScaler.transform(frame).get
      val data = frame2.dataset.toArray
      val norm = data(0).getVector(1)

      assert(norm(0) == 0.0)
      assert(norm(1) == 1.0)
      assert(norm(2) == 0.5)
    }

    describe("with invalid input column") {
      val maxAbsScaler2 = maxAbsScaler.copy(inputCol = "bad_input")

      it("returns a Failure") { assert(maxAbsScaler2.transform(frame).isFailure) }
    }
  }
}
