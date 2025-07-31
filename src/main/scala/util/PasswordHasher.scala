package util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Utility class for password hashing and verification
 * Uses SHA-256 with salt for secure password storage
 */
object PasswordHasher {
  
  private val ALGORITHM = "SHA-256"
  private val SALT_LENGTH = 32
  private val ITERATIONS = 10000
  
  /**
   * Generate a random salt
   * @return Base64 encoded salt string
   */
  private def generateSalt(): String = {
    val random = new SecureRandom()
    val salt = new Array[Byte](SALT_LENGTH)
    random.nextBytes(salt)
    Base64.getEncoder.encodeToString(salt)
  }
  
  /**
   * Hash a password with a given salt
   * @param password the plain text password
   * @param salt the salt to use
   * @return the hashed password
   */
  private def hashPasswordWithSalt(password: String, salt: String): String = {
    val md = MessageDigest.getInstance(ALGORITHM)
    val saltBytes = Base64.getDecoder.decode(salt)
    
    // Add salt to password
    md.update(saltBytes)
    val passwordBytes = password.getBytes("UTF-8")
    
    // Apply multiple iterations for security
    var hash = md.digest(passwordBytes)
    for (_ <- 1 until ITERATIONS) {
      md.reset()
      hash = md.digest(hash)
    }
    
    Base64.getEncoder.encodeToString(hash)
  }
  
  /**
   * Hash a password for storage
   * @param password the plain text password
   * @return a string containing salt and hash separated by ':'
   */
  def hashPassword(password: String): String = {
    val salt = generateSalt()
    val hash = hashPasswordWithSalt(password, salt)
    s"$salt:$hash"
  }
  
  /**
   * Verify a password against a stored hash
   * @param password the plain text password to verify
   * @param storedHash the stored hash (salt:hash format)
   * @return true if password matches, false otherwise
   */
  def verifyPassword(password: String, storedHash: String): Boolean = {
    try {
      val parts = storedHash.split(":")
      if (parts.length != 2) {
        return false
      }
      
      val salt = parts(0)
      val hash = parts(1)
      val computedHash = hashPasswordWithSalt(password, salt)
      
      // Use constant-time comparison to prevent timing attacks
      MessageDigest.isEqual(hash.getBytes("UTF-8"), computedHash.getBytes("UTF-8"))
    } catch {
      case _: Exception => false
    }
  }
  
  /**
   * Check if a password meets basic security requirements
   * @param password the password to validate
   * @return true if password is valid, false otherwise
   */
  def isPasswordValid(password: String): Boolean = {
    password.length >= 8 && 
    password.exists(_.isDigit) && 
    password.exists(_.isLetter) &&
    password.exists(c => !c.isLetterOrDigit)
  }
  
  /**
   * Get password requirements description
   * @return string describing password requirements
   */
  def getPasswordRequirements: String = {
    "Password must be at least 8 characters long and contain at least one letter, one digit, and one special character."
  }
}
