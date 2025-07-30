package util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import scala.util.{Try, Success, Failure}

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
   * 哈希密码用于存储
   * @param password 明文密码
   * @return Try[String] 成功返回包含盐值和哈希的字符串，失败返回异常
   */
  def hashPassword(password: String): String = {
    val salt = generateSalt()
    val hash = hashPasswordWithSalt(password, salt)
    s"$salt:$hash"
  }
  
  /**
   * 安全的密码哈希方法，返回 Try 类型
   * @param password 明文密码
   * @return Try[String] 哈希结果
   */
  def hashPasswordSafe(password: String): Try[String] = {
    Try {
      val salt = generateSalt()
      val hash = hashPasswordWithSalt(password, salt)
      s"$salt:$hash"
    }
  }
  
  /**
   * 验证密码
   * @param password 明文密码
   * @param storedHash 存储的哈希值（盐值:哈希 格式）
   * @return 验证结果
   */
  def verifyPassword(password: String, storedHash: String): Boolean = {
    verifyPasswordSafe(password, storedHash).getOrElse(false)
  }
  
  /**
   * 安全的密码验证方法，返回 Try 类型
   * @param password 明文密码
   * @param storedHash 存储的哈希值
   * @return Try[Boolean] 验证结果
   */
  def verifyPasswordSafe(password: String, storedHash: String): Try[Boolean] = {
    Try {
      val parts = storedHash.split(":")
      require(parts.length == 2, "Invalid hash format")
      
      val salt = parts(0)
      val hash = parts(1)
      val computedHash = hashPasswordWithSalt(password, salt)
      
      // 使用常量时间比较防止时序攻击
      MessageDigest.isEqual(hash.getBytes("UTF-8"), computedHash.getBytes("UTF-8"))
    }
  }
  
  /**
   * 检查密码是否符合安全要求
   * @param password 待验证的密码
   * @return 密码是否有效
   */
  def isPasswordValid(password: String): Boolean = {
    password.length >= 8 && 
    password.exists(_.isDigit) && 
    password.exists(_.isLetter) &&
    password.exists(c => !c.isLetterOrDigit)
  }
  
  /**
   * 获取密码要求描述
   * @return 密码要求说明
   */
  def getPasswordRequirements: String = {
    "密码必须至少8个字符，包含至少一个字母、一个数字和一个特殊字符。"
  }
  
  /**
   * 验证密码强度并返回详细结果
   * @param password 待验证的密码
   * @return Try[Unit] 成功表示密码有效，失败包含具体错误信息
   */
  def validatePasswordStrength(password: String): Try[Unit] = {
    if (password.length < 8) {
      Failure(new IllegalArgumentException("密码长度至少需要8个字符"))
    } else if (!password.exists(_.isDigit)) {
      Failure(new IllegalArgumentException("密码必须包含至少一个数字"))
    } else if (!password.exists(_.isLetter)) {
      Failure(new IllegalArgumentException("密码必须包含至少一个字母"))
    } else if (!password.exists(c => !c.isLetterOrDigit)) {
      Failure(new IllegalArgumentException("密码必须包含至少一个特殊字符"))
    } else {
      Success(())
    }
  }
}
