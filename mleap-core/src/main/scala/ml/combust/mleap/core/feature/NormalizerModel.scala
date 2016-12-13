package ml.combust.mleap.core.feature

import ml.combust.mleap.core.annotation.SparkCode
import org.apache.spark.mllib.linalg.{DenseVector, SparseVector, Vector, Vectors}

/** Class for storing a normalizer model.
  *
  * @param pNorm p normalization param
  */
@SparkCode(uri = "https://github.com/apache/spark/blob/v2.0.0/mllib/src/main/scala/org/apache/spark/ml/feature/Normalizer.scala")
case class NormalizerModel(pNorm: Double) extends Serializable {
  /** Normalizes a feature vector.
    *
    * @param features features to normalize
    * @return normalized feature vector
    */
  def apply(features: Vector): Vector = {
    val norm = Vectors.norm(features, pNorm)

    if (norm != 0.0) {
      // For dense vector, we've to allocate new memory for new output vector.
      // However, for sparse vector, the `index` array will not be changed,
      // so we can re-use it to save memory.
      features match {
        case DenseVector(vs) =>
          val values = vs.clone()
          val size = values.length
          var i = 0
          while (i < size) {
            values(i) /= norm
            i += 1
          }
          Vectors.dense(values)
        case SparseVector(size, ids, vs) =>
          val values = vs.clone()
          val nnz = values.length
          var i = 0
          while (i < nnz) {
            values(i) /= norm
            i += 1
          }
          Vectors.sparse(size, ids, values)
        case v => throw new IllegalArgumentException("Do not support vector type " + v.getClass)
      }
    } else {
      // Since the norm is zero, return the input vector object itself.
      // Note that it's safe since we always assume that the data in RDD
      // should be immutable.
      features
    }
  }
}
