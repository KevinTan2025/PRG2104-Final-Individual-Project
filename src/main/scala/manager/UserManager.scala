package manager

import model._
import scala.util.{Try, Success, Failure}
import scala.util.control.NonFatal

/**
 * 不可变状态表示，用于管理集合项目
 * @tparam T 被管理项目的类型
 */
case class ManagerState[T](
  items: Map[String, T] = Map.empty
) {
  /**
   * 获取所有项目
   * @return 项目列表
   */
  def getAll: List[T] = items.values.toList
  
  /**
   * 检查项目是否存在
   * @param id 项目ID
   * @return 如果存在返回true
   */
  def exists(id: String): Boolean = items.contains(id)
  
  /**
   * 获取项目数量
   * @return 项目数量
   */
  def size: Int = items.size
  
  /**
   * 检查是否为空
   * @return 如果为空返回true
   */
  def isEmpty: Boolean = items.isEmpty
}

/**
 * 传统管理器trait，保持向后兼容
 * @tparam T 被管理项目的类型
 */
trait Manager[T] {
  protected val items: scala.collection.mutable.Map[String, T] = scala.collection.mutable.Map.empty
  
  def add(id: String, item: T): Unit = {
    items(id) = item
  }
  
  def remove(id: String): Option[T] = {
    items.remove(id)
  }
  
  def get(id: String): Option[T] = {
    items.get(id)
  }
  
  def getAll: List[T] = {
    items.values.toList
  }
  
  def exists(id: String): Boolean = {
    items.contains(id)
  }
  
  def size: Int = items.size
  
  def clear(): Unit = {
    items.clear()
  }
}

/**
 * 函数式管理器工具对象，提供不可变状态操作
 */
object FunctionalManager {
  
  /**
   * 添加项目到状态
   * @param state 当前状态
   * @param id 项目ID
   * @param item 项目
   * @return 新状态
   */
  def add[T](state: ManagerState[T], id: String, item: T): ManagerState[T] = {
    state.copy(items = state.items + (id -> item))
  }
  
  /**
   * 从状态中移除项目
   * @param state 当前状态
   * @param id 项目ID
   * @return (新状态, 被移除的项目)
   */
  def remove[T](state: ManagerState[T], id: String): (ManagerState[T], Option[T]) = {
    val item = state.items.get(id)
    val newState = state.copy(items = state.items - id)
    (newState, item)
  }
  
  /**
   * 从状态中获取项目
   * @param state 当前状态
   * @param id 项目ID
   * @return 项目（如果存在）
   */
  def get[T](state: ManagerState[T], id: String): Option[T] = {
    state.items.get(id)
  }
  
  /**
   * 清空状态
   * @param state 当前状态
   * @return 空状态
   */
  def clear[T](state: ManagerState[T]): ManagerState[T] = {
    state.copy(items = Map.empty)
  }
}

/**
 * 用户管理器状态，包含用户数据和索引
 */
case class UserManagerState(
  users: ManagerState[User] = ManagerState(),
  usernames: Set[String] = Set.empty,
  emails: Set[String] = Set.empty
) {
  /**
   * 检查用户名是否可用
   * @param username 用户名
   * @return 如果可用返回true
   */
  def isUsernameAvailable(username: String): Boolean = !usernames.contains(username)
  
  /**
   * 检查邮箱是否可用
   * @param email 邮箱
   * @return 如果可用返回true
   */
  def isEmailAvailable(email: String): Boolean = !emails.contains(email)
}

/**
 * 函数式用户管理器，处理用户操作
 */
class UserManager extends Manager[User] {
  
  /**
   * 注册新用户
   * @param state 当前状态
   * @param user 要注册的用户
   * @return Try包装的(新状态, 注册结果)
   */
  def registerUser(state: UserManagerState, user: User): Try[(UserManagerState, Boolean)] = {
    Try {
      if (state.usernames.contains(user.username) || state.emails.contains(user.email)) {
        (state, false)
      } else {
         val newUsers = FunctionalManager.add(state.users, user.userId, user)
         val newState = state.copy(
           users = newUsers,
           usernames = state.usernames + user.username,
           emails = state.emails + user.email
         )
         (newState, true)
       }
    }.recover {
      case NonFatal(e) =>
        println(s"注册用户失败: ${e.getMessage}")
        (state, false)
    }
  }
  
  /**
   * 安全版本的用户注册
   * @param state 当前状态
   * @param user 要注册的用户
   * @return (新状态, 注册结果)
   */
  def registerUserSafe(state: UserManagerState, user: User): (UserManagerState, Boolean) = {
    registerUser(state, user).getOrElse((state, false))
  }
  
  /**
   * 用户认证
   * @param state 当前状态
   * @param username 用户名
   * @param password 密码（简化版本用于演示）
   * @return Try包装的用户（如果认证成功）
   */
  def authenticate(state: UserManagerState, username: String, password: String): Try[Option[User]] = {
    Try {
      // 简化的认证 - 实际应用中密码应该被哈希处理
      state.users.items.values.find(_.username == username)
    }.recover {
      case NonFatal(e) =>
        println(s"用户认证失败: ${e.getMessage}")
        None
    }
  }
  
  /**
   * 安全版本的用户认证
   * @param state 当前状态
   * @param username 用户名
   * @param password 密码
   * @return 用户（如果认证成功）
   */
  def authenticateSafe(state: UserManagerState, username: String, password: String): Option[User] = {
    authenticate(state, username, password).getOrElse(None)
  }
  
  /**
   * 根据用户名获取用户
   * @param state 当前状态
   * @param username 要搜索的用户名
   * @return Try包装的用户（如果找到）
   */
  def getUserByUsername(state: UserManagerState, username: String): Try[Option[User]] = {
    Try {
      state.users.items.values.find(_.username == username)
    }.recover {
      case NonFatal(e) =>
        println(s"根据用户名获取用户失败: ${e.getMessage}")
        None
    }
  }
  
  /**
   * 安全版本的根据用户名获取用户
   * @param state 当前状态
   * @param username 用户名
   * @return 用户（如果找到）
   */
  def getUserByUsernameSafe(state: UserManagerState, username: String): Option[User] = {
    getUserByUsername(state, username).getOrElse(None)
  }
  
  /**
   * 根据邮箱获取用户
   * @param state 当前状态
   * @param email 要搜索的邮箱
   * @return Try包装的用户（如果找到）
   */
  def getUserByEmail(state: UserManagerState, email: String): Try[Option[User]] = {
    Try {
      state.users.items.values.find(_.email == email)
    }.recover {
      case NonFatal(e) =>
        println(s"根据邮箱获取用户失败: ${e.getMessage}")
        None
    }
  }
  
  /**
   * 安全版本的根据邮箱获取用户
   * @param state 当前状态
   * @param email 邮箱
   * @return 用户（如果找到）
   */
  def getUserByEmailSafe(state: UserManagerState, email: String): Option[User] = {
    getUserByEmail(state, email).getOrElse(None)
  }
  
  /**
   * 获取所有管理员用户
   * @param state 当前状态
   * @return Try包装的管理员用户列表
   */
  def getAdminUsers(state: UserManagerState): Try[List[AdminUser]] = {
    Try {
      state.users.items.values.collect {
        case admin: AdminUser => admin
      }.toList
    }.recover {
      case NonFatal(e) =>
        println(s"获取管理员用户失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 安全版本的获取所有管理员用户
   * @param state 当前状态
   * @return 管理员用户列表
   */
  def getAdminUsersSafe(state: UserManagerState): List[AdminUser] = {
    getAdminUsers(state).getOrElse(List.empty)
  }
  
  /**
   * 获取所有社区成员
   * @param state 当前状态
   * @return Try包装的社区成员列表
   */
  def getCommunityMembers(state: UserManagerState): Try[List[CommunityMember]] = {
    Try {
      state.users.items.values.collect {
        case member: CommunityMember => member
      }.toList
    }.recover {
      case NonFatal(e) =>
        println(s"获取社区成员失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 安全版本的获取所有社区成员
   * @param state 当前状态
   * @return 社区成员列表
   */
  def getCommunityMembersSafe(state: UserManagerState): List[CommunityMember] = {
    getCommunityMembers(state).getOrElse(List.empty)
  }
  
  /**
   * 移除用户
   * @param state 当前状态
   * @param id 用户ID
   * @return Try包装的(新状态, 被移除的用户)
   */
  def removeUser(state: UserManagerState, id: String): Try[(UserManagerState, Option[User])] = {
    Try {
       val (newUsers, removedUser) = FunctionalManager.remove(state.users, id)
       removedUser match {
         case Some(user) =>
           val newState = state.copy(
             users = newUsers,
             usernames = state.usernames - user.username,
             emails = state.emails - user.email
           )
           (newState, Some(user))
         case None =>
           (state, None)
       }
     }.recover {
      case NonFatal(e) =>
        println(s"移除用户失败: ${e.getMessage}")
        (state, None)
    }
  }
  
  /**
   * 安全版本的移除用户
   * @param state 当前状态
   * @param id 用户ID
   * @return (新状态, 被移除的用户)
   */
  def removeUserSafe(state: UserManagerState, id: String): (UserManagerState, Option[User]) = {
    removeUser(state, id).getOrElse((state, None))
  }
  
  /**
   * 获取用户
   * @param state 当前状态
   * @param id 用户ID
   * @return Try包装的用户（如果存在）
   */
  def getUser(state: UserManagerState, id: String): Try[Option[User]] = {
     Try {
       FunctionalManager.get(state.users, id)
     }.recover {
      case NonFatal(e) =>
        println(s"获取用户失败: ${e.getMessage}")
        None
    }
  }
  
  /**
   * 安全版本的获取用户
   * @param state 当前状态
   * @param id 用户ID
   * @return 用户（如果存在）
   */
  def getUserSafe(state: UserManagerState, id: String): Option[User] = {
    getUser(state, id).getOrElse(None)
  }
  
  /**
   * 获取所有用户
   * @param state 当前状态
   * @return Try包装的用户列表
   */
  def getAllUsers(state: UserManagerState): Try[List[User]] = {
    Try {
      state.users.getAll
    }.recover {
      case NonFatal(e) =>
        println(s"获取所有用户失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 安全版本的获取所有用户
   * @param state 当前状态
   * @return 用户列表
   */
  def getAllUsersSafe(state: UserManagerState): List[User] = {
    getAllUsers(state).getOrElse(List.empty)
  }
}
