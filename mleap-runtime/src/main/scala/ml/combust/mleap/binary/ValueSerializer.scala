package ml.combust.mleap.binary

import java.io.{DataInputStream, DataOutputStream}
import java.nio.charset.Charset

import ml.combust.mleap.runtime.types._
import org.apache.spark.mllib.linalg.{DenseVector, SparseVector, Vector, Vectors}

import scala.reflect.ClassTag

/**
  * Created by hollinwilkins on 11/1/16.
  */
object ValueSerializer {
  val byteCharset = Charset.forName("UTF-8")

  def serializerForDataType(dataType: DataType): ValueSerializer[Any] = (dataType match {
    case DoubleType => DoubleSerializer
    case StringType => StringSerializer
    case IntegerType => IntegerSerializer
    case LongType => LongSerializer
    case BooleanType => BooleanSerializer
    case at: ArrayType =>
      at.base match {
        case DoubleType => ArraySerializer(DoubleSerializer)
        case StringType => ArraySerializer(StringSerializer)
        case IntegerType => ArraySerializer(IntegerSerializer)
        case LongType => ArraySerializer(LongSerializer)
        case BooleanType => ArraySerializer(BooleanSerializer)
        case _ => ArraySerializer(serializerForDataType(at.base))
      }
    case tt: TensorType if tt.base == DoubleType && tt.dimensions.length == 1 => VectorSerializer
    case ct: CustomType => CustomSerializer(ct)
    case _ => throw new IllegalArgumentException(s"invalid data type for serialization: $dataType")
  }).asInstanceOf[ValueSerializer[Any]]
}

trait ValueSerializer[T] {
  def write(value: T, out: DataOutputStream): Unit
  def read(in: DataInputStream): T
}

object DoubleSerializer extends ValueSerializer[Double] {
  override def write(value: Double, out: DataOutputStream): Unit = out.writeDouble(value)
  override def read(in: DataInputStream): Double = in.readDouble()
}

object IntegerSerializer extends ValueSerializer[Int] {
  override def write(value: Int, out: DataOutputStream): Unit = out.writeInt(value)
  override def read(in: DataInputStream): Int = in.readInt()
}

object LongSerializer extends ValueSerializer[Long] {
  override def write(value: Long, out: DataOutputStream): Unit = out.writeLong(value)
  override def read(in: DataInputStream): Long = in.readLong()
}

object BooleanSerializer extends ValueSerializer[Boolean] {
  override def write(value: Boolean, out: DataOutputStream): Unit = out.writeBoolean(value)
  override def read(in: DataInputStream): Boolean = in.readBoolean()
}

object StringSerializer extends ValueSerializer[String] {
  override def write(value: String, out: DataOutputStream): Unit = {
    val bytes = value.getBytes(ValueSerializer.byteCharset)
    out.writeInt(bytes.length)
    out.write(bytes)
  }
  override def read(in: DataInputStream): String = {
    val bytes = new Array[Byte](in.readInt())
    in.readFully(bytes)
    new String(bytes, ValueSerializer.byteCharset)
  }
}

case class ArraySerializer[T: ClassTag](base: ValueSerializer[T]) extends ValueSerializer[Array[T]] {
  override def write(value: Array[T], out: DataOutputStream): Unit = {
    out.writeInt(value.length)
    for(v <- value) { base.write(v, out) }
  }

  override def read(in: DataInputStream): Array[T] = {
    val length = in.readInt()
    val arr = new Array[T](length)
    for(i <- 0 until length) { arr(i) = base.read(in) }
    arr
  }
}

object VectorSerializer extends ValueSerializer[Vector] {
  val DENSE_VECTOR = 0
  val SPARSE_VECTOR = 1

  val indicesSerializer = ArraySerializer(IntegerSerializer)
  val valuesSerializer = ArraySerializer(DoubleSerializer)

  override def write(value: Vector, out: DataOutputStream): Unit = value match {
    case DenseVector(values) =>
      out.writeInt(DENSE_VECTOR)
      valuesSerializer.write(value.toArray, out)
    case SparseVector(size, indices, values) =>
      out.writeInt(SPARSE_VECTOR)
      out.writeInt(size)
      indicesSerializer.write(indices, out)
      valuesSerializer.write(values, out)
  }

  override def read(in: DataInputStream): Vector = {
    in.readInt() match {
      case DENSE_VECTOR => Vectors.dense(valuesSerializer.read(in))
      case SPARSE_VECTOR => Vectors.sparse(in.readInt(), indicesSerializer.read(in), valuesSerializer.read(in))
      case _ => throw new IllegalArgumentException("invalid vector type")
    }
  }
}

case class CustomSerializer(ct: CustomType) extends ValueSerializer[Any] {
  override def write(value: Any, out: DataOutputStream): Unit = {
    val bytes = ct.toBytes(value)
    out.writeInt(bytes.length)
    out.write(bytes)
  }

  override def read(in: DataInputStream): Any = {
    val length = in.readInt()
    val bytes = new Array[Byte](length)
    in.readFully(bytes)
    ct.fromBytes(bytes)
  }
}
