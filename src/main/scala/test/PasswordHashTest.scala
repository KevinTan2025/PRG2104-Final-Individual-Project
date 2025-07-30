package test

import database.DatabaseConnection
import util.PasswordHasher

/**
 * Simple test to verify password hashes in database
 */
object PasswordHashTest {
  def main(args: Array[String]): Unit = {
    println("Testing password hashes in database...")
    
    try {
      val rs = DatabaseConnection.executeQuery("SELECT username, password_hash FROM users")
      
      while (rs.next()) {
        val username = rs.getString("username")
        val storedHash = rs.getString("password_hash")
        val testPassword = "Password123!"
        
        val isValid = PasswordHasher.verifyPassword(testPassword, storedHash)
        
        println(s"User: $username")
        println(s"Hash starts with: ${storedHash.take(20)}...")
        println(s"Password verification: ${if (isValid) "✓ SUCCESS" else "✗ FAILED"}")
        println()
      }
      
      rs.close()
      
    } catch {
      case e: Exception =>
        println(s"Error: ${e.getMessage}")
    }
  }
}
