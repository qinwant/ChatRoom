package whisper.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCUtil {
	// 驱动名
	public static String driverName = "com.mysql.jdbc.Driver";
	// 连接路径
	public static String url = "jdbc:mysql://localhost:3306/se1917pro?useUnicode=true&characterEncoding=UTF-8&useSSL=false";
	// 用户名
	public static String user = "root";
	// 密码
	public static String password = "toor";

	/**
	 * 构造方法私有化
	 */
	public JDBCUtil() {
	}

	/**
	 * 获取连接的方法
	 * 
	 * @return
	 */
	public static Connection getConnection() {
		Connection conn = null;
		try {
			// 1、加载驱动
			Class.forName(driverName);
			// 2、连接数据库
			conn = DriverManager.getConnection(url, user, password);
			return conn;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 关闭资源 先开的后关 结果集rs先关、st、 连接 conn
	 * 
	 * @param st
	 * @param conn
	 * @param rs
	 */
	public static void close(Statement st, Connection conn, ResultSet rs) {
		try {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
			} finally {
				try {
					if (st != null) {
						st.close();
						st = null;
					}
				} finally {
					if (conn != null) {
						conn.close();
						conn = null;
					}
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * 增删改的方法封装
	 * 
	 * @param sql
	 *            对应的sql语句
	 * @param args
	 *            可变参数,实际上是数组。可以通过循环的方式，设置参数值
	 * @return 如果修改成功则返回受影响行数。如果不成功则返回0
	 */
	public static int update(String sql, Object... args) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			// 设置参数
			for (int i = 0; args != null && i < args.length; i++) {
				ps.setObject(i + 1, args[i]);
			}
			return ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			close(ps, conn, null);
		}

		return 0;
	}

	/**
	 * 查询返回结果为list<Map<String,Object>> String 表示字段名 Object 表示字段对应的值
	 * 
	 * @param sql
	 * @param args
	 * @return
	 */
	public static List<Map<String, Object>> queryForList(String sql, Object... args) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			// 参数绑定
			for (int i = 0; args != null && i < args.length; i++) {
				ps.setObject(i + 1, args[i]);
			}

			rs = ps.executeQuery();
			// 构造出一个返回值类型的对象
			List<Map<String, Object>> list = new ArrayList<>();
			// 获取元数据
			ResultSetMetaData rsmd = rs.getMetaData();
			// 获取总的字段数
			int count = rsmd.getColumnCount();
			// 遍历结果集
			while (rs.next()) {
				Map<String, Object> map = new HashMap<>();
				// String key = ""
				// Object value = rs.getObject(key);
				// map.put(key, value);
				for (int i = 0; i < count; i++) {
					String key = rsmd.getColumnName(i + 1);
					Object value = rs.getObject(key);
					map.put(key, value);
				}
				list.add(map);
			}
			if (list != null && list.size() > 0) {
				return list;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			close(ps, conn, rs);
		}
		return null;
	}

	/**
	 * 查询： 返回结果为Map<String,Object>
	 * 
	 * @param sql
	 * @param args
	 * @return
	 */
	public static Map<String, Object> queryForMap(String sql, Object... args) {
		List<Map<String, Object>> list = queryForList(sql, args);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * T:指代泛型 如果要操作user表则，这个t表示user的实体类。以此类推
	 * 
	 * @param sql
	 * @param c
	 *            表示对应数据库表的实体类
	 * @param args
	 * @return
	 */
	public static <T> List<T> query(String sql, Class<T> c, Object... args) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			//设置参数
			for (int i = 0;args!=null&& i < args.length; i++) {
				ps.setObject(i+1, args[i]);
			}
			rs = ps.executeQuery();
			//创建出list<T> 返回值类型的对象
			List<T> list = new ArrayList<>();
			//根据反射机制获取类中所有的属性
			Field[] fields = c.getDeclaredFields();
			//获取元数据
			ResultSetMetaData rsmd = rs.getMetaData();
			//遍历结果集
			while(rs.next()){
				//实例化出类的具体对象
				T o = c.newInstance();
				for (int i = 0; i < fields.length; i++) {
					//拿到每一个字段
					Field field = fields[i];
					//获取每一个字段的名字
					String fieldName = field.getName();
					//获取每个字段的类型
					Class<?> fieldType = field.getType();
					//获取set方法名  name -- >  setName
					String methodName = "set"+ (fieldName.charAt(0)+"").toUpperCase()+fieldName.substring(1);
					//获取set方法对象
					Method method = c.getMethod(methodName, fieldType);
					//判断是否存在结果中
					if (isExist(rsmd, fieldName)) {
						Object value = rs.getObject(fieldName);
						if (value!=null) {
							//
							method.invoke(o, value);
						}
					}
					list.add(o);
				}
			}
			
			if (list!=null&&list.size()>0) {
				return list;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			close(ps, conn, rs);
		}
		return null;

	}

	/**
	 * 查询单个
	 * 
	 * @param sql
	 * @param c
	 * @param args
	 * @return
	 */
	public static <T> T queryForOne(String sql, Class<T> c, Object... args) {
		List<T> list = query(sql, c, args);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * 判断一个字段是否存在结果集中
	 * 
	 * @return
	 */
	public static boolean isExist(ResultSetMetaData rsmd, String fieldName) {
		try {
			int count  = rsmd.getColumnCount();
			for (int i = 0; i < count; i++) {
				String columnName = rsmd.getColumnName(i+1);
				if (columnName.equals(fieldName)) {
					return true;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
