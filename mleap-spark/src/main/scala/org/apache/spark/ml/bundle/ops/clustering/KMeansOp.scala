package org.apache.spark.ml.bundle.ops.clustering

import ml.combust.bundle.BundleContext
import ml.combust.bundle.dsl._
import ml.combust.bundle.op.{OpModel, OpNode}
import org.apache.spark.ml.bundle.SparkBundleContext
import org.apache.spark.ml.clustering.KMeansModel
import org.apache.spark.mllib.linalg.{DenseVector, SparseVector}
import org.apache.spark.mllib.clustering
import org.apache.spark.mllib.linalg.Vectors

/**
  * Created by hollinwilkins on 9/30/16.
  */
class KMeansOp extends OpNode[SparkBundleContext, KMeansModel, KMeansModel] {
  override val Model: OpModel[SparkBundleContext, KMeansModel] = new OpModel[SparkBundleContext, KMeansModel] {
    override val klazz: Class[KMeansModel] = classOf[KMeansModel]

    override def opName: String = Bundle.BuiltinOps.clustering.k_means

    override def store(model: Model, obj: KMeansModel)
                      (implicit context: BundleContext[SparkBundleContext]): Model = {
      model.withAttr("cluster_centers", Value.tensorList(
        value = obj.clusterCenters.map(_.toArray.toSeq),
        dims = Seq(-1)))
    }

    override def load(model: Model)
                     (implicit context: BundleContext[SparkBundleContext]): KMeansModel = {
      val clusterCenters = model.value("cluster_centers").
        getTensorList[Double].toArray.
        map(t => Vectors.dense(t.toArray))
      val mllibModel = new clustering.KMeansModel(clusterCenters)

      new KMeansModel(uid = "", parentModel = mllibModel)
    }
  }

  override val klazz: Class[KMeansModel] = classOf[KMeansModel]

  override def name(node: KMeansModel): String = node.uid

  override def model(node: KMeansModel): KMeansModel = node

  override def load(node: Node, model: KMeansModel)
                   (implicit context: BundleContext[SparkBundleContext]): KMeansModel = {
    val clusterCenters = model.clusterCenters.map {
      case DenseVector(values) => Vectors.dense(values)
      case SparseVector(size, indices, values) => Vectors.sparse(size, indices, values)
    }

    val m = new KMeansModel(uid = node.name,
      parentModel = new clustering.KMeansModel(clusterCenters))
    m.set(m.featuresCol, node.shape.input("features").name)
    m.set(m.predictionCol, node.shape.input("prediction").name)
    m
  }

  override def shape(node: KMeansModel): Shape = Shape().withInput(node.getFeaturesCol, "features").
    withOutput(node.getPredictionCol, "prediction")
}
