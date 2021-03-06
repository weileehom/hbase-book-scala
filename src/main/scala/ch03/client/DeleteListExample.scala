package ch03.client

import org.apache.hadoop.hbase.client.{Delete, ConnectionFactory}
import org.apache.hadoop.hbase.{TableName, HBaseConfiguration}
import util.{CFValues, HBaseHelper}
import scala.collection.JavaConverters._
import util.ByteConverter._

object DeleteListExample extends App {
  val conf = HBaseConfiguration.create()

  val helper = new HBaseHelper(conf)

  val tableName = TableName.valueOf("testtable")

  helper.dropTable(tableName)
  helper.createTable(tableName, List("colfam1", "colfam2"), 100)

  helper.put(tableName, List("row1", "row2", "row3"), List("colfam1", "colfam2"), List(
    CFValues("qual1", 1, "val1"),
    CFValues("qual1", 2, "val2"),
    CFValues("qual2", 3, "val3"),
    CFValues("qual2", 4, "val4"),
    CFValues("qual3", 5, "val5"),
    CFValues("qual3", 6, "val6")
  ))

  println("Before delete call...")
  helper.dump(tableName, List("row1", "row2", "row3"))

  val connection = ConnectionFactory.createConnection(conf)
  val table = connection.getTable(tableName)

  val delete1 = new Delete("row1".toUTF8Byte).setTimestamp(4)

  val delete2 = new Delete("row2".toUTF8Byte)
    .addColumn("colfam1".toUTF8Byte, "qual1".toUTF8Byte)
    .addColumns("colfam2".toUTF8Byte, "qual3".toUTF8Byte, 5)

  val delete3 = new Delete("row3".toUTF8Byte)
    .addFamily("colfam1".toUTF8Byte)
    .addFamily("colfam2".toUTF8Byte, 3)

//  val deletes = List(delete1, delete2, delete3)
  val deletes = new java.util.ArrayList[Delete]()

  deletes.add(delete1)
  deletes.add(delete2)
  deletes.add(delete3)

  /*
   * > The handed-in deletes parameter, that is, the list of De lete instances,
   * is modified to only contain the failed delete instances when the call returns.
   * In other words, when everything has succee‐ ded, the list will be empty.
   *
   * That is why passing deletes.asJava fails
   */
  table.delete(deletes) //deletes.asJava => java.lang.UnsupportedOperationException
  table.close()
  connection.close()

  println("After delete call...")
  helper.dump(tableName, List("row1", "row2", "row3"))
  helper.close()
}
