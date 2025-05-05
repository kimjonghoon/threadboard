package net.java_school.board;

import java.io.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.sql.*;

import net.java_school.db.dbpool.*;
import net.java_school.util.*;

public class BoardReplier extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7566062019730529502L;

	OracleConnectionManager dbmgr = null;

	//update depth
	String sql1 = "UPDATE thread_article SET depth = depth + 1 " + 
			"WHERE family = ? AND depth > ? ";

	String sql2 = "INSERT INTO thread_article " + 
			"(articleno, family, parent, depth, indent, title, content, regdate) " + 
			"VALUES (seq_thread_article.nextval, ?, ?, ?, ?, ?, ?, sysdate)";

	@Override
	public void init() throws ServletException {
		ServletContext sc = getServletContext();
		dbmgr = (OracleConnectionManager)sc.getAttribute("dbmgr");
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8");
		Log log = new Log();

		int parent = Integer.parseInt(req.getParameter("no"));
		int family = Integer.parseInt(req.getParameter("family"));
		int depth = Integer.parseInt(req.getParameter("depth"));
		int indent = Integer.parseInt(req.getParameter("indent"));
		String title = req.getParameter("title");
		String content = req.getParameter("content");

		String curPage = req.getParameter("curPage");
		String keyword = req.getParameter("keyword");

		Connection con = null;
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;

		try {
			con = dbmgr.getConnection();
			con.setAutoCommit(false);

			stmt1 = con.prepareStatement(sql1);
			stmt1.setInt(1,family);
			stmt1.setInt(2,depth);
			stmt1.executeUpdate();

			stmt2 = con.prepareStatement(sql2);
			stmt2.setInt(1, family);
			stmt2.setInt(2, parent);
			stmt2.setInt(3, depth+1);
			stmt2.setInt(4, indent+1);
			stmt2.setString(5, title);
			stmt2.setString(6, content);
			stmt2.executeUpdate();
			con.commit();
		} catch (SQLException e) {
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			log.debug("Error Source:BoardReplier.java : SQLException");
			log.debug("SQLState : " + e.getSQLState());
			log.debug("Message : " + e.getMessage());
			log.debug("Oracle Error Code : " + e.getErrorCode());
			log.debug("sql : " + sql2);
		} finally {
			if (stmt1 != null) {
				try {
					stmt1.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (stmt2 != null) {
				try {
					stmt2.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (con != null) {
				dbmgr.freeConnection(con);
			}
			log.close();
			String path = req.getContextPath();
			keyword = java.net.URLEncoder.encode(keyword,"UTF-8");
			resp.sendRedirect(path + "/board/list.jsp?curPage=" + curPage + "&keyword=" + keyword);
		}

	}
}