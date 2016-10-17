package ml.combust.mleap.core.feature

import org.apache.spark.ml.linalg.Vectors
import org.scalatest.FunSpec

/**
  * Created by mikhail on 10/16/16.
  */
class BinarizerModelSpec extends FunSpec {
  describe("#apply") {
    it("takes a double and returns a 1.0 or a 0.0 based on the threshold") {
      val binarizer = BinarizerModel(0.5)
      val input = 0.6
      val expectedOutput = 1.0

      assert(binarizer.apply(input) == expectedOutput)
    }
    it("takes a vector of doubles and returns a vector of 1.0 or a 0.0 based on the threshold") {
      val binarizer = BinarizerModel(0.5)
      val input = Array(0.6, 0.4, 0.5)
      val expectedOutput = Array(1.0, 0.0, 1.0)

      assert(binarizer.apply(Vectors.dense(input)).toArray.sameElements(expectedOutput))
    }
  }
}
