package model

import java.time.LocalDateTime
import util.PasswordHasher
import scala.util.{Try, Success, Failure}

/**
 * Sealed trait representing a user in the community engagement platform
 * 使用 sealed trait 代替 abstract class，更符合 Scala 函数式编程风格
 */
sealed trait User {
  def userId: String
  def username: String
  def email: String
  def name: String
  def contactInfo: String
  def passwordHash: String
  def registrationDate: LocalDateTime
  def isActive: Boolean
  def getUserRole: String
  def hasAdminPrivileges: Boolean = false
  
  /**
   * 验证密码
   * @param password 明文密码
   * @return 验证结果
   */
  def verifyPassword(password: String): Boolean = {
    if (passwordHash.isEmpty) false
    else PasswordHasher.verifyPassword(password, passwordHash)
  }
  
  override def toString: String = s"User($userId, $username, $name, $getUserRole)"
}

/**
 * 社区成员用户 - 使用 case class 实现 immutable 数据结构
 */
case class CommunityMember(
  userId: String,
  username: String,
  email: String,
  name: String,
  contactInfo: String,
  passwordHash: String = "",
  registrationDate: LocalDateTime = LocalDateTime.now(),
  isActive: Boolean = true
) extends User {
  
  override def getUserRole: String = "Community Member"
}

/**
 * 管理员用户 - 使用 case class 实现 immutable 数据结构
 */
case class AdminUser(
  userId: String,
  username: String,
  email: String,
  name: String,
  contactInfo: String,
  passwordHash: String = "",
  registrationDate: LocalDateTime = LocalDateTime.now(),
  isActive: Boolean = true
) extends User {
  
  override def getUserRole: String = "Administrator"
  override def hasAdminPrivileges: Boolean = true
}

/**
 * User 伴生对象 - 提供工厂方法和实用函数
 */
object User {
  
  /**
   * 更新用户资料
   * @param user 原用户对象
   * @param newName 新姓名
   * @param newContactInfo 新联系信息
   * @return 更新后的用户对象
   */
  def updateProfile(user: User, newName: String, newContactInfo: String): User = user match {
    case member: CommunityMember => member.copy(name = newName, contactInfo = newContactInfo)
    case admin: AdminUser => admin.copy(name = newName, contactInfo = newContactInfo)
  }
  
  /**
   * 设置新密码
   * @param user 用户对象
   * @param newPassword 新密码
   * @return Try[User] 成功返回更新后的用户，失败返回异常
   */
  def setPassword(user: User, newPassword: String): Try[User] = {
    if (PasswordHasher.isPasswordValid(newPassword)) {
      val hashedPassword = PasswordHasher.hashPassword(newPassword)
      Success(user match {
        case member: CommunityMember => member.copy(passwordHash = hashedPassword)
        case admin: AdminUser => admin.copy(passwordHash = hashedPassword)
      })
    } else {
      Failure(new IllegalArgumentException(PasswordHasher.getPasswordRequirements))
    }
  }
  
  /**
   * 重置密码
   * @param user 用户对象
   * @param currentPassword 当前密码
   * @param newPassword 新密码
   * @return Try[User] 成功返回更新后的用户，失败返回异常
   */
  def resetPassword(user: User, currentPassword: String, newPassword: String): Try[User] = {
    if (user.verifyPassword(currentPassword)) {
      setPassword(user, newPassword)
    } else {
      Failure(new IllegalArgumentException("当前密码验证失败"))
    }
  }
  
  /**
   * 设置密码哈希（用于从数据库加载）
   * @param user 用户对象
   * @param hash 密码哈希
   * @return 更新后的用户对象
   */
  def setPasswordHash(user: User, hash: String): User = user match {
    case member: CommunityMember => member.copy(passwordHash = hash)
    case admin: AdminUser => admin.copy(passwordHash = hash)
  }
  
  /**
   * 激活/停用用户
   * @param user 用户对象
   * @param active 是否激活
   * @return 更新后的用户对象
   */
  def setActive(user: User, active: Boolean): User = user match {
    case member: CommunityMember => member.copy(isActive = active)
    case admin: AdminUser => admin.copy(isActive = active)
  }
}
