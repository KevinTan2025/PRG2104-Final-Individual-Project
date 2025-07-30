package database

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet, SQLException}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.io.File

/**
 * Database connection manager for SQLite
 * Implements singleton pattern to ensure single database connection
 */
object DatabaseConnection {
  // Place database in src/main/resources directory
  private val DB_DIR = "src/main/resources"
  private val DB_NAME = "community_platform.db"
  private val DB_PATH = s"$DB_DIR/$DB_NAME"
  private val DB_URL = s"jdbc:sqlite:$DB_PATH"
  private var connection: Option[Connection] = None
  
  // Ensure database directory exists
  private def ensureDbDirectoryExists(): Unit = {
    val dbDir = new File(DB_DIR)
    if (!dbDir.exists()) {
      dbDir.mkdirs()
    }
  }
  
  /**
   * Get database connection, create if not exists
   */
  def getConnection: Connection = {
    connection match {
      case Some(conn) if !conn.isClosed => conn
      case _ =>
        try {
          ensureDbDirectoryExists()
          Class.forName("org.sqlite.JDBC")
          val conn = DriverManager.getConnection(DB_URL)
          connection = Some(conn)
          conn
        } catch {
          case e: SQLException =>
            throw new RuntimeException(s"Failed to connect to database: ${e.getMessage}")
          case e: ClassNotFoundException =>
            throw new RuntimeException("SQLite JDBC driver not found")
        }
    }
  }
  
  /**
   * Execute SQL query and return ResultSet
   */
  def executeQuery(sql: String, params: Any*): ResultSet = {
    val conn = getConnection
    val stmt = conn.prepareStatement(sql)
    setParameters(stmt, params: _*)
    stmt.executeQuery()
  }
  
  /**
   * Execute SQL update/insert/delete and return affected rows count
   */
  def executeUpdate(sql: String, params: Any*): Int = {
    val conn = getConnection
    val stmt = conn.prepareStatement(sql)
    setParameters(stmt, params: _*)
    val result = stmt.executeUpdate()
    stmt.close()
    result
  }
  
  /**
   * Execute SQL update/insert and return generated ID
   */
  def executeUpdateWithGeneratedKey(sql: String, params: Any*): Option[String] = {
    val conn = getConnection
    val stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
    setParameters(stmt, params: _*)
    stmt.executeUpdate()
    
    val keys = stmt.getGeneratedKeys
    val result = if (keys.next()) Some(keys.getString(1)) else None
    keys.close()
    stmt.close()
    result
  }
  
  /**
   * Set parameters for prepared statement
   */
  private def setParameters(stmt: PreparedStatement, params: Any*): Unit = {
    params.zipWithIndex.foreach { case (param, index) =>
      param match {
        case s: String => stmt.setString(index + 1, s)
        case i: Int => stmt.setInt(index + 1, i)
        case l: Long => stmt.setLong(index + 1, l)
        case b: Boolean => stmt.setBoolean(index + 1, b)
        case d: Double => stmt.setDouble(index + 1, d)
        case ldt: LocalDateTime => stmt.setString(index + 1, ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        case opt: Option[_] => opt match {
          case Some(value) => 
            param match {
              case ldt: LocalDateTime => stmt.setString(index + 1, ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
              case _ => stmt.setObject(index + 1, value)
            }
          case None => stmt.setNull(index + 1, java.sql.Types.NULL)
        }
        case null => stmt.setNull(index + 1, java.sql.Types.NULL)
        case _ => stmt.setObject(index + 1, param)
      }
    }
  }
  
  /**
   * Close database connection
   */
  def close(): Unit = {
    connection.foreach { conn =>
      if (!conn.isClosed) {
        conn.close()
      }
    }
    connection = None
  }
  
  /**
   * Execute SQL script from string
   */
  def executeSqlScript(script: String): Unit = {
    val conn = getConnection
    val statements = script.split(";").filter(_.trim.nonEmpty)
    
    statements.foreach { sql =>
      try {
        val stmt = conn.createStatement()
        stmt.execute(sql.trim)
        stmt.close()
      } catch {
        case e: SQLException =>
          println(s"Error executing SQL: ${sql.trim}")
          throw e
      }
    }
  }
  
  /**
   * Check if database exists and has tables
   */
  def isDatabaseInitialized: Boolean = {
    try {
      val rs = executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='users'")
      val exists = rs.next()
      rs.close()
      exists
    } catch {
      case _: SQLException => false
    }
  }
  
  /**
   * Utility method to convert database timestamp to LocalDateTime
   */
  def parseDateTime(dateTimeStr: String): LocalDateTime = {
    LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  }
  
  /**
   * Utility method to convert LocalDateTime to database string
   */
  def formatDateTime(dateTime: LocalDateTime): String = {
    dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  }
}
