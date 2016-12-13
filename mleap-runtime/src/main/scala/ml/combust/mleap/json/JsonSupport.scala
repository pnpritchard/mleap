package ml.combust.mleap.json

import ml.combust.mleap.runtime.{DefaultLeapFrame, LeapFrame, MleapContext}
import ml.combust.mleap.runtime.types._
import org.apache.spark.mllib.linalg.{DenseVector, SparseVector, Vector}
import spray.json.DefaultJsonProtocol._
import spray.json._
import spray.json.{JsValue, JsonFormat}

import scala.language.implicitConversions

/**
  * Created by hollinwilkins on 8/23/16.
  */
trait JsonSupport {
  implicit val mleapListTypeWriterFormat: JsonWriter[ArrayType] = new JsonWriter[ArrayType] {
    override def write(obj: ArrayType): JsValue = {
      JsObject("type" -> JsString("list"),
        "base" -> obj.base.toJson)
    }
  }

  implicit def mleapListTypeReaderFormat(implicit context: MleapContext): JsonReader[ArrayType] = new JsonReader[ArrayType] {
    override def read(json: JsValue): ArrayType = {
      val obj = json.asJsObject("invalid list type")

      ArrayType(obj.fields("base").convertTo[DataType])
    }
  }

  implicit val mleapTensorTypeFormat: JsonFormat[TensorType] = lazyFormat(new JsonFormat[TensorType] {
    override def write(obj: TensorType): JsValue = {
      JsObject("type" -> JsString("tensor"),
        "base" -> mleapBasicTypeFormat.write(obj.base),
        "dimensions" -> obj.dimensions.toJson)
    }

    override def read(json: JsValue): TensorType = {
      val obj = json.asJsObject("invalid tensor type")

      TensorType(base = mleapBasicTypeFormat.read(obj.fields("base")),
        dimensions = obj.fields("dimensions").convertTo[Seq[Int]])
    }
  })

  val mleapBasicTypeFormat: JsonFormat[BasicType] = lazyFormat(new JsonFormat[BasicType] {
    override def write(obj: BasicType): JsValue = obj match {
      case DoubleType => JsString("double")
      case StringType => JsString("string")
      case LongType => JsString("long")
      case BooleanType => JsString("boolean")
      case IntegerType => JsString("integer")
    }

    override def read(json: JsValue): BasicType = json match {
      case JsString("double") => DoubleType
      case JsString("string") => StringType
      case JsString("long") => LongType
      case JsString("boolean") => BooleanType
      case JsString("integer") => IntegerType
      case _ => throw new Error("Invalid basic type") // TODO: better error
    }
  })

  implicit val mleapCustomTypeWriterFormat: JsonWriter[CustomType] = new JsonWriter[CustomType] {
    override def write(obj: CustomType): JsValue = JsObject("type" -> JsString("custom"),
      "custom" -> JsString(obj.name))
  }

  implicit def mleapCustomTypeReaderFormat(implicit context: MleapContext): JsonReader[CustomType] = new JsonReader[CustomType] {
    override def read(json: JsValue): CustomType = {
      val obj = json.asJsObject("invalid custom type")

      obj.fields("custom") match {
        case JsString(custom) => context.customTypeForAlias(custom)
        case _ => deserializationError("invalid custom type")
      }
    }
  }

  implicit val mleapDataTypeWriterFormat: JsonWriter[DataType] = new JsonWriter[DataType] {
    override def write(obj: DataType): JsValue = obj match {
      case bt: BasicType => mleapBasicTypeFormat.write(bt)
      case lt: ArrayType => mleapListTypeWriterFormat.write(lt)
      case tt: TensorType => mleapTensorTypeFormat.write(tt)
      case ct: CustomType => mleapCustomTypeWriterFormat.write(ct)
      case AnyType => serializationError("AnyType not supported for JSON serialization")
    }
  }

  implicit def mleapDataTypeReaderFormat(implicit context: MleapContext): JsonReader[DataType] = new JsonReader[DataType] {
    override def read(json: JsValue): DataType = json match {
      case obj: JsString => mleapBasicTypeFormat.read(obj)
      case obj: JsObject =>
        obj.fields.get("type") match {
          case Some(JsString("list")) => mleapListTypeReaderFormat.read(obj)
          case Some(JsString("tensor")) => mleapTensorTypeFormat.read(obj)
          case Some(JsString("custom")) => mleapCustomTypeReaderFormat.read(obj)
          case _ => deserializationError(s"invalid data type: ${obj.fields.get("type")}")
        }
      case _ => throw new Error("Invalid data type") // TODO: better error
    }
  }

  implicit val mleapDenseVectorFormat: JsonFormat[DenseVector] = new JsonFormat[DenseVector] {
    override def write(obj: DenseVector): JsValue = obj.values.toJson
    override def read(json: JsValue): DenseVector = new DenseVector(json.convertTo[Array[Double]])
  }

  implicit val mleapSparseVectorFormat: JsonFormat[SparseVector] = new JsonFormat[SparseVector] {
    override def write(obj: SparseVector): JsValue = JsObject("size" -> obj.size.toJson,
      "indices" -> obj.indices.toJson,
      "values" -> obj.values.toJson)

    override def read(json: JsValue): SparseVector = {
      val obj = json.asJsObject("invalid sparse vector")
      new SparseVector(size = obj.fields("size").convertTo[Int],
        indices = obj.fields("indices").convertTo[Array[Int]],
        values = obj.fields("values").convertTo[Array[Double]])
    }
  }

  implicit val mleapVectorFormat: JsonFormat[Vector] = new JsonFormat[Vector] {
    override def write(obj: Vector): JsValue = obj match {
      case obj: DenseVector => mleapDenseVectorFormat.write(obj)
      case obj: SparseVector => mleapSparseVectorFormat.write(obj)
      case _ => serializationError("invalid vector")
    }

    override def read(json: JsValue): Vector = json match {
      case json: JsArray => mleapDenseVectorFormat.read(json)
      case json: JsObject => mleapSparseVectorFormat.read(json)
      case _ => deserializationError(s"invalid vector: $json")
    }
  }

  implicit val mleapStructFieldWriterFormat: JsonWriter[StructField] = new JsonWriter[StructField] {
    override def write(obj: StructField): JsValue = {
      JsObject("name" -> JsString(obj.name),
        "type" -> obj.dataType.toJson)
    }
  }

  implicit def mleapStructFieldReaderFormat(implicit context: MleapContext): JsonReader[StructField] = new JsonReader[StructField] {
    override def read(json: JsValue): StructField = json match {
      case obj: JsObject =>
        val name = StringJsonFormat.read(obj.fields("name"))
        val dataType = mleapDataTypeReaderFormat.read(obj.fields("type"))

        StructField(name = name, dataType = dataType)
      case _ => throw new Error("Invalid StructField") // TODO: better error
    }
  }

  implicit val mleapStructTypeWriterFormat: JsonWriter[StructType] = new JsonWriter[StructType] {
    implicit val writer = lift(mleapStructFieldWriterFormat)

    override def write(obj: StructType): JsValue = {
      JsObject("fields" -> obj.fields.toJson)
    }
  }

  implicit def mleapStructTypeReaderFormat(implicit context: MleapContext): RootJsonReader[StructType] = new RootJsonReader[StructType] {
    implicit val reader = lift(mleapStructFieldReaderFormat)

    override def read(json: JsValue): StructType = json match {
      case json: JsObject =>
        json.fields.get("fields") match {
          case Some(fields: JsArray) =>
            StructType(fields.convertTo[Seq[StructField]]).get
          case _ => throw new Error("Invalid StructType") // TODO: better error
        }
      case _ => throw new Error("Invalid StructType") // TODO: better error
    }
  }

  implicit def mleapLeapFrameWriterFormat[LF <: LeapFrame[LF]]: JsonWriter[LF] = new JsonWriter[LF] {
    override def write(obj: LF): JsValue = {
      val formatter = DatasetFormat(obj.schema)
      val rows = formatter.write(obj.dataset)
      JsObject(("schema", obj.schema.toJson), ("rows", rows))
    }
  }

  implicit def mleapDefaultLeapFrameReaderFormat(implicit context: MleapContext): RootJsonReader[DefaultLeapFrame] = new RootJsonReader[DefaultLeapFrame] {
    override def read(json: JsValue): DefaultLeapFrame = {
      val obj = json.asJsObject("invalid LeapFrame")

      val schema = obj.fields("schema").convertTo[StructType]
      val formatter = DatasetFormat(schema)
      val rows = formatter.read(obj.fields("rows"))

      DefaultLeapFrame(schema, rows)
    }
  }
}
object JsonSupport extends JsonSupport
