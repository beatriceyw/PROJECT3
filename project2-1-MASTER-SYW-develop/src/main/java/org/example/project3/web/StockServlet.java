package org.example.project3.web;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.example.project3.dao.StockDao;
import org.example.project3.dto.StockDTO;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

public class StockServlet extends HttpServlet {

    private static final String VIEW_LIST = "/WEB-INF/views/list.jsp";
    private static final String VIEW_FORM = "/WEB-INF/views/form.jsp";

    private StockDao dao;

    @Override
    public void init() throws ServletException {
        try {
            // 1) web.xml 에서 파일 이름 읽기 (예: Stock.db)
            String resourceName = getServletContext().getInitParameter("sqlite.url");
            if (resourceName == null || resourceName.isBlank()) {
                throw new IllegalStateException("context-param 'sqlite.url' 이 비어 있습니다.");
            }

            // 2) src/main/resources 안에 있는 파일을 classpath 에서 찾기
            //    (= main > resources > Stock.db)
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            java.net.URL resourceUrl = cl.getResource(resourceName);
            if (resourceUrl == null) {
                throw new IllegalStateException("classpath 에서 DB 파일을 찾을 수 없습니다: " + resourceName);
            }

            // 3) URL -> 실제 파일 시스템 경로로 변환
            java.nio.file.Path path = java.nio.file.Paths.get(resourceUrl.toURI());
            String dbPath = path.toString();              // 예: /usr/local/tomcat/webapps/.../WEB-INF/classes/Stock.db
            String jdbcUrl = "jdbc:sqlite:" + dbPath;

            getServletContext().log("[StockServlet] SQLite DB = " + jdbcUrl);

            // 4) DAO 생성
            this.dao = new StockDao(jdbcUrl);

        } catch (Exception e) {
            getServletContext().log("[StockServlet] init 실패", e);
            throw new ServletException(e);
        }
    }

    /* ===== GET ===== */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String path = path(req);
        switch (path) {
            case "":
            case "/":
            case "/list":
                showList(req, resp);
                break;

            case "/new":
                forward(req, resp, VIEW_FORM);
                break;

            case "/edit": {
                String code = param(req, "code");
                if (!isBlank(code)) {
                    StockDTO found = dao.findByCode(code); // 편의상 코드로 조회해 프리필
                    if (found != null) req.setAttribute("prefill", found);
                }
                forward(req, resp, VIEW_FORM);
                break;
            }

            case "/create":
            case "/update":
            case "/delete":
                methodNotAllowed(resp, "POST");
                break;

            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /* ===== POST ===== */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String path = path(req);
        switch (path) {
            case "/create":
                handleCreate(req, resp);
                break;

            case "/update":
                handleUpdate(req, resp); // ★ id 기반 업데이트
                break;

            case "/delete":
                handleDelete(req, resp);
                break;

            case "":
            case "/":
            case "/list":
            case "/new":
            case "/edit":
                methodNotAllowed(resp, "GET");
                break;

            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /* ===== Handlers ===== */

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String mode = param(req, "mode");
        String q = param(req, "q");

        List<StockDTO> list;
        if (!isBlank(q)) {
            list = dao.search(q);
        } else if ("inserted".equalsIgnoreCase(mode)) {
            list = dao.findAllOrderByInserted();
        } else {
            list = dao.findAllOrderByName();
        }

        req.setAttribute("stocks", list);
        forward(req, resp, VIEW_LIST);
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = trim(req.getParameter("stockCode"));
        String name = trim(req.getParameter("stockName"));
        Double pbr = parseD(req.getParameter("pbr"));
        Double per = parseD(req.getParameter("per"));

        if (isBlank(code) || isBlank(name)) {
            redirectWithToast(req, resp, "/stocks/list", "코드와 이름은 필수입니다");
            return;
        }

        try {
            dao.insert(new StockDTO(null, code, name, pbr, per, null));
            redirectWithToast(req, resp, "/stocks/list", "추가 완료");
        } catch (RuntimeException e) {
            redirectWithToast(req, resp, "/stocks/list", "추가 실패(중복 코드 확인)");
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = trim(req.getParameter("id"));
        String code = trim(req.getParameter("stockCode"));
        String inName = trim(req.getParameter("stockName"));
        Double pbr = parseD(req.getParameter("pbr"));
        Double per = parseD(req.getParameter("per"));

        getServletContext().log("[UPDATE] recv id=" + idStr + ", code=" + code + ", name=" + inName + ", pbr=" + pbr + ", per=" + per);

        if (isBlank(idStr)) {
            redirectWithToast(req, resp, "/stocks/list", "수정 실패(id 누락)");
            return;
        }

        Long id;
        try {
            id = Long.valueOf(idStr);
        } catch (NumberFormatException nfe) {
            redirectWithToast(req, resp, "/stocks/list", "수정 실패(id 형식 오류)");
            return;
        }

        StockDTO current = dao.findById(id);
        if (current == null) {
            redirectWithToast(req, resp, "/stocks/list", "해당 항목 없음");
            return;
        }

        if (isBlank(code)) code = current.getStockCode();      // 코드 비우면 기존 유지
        String newName = !isBlank(inName) ? inName : current.getStockName(); // 이름 비우면 기존 유지

        try {
            boolean ok = dao.updateById(id, code, newName, pbr, per);
            getServletContext().log("[UPDATE] result=" + ok + " -> code=" + code + ", name=" + newName);
            redirectWithToast(req, resp, "/stocks/list", ok ? "수정 완료" : "수정 실패");
        } catch (RuntimeException e) {
            String msg = e.getMessage() != null && e.getMessage().contains("UNIQUE")
                    ? "수정 실패(코드 중복)"
                    : "수정 실패";
            getServletContext().log("[UPDATE] 실패: " + e.getMessage(), e);
            redirectWithToast(req, resp, "/stocks/list", msg);
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = trim(req.getParameter("code"));
        if (isBlank(code)) {
            redirectWithToast(req, resp, "/stocks/list", "삭제 실패(코드 누락)");
            return;
        }

        try {
            boolean ok = dao.delete(code);
            redirectWithToast(req, resp, "/stocks/list", ok ? "삭제 완료" : "해당 코드 없음");
        } catch (RuntimeException e) {
            redirectWithToast(req, resp, "/stocks/list", "삭제 실패");
        }
    }

    /* ===== Redirect helpers ===== */
    private static void redirectWithToast(HttpServletRequest req, HttpServletResponse resp,
                                          String path, String toast) throws IOException {
        String encoded = URLEncoder.encode(toast, StandardCharsets.UTF_8);
        String base = req.getContextPath() + path;
        String url = base + (base.contains("?") ? "&" : "?") + "toast=" + encoded;
        resp.sendRedirect(resp.encodeRedirectURL(url));
    }

    /* ===== Utils ===== */

    private static String path(HttpServletRequest req) {
        String p = req.getPathInfo();
        return (p == null) ? "" : p;
    }

    private static String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return (v == null) ? null : v.trim();
    }

    private static void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher(view).forward(req, resp);
    }

    private static void methodNotAllowed(HttpServletResponse resp, String allowed) throws IOException {
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        resp.setHeader("Allow", allowed);
        resp.getWriter().write("Method Not Allowed. Use " + allowed);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String trim(String s) {
        return (s == null) ? null : s.trim();
    }

    private static Double parseD(String s) {
        s = trim(s);
        if (isBlank(s)) return null;
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) { return null; }
    }
}
