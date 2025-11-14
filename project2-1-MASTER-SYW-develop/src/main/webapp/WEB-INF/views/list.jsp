<%-- Created by IntelliJ IDEA. --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <title>주식 마스터 - 목록</title>
    <link rel="stylesheet" href="<c:url value='/css/style.css'/>"/>
</head>
<body>

<%-- 네비게이션용 URL 미리 생성 --%>
<c:url var="nameUrl" value="/stocks/list">
    <c:param name="mode" value="name"/>
</c:url>
<c:url var="insertedUrl" value="/stocks/list">
    <c:param name="mode" value="inserted"/>
</c:url>
<c:url var="newUrl" value="/stocks/new"/>

<header>
    <h1>주식 마스터</h1>
    <nav>
        <a href="${nameUrl}">이름순</a>
        <a href="${insertedUrl}">입력순</a>
        <a href="${newUrl}">종목 추가</a>
    </nav>
</header>

<c:if test="${not empty param.toast}">
    <div class="toast"><c:out value="${param.toast}"/></div>
</c:if>

<form method="get" action="<c:url value='/stocks/list'/>" class="search">
    <input type="text" name="q" value="${param.q}" placeholder="코드/이름 검색"/>
    <button type="submit">검색</button>
</form>

<c:choose>
    <c:when test="${empty stocks}">
        <p>(데이터 없음)</p>
    </c:when>
    <c:otherwise>
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>코드</th>
                <th>이름</th>
                <th>PBR</th>
                <th>PER</th>
                <th>생성일</th>
                <th>액션</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="s" items="${stocks}">
                <%-- 행별 수정 URL 구성 --%>
                <c:url var="editUrl" value="/stocks/edit">
                    <c:param name="code" value="${s.stockCode}"/>
                </c:url>

                <tr>
                    <td>${s.id}</td>
                    <td>${s.stockCode}</td>
                    <td>${s.stockName}</td>
                    <td><fmt:formatNumber value="${s.pbr}" pattern="#,##0.####"/></td>
                    <td><fmt:formatNumber value="${s.per}" pattern="#,##0.####"/></td>
                    <td>${s.createDateText}</td>
                    <td class="actions">
                        <a href="${editUrl}">수정</a>
                        <form method="post" action="<c:url value='/stocks/delete'/>"
                              onsubmit="return confirm('삭제할까요?');" style="display:inline;">
                            <input type="hidden" name="code" value="${s.stockCode}"/>
                            <button type="submit">삭제</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        <p>총 <strong>${fn:length(stocks)}</strong>건</p>
    </c:otherwise>
</c:choose>

</body>
</html>
