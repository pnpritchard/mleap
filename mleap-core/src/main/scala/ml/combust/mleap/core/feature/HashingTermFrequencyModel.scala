package ml.combust.mleap.core.feature

import ml.combust.mleap.core.annotation.SparkCode
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import ml.combust.mleap.core.util.Murmur3_x86_32._
import ml.combust.mleap.core.util.Platform

import scala.collection.mutable

object HashingTermFrequencyModel {
  val seed = 42

  def murmur3(term: Any): Int = {
    term match {
      case null => seed
      case b: Boolean => hashInt(if (b) 1 else 0, seed)
      case b: Byte => hashInt(b, seed)
      case s: Short => hashInt(s, seed)
      case i: Int => hashInt(i, seed)
      case l: Long => hashLong(l, seed)
      case f: Float => hashInt(java.lang.Float.floatToIntBits(f), seed)
      case d: Double => hashLong(java.lang.Double.doubleToLongBits(d), seed)
      case s: String =>
        val utf8 = s.getBytes("UTF-8")
        hashUnsafeBytes(utf8, Platform.BYTE_ARRAY_OFFSET, utf8.length, seed)
      case _ => throw new IllegalStateException("HashingTF with murmur3 algorithm does not " +
        s"support type ${term.getClass.getCanonicalName} of input data.")
    }
  }
}

/** Class for hashing token frequencies into a vector.
  *
  * Source adapted from: Apache Spark Utils and HashingTF, see NOTICE for contributors
  *
  * @param numFeatures size of feature vector to hash into
  */
@SparkCode(uri = "https://github.com/apache/spark/blob/v2.0.0/mllib/src/main/scala/org/apache/spark/ml/feature/HashingTF.scala")
case class HashingTermFrequencyModel(numFeatures: Int = 1 << 18,
                                     binary: Boolean = false) {
  def indexOf(term: Any): Int = nonNegativeMod(term.##, numFeatures)

  def apply(document: Iterable[_]): Vector = {
    val termFrequencies = mutable.HashMap.empty[Int, Double]
    val setTF = if (binary) (i: Int) => 1.0 else (i: Int) => termFrequencies.getOrElse(i, 0.0) + 1.0
    document.foreach { term =>
      val i = nonNegativeMod(HashingTermFrequencyModel.murmur3(term), numFeatures)
      termFrequencies.put(i, setTF(i))
    }
    Vectors.sparse(numFeatures, termFrequencies.toSeq)
  }

    /* Calculates 'x' modulo 'mod', takes to consideration sign of x,
 * i.e. if 'x' is negative, than 'x' % 'mod' is negative too
 * so function return (x % mod) + mod in that case.
 */
  @SparkCode(uri = "https://github.com/apache/spark/blob/v2.0.0/core/src/main/scala/org/apache/spark/util/Utils.scala")
  def nonNegativeMod(x: Int, mod: Int): Int = {
    val rawMod = x % mod
    rawMod + (if (rawMod < 0) mod else 0)
  }
}
